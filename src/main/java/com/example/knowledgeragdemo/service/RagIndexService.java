package com.example.knowledgeragdemo.service;

import com.example.knowledgeragdemo.dto.RagAskRequest;
import com.example.knowledgeragdemo.dto.RagAskResponse;
import com.example.knowledgeragdemo.dto.RagChunk;
import com.example.knowledgeragdemo.dto.RagChunksResponse;
import com.example.knowledgeragdemo.dto.RagDocumentMetadata;
import com.example.knowledgeragdemo.dto.RagIndexStatusResponse;
import com.example.knowledgeragdemo.dto.RagSearchResponse;
import com.example.knowledgeragdemo.dto.RagSearchResult;
import com.example.knowledgeragdemo.dto.RefusalReason;
import com.example.knowledgeragdemo.filter.TraceIdContext;
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
import java.util.stream.Collectors;

/**
 * RAG（Retrieval-Augmented Generation，检索增强生成）索引编排服务。
 *
 * <p>这是整个 RAG 链路的核心编排类，串联了四个能力：</p>
 * <ol>
 *   <li><b>Ingestion（导入）</b>：{@link RagIngestionService} 把 Markdown 文档读进来；</li>
 *   <li><b>Chunking（切片）</b>：{@link RagChunkService} 把长文档切成可检索的小片段；</li>
 *   <li><b>Embedding + 向量检索（召回）</b>：把文本转成向量，从 pgvector 里找最相似的片段；</li>
 *   <li><b>Grounded QA（基于证据的问答）</b>：把召回的片段作为上下文拼进 prompt，
 *       让模型只能"照着材料回答"，不能凭空编造。</li>
 * </ol>
 *
 * <p>学习要点：RAG 解决的核心问题是<b>幻觉（hallucination）</b>——
 * 大模型不知道企业内部知识，直接问会编答案。RAG 的做法是先把相关资料检索出来
 * 塞进 prompt，让模型"开卷考试"。</p>
 */
@Service
public class RagIndexService {

    private static final Logger log = LoggerFactory.getLogger(RagIndexService.class);

    // ------------------------------------------------------------------
    // 依赖注入（构造器注入是 Spring 官方推荐的方式，便于测试时替换 mock）
    // ------------------------------------------------------------------

    /** 文档导入服务：负责扫描 docs/sample-docs 下的 Markdown 文件。 */
    private final RagIngestionService ragIngestionService;

    /** 文档切片服务：把长文档切成带元数据的小 chunk。 */
    private final RagChunkService ragChunkService;

    /**
     * 向量存储（pgvector）：保存 chunk 的 embedding 向量，并提供相似度检索。
     * Spring AI 统一抽象，不关心底层是 pgvector 还是别的向量库。
     */
    private final VectorStore vectorStore;

    /**
     * Embedding 模型：把文本转成高维向量（本项目配置的是 text-embedding-v3）。
     * 向量在空间中越接近，表示语义越相似。
     */
    private final EmbeddingModel embeddingModel;

    /**
     * JDBC 模板：直接执行 SQL 访问向量表。
     * 这里没有用 VectorStore 的检索 API，而是手写 SQL，
     * 是为了能拿到 similarity 分数并做阈值过滤，学习价值更高。
     */
    private final JdbcTemplate jdbcTemplate;

    /** ChatClient：Spring AI 的模型调用入口，负责最终生成回答。 */
    private final ChatClient chatClient;

    /** pgvector 表的 schema 名（默认 public），来自配置。 */
    private final String vectorStoreSchemaName;

    /** pgvector 表名（默认 vector_store），来自配置。 */
    private final String vectorStoreTableName;

    /**
     * 检索相似度阈值：低于它的 chunk 会被过滤掉。
     * 配置项 app.rag.min-score-threshold，默认 0.5。
     */
    private final double minScoreThreshold;

    /**
     * Prompt 调试开关：开启后 /rag/ask 会额外返回最终发给模型的完整 prompt，
     * 方便排查"为什么模型答得不好"。配置项 app.ai.prompt-debug，默认 false。
     */
    private final boolean promptDebug;

