package com.example.knowledgeragdemo.dto;

import java.util.List;

/**
 * RAG chunk 调试接口响应。
 */
public class RagChunksResponse {

    /**
     * 当前参与切片的文档数量。
     *
     * <p>空文档也会计入 documentCount，因为它确实被导入过，只是不会产生 chunk。</p>
     */
    private int documentCount;

    /**
     * 本次实际生成的 chunk 数量。
     */
    private int chunkCount;

    /**
     * 当前生效的 chunk size 配置。
     *
     * <p>调试接口返回该字段，可以帮助我们确认后端是否读取到了预期配置。</p>
     */
    private int chunkSize;

    /**
     * 当前生效的 overlap 配置。
     */
    private int chunkOverlap;

    /**
     * 按文档顺序、章节顺序、切片顺序排列的 chunk 列表。
     */
    private List<RagChunk> chunks;

    public RagChunksResponse() {
    }

    public RagChunksResponse(int documentCount, int chunkCount, int chunkSize, int chunkOverlap, List<RagChunk> chunks) {
        this.documentCount = documentCount;
        this.chunkCount = chunkCount;
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
        this.chunks = chunks;
    }

    public int getDocumentCount() {
        return documentCount;
    }

    public void setDocumentCount(int documentCount) {
        this.documentCount = documentCount;
    }

    public int getChunkCount() {
        return chunkCount;
    }

    public void setChunkCount(int chunkCount) {
        this.chunkCount = chunkCount;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public int getChunkOverlap() {
        return chunkOverlap;
    }

    public void setChunkOverlap(int chunkOverlap) {
        this.chunkOverlap = chunkOverlap;
    }

    public List<RagChunk> getChunks() {
        return chunks;
    }

    public void setChunks(List<RagChunk> chunks) {
        this.chunks = chunks;
    }
}
