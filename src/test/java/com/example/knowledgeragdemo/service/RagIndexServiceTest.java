package com.example.knowledgeragdemo.service;

import com.example.knowledgeragdemo.dto.RagChunk;
import com.example.knowledgeragdemo.dto.RagChunksResponse;
import com.example.knowledgeragdemo.dto.RagDocumentMetadata;
import com.example.knowledgeragdemo.dto.RagIngestResponse;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class RagIndexServiceTest {

    @Test
    void rebuildClearsPgVectorTableBeforeWritingNewEmbeddings() {
        RagIngestionService ingestionService = mock(RagIngestionService.class);
        RagChunkService chunkService = mock(RagChunkService.class);
        VectorStore vectorStore = mock(VectorStore.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);

        RagDocumentMetadata document = new RagDocumentMetadata(
                "doc-1",
                "sample.md",
                "docs/sample-docs/sample.md",
                "Sample",
                "hash",
                LocalDateTime.now()
        );
        RagChunk chunk = new RagChunk(
                "doc-1",
                "sample.md",
                "docs/sample-docs/sample.md",
                "Sample",
                "Overview",
                0,
                "content"
        );

        when(ingestionService.ingest()).thenReturn(new RagIngestResponse(1, 0, List.of(document)));
        when(chunkService.listChunks()).thenReturn(new RagChunksResponse(1, 1, 500, 80, List.of(chunk)));

        RagIndexService service = new RagIndexService(
                ingestionService,
                chunkService,
                vectorStore,
                jdbcTemplate,
                "public",
                "vector_store"
        );

        service.rebuildIndex();

        InOrder inOrder = inOrder(jdbcTemplate, vectorStore);
        inOrder.verify(jdbcTemplate).update("TRUNCATE TABLE public.vector_store");
        inOrder.verify(vectorStore).add(anyList());
    }

    @Test
    void rebuildSendsEmbeddingsInBatchesOfAtMostTen() {
        RagIngestionService ingestionService = mock(RagIngestionService.class);
        RagChunkService chunkService = mock(RagChunkService.class);
        VectorStore vectorStore = mock(VectorStore.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);

        RagDocumentMetadata document = new RagDocumentMetadata(
                "doc-1",
                "sample.md",
                "docs/sample-docs/sample.md",
                "Sample",
                "hash",
                LocalDateTime.now()
        );

        // 生成 25 个 chunk，期望分成 3 批：10 + 10 + 5
        List<RagChunk> chunks = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            chunks.add(new RagChunk(
                    "doc-1",
                    "sample.md",
                    "docs/sample-docs/sample.md",
                    "Sample",
                    "Section",
                    i,
                    "content-" + i
            ));
        }

        when(ingestionService.ingest()).thenReturn(new RagIngestResponse(1, 0, List.of(document)));
        when(chunkService.listChunks()).thenReturn(new RagChunksResponse(1, 25, 500, 80, chunks));

        RagIndexService service = new RagIndexService(
                ingestionService,
                chunkService,
                vectorStore,
                jdbcTemplate,
                "public",
                "vector_store"
        );

        service.rebuildIndex();

        // 验证 add 被调用了 3 次，且每次传入的文档数不超过 10
        verify(vectorStore, times(3)).add(anyList());
        verify(vectorStore).add(argThat(list -> list.size() == 10));
        verify(vectorStore).add(argThat(list -> list.size() == 10));
        verify(vectorStore).add(argThat(list -> list.size() == 5));
    }
}