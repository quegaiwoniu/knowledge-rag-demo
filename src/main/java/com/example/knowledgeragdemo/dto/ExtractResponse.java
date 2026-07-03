package com.example.knowledgeragdemo.dto;

import java.util.List;

/**
 * /ai/extract 接口对外暴露的稳定结构化结果。
 */
public class ExtractResponse {

    private final String title;
    private final ClassificationCategory category;
    private final ExtractionPriority priority;
    private final List<String> keywords;

    public ExtractResponse(String title,
                           ClassificationCategory category,
                           ExtractionPriority priority,
                           List<String> keywords) {
        this.title = title;
        this.category = category;
        this.priority = priority;
        this.keywords = keywords;
    }

    public String getTitle() {
        return title;
    }

    public ClassificationCategory getCategory() {
        return category;
    }

    public ExtractionPriority getPriority() {
        return priority;
    }

    public List<String> getKeywords() {
        return keywords;
    }
}
