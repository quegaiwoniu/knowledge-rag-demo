package com.example.knowledgeragdemo.dto;

/**
 * 单条检索命中结果。
 */
public class RagSearchResult {

    /** 文档 ID */
    private String docId;

    /** 文件名 */
    private String fileName;

    /** 文档标题 */
    private String title;

    /** 章节标题 */
    private String sectionTitle;

    /** chunk 顺序号 */
    private int chunkIndex;

    /** chunk 原文片段 */
    private String content;

    /** 相似度分数（越高越相似） */
    private double score;

    public RagSearchResult() {
    }

    public RagSearchResult(String docId, String fileName, String title,
                           String sectionTitle, int chunkIndex, String content, double score) {
        this.docId = docId;
        this.fileName = fileName;
        this.title = title;
        this.sectionTitle = sectionTitle;
        this.chunkIndex = chunkIndex;
        this.content = content;
        this.score = score;
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

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}