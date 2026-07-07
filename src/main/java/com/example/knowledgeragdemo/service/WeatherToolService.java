package com.example.knowledgeragdemo.service;

import com.example.knowledgeragdemo.dto.ToolCallResponse;
import com.example.knowledgeragdemo.dto.WeatherToolResult;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

/**
 * 暴露给 Spring AI 的天气工具对象。
 *
 * 这个类的职责很单一：
 * 1. 对外暴露 getWeather 这个可被模型调用的工具；
 * 2. 临时保存本轮工具调用结果；
 * 3. 在模型回答完成后，把工具结果组装成接口响应。
 */
@Service
public class WeatherToolService {

    /**
     * 当前项目里一次请求只关心“最后一次工具结果”，
     * 所以这里用 ThreadLocal 暂存本轮对话的天气结果。
     */
    private static final ThreadLocal<WeatherToolResult> LAST_TOOL_RESULT = new ThreadLocal<>();

    private final WeatherProvider weatherProvider;

    public WeatherToolService(WeatherProvider weatherProvider) {
        this.weatherProvider = weatherProvider;
    }

    /**
     * Spring AI 真正注册给模型的工具方法。
     */
    @Tool(name = "getWeather", description = "根据城市名查询当前天气，包括天气现象、温度、湿度和风向")
    public WeatherToolResult getWeather(@ToolParam(description = "要查询天气的城市名，例如北京、上海、广州") String location) {
        WeatherToolResult result = weatherProvider.getWeather(location);
        LAST_TOOL_RESULT.set(result);
        return result;
    }

    public WeatherToolResult getLastToolResult() {
        return LAST_TOOL_RESULT.get();
    }

    public void clearLastToolResult() {
        LAST_TOOL_RESULT.remove();
    }

    /**
     * 把“模型自然语言回答”和“工具结构化结果”拼成统一返回对象。
     */
    public ToolCallResponse buildToolResponse(String answer) {
        WeatherToolResult result = getLastToolResult();
        return new ToolCallResponse(
                answer,
                result != null,
                result != null ? "getWeather" : null,
                result != null ? weatherProvider.source() : null,
                result
        );
    }
}