    // ------------------------------------------------------------------
    // 索引状态（volatile 保证多线程可见性；这里是内存状态，重启后重置）
    // ------------------------------------------------------------------

    /** 当前索引中的文档数。 */
    private volatile int documentCount = 0;

    /** 当前索引中的 chunk 数。 */
    private volatile int chunkCount = 0;

    /** 已向量化入库的 chunk 数。 */
    private volatile int embeddedChunkCount = 0;

    /** 最近一次索引重建时间。 */
    private volatile LocalDateTime lastRebuildAt = null;

    /**
     * 构造器注入所有依赖。
     *
     * <p>注意几个 {@code @Value} 参数：它们从 application.yml 读取配置，
     * {@code ${xxx:默认值}} 语法表示"读不到就用冒号后面的默认值"，
     * 这样即使配置缺失应用也能启动。</p>
     */
    public RagIndexService(RagIngestionService ragIngestionService,
                           RagChunkService ragChunkService,
                           VectorStore vectorStore,
                           EmbeddingModel embeddingModel,
                           JdbcTemplate jdbcTemplate,
                           ChatClient.Builder chatClientBuilder,
                           @Value("${spring.ai.vectorstore.pgvector.schema-name:public}") String vectorStoreSchemaName,
                           @Value("${spring.ai.vectorstore.pgvector.table-name:vector_store}") String vectorStoreTableName,
                           @Value("${app.rag.min-score-threshold:0.5}") double minScoreThreshold,
                           @Value("${app.ai.prompt-debug:false}") boolean promptDebug) {
        this.ragIngestionService = ragIngestionService;
        this.ragChunkService = ragChunkService;
        this.vectorStore = vectorStore;
        this.embeddingModel = embeddingModel;
        this.jdbcTemplate = jdbcTemplate;
        // ChatClient.Builder 是 Spring AI 提供的流式构建器，build() 生成可用的 ChatClient
        this.chatClient = chatClientBuilder.build();
        // 表名 / schema 名要拼进 SQL，必须校验格式，防止 SQL 注入
        this.vectorStoreSchemaName = validateIdentifier(vectorStoreSchemaName, "schema-name");
        this.vectorStoreTableName = validateIdentifier(vectorStoreTableName, "table-name");
        this.minScoreThreshold = minScoreThreshold;
        this.promptDebug = promptDebug;
    }

    /**
     * 重建整个向量索引（POST /rag/index/rebuild）。
     *
     * <p>流程是一条完整的"数据流水线"：</p>
     * <pre>
     * Markdown 文档 → 导入(ingest) → 切片(chunk) → 向量化(embedding) → 写入 pgvector
     * </pre>
     *
     * <p>为什么要先清空再重建？因为当前是"样例知识库 + 全量重建"策略：
     * 每次重建都认为知识库内容可能变了，直接清空表再写入最新数据，保证索引和文档一致。
     * （真实生产环境通常是增量更新，这里为了简单和学习用全量重建。）</p>
     *
     * <p>为什么要分批（batchSize=10）写入？embedding 调用是外部网络请求，
     * 一次传 10 个文档能减少请求次数；如果一次传几百个，容易超时或触发供应商限流。</p>
     */
    public RagIndexStatusResponse rebuildIndex() {
        String traceId = TraceIdContext.get();
        long start = System.currentTimeMillis();
        log.info("[{}] Starting RAG index rebuild...", traceId);

        // 第 1 步：导入文档（读取 Markdown 并提取 docId/fileName/title 等元数据）
        List<RagDocumentMetadata> documents = ragIngestionService.ingest().getDocuments();
        long ingestMs = System.currentTimeMillis() - start;
        log.info("[{}] Ingested {} documents in {}ms", traceId, documents.size(), ingestMs);

        // 第 2 步：切片（把每篇文档切成多个可检索的 chunk，保留可追溯元数据）
        RagChunksResponse chunksResponse = ragChunkService.listChunks();
        List<RagChunk> chunks = chunksResponse.getChunks();
        log.info("[{}] Generated {} chunks from {} documents", traceId, chunks.size(), documents.size());

        // 第 3 步：清空旧索引，保证重建结果干净
        clearVectorStore();

        // 第 4 步：把业务 chunk 转成 Spring AI 的 Document（带 metadata），
        //         这是 VectorStore.add() 要求的输入格式
        List<Document> springAiDocuments = convertToDocuments(chunks);

        // 第 5 步：分批向量化并入库
        int batchSize = 10;
        int total = springAiDocuments.size();
        for (int i = 0; i < total; i += batchSize) {
            int end = Math.min(i + batchSize, total);
            List<Document> batch = springAiDocuments.subList(i, end);
            // VectorStore.add() 内部会调用 embedding 模型把文本转成向量，再写入 pgvector
            vectorStore.add(batch);
            log.info("[{}] Embedded batch {}/{} ({} chunks)", traceId, (i / batchSize + 1), (total + batchSize - 1) / batchSize, batch.size());
        }

        long totalMs = System.currentTimeMillis() - start;
        log.info("[{}] Successfully embedded and stored {} chunks in {}ms", traceId, total, totalMs);

        // 更新内存状态，供 /rag/index/status 查询
        this.documentCount = documents.size();
        this.chunkCount = chunks.size();
        this.embeddedChunkCount = springAiDocuments.size();
        this.lastRebuildAt = LocalDateTime.now();
        return getStatus();
    }

