package com.example.knowledgeragdemo.service;

import com.example.knowledgeragdemo.dto.RagAskResponse;
import com.example.knowledgeragdemo.dto.RagChunk;
import com.example.knowledgeragdemo.dto.RagChunksResponse;
import com.example.knowledgeragdemo.dto.RagDocumentMetadata;
import com.example.knowledgeragdemo.dto.RagIngestResponse;
import com.example.knowledgeragdemo.dto.RagSearchResponse;
import com.example.knowledgeragdemo.dto.RagSearchResult;
import com.example.knowledgeragdemo.dto.RefusalReason;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * RagIndexService 的单元测试——学习 Mockito 测试隔离的最佳范例。
 *
 * <h3>为什么所有依赖都要 mock？</h3>
 * <p>RagIndexService 依赖数据库(JdbcTemplate)、向量库(VectorStore)、
 * embedding 模型、大模型 ChatClient——全是外部资源。如果测试真的去连这些，
 * 就会：慢、不稳定（网络波动）、依赖环境（没数据库就跑不了）。
 * 用 Mockito 把它们替换成"假对象"，测试就变成纯内存运行，快且可复现。</p>
 *
 * <h3>常用 Mockito 技巧（在本文件里都能找到例子）</h3>
 * <ul>
 *   <li>{@code mock(X.class)}：创建一个假对象，所有方法默认返回 null/0/false；</li>
 *   <li>{@code when(x.method()).thenReturn(v)}：指定某方法调用时返回什么；</li>
 *   <li>{@code when(x.method()).thenAnswer(...)}：调用时才计算返回值（灵活）；</li>
 *   <li>{@code verify(x).method()}：断言某个方法确实被调用过；
 *       {@code times(n)} 断言调用次数；</li>
 *   <li>{@code InOrder}：验证多个 mock 的调用顺序（例如"先清空表再写入"）；</li>
 *   <li>{@code argThat(...)}：按自定义条件匹配参数。</li>
 * </ul>
 */
class RagIndexServiceTest {

