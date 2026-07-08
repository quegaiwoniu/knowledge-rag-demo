package com.example.knowledgeragdemo.dto;

import java.time.LocalDateTime;

/**
 * RAG 文档导入后的元数据。
 *
 * <p>这里暂时不保存 chunk 和向量，只保留后续切片、索引和引用答案需要用到的
 * 原始文档级信息。</p>
 */
public class RagDocumentMetadata {

    /**
     * 文档唯一标识。
     *
     * <p>当前用 contentHash 的前 12 位生成，便于同一份内容在不同环境中得到稳定 ID。</p>
     */
    private String docId;

    /**
     * Markdown 文件名，例如 order-status-definition.md。
     */
    private String fileName;

    /**
     * 文档来源路径，用于后续回答时展示引用来源。
     */
    private String sourcePath;

    /**
     * 文档标题，默认从 Markdown 一级标题中解析。
     */
    private String title;

    /**
     * 文档内容的 SHA-256 哈希，用于识别重复文档。
     */
    private String contentHash;

    /**
     * 本次导入时间，方便排查“知识库是什么时候刷新过”的问题。
     */
    private LocalDateTime ingestedAt;

    public RagDocumentMetadata() {
    }

    public RagDocumentMetadata(String docId, String fileName, String sourcePath, String title,
                               String contentHash, LocalDateTime ingestedAt) {
        this.docId = docId;
        this.fileName = fileName;
        this.sourcePath = sourcePath;
        this.title = title;
        this.contentHash = contentHash;
        this.ingestedAt = ingestedAt;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public LocalDateTime getIngestedAt() {
        return ingestedAt;
    }

    public void setIngestedAt(LocalDateTime ingestedAt) {
        this.ingestedAt = ingestedAt;
    }
}
