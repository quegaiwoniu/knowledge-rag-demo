package com.example.knowledgeragdemo.dto;

/**
 * /ai/extract 接口的请求体。
 */
public class ExtractRequest {

    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
