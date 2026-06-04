package com.example.knowledgeragdemo.service;

import com.example.knowledgeragdemo.config.AppAiProperties;
import com.example.knowledgeragdemo.dto.AiPingResponse;
import com.example.knowledgeragdemo.dto.SummaryResponse;

/**
 * 测试和本地兜底使用的 stub AI 实现。
 *
 * <p>这个类保留的原因有三个：
 * 1. 单测不依赖外部模型供应商
 * 2. 前后端联调时可以得到稳定结果
 * 3. 真实模型调用出问题时，仍然有一个容易排查的对照实现</p>
 */
public class StubAiService implements AiService {

    private static final int SUMMARY_PREVIEW_LENGTH = 120;

    private final AppAiProperties properties;

    public StubAiService(AppAiProperties properties) {
        this.properties = properties;
    }

    @Override
    public AiPingResponse ping(String message) {
        String input = (message == null || message.isBlank()) ? properties.defaultMessage() : message;
        String output = "stub-response: " + input;
        return new AiPingResponse(properties.provider(), input, output);
    }

    @Override
    public SummaryResponse summarize(String text) {
        String normalized = text.trim().replaceAll("\\s+", " ");
        boolean truncated = normalized.length() > SUMMARY_PREVIEW_LENGTH;
        String summary = truncated
                ? normalized.substring(0, SUMMARY_PREVIEW_LENGTH) + "..."
                : normalized;
        return new SummaryResponse(summary, text.length(), truncated);
    }
}
