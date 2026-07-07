package com.example.knowledgeragdemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.weather")
public record AppWeatherProperties(
        @DefaultValue("true") boolean useMockProvider,
        @DefaultValue("https://geocoding-api.open-meteo.com/v1/search") String geocodingBaseUrl,
        @DefaultValue("https://api.open-meteo.com/v1/forecast") String forecastBaseUrl
) {
}
