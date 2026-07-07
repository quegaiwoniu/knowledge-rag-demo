package com.example.knowledgeragdemo.service;

import com.example.knowledgeragdemo.config.AppWeatherProperties;
import com.example.knowledgeragdemo.dto.WeatherToolResult;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * 基于 Open-Meteo 的真实天气 provider。
 *
 * 调用链路分两步：
 * 1. 先根据城市名做 geocoding，拿到经纬度；
 * 2. 再根据经纬度查询当前天气。
 */
public class OpenMeteoWeatherProvider implements WeatherProvider {

    private final RestClient restClient;
    private final AppWeatherProperties properties;

    public OpenMeteoWeatherProvider(RestClient restClient, AppWeatherProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    @SuppressWarnings("unchecked")
    public WeatherToolResult getWeather(String location) {
        // 第一步：城市名转经纬度。
        Map<String, Object> geocoding = restClient.get()
                .uri(properties.geocodingBaseUrl() + "?name={name}&count=1&language=zh&format=json", location)
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> results = geocoding == null ? List.of() : (List<Map<String, Object>>) geocoding.get("results");
        if (results == null || results.isEmpty()) {
            throw new IllegalStateException("weather location not found: " + location);
        }

        Map<String, Object> first = results.get(0);
        double latitude = ((Number) first.get("latitude")).doubleValue();
        double longitude = ((Number) first.get("longitude")).doubleValue();
        String resolvedName = String.valueOf(first.getOrDefault("name", location));

        // 第二步：用经纬度查询实时天气。
        Map<String, Object> forecast = restClient.get()
                .uri(properties.forecastBaseUrl()
                                + "?latitude={latitude}&longitude={longitude}&current=temperature_2m,relative_humidity_2m,weather_code,wind_direction_10m",
                        latitude, longitude)
                .retrieve()
                .body(Map.class);

        Map<String, Object> current = forecast == null ? Map.of() : (Map<String, Object>) forecast.get("current");
        int temperature = ((Number) current.getOrDefault("temperature_2m", 0)).intValue();
        int humidity = ((Number) current.getOrDefault("relative_humidity_2m", 0)).intValue();
        int weatherCode = ((Number) current.getOrDefault("weather_code", -1)).intValue();
        int windDirection = ((Number) current.getOrDefault("wind_direction_10m", 0)).intValue();

        return new WeatherToolResult(
                resolvedName,
                toCondition(weatherCode),
                temperature,
                humidity,
                toWindDirection(windDirection)
        );
    }

    @Override
    public String source() {
        return "open-meteo";
    }

    /**
     * 把 Open-Meteo 的 weather_code 映射成更适合演示的中文天气描述。
     */
    private String toCondition(int weatherCode) {
        return switch (weatherCode) {
            case 0 -> "晴";
            case 1, 2, 3 -> "多云";
            case 45, 48 -> "雾";
            case 51, 53, 55, 61, 63, 65, 80, 81, 82 -> "雨";
            case 71, 73, 75, 85, 86 -> "雪";
            case 95, 96, 99 -> "雷雨";
            default -> "阴";
        };
    }

    /**
     * 用一个简化版本把角度映射为东南西北风，足够支撑入门演示。
     */
    private String toWindDirection(int degrees) {
        if (degrees >= 45 && degrees < 135) {
            return "东风";
        }
        if (degrees >= 135 && degrees < 225) {
            return "南风";
        }
        if (degrees >= 225 && degrees < 315) {
            return "西风";
        }
        return "北风";
    }
}
