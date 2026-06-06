package com.example.knowledgeragdemo.service;

import com.example.knowledgeragdemo.config.AppAiProperties;
import com.example.knowledgeragdemo.dto.AiPingResponse;
import com.example.knowledgeragdemo.dto.ClassificationCategory;
import com.example.knowledgeragdemo.dto.ClassifyResponse;
import com.example.knowledgeragdemo.dto.SummaryResponse;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Locale;

/**
 * 基于 Spring AI ChatClient 的真实模型实现。
 *
 * <p>这个类负责把应用层需求翻译成提示词，再调用你已经配置好的模型供应商。
 * controller 不需要关心供应商地址、密钥和模型名，只关心结构化返回结果。</p>
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
                        你只能从以下四个分类中选择一个：
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

    /**
     * 把模型返回的文本映射成固定枚举。
     *
     * <p>即使模型多输出了空格或大小写变化，这里也会做一次归一化，
     * 避免把不稳定文本直接泄露到接口层。</p>
     */
    private ClassificationCategory parseCategory(String rawCategory, String originalText) {
        String normalized = rawCategory == null ? "" : rawCategory.trim().toLowerCase(Locale.ROOT);
        ClassificationCategory category = switch (normalized) {
            case "bug" -> ClassificationCategory.BUG;
            case "feature" -> ClassificationCategory.FEATURE;
            case "question" -> ClassificationCategory.QUESTION;
            case "complaint" -> ClassificationCategory.COMPLAINT;
            default -> throw new IllegalStateException("unsupported classification result: " + rawCategory);
        };
        return applyCategoryOverride(category, originalText);
    }

    /**
     * 针对容易混淆的场景做一次小范围兜底修正。
     *
     * <p>当前最常见的误判是：用户在抱怨报错现象时，模型更容易把文本归到 complaint。
     * 但对业务分流来说，这类文本通常更适合进入 bug 处理链路，所以这里优先纠正。</p>
     */
    private ClassificationCategory applyCategoryOverride(ClassificationCategory category, String originalText) {
        String normalizedText = originalText == null ? "" : originalText.toLowerCase(Locale.ROOT);
        if (category == ClassificationCategory.COMPLAINT
                && containsAny(normalizedText,
                "\u62a5\u9519",
                "\u5f02\u5e38",
                "\u5931\u8d25",
                "\u65e0\u6cd5",
                "\u9519\u8bef",
                "\u7a7a\u6307\u9488",
                "nullpointer",
                "error",
                "bug")) {
            return ClassificationCategory.BUG;
        }
        return category;
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
