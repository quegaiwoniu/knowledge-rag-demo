package com.example.knowledgeragdemo.service;

import com.example.knowledgeragdemo.config.AppAiProperties;
import com.example.knowledgeragdemo.dto.ClassifyResponse;
import com.example.knowledgeragdemo.dto.ExtractResponse;
import com.example.knowledgeragdemo.dto.ExtractResult;
import com.example.knowledgeragdemo.dto.ToolCallResponse;
import com.example.knowledgeragdemo.dto.WeatherToolResult;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

        WeatherToolService weatherToolService = createWeatherToolService();

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn("complaint");

        ProviderAiService providerAiService = new ProviderAiService(chatClient, properties, weatherToolService);

        ClassifyResponse response = providerAiService.classify("系统升级后，提交订单一直报空指针异常，页面也无法保存。");

        assertEquals("BUG", response.getCategory().name());
    }

    @Test
    void extractNormalizesPriorityAndDeduplicatesKeywords() {
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec responseSpec = mock(ChatClient.CallResponseSpec.class);
        AppAiProperties properties = new AppAiProperties("provider", "hello", 1200, false);

        WeatherToolService weatherToolService = createWeatherToolService();

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.entity(ExtractResult.class)).thenReturn(buildResult(
                "  订单提交失败排查。 ",
                "bug",
                "high",
                List.of("订单提交", "超时日志", "订单提交", "支付接口")
        ));

        ProviderAiService providerAiService = new ProviderAiService(chatClient, properties, weatherToolService);

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

        WeatherToolService weatherToolService = createWeatherToolService();

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

        ProviderAiService providerAiService = new ProviderAiService(chatClient, properties, weatherToolService);

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

        WeatherToolService weatherToolService = createWeatherToolService();

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

        ProviderAiService providerAiService = new ProviderAiService(chatClient, properties, weatherToolService);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> providerAiService.extract("支付接口上线后，订单提交失败并出现超时日志"));

        assertEquals("invalid extract keywords: too few valid keywords", exception.getMessage());
    }

    @Test
    void toolCallBuildsStructuredResponseAfterWeatherToolInvocation() {
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec responseSpec = mock(ChatClient.CallResponseSpec.class);
        AppAiProperties properties = new AppAiProperties("provider", "hello", 1200, false);

        WeatherToolService weatherToolService = createWeatherToolService();

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenAnswer(invocation -> {
            String question = invocation.getArgument(0, String.class);
            if (question.contains("上海")) {
                weatherToolService.getWeather("上海");
            }
            return requestSpec;
        });
        when(requestSpec.tools(any(Object[].class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn("上海当前多云，气温28℃，出门可以带把伞。");

        ProviderAiService providerAiService = new ProviderAiService(chatClient, properties, weatherToolService);

        ToolCallResponse response = providerAiService.toolCall("上海今天天气怎么样");

        assertTrue(response.toolCalled());
        assertEquals("getWeather", response.toolName());
        assertEquals("mock-weather", response.toolSource());
        assertNotNull(response.toolResult());
        assertEquals("上海", response.toolResult().location());
        assertEquals("多云", response.toolResult().condition());
        assertTrue(response.answer().contains("上海"));
    }

    @Test
    void toolCallReturnsDirectAnswerWhenNoToolWasUsed() {
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec responseSpec = mock(ChatClient.CallResponseSpec.class);
        AppAiProperties properties = new AppAiProperties("provider", "hello", 1200, false);

        WeatherToolService weatherToolService = createWeatherToolService();

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.tools(any(Object[].class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn("当前接口主要演示天气工具调用。");

        ProviderAiService providerAiService = new ProviderAiService(chatClient, properties, weatherToolService);

        ToolCallResponse response = providerAiService.toolCall("介绍一下这个项目");

        assertFalse(response.toolCalled());
        assertEquals(null, response.toolName());
        assertEquals(null, response.toolSource());
        assertEquals(null, response.toolResult());
        assertEquals("当前接口主要演示天气工具调用。", response.answer());
    }

    private WeatherToolService createWeatherToolService() {
        WeatherProvider provider = new WeatherProvider() {
            @Override
            public WeatherToolResult getWeather(String location) {
                return switch (location) {
                    case "上海" -> new WeatherToolResult("上海", "多云", 28, 66, "东北风");
                    case "北京" -> new WeatherToolResult("北京", "晴", 30, 40, "东南风");
                    default -> new WeatherToolResult(location, "晴", 30, 40, "东南风");
                };
            }

            @Override
            public String source() {
                return "mock-weather";
            }
        };
        return new WeatherToolService(provider);
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
