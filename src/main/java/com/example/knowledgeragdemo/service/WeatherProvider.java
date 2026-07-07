package com.example.knowledgeragdemo.service;

import com.example.knowledgeragdemo.dto.WeatherToolResult;

/**
 * 天气数据提供者抽象。
 *
 * 这里把“天气数据从哪里来”独立成一层：
 * 1. 课堂演示时可以用 mock 数据快速跑通；
 * 2. 需要联调真实接口时可以无感切换到真实 provider。
 */
public interface WeatherProvider {

    /**
     * 根据城市名查询天气。
     */
    WeatherToolResult getWeather(String location);

    /**
     * 返回当前 provider 的来源标识。
     * 这个值会透传给前端，方便页面上直接展示“mock / real”模式。
     */
    String source();
}
