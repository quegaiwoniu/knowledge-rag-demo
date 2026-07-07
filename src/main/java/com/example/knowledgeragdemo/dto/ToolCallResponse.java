package com.example.knowledgeragdemo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Tool Calling 接口的统一响应体。
 *
 * toolSource 是这次额外补上的字段，用来说明天气数据到底来自：
 * - mock-weather
 * - open-meteo
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ToolCallResponse(
        String answer,
        boolean toolCalled,
        String toolName,
        String toolSource,
        WeatherToolResult toolResult
) {
}
