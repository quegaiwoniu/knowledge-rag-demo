package com.example.knowledgeragdemo.dto;

/**
 * RAG 文档切片结果。
 *
 * <p>chunk 保留文档级元数据和章节标题，是为了后续检索、答案引用和问题排查时能追溯到原文来源。</p>
 */
public class RagChunk {

    /**
     * 所属文档 ID，来自 Day 9 导入阶段生成的 RagDocumentMetadata.docId。
     *
     * <p>后续做检索命中和答案引用时，可以通过 docId 反查原始文档。</p>
     */
    private String docId;

    /**
     * 原始 Markdown 文件名，便于调试接口直接展示用户能看懂的来源。
     */
    private String fileName;

    /**
     * 原始 Markdown 文件路径。
     *
     * <p>Day 10 仍然是本地文件版 RAG，所以这里保留路径，方便重新读取原文和排查切片问题。</p>
     */
    private String sourcePath;

    /**
     * 文档标题，通常来自 Markdown 一级标题。
     */
    private String title;

    /**
     * chunk 所属章节标题，来自 Markdown 的二级到六级标题。
     *
     * <p>这个字段非常重要：企业知识库回答时，用户通常不只想知道“来自哪个文件”，
     * 还想知道“来自哪个章节”。</p>
     */
    private String sectionTitle;

    /**
     * 本次切片结果中的全局顺序号。
     *
     * <p>当前设计为一次 /rag/chunks 响应内全局递增，而不是每个文档从 0 开始。
     * 这样前端调试时可以直接按 chunkIndex 判断整体切片顺序。</p>
     */
    private int chunkIndex;

    /**
     * 当前 chunk 的正文内容。
     *
     * <p>Day 10 暂时不存 embedding，只返回原文片段，方便人工检查切片质量。</p>
     */
    private String content;

    public RagChunk() {
    }

    public RagChunk(String docId, String fileName, String sourcePath, String title,
                    String sectionTitle, int chunkIndex, String content) {
        this.docId = docId;
        this.fileName = fileName;
        this.sourcePath = sourcePath;
        this.title = title;
        this.sectionTitle = sectionTitle;
        this.chunkIndex = chunkIndex;
        this.content = content;
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

    public String getSectionTitle() {
        return sectionTitle;
    }

    public void setSectionTitle(String sectionTitle) {
        this.sectionTitle = sectionTitle;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(int chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
