package com.example.knowledgeragdemo.config;

import com.example.knowledgeragdemo.service.AiService;
import com.example.knowledgeragdemo.service.ProviderAiService;
import com.example.knowledgeragdemo.service.StubAiService;
import com.example.knowledgeragdemo.service.WeatherToolService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    @ConditionalOnProperty(prefix = "app.ai", name = "use-stub-service", havingValue = "true")
    public AiService stubAiService(AppAiProperties properties, WeatherToolService weatherToolService) {
        return new StubAiService(properties, weatherToolService);
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.ai", name = "use-stub-service", havingValue = "false", matchIfMissing = true)
    public AiService providerAiService(ChatClient.Builder chatClientBuilder, AppAiProperties properties,
            WeatherToolService weatherToolService) {
        return new ProviderAiService(chatClientBuilder.build(), properties, weatherToolService);
    }
}
