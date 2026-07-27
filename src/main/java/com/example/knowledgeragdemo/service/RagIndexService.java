package com.example.knowledgeragdemo.service;

import com.example.knowledgeragdemo.dto.RagChunk;
import com.example.knowledgeragdemo.dto.RagChunksResponse;
import com.example.knowledgeragdemo.dto.RagDocumentMetadata;
import com.example.knowledgeragdemo.dto.RagIndexStatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * RAG 索引编排服务。
 *
 * <p>Day 11 的核心职责：把 Day 9 的 ingestion 和 Day 10 的 chunking 编排起来，
 * 生成 embedding 并写入 pgvector，同时维护索引状态。</p>
 *
 * <p>当前实现是"全量重建"策略：每次 rebuild 都清空旧向量，重新写入。
 * 这样最简单可靠，后续可以优化为增量更新。</p>
 */
@Service
public class RagIndexService {

    private static final Logger log = LoggerFactory.getLogger(RagIndexService.class);

    private final RagIngestionService ragIngestionService;
    private final RagChunkService ragChunkService;
    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;
    private final String vectorStoreSchemaName;
    private final String vectorStoreTableName;

    /**
     * 索引状态：当前内存中缓存的最近一次重建结果。
     * 后续可以改为从 pgvector 实时查询，但 Day 11 先用内存简化。
     */
    private volatile int documentCount = 0;
    private volatile int chunkCount = 0;
    private volatile int embeddedChunkCount = 0;
    private volatile LocalDateTime lastRebuildAt = null;

    public RagIndexService(RagIngestionService ragIngestionService,
                           RagChunkService ragChunkService,
                           VectorStore vectorStore,
                           JdbcTemplate jdbcTemplate,
                           @Value("${spring.ai.vectorstore.pgvector.schema-name:public}") String vectorStoreSchemaName,
                           @Value("${spring.ai.vectorstore.pgvector.table-name:vector_store}") String vectorStoreTableName) {
        this.ragIngestionService = ragIngestionService;
        this.ragChunkService = ragChunkService;
        this.vectorStore = vectorStore;
        this.jdbcTemplate = jdbcTemplate;
        this.vectorStoreSchemaName = validateIdentifier(vectorStoreSchemaName, "schema-name");
        this.vectorStoreTableName = validateIdentifier(vectorStoreTableName, "table-name");
    }

    /**
     * 执行一次完整的索引重建。
     *
     * <p>流程：导入文档 → 切片 → 向量化 → 写入 pgvector。</p>
     *
     * @return 本次重建的统计信息
     */
    public RagIndexStatusResponse rebuildIndex() {
        log.info("Starting RAG index rebuild...");

        // Step 1: 导入文档
        List<RagDocumentMetadata> documents = ragIngestionService.ingest().getDocuments();
        log.info("Ingested {} documents", documents.size());

        // Step 2: 切片
        RagChunksResponse chunksResponse = ragChunkService.listChunks();
        List<RagChunk> chunks = chunksResponse.getChunks();
        log.info("Generated {} chunks from {} documents", chunks.size(), documents.size());

        // Step 3: 转换为 Spring AI Document 并向量化写入。
        // 全量重建必须先清空旧向量，避免 pgvector 中残留已经删除或修改过的知识片段。
        clearVectorStore();

        List<Document> springAiDocuments = convertToDocuments(chunks);

        // 分批写入，单次 API 请求不超过 10 条文本
        int batchSize = 10;
        int total = springAiDocuments.size();
        for (int i = 0; i < total; i += batchSize) {
            int end = Math.min(i + batchSize, total);
            List<Document> batch = springAiDocuments.subList(i, end);
            vectorStore.add(batch);
            log.info("Embedded batch {}/{} ({} chunks)", (i / batchSize + 1), (total + batchSize - 1) / batchSize, batch.size());
        }

        log.info("Successfully embedded and stored {} chunks", total);

        // Step 4: 更新状态
        this.documentCount = documents.size();
        this.chunkCount = chunks.size();
        this.embeddedChunkCount = springAiDocuments.size();
        this.lastRebuildAt = LocalDateTime.now();

        return getStatus();
    }

    /**
     * 获取当前索引状态。
     */
    public RagIndexStatusResponse getStatus() {
        return new RagIndexStatusResponse(
                documentCount,
                chunkCount,
                embeddedChunkCount,
                lastRebuildAt,
                lastRebuildAt != null
        );
    }

    /**
     * 将内部 RagChunk 转换为 Spring AI 的 Document。
     *
     * <p>每个 chunk 的元数据都会保留在 Document 的 metadata 中，
     * 这样后续检索命中时可以追溯来源。</p>
     */
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
}
