package com.example.knowledgeragdemo.service;

import com.example.knowledgeragdemo.config.AppAiProperties;
import com.example.knowledgeragdemo.dto.AiPingResponse;
import com.example.knowledgeragdemo.dto.ClassificationCategory;
import com.example.knowledgeragdemo.dto.ClassifyResponse;
import com.example.knowledgeragdemo.dto.ExtractResponse;
import com.example.knowledgeragdemo.dto.ExtractionPriority;
import com.example.knowledgeragdemo.dto.SummaryResponse;

import java.util.List;

/**
 * 测试和本地兜底使用的 stub AI 实现。
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

    @Override
    public ExtractResponse extract(String text) {
        String normalized = text == null ? "" : text.trim().replaceAll("\\s+", " ").toLowerCase();
        ClassificationCategory category = resolveCategory(normalized);
        ExtractionPriority priority = containsAny(normalized,
                "优先",
                "紧急",
                "立即",
                "失败",
                "异常") ? ExtractionPriority.HIGH : ExtractionPriority.MEDIUM;

        List<String> keywords = containsAny(normalized, "支付", "订单")
                ? List.of("支付接口", "订单提交", "超时日志")
                : List.of("结构化抽取", "接口设计", "学习项目");
        String title = category == ClassificationCategory.BUG ? "订单提交失败排查" : "结构化信息提取";

        return new ExtractResponse(
                title,
                category,
                priority,
                keywords
        );
    }

    private ClassificationCategory resolveCategory(String normalized) {
        if (containsAny(normalized,
                "报错",
                "异常",
                "失败",
                "bug",
                "error",
                "无法",
                "闪退")) {
            return ClassificationCategory.BUG;
        }
        if (containsAny(normalized,
                "建议",
                "希望",
                "新增",
                "增加",
                "支持",
                "优化")) {
            return ClassificationCategory.FEATURE;
        }
        if (containsAny(normalized,
                "投诉",
                "不满",
                "太差",
                "太慢",
                "生气",
                "糟糕",
                "抱怨")) {
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
