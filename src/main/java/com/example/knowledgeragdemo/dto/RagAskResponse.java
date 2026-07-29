package com.example.knowledgeragdemo.dto;

import java.util.List;

/**
 * /rag/ask 接口的响应体。
 *
 * <p>包含模型生成的答案、是否有足够上下文、引用来源列表和原始检索结果。</p>
 */
public class RagAskResponse {

    /** 模型生成的答案 */
    private String answer;

    /** 是否有足够上下文来回答问题 */
    private boolean enoughContext;

    /** 引用来源列表，用于前端展示 */
    private List<RagSearchResult> citations;

    /** 原始检索结果，便于调试 */
    private List<RagSearchResult> retrievedChunks;

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public boolean isEnoughContext() {
        return enoughContext;
    }

    public void setEnoughContext(boolean enoughContext) {
        this.enoughContext = enoughContext;
    }

    public List<RagSearchResult> getCitations() {
        return citations;
    }

    public void setCitations(List<RagSearchResult> citations) {
        this.citations = citations;
    }

    public List<RagSearchResult> getRetrievedChunks() {
        return retrievedChunks;
    }

    public void setRetrievedChunks(List<RagSearchResult> retrievedChunks) {
        this.retrievedChunks = retrievedChunks;
    }
}
