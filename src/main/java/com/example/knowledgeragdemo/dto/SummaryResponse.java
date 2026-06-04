package com.example.knowledgeragdemo.dto;

/**
 * /ai/summary 接口的响应体。
 *
 * <p>字段说明：
 * summary：服务返回的摘要文本。
 * originalLength：原始输入文本长度，便于调试和前端展示。
 * truncated：当前实现是否对输出结果做了截断。</p>
 */
public class SummaryResponse {

    private final String summary;
    private final int originalLength;
    private final boolean truncated;

    public SummaryResponse(String summary, int originalLength, boolean truncated) {
        this.summary = summary;
        this.originalLength = originalLength;
        this.truncated = truncated;
    }

    /**
     * 返回摘要结果。
     */
    public String getSummary() {
        return summary;
    }

    /**
     * 返回原始输入文本的长度。
     */
    public int getOriginalLength() {
        return originalLength;
    }

    /**
     * 标记当前实现是否对输出结果做了缩短处理。
     */
    public boolean isTruncated() {
        return truncated;
    }
}
