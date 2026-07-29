package com.example.knowledgeragdemo.config;

import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 测试环境 ChatModel 桩配置。
 */
@Configuration
public class TestChatModelConfig {

    @Bean
    @Primary
    public ChatModel chatModel() {
        return Mockito.mock(ChatModel.class);
    }

    @Bean
    @Primary
    public ChatClient.Builder chatClientBuilder() {
        ChatClient chatClient = Mockito.mock(ChatClient.class);
        ChatClient.Builder builder = Mockito.mock(ChatClient.Builder.class);
        Mockito.when(builder.build()).thenReturn(chatClient);
        return builder;
    }
}
