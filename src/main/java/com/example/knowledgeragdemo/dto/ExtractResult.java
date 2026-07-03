package com.example.knowledgeragdemo.dto;

import java.util.List;

/**
 * 仅供 service 内部接收模型候选结构的对象。
 */
public class ExtractResult {

    private String title;
    private String category;
    private String priority;
    private List<String> keywords;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
}
