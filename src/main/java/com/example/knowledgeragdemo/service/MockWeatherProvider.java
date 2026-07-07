package com.example.knowledgeragdemo.service;

import com.example.knowledgeragdemo.dto.WeatherToolResult;

import java.util.Map;

/**
 * 本地演示用的天气 provider。
 *
 * 这个实现不依赖外网，适合在课堂练手、接口联调早期、
 * 或者网络条件不稳定时使用。
 */
public class MockWeatherProvider implements WeatherProvider {

    /**
     * 预置几组常见城市天气数据，方便我们稳定演示 Tool Calling。
     */
    private static final Map<String, WeatherToolResult> MOCK_WEATHER = Map.of(
            "北京", new WeatherToolResult("北京", "晴", 30, 35, "西北风"),
            "上海", new WeatherToolResult("上海", "多云", 29, 71, "东南风"),
            "广州", new WeatherToolResult("广州", "阵雨", 31, 82, "南风"),
            "深圳", new WeatherToolResult("深圳", "雷阵雨", 30, 85, "西南风"),
            "杭州", new WeatherToolResult("杭州", "阴", 28, 64, "东北风")
    );

    @Override
    public WeatherToolResult getWeather(String location) {
        // 如果城市不在预置列表中，就返回一组可读的默认天气，
        // 避免前端因为空结果而影响演示。
        return MOCK_WEATHER.getOrDefault(location, new WeatherToolResult(location, "阴", 26, 60, "北风"));
    }

    @Override
    public String source() {
        return "mock-weather";
    }
}
