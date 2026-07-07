package com.example.knowledgeragdemo.dto;

public record WeatherToolResult(
        String location,
        String condition,
        int temperatureCelsius,
        int humidityPercent,
        String windDirection
) {
}
