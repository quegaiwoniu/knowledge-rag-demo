package com.example.knowledgeragdemo.service;

import com.example.knowledgeragdemo.config.AppAiProperties;
import com.example.knowledgeragdemo.dto.ClassifyResponse;
import com.example.knowledgeragdemo.dto.ExtractResponse;
import com.example.knowledgeragdemo.dto.ExtractResult;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
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
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn("complaint");

        ProviderAiService providerAiService = new ProviderAiService(chatClient, properties);

        ClassifyResponse response = providerAiService.classify("系统升级后，提交订单一直报空指针异常，页面也无法保存。");

        assertEquals("BUG", response.getCategory().name());
    }

    @Test
    void extractNormalizesPriorityAndDeduplicatesKeywords() {
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec responseSpec = mock(ChatClient.CallResponseSpec.class);
        AppAiProperties properties = new AppAiProperties("provider", "hello", 1200, false);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.entity(ExtractResult.class)).thenReturn(buildResult(
                "  订单提交失败排查。  ",
                "bug",
                "high",
                List.of("订单提交", "超时日志", "订单提交", "支付接口")
        ));

        ProviderAiService providerAiService = new ProviderAiService(chatClient, properties);

        ExtractResponse response = providerAiService.extract("支付接口上线后，订单提交失败并出现超时日志");

        assertEquals("订单提交失败排查", response.getTitle());
        assertEquals("BUG", response.getCategory().name());
        assertEquals("HIGH", response.getPriority().name());
        assertEquals(List.of("订单提交", "超时日志", "支付接口"), response.getKeywords());
    }

    @Test
    void extractThrowsWhenCategoryIsUnsupported() {
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec responseSpec = mock(ChatClient.CallResponseSpec.class);
        AppAiProperties properties = new AppAiProperties("provider", "hello", 1200, false);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.entity(ExtractResult.class)).thenReturn(buildResult(
                "订单提交失败排查",
                "incident",
                "high",
                List.of("订单提交", "超时日志", "支付接口")
        ));

        ProviderAiService providerAiService = new ProviderAiService(chatClient, properties);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> providerAiService.extract("支付接口上线后，订单提交失败并出现超时日志"));

        assertEquals("invalid extract category: incident", exception.getMessage());
    }

    @Test
    void extractThrowsWhenNormalizedKeywordsAreTooFew() {
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec responseSpec = mock(ChatClient.CallResponseSpec.class);
        AppAiProperties properties = new AppAiProperties("provider", "hello", 1200, false);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.entity(ExtractResult.class)).thenReturn(buildResult(
                "订单提交失败排查",
                "bug",
                "high",
                List.of("订单提交", "订单提交", " ")
        ));

        ProviderAiService providerAiService = new ProviderAiService(chatClient, properties);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> providerAiService.extract("支付接口上线后，订单提交失败并出现超时日志"));

        assertEquals("invalid extract keywords: too few valid keywords", exception.getMessage());
    }

    private ExtractResult buildResult(String title, String category, String priority, List<String> keywords) {
        ExtractResult result = new ExtractResult();
        result.setTitle(title);
        result.setCategory(category);
        result.setPriority(priority);
        result.setKeywords(keywords);
        return result;
    }
}
