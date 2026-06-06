package com.example.knowledgeragdemo.service;

import com.example.knowledgeragdemo.config.AppAiProperties;
import com.example.knowledgeragdemo.dto.ClassifyResponse;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProviderAiServiceTest {

    @Test
    void classifyPrefersBugWhenModelMisclassifiesComplaintForErrorText() {
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec responseSpec = mock(ChatClient.CallResponseSpec.class);
        AppAiProperties properties = new AppAiProperties(
                "zetatechs-openai-compatible",
                "Hello from test",
                1200,
                false
        );

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(org.mockito.ArgumentMatchers.anyString())).thenReturn(requestSpec);
        when(requestSpec.user(org.mockito.ArgumentMatchers.anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn("complaint");

        ProviderAiService providerAiService = new ProviderAiService(chatClient, properties);

        ClassifyResponse response = providerAiService.classify("系统升级后，提交订单一直报空指针异常，页面也无法保存。");

        assertEquals("BUG", response.getCategory().name());
    }
}
