package com.example.knowledgeragdemo.dto;

/**
 * /rag/ask 接口的请求体。
 *
 * <p>包含用户提问和检索 topK 参数。</p>
 */
public class RagAskRequest {

    private String query;
    private int topK = 5;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getTopK() {
        return topK;
    }

    public void setTopK(int topK) {
        this.topK = topK;
    }
}
