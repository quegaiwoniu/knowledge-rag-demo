package com.example.knowledgeragdemo.service;

import com.example.knowledgeragdemo.config.AppRagProperties;
import com.example.knowledgeragdemo.dto.RagIngestResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RagIngestionServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void ingestFailsWhenSampleDocsDirectoryDoesNotExist() {
        Path missingDirectory = tempDir.resolve("missing-docs");
        RagIngestionService service = new RagIngestionService(new AppRagProperties(missingDirectory.toString(), 500, 80));

        assertThatThrownBy(service::ingest)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sample docs directory does not exist");
    }

    @Test
    void ingestSkipsDocumentsWithDuplicateContentHash() throws Exception {
        Path docsDirectory = tempDir.resolve("sample-docs");
        Files.createDirectories(docsDirectory);
        Files.writeString(docsDirectory.resolve("a.md"), """
                # 相同文档 A

                ## 说明

                这是一份内容完全相同的测试文档。
                """);
        Files.writeString(docsDirectory.resolve("b.md"), """
                # 相同文档 A

                ## 说明

                这是一份内容完全相同的测试文档。
                """);

        RagIngestionService service = new RagIngestionService(new AppRagProperties(docsDirectory.toString(), 500, 80));

        RagIngestResponse response = service.ingest();

        assertThat(response.getImportedCount()).isEqualTo(1);
        assertThat(response.getDuplicateCount()).isEqualTo(1);
        assertThat(response.getDocuments()).hasSize(1);
    }

    @Test
    void ingestResolvesDefaultDocsDirectoryWhenStartedFromWorkspaceParent() throws Exception {
        Path workspaceDirectory = tempDir.resolve("ragdemo");
        Path backendDirectory = workspaceDirectory.resolve("knowledge-rag-demo");
        Path docsDirectory = backendDirectory.resolve("docs").resolve("sample-docs");
        Files.createDirectories(docsDirectory);
        Files.writeString(docsDirectory.resolve("order.md"), """
                # 订单测试文档

                ## 说明

                这是一份用于模拟从父级工作区启动后端的文档。
                """);

        RagIngestionService service = new RagIngestionService(new AppRagProperties("docs/sample-docs", 500, 80)) {
            @Override
            protected Path getWorkingDirectory() {
                return workspaceDirectory;
            }
        };

        RagIngestResponse response = service.ingest();

        assertThat(response.getImportedCount()).isEqualTo(1);
        assertThat(response.getDocuments().get(0).getTitle()).isEqualTo("订单测试文档");
    }
}
