package com.example.knowledgeragdemo.service;

import com.example.knowledgeragdemo.dto.RagChunk;
import com.example.knowledgeragdemo.dto.RagChunksResponse;
import com.example.knowledgeragdemo.dto.RagDocumentMetadata;
import com.example.knowledgeragdemo.dto.RagIngestResponse;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class RagIndexServiceTest {

    @Test
    void rebuildClearsPgVectorTableBeforeWritingNewEmbeddings() {
        RagIngestionService ingestionService = mock(RagIngestionService.class);
        RagChunkService chunkService = mock(RagChunkService.class);
        VectorStore vectorStore = mock(VectorStore.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        EmbeddingModel embeddingModel = mock(EmbeddingModel.class);
        ChatClient.Builder chatClientBuilder = mock(ChatClient.Builder.class);
        when(chatClientBuilder.build()).thenReturn(mock(ChatClient.class));

        RagDocumentMetadata document = new RagDocumentMetadata("doc-1", "sample.md", "docs/sample-docs/sample.md", "Sample", "hash", LocalDateTime.now());
        RagChunk chunk = new RagChunk("doc-1", "sample.md", "docs/sample-docs/sample.md", "Sample", "Overview", 0, "content");

        when(ingestionService.ingest()).thenReturn(new RagIngestResponse(1, 0, List.of(document)));
        when(chunkService.listChunks()).thenReturn(new RagChunksResponse(1, 1, 500, 80, List.of(chunk)));
        when(embeddingModel.embed(anyString())).thenReturn(new float[]{0.1f, 0.2f, 0.3f});

        RagIndexService service = new RagIndexService(ingestionService, chunkService, vectorStore, embeddingModel, jdbcTemplate, chatClientBuilder, "public", "vector_store");
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
        EmbeddingModel embeddingModel = mock(EmbeddingModel.class);
        ChatClient.Builder chatClientBuilder = mock(ChatClient.Builder.class);
        when(chatClientBuilder.build()).thenReturn(mock(ChatClient.class));

        RagDocumentMetadata document = new RagDocumentMetadata("doc-1", "sample.md", "docs/sample-docs/sample.md", "Sample", "hash", LocalDateTime.now());
        List<RagChunk> chunks = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            chunks.add(new RagChunk("doc-1", "sample.md", "docs/sample-docs/sample.md", "Sample", "Section", i, "content-" + i));
        }

        when(ingestionService.ingest()).thenReturn(new RagIngestResponse(1, 0, List.of(document)));
        when(chunkService.listChunks()).thenReturn(new RagChunksResponse(1, 25, 500, 80, chunks));
        when(embeddingModel.embed(anyString())).thenReturn(new float[]{0.1f, 0.2f, 0.3f});

        RagIndexService service = new RagIndexService(ingestionService, chunkService, vectorStore, embeddingModel, jdbcTemplate, chatClientBuilder, "public", "vector_store");
        service.rebuildIndex();

        verify(vectorStore, times(3)).add(anyList());
        verify(vectorStore, times(2)).add(argThat(list -> list.size() == 10));
        verify(vectorStore, times(1)).add(argThat(list -> list.size() == 5));
    }
}
