package com.example.knowledgeragdemo.config;

import com.example.knowledgeragdemo.service.AiService;
import com.example.knowledgeragdemo.service.ProviderAiService;
import com.example.knowledgeragdemo.service.StubAiService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 相关 Spring Bean 的装配配置。
 *
 * <p>这里把真实模型实现和 stub 实现分开装配：
 * 运行环境默认接真实模型，测试环境通过配置切回 stub，
 * 这样既能让项目真正调用模型，也不会把现有单测搞坏。</p>
 */
@Configuration
public class AiConfig {

    /**
     * 当配置要求使用 stub 时，装配一个可预测、便于测试的本地实现。
     */
    @Bean
    @ConditionalOnProperty(prefix = "app.ai", name = "use-stub-service", havingValue = "true")
    public AiService stubAiService(AppAiProperties properties) {
        return new StubAiService(properties);
    }

    /**
     * 默认装配真实模型实现。
     *
     * <p>这里依赖 Spring AI 自动提供的 {@link ChatClient.Builder}。
     * 只要 application.yml 中已经配置好供应商地址、密钥和模型名，就可以直接调用。</p>
     */
    @Bean
    @ConditionalOnProperty(prefix = "app.ai", name = "use-stub-service", havingValue = "false", matchIfMissing = true)
    public AiService providerAiService(ChatClient.Builder chatClientBuilder, AppAiProperties properties) {
        return new ProviderAiService(chatClientBuilder.build(), properties);
    }
}
