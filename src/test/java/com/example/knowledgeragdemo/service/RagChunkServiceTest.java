package com.example.knowledgeragdemo.service;

import com.example.knowledgeragdemo.config.AppRagProperties;
import com.example.knowledgeragdemo.dto.RagChunk;
import com.example.knowledgeragdemo.dto.RagChunksResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RagChunkServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void chunksKeepDocumentMetadataAndSectionTitle() throws Exception {
        Path docsDirectory = tempDir.resolve("sample-docs");
        Files.createDirectories(docsDirectory);
        Files.writeString(docsDirectory.resolve("order.md"), """
                # 订单排障手册

                ## 支付状态

                支付中超过五分钟需要先检查支付流水，再触发订单状态同步。

                ## 退款状态

                退款失败后需要先确认渠道失败原因，不能直接重复提交。
                """);

        RagChunkService chunkService = createChunkService(docsDirectory, 30, 5);

        RagChunksResponse response = chunkService.listChunks();
        List<RagChunk> chunks = response.getChunks();

        assertThat(response.getChunkCount()).isEqualTo(chunks.size());
        assertThat(chunks).isNotEmpty();
        assertThat(chunks.get(0).getDocId()).startsWith("doc-");
        assertThat(chunks.get(0).getFileName()).isEqualTo("order.md");
        assertThat(chunks.get(0).getSourcePath()).contains("order.md");
        assertThat(chunks.get(0).getTitle()).isEqualTo("订单排障手册");
        assertThat(chunks.get(0).getSectionTitle()).isEqualTo("支付状态");
        assertThat(chunks.get(0).getChunkIndex()).isZero();
        assertThat(chunks.get(0).getContent()).contains("支付");
    }

    @Test
    void chunksKeepStableOrderAcrossSectionsAndSplitParts() throws Exception {
        Path docsDirectory = tempDir.resolve("sample-docs");
        Files.createDirectories(docsDirectory);
        Files.writeString(docsDirectory.resolve("order.md"), """
                # 订单排障手册

                ## 第一章

                甲甲甲甲甲甲甲甲甲甲甲甲甲甲甲甲甲甲甲甲甲甲甲甲甲甲甲甲甲甲

                ## 第二章

                乙乙乙乙乙乙乙乙乙乙乙乙乙乙乙乙乙乙乙乙乙乙乙乙乙乙乙乙乙乙
                """);

        RagChunkService chunkService = createChunkService(docsDirectory, 12, 2);

        List<RagChunk> chunks = chunkService.listChunks().getChunks();

        assertThat(chunks).extracting(RagChunk::getChunkIndex)
                .containsExactlyElementsOf(java.util.stream.IntStream.range(0, chunks.size()).boxed().toList());
        assertThat(chunks.get(0).getSectionTitle()).isEqualTo("第一章");
        assertThat(chunks.get(chunks.size() - 1).getSectionTitle()).isEqualTo("第二章");
    }

    @Test
    void emptyMarkdownDocumentProducesNoChunks() throws Exception {
        Path docsDirectory = tempDir.resolve("sample-docs");
        Files.createDirectories(docsDirectory);
        Files.writeString(docsDirectory.resolve("empty.md"), "   \n\n  ");

        RagChunkService chunkService = createChunkService(docsDirectory, 20, 5);

        RagChunksResponse response = chunkService.listChunks();

        assertThat(response.getDocumentCount()).isEqualTo(1);
        assertThat(response.getChunkCount()).isZero();
        assertThat(response.getChunks()).isEmpty();
    }

    private RagChunkService createChunkService(Path docsDirectory, int chunkSize, int chunkOverlap) {
        AppRagProperties properties = new AppRagProperties(docsDirectory.toString(), chunkSize, chunkOverlap, 0.5);
        RagIngestionService ingestionService = new RagIngestionService(properties);
        ingestionService.ingest();
        return new RagChunkService(properties, ingestionService);
    }
}
