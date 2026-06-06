package com.example.knowledgeragdemo.service;

import com.example.knowledgeragdemo.config.AppAiProperties;
import com.example.knowledgeragdemo.dto.AiPingResponse;
import com.example.knowledgeragdemo.dto.ClassificationCategory;
import com.example.knowledgeragdemo.dto.ClassifyResponse;
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

    @Override
    public ClassifyResponse classify(String text) {
        String normalized = text == null ? "" : text.trim().toLowerCase();
        ClassificationCategory category = resolveCategory(normalized);
        return new ClassifyResponse(category, category.getDescription());
    }

    /**
     * 使用固定关键字规则做分类，保证测试输出稳定。
     */
    private ClassificationCategory resolveCategory(String normalized) {
        if (containsAny(normalized,
                "\u62a5\u9519",
                "\u5f02\u5e38",
                "\u5931\u8d25",
                "bug",
                "error",
                "\u65e0\u6cd5",
                "\u95ea\u9000")) {
            return ClassificationCategory.BUG;
        }
        if (containsAny(normalized,
                "\u5efa\u8bae",
                "\u5e0c\u671b",
                "\u65b0\u589e",
                "\u589e\u52a0",
                "\u652f\u6301",
                "\u4f18\u5316")) {
            return ClassificationCategory.FEATURE;
        }
        if (containsAny(normalized,
                "\u6295\u8bc9",
                "\u4e0d\u6ee1",
                "\u592a\u5dee",
                "\u592a\u6162",
                "\u751f\u6c14",
                "\u7cdf\u7cd5",
                "\u62b1\u6028")) {
            return ClassificationCategory.COMPLAINT;
        }
        return ClassificationCategory.QUESTION;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
