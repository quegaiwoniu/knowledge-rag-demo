package com.example.knowledgeragdemo.config;

import com.example.knowledgeragdemo.service.MockWeatherProvider;
import com.example.knowledgeragdemo.service.OpenMeteoWeatherProvider;
import com.example.knowledgeragdemo.service.WeatherProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class WeatherConfig {

    @Bean
    @ConditionalOnProperty(prefix = "app.weather", name = "use-mock-provider", havingValue = "true", matchIfMissing = true)
    public WeatherProvider mockWeatherProvider() {
        return new MockWeatherProvider();
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.weather", name = "use-mock-provider", havingValue = "false")
    public WeatherProvider openMeteoWeatherProvider(AppWeatherProperties properties) {
        return new OpenMeteoWeatherProvider(RestClient.create(), properties);
    }
}