    /**
     * 返回索引状态（GET /rag/index/status）。
     *
     * <p>最后一个参数 {@code lastRebuildAt != null} 用来表示"是否已经重建过"，
     * 前端可以根据它提示用户"索引尚未初始化，请先重建"。</p>
     */
    public RagIndexStatusResponse getStatus() {
        return new RagIndexStatusResponse(documentCount, chunkCount, embeddedChunkCount, lastRebuildAt, lastRebuildAt != null);
    }

    /**
     * 纯检索接口（GET /rag/index/search）——RAG 的"调试窗口"。
     *
     * <p>这个接口只做"召回"，不生成回答。它的价值在于：当问答结果不对时，
     * 开发者可以先在这里看"到底召回的内容对不对"，从而判断问题出在
     * <b>召回（retrieval）</b>还是<b>生成（generation）</b>。</p>
     *
     * <p>流程：问题 → embedding 转向量 → pgvector 余弦相似度查询 → 阈值过滤 → 返回。</p>
     */
    public RagSearchResponse search(String query, int topK) {
        String traceId = TraceIdContext.get();
        long start = System.currentTimeMillis();
        log.info("[{}] search start, query={}, topK={}, minScore={}", traceId, query, topK, minScoreThreshold);

        // 1. 原始检索：拿到按相似度排序的 topK 个片段（未过滤）
        List<RagSearchResult> rawResults = retrieve(query, topK);
        long retrieveMs = System.currentTimeMillis() - start;

        // 2. 阈值过滤：只保留相似度达标的片段。
        //    这样做的意义：防止把"看起来沾边但实际无关"的内容暴露出去，
        //    也是控制幻觉的第一道闸门（分数太低的内容根本不该进 prompt）
        List<RagSearchResult> filtered = rawResults.stream()
                .filter(result -> result.getScore() >= minScoreThreshold)
                .collect(Collectors.toList());

        long totalMs = System.currentTimeMillis() - start;
        log.info("[{}] search done in {}ms (retrieve {}ms), raw={}, filtered={}",
                traceId, totalMs, retrieveMs, rawResults.size(), filtered.size());
        return new RagSearchResponse(query, filtered);
    }

