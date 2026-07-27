package com.example.knowledgeragdemo.service;

import com.example.knowledgeragdemo.config.AppRagProperties;
import com.example.knowledgeragdemo.dto.RagChunk;
import com.example.knowledgeragdemo.dto.RagChunksResponse;
import com.example.knowledgeragdemo.dto.RagDocumentMetadata;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * RAG 文档切片服务。
 *
 * <p>Day 10 先做可解释、可调试的简单字符切片；暂时不做 embedding 和向量入库。</p>
 */
@Service
public class RagChunkService {

    /**
     * RAG 配置，当前主要使用 chunkSize 和 chunkOverlap。
     */
    private final AppRagProperties ragProperties;

    /**
     * Day 10 的 chunk 来源是 Day 9 最近一次导入的文档元数据。
     */
    private final RagIngestionService ragIngestionService;

    public RagChunkService(AppRagProperties ragProperties, RagIngestionService ragIngestionService) {
        this.ragProperties = ragProperties;
        this.ragIngestionService = ragIngestionService;
    }

    /**
     * 基于最近一次导入的文档元数据生成 chunks。
     *
     * <p>如果还没有调用过 /rag/ingest，这里会返回空列表，方便前端作为调试页直接展示。</p>
     */
    public RagChunksResponse listChunks() {
        validateChunkConfig();

        List<RagDocumentMetadata> documents = ragIngestionService.getImportedDocuments();
        List<RagChunk> chunks = new ArrayList<>();

        /*
         * chunkIndex 使用“本次响应内全局递增”的方式。
         * 这样 GET /rag/chunks 返回后，前端和日志里看到的顺序就是后续检索前的真实切片顺序。
         */
        int nextChunkIndex = 0;

        for (RagDocumentMetadata document : documents) {
            String markdown = readMarkdown(document);

            /*
             * 空文档是合法输入：它代表知识库里存在这个文件，但没有可检索内容。
             * 因此 documentCount 会包含它，chunkCount 不会包含它。
             */
            if (markdown.isBlank()) {
                continue;
            }

            // 先按 Markdown 章节切开，再在每个章节内部按 chunkSize/overlap 做字符切片。
            for (MarkdownSection section : splitSections(document.getTitle(), markdown)) {
                nextChunkIndex = appendChunks(chunks, document, section, nextChunkIndex);
            }
        }

        return new RagChunksResponse(
                documents.size(),
                chunks.size(),
                ragProperties.chunkSize(),
                ragProperties.chunkOverlap(),
                chunks
        );
    }

    /**
     * 校验 chunk 配置，避免出现死循环或不可解释的切片结果。
     */
    private void validateChunkConfig() {
        if (ragProperties.chunkSize() <= 0) {
            throw new IllegalStateException("chunk size must be greater than 0");
        }
        if (ragProperties.chunkOverlap() < 0) {
            throw new IllegalStateException("chunk overlap must not be negative");
        }
        if (ragProperties.chunkOverlap() >= ragProperties.chunkSize()) {
            throw new IllegalStateException("chunk overlap must be less than chunk size");
        }
    }

    /**
     * 根据 Day 9 保留的 sourcePath 重新读取 Markdown 原文。
     *
     * <p>这里没有把全文塞进 RagDocumentMetadata，是为了让导入接口保持轻量；
     * Day 10 调试切片时再按需读取源文件。</p>
     */
    private String readMarkdown(RagDocumentMetadata document) {
        try {
            return Files.readString(Path.of(document.getSourcePath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("failed to read imported markdown file: " + document.getSourcePath(), e);
        }
    }

    /**
     * 按 Markdown 章节拆分文档。
     *
     * <p>一级标题通常是文档标题，已经保存在 RagDocumentMetadata.title 中；
     * 二级到六级标题才作为 sectionTitle，用于表示 chunk 的业务章节来源。</p>
     */
    private List<MarkdownSection> splitSections(String documentTitle, String markdown) {
        List<MarkdownSection> sections = new ArrayList<>();
        String currentSectionTitle = documentTitle;
        StringBuilder currentContent = new StringBuilder();

        for (String line : markdown.lines().toList()) {
            // 一级标题只作为文档标题，不进入 chunk 内容，避免第一个 chunk 只有标题信息。
            if (isDocumentTitle(line)) {
                continue;
            }

            /*
             * 遇到新的章节标题时，先把上一段章节内容收口。
             * currentContent 为空时不生成 section，避免连续标题产生空 chunk。
             */
            if (isSectionHeading(line) && !currentContent.toString().isBlank()) {
                sections.add(new MarkdownSection(currentSectionTitle, currentContent.toString().trim()));
                currentContent.setLength(0);
            }

            // sectionTitle 去掉 Markdown 的 # 前缀，只保留用户可读的章节名称。
            if (isSectionHeading(line)) {
                currentSectionTitle = line.replaceFirst("^#{1,6}\\s+", "").trim();
            }

            // 标题行也保留在 content 中，让 chunk 自身带一点上下文，后续给模型更容易理解。
            currentContent.append(line).append(System.lineSeparator());
        }

        // 循环结束后，把最后一个章节收口。
        if (!currentContent.toString().isBlank()) {
            sections.add(new MarkdownSection(currentSectionTitle, currentContent.toString().trim()));
        }

        return sections;
    }

    private boolean isDocumentTitle(String line) {
        return line.matches("^#\\s+.+");
    }

    /**
     * 判断二级到六级 Markdown 标题。
     */
    private boolean isSectionHeading(String line) {
        return line.matches("^#{2,6}\\s+.+");
    }

    /**
     * 把一个章节内容按 chunkSize 和 chunkOverlap 切成多个 chunk。
     *
     * <p>当前使用字符数切片，优点是简单透明；缺点是还不能精确控制 token。
     * 后续接入 tokenizer 后，可以只替换这里的切分策略，外部 DTO 不需要变。</p>
     */
    private int appendChunks(List<RagChunk> chunks, RagDocumentMetadata document,
                             MarkdownSection section, int nextChunkIndex) {
        String content = section.content();
        int start = 0;

        while (start < content.length()) {
            int end = Math.min(content.length(), start + ragProperties.chunkSize());
            String chunkContent = content.substring(start, end).trim();

            if (!chunkContent.isBlank()) {
                // 每个 chunk 都复制文档元数据和章节标题，保证单独拿出来也能追溯来源。
                chunks.add(new RagChunk(
                        document.getDocId(),
                        document.getFileName(),
                        document.getSourcePath(),
                        document.getTitle(),
                        section.sectionTitle(),
                        nextChunkIndex++,
                        chunkContent
                ));
            }

            if (end == content.length()) {
                break;
            }

            /*
             * 下一个 chunk 从 end - overlap 开始，保留上一段尾部上下文。
             * validateChunkConfig 已保证 overlap 小于 chunkSize，所以这里不会原地踏步。
             */
            start = Math.max(0, end - ragProperties.chunkOverlap());
        }

        return nextChunkIndex;
    }

    /**
     * Markdown 章节的内部表示，只在切片服务中使用，不暴露给接口调用方。
     */
    private record MarkdownSection(String sectionTitle, String content) {
    }
}
