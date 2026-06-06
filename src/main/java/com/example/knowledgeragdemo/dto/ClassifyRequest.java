package com.example.knowledgeragdemo.dto;

/**
 * /ai/classify 接口的请求体。
 *
 * <p>当前只接收一段待分类文本，后续如果要补充业务来源、渠道或语言等上下文，
 * 可以继续在这里扩展字段。</p>
 */
public class ClassifyRequest {

    private String text;

    /**
     * 返回待分类文本。
     */
    public String getText() {
        return text;
    }

    /**
     * 设置待分类文本。
     */
    public void setText(String text) {
        this.text = text;
    }
}
