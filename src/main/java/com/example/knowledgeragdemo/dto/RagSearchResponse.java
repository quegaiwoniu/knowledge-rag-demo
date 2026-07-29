package com.example.knowledgeragdemo.dto;

import java.util.List;

/**
 * RAG 检索接口响应。
 */
public class RagSearchResponse {

    /** 用户原始问题 */
    private String query;

    /** 检索命中的 chunk 列表，按相似度从高到低排序 */
    private List<RagSearchResult> results;

    public RagSearchResponse() {
    }

    public RagSearchResponse(String query, List<RagSearchResult> results) {
        this.query = query;
        this.results = results;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<RagSearchResult> getResults() {
        return results;
    }

    public void setResults(List<RagSearchResult> results) {
        this.results = results;
    }
}