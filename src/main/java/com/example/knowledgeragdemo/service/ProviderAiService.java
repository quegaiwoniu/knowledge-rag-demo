package com.example.knowledgeragdemo.service;

import com.example.knowledgeragdemo.config.AppAiProperties;
import com.example.knowledgeragdemo.dto.AiPingResponse;
import com.example.knowledgeragdemo.dto.SummaryResponse;
import org.springframework.ai.chat.client.ChatClient;

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
}