    /**
     * 验证"重建索引时，先清空向量表，再写入新的 embedding"。
     *
     * <p>这个顺序很重要：如果先写后清，旧数据可能残留；
     * InOrder 就是专门用来验证这种"顺序敏感"逻辑的。</p>
     */
    @Test
    void rebuildClearsPgVectorTableBeforeWritingNewEmbeddings() {
        // --- 1. 准备 mock 依赖 ---
        RagIngestionService ingestionService = mock(RagIngestionService.class);
        RagChunkService chunkService = mock(RagChunkService.class);
        VectorStore vectorStore = mock(VectorStore.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        EmbeddingModel embeddingModel = mock(EmbeddingModel.class);
        ChatClient.Builder chatClientBuilder = mock(ChatClient.Builder.class);
        when(chatClientBuilder.build()).thenReturn(mock(ChatClient.class));

        // 造一份"1 个文档、1 个 chunk"的假数据
        RagDocumentMetadata document = new RagDocumentMetadata("doc-1", "sample.md", "docs/sample-docs/sample.md", "Sample", "hash", LocalDateTime.now());
        RagChunk chunk = new RagChunk("doc-1", "sample.md", "docs/sample-docs/sample.md", "Sample", "Overview", 0, "content");

        // --- 2. 指定 mock 行为：导入返回 1 个文档，切片返回 1 个 chunk ---
        when(ingestionService.ingest()).thenReturn(new RagIngestResponse(1, 0, List.of(document)));
        when(chunkService.listChunks()).thenReturn(new RagChunksResponse(1, 1, 500, 80, List.of(chunk)));
        // embed 方法接收任意字符串，都返回一个 3 维假向量
        when(embeddingModel.embed(anyString())).thenReturn(new float[]{0.1f, 0.2f, 0.3f});

        // --- 3. 构造被测对象并执行 ---
        RagIndexService service = new RagIndexService(ingestionService, chunkService, vectorStore, embeddingModel, jdbcTemplate, chatClientBuilder, "public", "vector_store", 0.5, false);
        service.rebuildIndex();

        // --- 4. 断言：先 TRUNCATE（清空表），再 add（写入）---
        InOrder inOrder = inOrder(jdbcTemplate, vectorStore);
        inOrder.verify(jdbcTemplate).update("TRUNCATE TABLE public.vector_store");
        inOrder.verify(vectorStore).add(anyList());
    }

    /**
     * 验证"embedding 按每批最多 10 个写入"。
     *
     * <p>25 个 chunk 应该被分成 3 批：10 + 10 + 5。
     * 这个测试保护了批量写入逻辑——如果哪天有人把 batchSize 改没了，
     * 这个测试会立即失败。</p>
     */
    @Test
    void rebuildSendsEmbeddingsInBatchesOfAtMostTen() {
        RagIngestionService ingestionService = mock(RagIngestionService.class);
        RagChunkService chunkService = mock(RagChunkService.class);
        VectorStore vectorStore = mock(VectorStore.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        EmbeddingModel embeddingModel = mock(EmbeddingModel.class);
        ChatClient.Builder chatClientBuilder = mock(ChatClient.Builder.class);
        when(chatClientBuilder.build()).thenReturn(mock(ChatClient.class));

        // 造 25 个 chunk（模拟一篇被切成 25 段的文档）
        RagDocumentMetadata document = new RagDocumentMetadata("doc-1", "sample.md", "docs/sample-docs/sample.md", "Sample", "hash", LocalDateTime.now());
        List<RagChunk> chunks = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            chunks.add(new RagChunk("doc-1", "sample.md", "docs/sample-docs/sample.md", "Sample", "Section", i, "content-" + i));
        }

        when(ingestionService.ingest()).thenReturn(new RagIngestResponse(1, 0, List.of(document)));
        when(chunkService.listChunks()).thenReturn(new RagChunksResponse(1, 25, 500, 80, chunks));
        when(embeddingModel.embed(anyString())).thenReturn(new float[]{0.1f, 0.2f, 0.3f});

        RagIndexService service = new RagIndexService(ingestionService, chunkService, vectorStore, embeddingModel, jdbcTemplate, chatClientBuilder, "public", "vector_store", 0.5, false);
        service.rebuildIndex();

        // add 一共被调用 3 次（25 = 10 + 10 + 5）
        verify(vectorStore, times(3)).add(anyList());
        // 其中 2 批是 10 个，1 批是 5 个（用 argThat 自定义匹配）
        verify(vectorStore, times(2)).add(argThat(list -> list.size() == 10));
        verify(vectorStore, times(1)).add(argThat(list -> list.size() == 5));
    }

    /**
     * 验证"search 会过滤掉低于相似度阈值的结果"（Day 12 边界防御）。
     *
     * <p>这里展示了如何 mock JDBC：</p>
     * <ul>
     *   <li>mock 一个 ResultSet，用 {@code when(rs.next()).thenReturn(true, true, false)}
     *       模拟"有 2 行数据"——注意这个技巧：依次返回的值列表，false 表示没有更多行；</li>
     *   <li>jdbcTemplate.query 用 thenAnswer 手动调用 RowMapper.mapRow，
     *       把"数据库行"转换成我们预定义的两条结果。</li>
     * </ul>
     */
    @Test
    void searchFiltersOutResultsBelowScoreThreshold() throws SQLException {
        RagIngestionService ingestionService = mock(RagIngestionService.class);
        RagChunkService chunkService = mock(RagChunkService.class);
        VectorStore vectorStore = mock(VectorStore.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        EmbeddingModel embeddingModel = mock(EmbeddingModel.class);
        ChatClient.Builder chatClientBuilder = mock(ChatClient.Builder.class);
        when(chatClientBuilder.build()).thenReturn(mock(ChatClient.class));

        // 模拟 ResultSet：第一行相似度 0.9，第二行 0.3（阈值是 0.5）
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, true, false); // 有 2 行数据
        when(rs.getString("metadata")).thenReturn(
                "{\"docId\":\"doc-1\",\"fileName\":\"a.md\",\"title\":\"A\",\"sectionTitle\":\"S1\",\"chunkIndex\":0}",
                "{\"docId\":\"doc-2\",\"fileName\":\"b.md\",\"title\":\"B\",\"sectionTitle\":\"S2\",\"chunkIndex\":1}");
        when(rs.getString("content")).thenReturn("content-a", "content-b");
        when(rs.getDouble("similarity")).thenReturn(0.9, 0.3);

        when(embeddingModel.embed(anyString())).thenReturn(new float[]{0.1f, 0.2f});
        // thenAnswer：真正执行 RowMapper，把两行"数据"映射成两个 RagSearchResult
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), anyString(), anyString(), anyInt()))
                .thenAnswer(invocation -> {
                    org.springframework.jdbc.core.RowMapper<RagSearchResult> mapper =
                            invocation.getArgument(1);
                    List<RagSearchResult> results = new ArrayList<>();
                    results.add(mapper.mapRow(rs, 0));
                    results.add(mapper.mapRow(rs, 1));
                    return results;
                });

        RagIndexService service = new RagIndexService(ingestionService, chunkService, vectorStore, embeddingModel, jdbcTemplate, chatClientBuilder, "public", "vector_store", 0.5, false);
        RagSearchResponse response = service.search("测试问题", 5);

        // 只有 0.9 的那条通过过滤（0.3 < 0.5 被丢弃）
        assertEquals(1, response.getResults().size());
        assertEquals("doc-1", response.getResults().get(0).getDocId());
        assertEquals(0.9, response.getResults().get(0).getScore(), 0.0001);
    }

    /**
     * 验证"检索结果为空时拒答，且拒答原因是 NO_RETRIEVED_CHUNKS"。
     *
     * <p>jdbcTemplate.query 直接返回空列表，模拟"知识库里完全没有相关内容"。
     * 这是 RAG 拒答机制的第一条防线：没资料就明说，绝不硬编。</p>
     */
    @Test
    void askRefusesWithNoRetrievedChunksWhenNothingFound() throws SQLException {
        RagIngestionService ingestionService = mock(RagIngestionService.class);
        RagChunkService chunkService = mock(RagChunkService.class);
        VectorStore vectorStore = mock(VectorStore.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        EmbeddingModel embeddingModel = mock(EmbeddingModel.class);
        ChatClient.Builder chatClientBuilder = mock(ChatClient.Builder.class);
        when(chatClientBuilder.build()).thenReturn(mock(ChatClient.class));

        when(embeddingModel.embed(anyString())).thenReturn(new float[]{0.1f, 0.2f});
        // 核心 mock：检索直接返回空
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), anyString(), anyString(), anyInt()))
                .thenReturn(new ArrayList<>());

        RagIndexService service = new RagIndexService(ingestionService, chunkService, vectorStore, embeddingModel, jdbcTemplate, chatClientBuilder, "public", "vector_store", 0.5, false);
        RagAskResponse response = service.ask("不存在的知识", 5);

        assertFalse(response.isEnoughContext());
        assertEquals(RefusalReason.NO_RETRIEVED_CHUNKS, response.getRefusalReason());
        assertNotNull(response.getAnswer()); // 拒答也要给出礼貌的说明文案
    }

    /**
     * 验证"检索到内容但相似度全低于阈值时拒答，原因是 LOW_SIMILARITY_SCORE"。
     *
     * <p>与上一个测试的区别：这次"检索到了"（rawResults 非空），
     * 只是分数不够（0.3 < 0.5）。这正是 ask() 保留原始结果的原因——
     * 否则无法区分"没资料"和"资料不够相关"两种拒答场景。</p>
     */
    @Test
    void askRefusesWithLowSimilarityWhenAllScoresBelowThreshold() throws SQLException {
        RagIngestionService ingestionService = mock(RagIngestionService.class);
        RagChunkService chunkService = mock(RagChunkService.class);
        VectorStore vectorStore = mock(VectorStore.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        EmbeddingModel embeddingModel = mock(EmbeddingModel.class);
        ChatClient.Builder chatClientBuilder = mock(ChatClient.Builder.class);
        when(chatClientBuilder.build()).thenReturn(mock(ChatClient.class));

        // 模拟 ResultSet：只有 1 行，相似度 0.3（低于阈值 0.5）
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("metadata")).thenReturn("{\"docId\":\"doc-1\",\"fileName\":\"a.md\",\"title\":\"A\",\"sectionTitle\":\"S1\",\"chunkIndex\":0}");
        when(rs.getString("content")).thenReturn("content-a");
        when(rs.getDouble("similarity")).thenReturn(0.3);

        when(embeddingModel.embed(anyString())).thenReturn(new float[]{0.1f, 0.2f});
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), anyString(), anyString(), anyInt()))
                .thenAnswer(invocation -> {
                    org.springframework.jdbc.core.RowMapper<RagSearchResult> mapper =
                            invocation.getArgument(1);
                    return List.of(mapper.mapRow(rs, 0));
                });

        RagIndexService service = new RagIndexService(ingestionService, chunkService, vectorStore, embeddingModel, jdbcTemplate, chatClientBuilder, "public", "vector_store", 0.5, false);
        RagAskResponse response = service.ask("测试问题", 5);

        assertFalse(response.isEnoughContext());
        assertEquals(RefusalReason.LOW_SIMILARITY_SCORE, response.getRefusalReason());
        assertNotNull(response.getAnswer());
    }

    /**
     * 验证"开启 prompt-debug 时，响应里返回完整 prompt"。
     *
     * <p>这里展示了如何 mock ChatClient 的<b>链式调用</b>：
     * {@code chatClient.prompt().system(...).user(...).call().content()}
     * 是一条调用链，每一环返回的对象都要 mock，并让它们互相衔接：
     * </p>
     * <pre>
     * chatClient.prompt()          → requestSpec
     * requestSpec.system(...)      → requestSpec（返回自己，方便继续链式调用）
     * requestSpec.user(...)        → requestSpec
     * requestSpec.call()           → callSpec
     * callSpec.content()           → "这是答案"
     * </pre>
     */
    @Test
    void askReturnsDebugPromptWhenPromptDebugEnabled() throws SQLException {
        RagIngestionService ingestionService = mock(RagIngestionService.class);
        RagChunkService chunkService = mock(RagChunkService.class);
        VectorStore vectorStore = mock(VectorStore.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        EmbeddingModel embeddingModel = mock(EmbeddingModel.class);

        // --- mock 模型调用链 ---
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callSpec = mock(ChatClient.CallResponseSpec.class);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec); // 返回自己：链式调用得以继续
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callSpec);
        when(callSpec.content()).thenReturn("这是答案");

        ChatClient.Builder chatClientBuilder = mock(ChatClient.Builder.class);
        when(chatClientBuilder.build()).thenReturn(chatClient);

        // 模拟检索结果：1 行，相似度 0.9（通过阈值）
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("metadata")).thenReturn("{\"docId\":\"doc-1\",\"fileName\":\"a.md\",\"title\":\"A\",\"sectionTitle\":\"S1\",\"chunkIndex\":0}");
        when(rs.getString("content")).thenReturn("content-a");
        when(rs.getDouble("similarity")).thenReturn(0.9);

        when(embeddingModel.embed(anyString())).thenReturn(new float[]{0.1f, 0.2f});
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), anyString(), anyString(), anyInt()))
                .thenAnswer(invocation -> {
                    org.springframework.jdbc.core.RowMapper<RagSearchResult> mapper =
                            invocation.getArgument(1);
                    return List.of(mapper.mapRow(rs, 0));
                });

        // 注意最后一个参数是 true：开启 prompt-debug
        RagIndexService service = new RagIndexService(ingestionService, chunkService, vectorStore, embeddingModel, jdbcTemplate, chatClientBuilder, "public", "vector_store", 0.5, true);
        RagAskResponse response = service.ask("测试问题", 5);

        assertTrue(response.isEnoughContext());
        assertEquals("这是答案", response.getAnswer());
        // debugPrompt 应包含完整的 SYSTEM 和 USER 段
        assertNotNull(response.getDebugPrompt());
        assertTrue(response.getDebugPrompt().contains("[SYSTEM]"));
        assertTrue(response.getDebugPrompt().contains("[USER]"));
    }

    /**
     * 验证"关闭 prompt-debug 时，响应里不包含 prompt"。
     *
     * <p>这是上一个测试的对照组：开关为 false 时 debugPrompt 应为 null，
     * 防止调试信息在生产环境泄露。</p>
     */
    @Test
    void askDoesNotReturnDebugPromptWhenPromptDebugDisabled() throws SQLException {
        RagIngestionService ingestionService = mock(RagIngestionService.class);
        RagChunkService chunkService = mock(RagChunkService.class);
        VectorStore vectorStore = mock(VectorStore.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        EmbeddingModel embeddingModel = mock(EmbeddingModel.class);

        // 与上一个测试相同的模型调用链 mock
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callSpec = mock(ChatClient.CallResponseSpec.class);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callSpec);
        when(callSpec.content()).thenReturn("这是答案");

        ChatClient.Builder chatClientBuilder = mock(ChatClient.Builder.class);
        when(chatClientBuilder.build()).thenReturn(chatClient);

        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("metadata")).thenReturn("{\"docId\":\"doc-1\",\"fileName\":\"a.md\",\"title\":\"A\",\"sectionTitle\":\"S1\",\"chunkIndex\":0}");
        when(rs.getString("content")).thenReturn("content-a");
        when(rs.getDouble("similarity")).thenReturn(0.9);

        when(embeddingModel.embed(anyString())).thenReturn(new float[]{0.1f, 0.2f});
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), anyString(), anyString(), anyInt()))
                .thenAnswer(invocation -> {
                    org.springframework.jdbc.core.RowMapper<RagSearchResult> mapper =
                            invocation.getArgument(1);
                    return List.of(mapper.mapRow(rs, 0));
                });

        // 最后一个参数是 false：关闭 prompt-debug
        RagIndexService service = new RagIndexService(ingestionService, chunkService, vectorStore, embeddingModel, jdbcTemplate, chatClientBuilder, "public", "vector_store", 0.5, false);
        RagAskResponse response = service.ask("测试问题", 5);

        assertTrue(response.isEnoughContext());
        assertNull(response.getDebugPrompt());
    }
}
