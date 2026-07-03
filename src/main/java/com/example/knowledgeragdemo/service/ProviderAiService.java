package com.example.knowledgeragdemo.service;

import com.example.knowledgeragdemo.config.AppAiProperties;
import com.example.knowledgeragdemo.dto.AiPingResponse;
import com.example.knowledgeragdemo.dto.ClassificationCategory;
import com.example.knowledgeragdemo.dto.ClassifyResponse;
import com.example.knowledgeragdemo.dto.ExtractResponse;
import com.example.knowledgeragdemo.dto.ExtractResult;
import com.example.knowledgeragdemo.dto.ExtractionPriority;
import com.example.knowledgeragdemo.dto.SummaryResponse;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 基于 Spring AI ChatClient 的真实模型实现。
 */
public class ProviderAiService implements AiService {

    private final ChatClient chatClient;
    private final AppAiProperties properties;

    public ProviderAiService(ChatClient chatClient, AppAiProperties properties) {
        this.chatClient = chatClient;
        this.properties = properties;
    }

    @Override
    public AiPingResponse ping(String message) {
        String input = (message == null || message.isBlank()) ? properties.defaultMessage() : message;
        String output = chatClient.prompt()
                .system("""
                        你是一个后端联调助手。
                        请基于用户输入，返回一句简短、直接的响应，用于确认模型调用链路正常。
                        不要输出 markdown，不要解释过程。
                        """)
                .user(input)
                .call()
                .content();
        return new AiPingResponse(properties.provider(), input, output);
    }

    @Override
    public SummaryResponse summarize(String text) {
        String summary = chatClient.prompt()
                .system("""
                        你是一个企业应用里的文本总结助手。
                        请根据用户提供的文本生成简洁摘要：
                        1. 保留核心信息
                        2. 控制在 1 到 3 句话
                        3. 使用中文输出
                        4. 不要使用 markdown 列表
                        5. 不要捏造原文中不存在的信息
                        """)
                .user("""
                        请总结以下文本：

                        %s
                        """.formatted(text))
                .call()
                .content();

        return new SummaryResponse(summary, text.length(), false);
    }

    @Override
    public ClassifyResponse classify(String text) {
        String rawCategory = chatClient.prompt()
                .system("""
                        你是一个企业应用里的文本分类助手。
                        你只能够从以下四个分类中选择一个：
                        bug
                        feature
                        question
                        complaint

                        请严格遵守以下规则：
                        1. 只输出一个分类词
                        2. 不要输出解释
                        3. 不要输出标点
                        4. 不要输出 markdown
                        """)
                .user("""
                        请对以下文本分类：

                        %s
                        """.formatted(text))
                .call()
                .content();

        ClassificationCategory category = parseCategory(rawCategory, text);
        return new ClassifyResponse(category, category.getDescription());
    }

    @Override
    public ExtractResponse extract(String text) {
        ExtractResult result = chatClient.prompt()
                .system("""
                        你是企业应用中的文本结构化抽取助手。
                        你的任务是把用户输入提炼成固定结构，供后端系统直接消费。

                        请严格遵守以下规则：
                        1. title 是 1-20 字的简短中文标题
                        2. category 只能是 bug、feature、question、complaint 之一
                        3. priority 只能是 low、medium、high 之一
                        4. keywords 返回 3 到 5 个中文关键词
                        5. 不要输出解释，不要输出 markdown
                        6. 不要虚构原文中不存在的事实
                        7. 如果信息不足，也必须尽量给出最保守的结构化结果
                        """)
                .user("""
                        请从以下文本中提取结构化信息：

                        %s
                        """.formatted(text))
                .call()
                .entity(ExtractResult.class);

        String title = normalizeTitle(result == null ? null : result.getTitle());
        ClassificationCategory category = parseExtractCategory(result == null ? null : result.getCategory());
        ExtractionPriority priority = parsePriority(result == null ? null : result.getPriority());
        List<String> keywords = normalizeKeywords(result == null ? null : result.getKeywords());

        return new ExtractResponse(title, category, priority, keywords);
    }

    private ClassificationCategory parseCategory(String rawCategory, String originalText) {
        ClassificationCategory category = parseExtractCategory(rawCategory);
        return applyCategoryOverride(category, originalText);
    }

    private ClassificationCategory parseExtractCategory(String rawCategory) {
        String normalized = rawCategory == null ? "" : rawCategory.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "bug" -> ClassificationCategory.BUG;
            case "feature" -> ClassificationCategory.FEATURE;
            case "question" -> ClassificationCategory.QUESTION;
            case "complaint" -> ClassificationCategory.COMPLAINT;
            default -> throw new IllegalStateException("invalid extract category: " + rawCategory);
        };
    }

    private ClassificationCategory applyCategoryOverride(ClassificationCategory category, String originalText) {
        String normalizedText = originalText == null ? "" : originalText.toLowerCase(Locale.ROOT);
        if (category == ClassificationCategory.COMPLAINT
                && containsAny(normalizedText,
                "报错",
                "异常",
                "失败",
                "无法",
                "错误",
                "空指针",
                "nullpointer",
                "error",
                "bug")) {
            return ClassificationCategory.BUG;
        }
        return category;
    }

    private ExtractionPriority parsePriority(String rawPriority) {
        String normalized = rawPriority == null ? "" : rawPriority.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "low" -> ExtractionPriority.LOW;
            case "medium" -> ExtractionPriority.MEDIUM;
            case "high" -> ExtractionPriority.HIGH;
            default -> throw new IllegalStateException("invalid extract priority: " + rawPriority);
        };
    }

    private String normalizeTitle(String title) {
        String normalized = title == null ? "" : title.trim().replaceAll("\\s+", " ");
        normalized = normalized.replaceAll("[。！？,.!?，]+$", "");
        if (normalized.isBlank()) {
            throw new IllegalStateException("invalid extract title: blank after normalization");
        }
        if (normalized.length() > 20) {
            normalized = normalized.substring(0, 20);
        }
        return normalized;
    }

    private List<String> normalizeKeywords(List<String> rawKeywords) {
        if (rawKeywords == null || rawKeywords.isEmpty()) {
            throw new IllegalStateException("invalid extract keywords: empty result");
        }

        List<String> normalized = rawKeywords.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(keyword -> !keyword.isBlank())
                .map(keyword -> keyword.length() > 10 ? keyword.substring(0, 10) : keyword)
                .distinct()
                .limit(5)
                .toList();

        if (normalized.size() < 3) {
            throw new IllegalStateException("invalid extract keywords: too few valid keywords");
        }

        return normalized;
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