    /**
     * RAG 问答接口（POST /rag/ask）——带引用和拒答的"开卷考试"。
     *
     * <p>完整流程：</p>
     * <pre>
     * 问题 → 检索召回 → 阈值过滤 → 判断是否有足够上下文
     *   ├─ 没有 → 拒答（说明原因：检索为空 / 分数太低）
     *   └─ 有   → 把片段拼进 prompt → 调模型生成答案 → 附带引用
     * </pre>
     *
     * <p>三个关键设计：</p>
     * <ol>
     *   <li><b>Grounded（接地）</b>：prompt 明确要求"只根据提供的片段回答"，
     *       从源头压制幻觉；</li>
     *   <li><b>Citation（引用）</b>：citations 直接来自检索结果，
     *       不允许模型自己编来源；</li>
     *   <li><b>Refusal（拒答）</b>：上下文不足时明确拒绝回答，而不是硬编。
     *       refusalReason 精确告诉调用方拒答原因，方便前端展示和排障。</li>
     * </ol>
     */
    public RagAskResponse ask(String query, int topK) {
        String traceId = TraceIdContext.get();
        log.info("[{}] ask start, query={}, topK={}, minScore={}, promptDebug={}",
                traceId, query, topK, minScoreThreshold, promptDebug);

        // 1. 原始检索（不过滤）——之所以保留原始结果，
        //    是为了区分两种拒答原因："根本没检索到" vs "检索到了但分数太低"
        List<RagSearchResult> rawResults = retrieve(query, topK);

        // 2. 应用阈值过滤，得到真正可以当作证据的片段
        List<RagSearchResult> usedChunks = rawResults.stream()
                .filter(result -> result.getScore() >= minScoreThreshold)
                .collect(Collectors.toList());

        RagAskResponse response = new RagAskResponse();
        // retrievedChunks 供前端调试；citations 作为"引用来源"展示
        response.setRetrievedChunks(usedChunks);
        response.setCitations(usedChunks);

        // 3. 拒答场景一：完全没检索到任何片段
        if (rawResults.isEmpty()) {
            response.setEnoughContext(false);
            response.setRefusalReason(RefusalReason.NO_RETRIEVED_CHUNKS);
            response.setAnswer("抱歉，知识库中没有找到相关信息来回答您的问题。请尝试换一种方式提问，或者先导入更多文档。");
            return response;
        }

        // 4. 拒答场景二：检索到了，但所有片段相似度都低于阈值
        //    ——说明知识库里可能没有真正相关的内容，强行回答就是编造
        if (usedChunks.isEmpty()) {
            response.setEnoughContext(false);
            response.setRefusalReason(RefusalReason.LOW_SIMILARITY_SCORE);
            response.setAnswer("抱歉，知识库中召回的内容与您的问题相关度较低，无法给出可靠回答。请尝试换一种方式提问。");
            return response;
        }

        // 5. 组装上下文：把每个片段标上序号和来源，方便模型引用
        //    也方便用户核对"答案来自哪段原文"
        StringBuilder contextBuilder = new StringBuilder();
        for (int i = 0; i < usedChunks.size(); i++) {
            RagSearchResult chunk = usedChunks.get(i);
            contextBuilder.append(String.format("【片段 %d】（来源：%s - %s）\n%s\n\n",
                    i + 1, chunk.getFileName(), chunk.getSectionTitle(), chunk.getContent()));
        }

        // 6. System prompt：规定模型的行为边界。
        //    这里的每一句"规则"都是对幻觉的约束，是 RAG 的核心工程质量所在
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

        // User prompt：把问题和证据一起交给模型（"开卷"）
        String userPrompt = String.format("问题：%s\n\n检索到的文档片段：\n%s", query, contextBuilder.toString());

        // Prompt 调试模式：把最终发给模型的完整 prompt 原样返回，
        // 这样你不用看日志就能知道"模型到底看到了什么"，排查 prompt 质量问题
        if (promptDebug) {
            response.setDebugPrompt("[SYSTEM]\n" + systemPrompt + "\n\n[USER]\n" + userPrompt);
        }

        // 7. 调用模型生成答案（链式 API：prompt → system → user → call → content）
        String answer = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();
        response.setEnoughContext(true);
        response.setAnswer(answer);
        return response;
    }

