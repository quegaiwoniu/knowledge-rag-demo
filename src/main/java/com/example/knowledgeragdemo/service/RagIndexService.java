package com.example.knowledgeragdemo.service;

import com.example.knowledgeragdemo.dto.RagAskRequest;
import com.example.knowledgeragdemo.dto.RagAskResponse;
import com.example.knowledgeragdemo.dto.RagChunk;
import com.example.knowledgeragdemo.dto.RagChunksResponse;
import com.example.knowledgeragdemo.dto.RagDocumentMetadata;
import com.example.knowledgeragdemo.dto.RagIndexStatusResponse;
import com.example.knowledgeragdemo.dto.RagSearchResponse;
import com.example.knowledgeragdemo.dto.RagSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RAG 索引编排服务。
 */
@Service
public class RagIndexService {

    private static final Logger log = LoggerFactory.getLogger(RagIndexService.class);

    private final RagIngestionService ragIngestionService;
    private final RagChunkService ragChunkService;
    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;
    private final JdbcTemplate jdbcTemplate;
    private final ChatClient chatClient;
    private final String vectorStoreSchemaName;
    private final String vectorStoreTableName;

    private volatile int documentCount = 0;
    private volatile int chunkCount = 0;
    private volatile int embeddedChunkCount = 0;
    private volatile LocalDateTime lastRebuildAt = null;

    public RagIndexService(RagIngestionService ragIngestionService,
                           RagChunkService ragChunkService,
                           VectorStore vectorStore,
                           EmbeddingModel embeddingModel,
                           JdbcTemplate jdbcTemplate,
                           ChatClient.Builder chatClientBuilder,
                           @Value("${spring.ai.vectorstore.pgvector.schema-name:public}") String vectorStoreSchemaName,
                           @Value("${spring.ai.vectorstore.pgvector.table-name:vector_store}") String vectorStoreTableName) {
        this.ragIngestionService = ragIngestionService;
        this.ragChunkService = ragChunkService;
        this.vectorStore = vectorStore;
        this.embeddingModel = embeddingModel;
        this.jdbcTemplate = jdbcTemplate;
        this.chatClient = chatClientBuilder.build();
        this.vectorStoreSchemaName = validateIdentifier(vectorStoreSchemaName, "schema-name");
        this.vectorStoreTableName = validateIdentifier(vectorStoreTableName, "table-name");
    }

    public RagIndexStatusResponse rebuildIndex() {
        log.info("Starting RAG index rebuild...");
        List<RagDocumentMetadata> documents = ragIngestionService.ingest().getDocuments();
        log.info("Ingested {} documents", documents.size());
        RagChunksResponse chunksResponse = ragChunkService.listChunks();
        List<RagChunk> chunks = chunksResponse.getChunks();
        log.info("Generated {} chunks from {} documents", chunks.size(), documents.size());
        clearVectorStore();
        List<Document> springAiDocuments = convertToDocuments(chunks);
        int batchSize = 10;
        int total = springAiDocuments.size();
        for (int i = 0; i < total; i += batchSize) {
            int end = Math.min(i + batchSize, total);
            List<Document> batch = springAiDocuments.subList(i, end);
            vectorStore.add(batch);
            log.info("Embedded batch {}/{} ({} chunks)", (i / batchSize + 1), (total + batchSize - 1) / batchSize, batch.size());
        }
        log.info("Successfully embedded and stored {} chunks", total);
        this.documentCount = documents.size();
        this.chunkCount = chunks.size();
        this.embeddedChunkCount = springAiDocuments.size();
        this.lastRebuildAt = LocalDateTime.now();
        return getStatus();
    }

    public RagIndexStatusResponse getStatus() {
        return new RagIndexStatusResponse(documentCount, chunkCount, embeddedChunkCount, lastRebuildAt, lastRebuildAt != null);
    }

