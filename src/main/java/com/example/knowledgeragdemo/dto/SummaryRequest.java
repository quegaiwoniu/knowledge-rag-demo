package com.example.knowledgeragdemo.dto;

/**
 * /ai/summary 接口的请求体。
 *
 * <p>前端或其他调用方通过这个对象把原始文本传给后端，
 * 后端再基于这段文本生成摘要结果。</p>
 */
public class SummaryRequest {

    private String text;

    /**
     * 需要被总结的原始文本。
     */
    public String getText() {
        return text;
    }

    /**
     * 设置需要被总结的原始文本。
     */
    public void setText(String text) {
        this.text = text;
    }
}