    /**
     * 执行一次"原始"向量检索（不做阈值过滤），返回按相似度降序的结果。
     *
     * <p>search() 和 ask() 共用这段逻辑，只是过滤策略不同：</p>
     * <ul>
     *   <li>search() 对调用方直接返回过滤后的结果；</li>
     *   <li>ask() 需要原始结果来判断拒答原因（空 vs 低分）。</li>
     * </ul>
     *
     * <p>检索原理（这是 RAG 的关键知识点）：</p>
     * <ol>
     *   <li><b>Embedding</b>：把用户问题通过 embedding 模型转成向量（如 1024 维浮点数组）；
     *       语义相近的文本，向量在空间中也更接近。</li>
     *   <li><b>余弦相似度</b>：pgvector 的 {@code <=>} 运算符计算余弦距离，
     *       范围 [0, 2]，0 表示方向完全一致。用 {@code 1 - 距离} 转成"相似度"，
     *       越大越相关，方便理解和过滤。</li>
     *   <li><b>SQL 手写</b>：直接查表比用 VectorStore API 更能看清原理，
     *       也能自由拼接 LIMIT 和过滤条件。</li>
     * </ol>
     */
    private List<RagSearchResult> retrieve(String query, int topK) {
        String traceId = TraceIdContext.get();
        long start = System.currentTimeMillis();

        // 1. 文本 → 向量
        float[] embedding = embeddingModel.embed(query);
        long embeddingMs = System.currentTimeMillis() - start;
        log.info("[{}] embedding done in {}ms, dim={}", traceId, embeddingMs, embedding.length);

        // 2. 把 float 数组拼成 pgvector 的字符串格式，例如 [0.1,0.2,0.3]
        String embeddingStr = toPgVectorString(embedding);

        // 3. SQL：按余弦距离升序（越近越相似）取 topK 条
        //    similarity = 1 - cosine_distance，直接作为可读的分数
        String sql = "SELECT metadata, content, 1 - (embedding <=> ?::vector) AS similarity " +
                     "FROM " + vectorStoreSchemaName + "." + vectorStoreTableName + " " +
                     "ORDER BY embedding <=> ?::vector ASC LIMIT ?";

        // 4. RowMapper：把数据库行映射成业务对象 RagSearchResult。
        //    metadata 是 JSON 字符串（入库时用 Jackson 序列化的），这里反序列化回 Map，
        //    取出 docId/fileName/title/sectionTitle/chunkIndex 等可追溯信息
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

        long queryMs = System.currentTimeMillis() - start;
        log.info("[{}] pgvector query done in {}ms, rawHits={}", traceId, queryMs, results.size());
        return results;
    }

    /**
     * 把业务 chunk 转成 Spring AI 的 {@link Document}。
     *
     * <p>Spring AI 的 VectorStore 只认识 Document（content + metadata），
     * 所以入库前要做一次格式转换。metadata 会原样序列化进数据库的 metadata 列，
     * 检索时再从 JSON 里取出来用于展示引用。</p>
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

    /**
     * 清空向量表。
     *
     * <p>TRUNCATE 比 DELETE 快（不逐行记日志），适合全量重建场景。</p>
     */
    private void clearVectorStore() {
        jdbcTemplate.update("TRUNCATE TABLE " + vectorStoreSchemaName + "." + vectorStoreTableName);
    }

    /**
     * 校验表名/schema 名格式。
     *
     * <p>这两个值会直接拼进 SQL 字符串，如果允许任意字符就可能被 SQL 注入。
     * 限制为字母/数字/下划线开头，是最简单有效的防护。</p>
     */
    private String validateIdentifier(String identifier, String propertyName) {
        if (identifier == null || !identifier.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("Invalid pgvector " + propertyName + ": " + identifier);
        }
        return identifier;
    }

    /**
     * 把 float 向量数组拼成 pgvector 的字符串字面量，例如 {@code [0.1,0.2,0.3]}。
     *
     * <p>pgvector 的 {@code ?::vector} 占位符需要这种文本格式才能转换。</p>
     */
    private String toPgVectorString(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 解析数据库里 metadata 列的 JSON 字符串。
     *
     * <p>入库时 Spring AI 会把 Document 的 metadata Map 序列化成 JSON 存入，
     * 查询时再反序列化回来。解析失败时返回空 Map 并记录警告，
     * 避免一条脏数据导致整个查询崩溃。</p>
     */
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
