package com.example.knowledgeragdemo.dto;

import java.util.List;

/**
 * 文档导入接口响应。
 *
 * <p>importedCount 表示本次真正进入内存文档库的文档数量；
 * duplicateCount 表示因为 contentHash 重复而被跳过的文档数量。</p>
 */
public class RagIngestResponse {

    /**
     * 本次成功导入的文档数量。
     */
    private int importedCount;

    /**
     * 因 contentHash 重复而被跳过的文档数量。
     */
    private int duplicateCount;

    /**
     * 本次导入后的文档元数据列表。
     */
    private List<RagDocumentMetadata> documents;

    public RagIngestResponse() {
    }

    public RagIngestResponse(int importedCount, int duplicateCount, List<RagDocumentMetadata> documents) {
        this.importedCount = importedCount;
        this.duplicateCount = duplicateCount;
        this.documents = documents;
    }

    public int getImportedCount() {
        return importedCount;
    }

    public void setImportedCount(int importedCount) {
        this.importedCount = importedCount;
    }

    public int getDuplicateCount() {
        return duplicateCount;
    }

    public void setDuplicateCount(int duplicateCount) {
        this.duplicateCount = duplicateCount;
    }

    public List<RagDocumentMetadata> getDocuments() {
        return documents;
    }

    public void setDocuments(List<RagDocumentMetadata> documents) {
        this.documents = documents;
    }
}