    public RagSearchResponse search(String query, int topK) {
        log.info("Searching for query: {}", query);
        float[] embedding = embeddingModel.embed(query);
        String embeddingStr = toPgVectorString(embedding);
        String sql = "SELECT metadata, content, 1 - (embedding <=> ?::vector) AS similarity " +
                     "FROM " + vectorStoreSchemaName + "." + vectorStoreTableName + " " +
                     "ORDER BY embedding <=> ?::vector ASC LIMIT ?";
        List<RagSearchResult> results = jdbcTemplate.query(sql, (rs, rowNum) -> {
            String metadataStr = rs.getString("metadata");
            Map<String, Object> metadata = parseMetadata(metadataStr);
            double similarity = rs.getDouble("similarity");
            return new RagSearchResult(
                    metadata.get("docId") != null ? metadata.get("docId").toString() : null,
                    metadata.get("fileName") != null ? metadata.get("fileName").toString() : null,
                    metadata.get("title") != null ? metadata.get("title").toString() : null,
                    metadata.get("sectionTitle") != null ? metadata.get("sectionTitle").toString() : null,
                    metadata.get("chunkIndex") != null ? Integer.parseInt(metadata.get("chunkIndex").toString()) : 0,
                    rs.getString("content"),
                    similarity
            );
        }, embeddingStr, embeddingStr, topK);
        return new RagSearchResponse(query, results);
    }

    public RagAskResponse ask(String query, int topK) {
        log.info("Asking question: {}", query);
        RagSearchResponse searchResponse = search(query, topK);
        List<RagSearchResult> retrievedChunks = searchResponse.getResults();
        RagAskResponse response = new RagAskResponse();
        response.setRetrievedChunks(retrievedChunks);
        response.setCitations(retrievedChunks);
        if (retrievedChunks.isEmpty()) {
            response.setEnoughContext(false);
            response.setAnswer("抱歉，知识库中没有找到相关信息来回答您的问题。请尝试换一种方式提问，或者先导入更多文档。");
            return response;
        }
        StringBuilder contextBuilder = new StringBuilder();
        for (int i = 0; i < retrievedChunks.size(); i++) {
            RagSearchResult chunk = retrievedChunks.get(i);
            contextBuilder.append(String.format("【片段 %d】（来源：%s - %s）\n%s\n\n",
                    i + 1, chunk.getFileName(), chunk.getSectionTitle(), chunk.getContent()));
        }
        String systemPrompt = """
                你是一个企业知识库问答助手。
                请严格根据以下检索到的文档片段回答用户的问题。
                规则：
                1. 只使用提供的文档片段中的信息来回答
                2. 如果文档片段不足以回答问题，请明确说明"根据现有知识库，无法完整回答此问题"
                3. 回答要简洁、准确，使用中文
                4. 不要编造文档中不存在的信息
                5. 不要输出 markdown 格式
                6. 在回答末尾用【引用】标注信息来源的文件名和章节
                """;
        String answer = chatClient.prompt()
                .system(systemPrompt)
                .user(String.format("问题：%s\n\n检索到的文档片段：\n%s", query, contextBuilder.toString()))
                .call()
                .content();
        response.setEnoughContext(true);
        response.setAnswer(answer);
        return response;
    }

    private List<Document> convertToDocuments(List<RagChunk> chunks) {
        List<Document> documents = new ArrayList<>(chunks.size());
        for (RagChunk chunk : chunks) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("docId", chunk.getDocId());
            metadata.put("fileName", chunk.getFileName());
            metadata.put("sourcePath", chunk.getSourcePath());
            metadata.put("title", chunk.getTitle());
            metadata.put("sectionTitle", chunk.getSectionTitle());
            metadata.put("chunkIndex", chunk.getChunkIndex());
            documents.add(new Document(chunk.getContent(), metadata));
        }
        return documents;
    }

    private void clearVectorStore() {
        jdbcTemplate.update("TRUNCATE TABLE " + vectorStoreSchemaName + "." + vectorStoreTableName);
    }

    private String validateIdentifier(String identifier, String propertyName) {
        if (identifier == null || !identifier.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("Invalid pgvector " + propertyName + ": " + identifier);
        }
        return identifier;
    }

    private String toPgVectorString(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    private Map<String, Object> parseMetadata(String metadataStr) {
        if (metadataStr == null || metadataStr.isEmpty()) return new HashMap<>();
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(metadataStr, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse metadata JSON: {}", metadataStr, e);
            return new HashMap<>();
        }
    }
}
