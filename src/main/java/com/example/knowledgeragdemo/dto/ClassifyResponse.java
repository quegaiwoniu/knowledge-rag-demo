package com.example.knowledgeragdemo.dto;

/**
 * /ai/classify 接口的结构化返回体。
 *
 * <p>这里只返回固定字段：
 * 1. category：枚举分类结果
 * 2. description：分类结果的固定说明文案
 *
 * <p>不直接返回模型原始描述，可以减少输出风格漂移，让前端和调用方更容易稳定消费。</p>
 */
public class ClassifyResponse {

    private final ClassificationCategory category;
    private final String description;

    public ClassifyResponse(ClassificationCategory category, String description) {
        this.category = category;
        this.description = description;
    }

    /**
     * 返回固定枚举分类值。
     */
    public ClassificationCategory getCategory() {
        return category;
    }

    /**
     * 返回与分类对应的固定说明文案。
     */
    public String getDescription() {
        return description;
    }
}
