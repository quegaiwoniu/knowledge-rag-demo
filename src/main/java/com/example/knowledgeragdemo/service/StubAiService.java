package com.example.knowledgeragdemo.service;

import com.example.knowledgeragdemo.config.AppAiProperties;
import com.example.knowledgeragdemo.dto.AiPingResponse;
import com.example.knowledgeragdemo.dto.ClassificationCategory;
import com.example.knowledgeragdemo.dto.ClassifyResponse;
import com.example.knowledgeragdemo.dto.ExtractResponse;
import com.example.knowledgeragdemo.dto.ExtractionPriority;
import com.example.knowledgeragdemo.dto.SummaryResponse;
import com.example.knowledgeragdemo.dto.ToolCallResponse;
import com.example.knowledgeragdemo.dto.WeatherToolResult;

import java.util.List;

public class StubAiService implements AiService {

    private static final int SUMMARY_PREVIEW_LENGTH = 120;
    private static final List<String> SUPPORTED_LOCATIONS = List.of("北京", "上海", "广州", "深圳", "杭州", "成都", "南京", "苏州");

    private final AppAiProperties properties;
    private final WeatherToolService weatherToolService;

    public StubAiService(AppAiProperties properties, WeatherToolService weatherToolService) {
        this.properties = properties;
        this.weatherToolService = weatherToolService;
    }

    @Override
    public AiPingResponse ping(String message) {
        String input = (message == null || message.isBlank()) ? properties.defaultMessage() : message;
        return new AiPingResponse(properties.provider(), input, "stub-response: " + input);
    }

    @Override
    public SummaryResponse summarize(String text) {
        String normalized = text.trim().replaceAll("\\s+", " ");
        boolean truncated = normalized.length() > SUMMARY_PREVIEW_LENGTH;
        String summary = truncated ? normalized.substring(0, SUMMARY_PREVIEW_LENGTH) + "..." : normalized;
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
        ExtractionPriority priority = containsAny(normalized, "优先", "紧急", "立即", "失败", "异常")
                ? ExtractionPriority.HIGH : ExtractionPriority.MEDIUM;

        List<String> keywords = containsAny(normalized, "支付", "订单")
                ? List.of("支付接口", "订单提交", "超时日志")
                : List.of("结构化抽取", "接口设计", "学习项目");
        String title = category == ClassificationCategory.BUG ? "订单提交失败排查" : "结构化信息提取";

        return new ExtractResponse(title, category, priority, keywords);
    }

    @Override
    public ToolCallResponse toolCall(String question) {
        String normalized = question == null ? "" : question.trim();
        if (containsAny(normalized, "天气", "气温", "下雨", "温度")) {
            weatherToolService.clearLastToolResult();
            String location = extractLocation(normalized);
            WeatherToolResult result = weatherToolService.getWeather(location);
            return new ToolCallResponse(
                    "%s当前%s，气温%d℃，湿度%d%%，%s。".formatted(
                            result.location(),
                            result.condition(),
                            result.temperatureCelsius(),
                            result.humidityPercent(),
                            result.windDirection()
                    ),
                    true,
                    "getWeather",
                    "mock-weather",
                    result
            );
        }

        return new ToolCallResponse(
                "当前这个接口主要演示天气工具调用。你可以直接问我“北京今天天气怎么样”这类问题，我会先调用工具再给出结论。",
                false,
                null,
                null,
                null
        );
    }

    private ClassificationCategory resolveCategory(String normalized) {
        if (containsAny(normalized, "报错", "异常", "失败", "bug", "error", "无法", "闪退")) {
            return ClassificationCategory.BUG;
        }
        if (containsAny(normalized, "建议", "希望", "新增", "增加", "支持", "优化")) {
            return ClassificationCategory.FEATURE;
        }
        if (containsAny(normalized, "投诉", "不满", "太差", "太慢", "生气", "糟糕", "抱怨")) {
            return ClassificationCategory.COMPLAINT;
        }
        return ClassificationCategory.QUESTION;
    }

    private String extractLocation(String question) {
        for (String location : SUPPORTED_LOCATIONS) {
            if (question.contains(location)) {
                return location;
            }
        }
        return "北京";
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
