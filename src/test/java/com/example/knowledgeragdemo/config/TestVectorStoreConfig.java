package com.example.knowledgeragdemo.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.ai.vectorstore.VectorStore;

/**
 * 测试环境 VectorStore 桩配置。
 *
 * <p>测试环境没有 PostgreSQL + pgvector，这里提供一个 mock VectorStore，
 * 让 Spring 上下文能正常加载，同时避免测试依赖外部数据库。</p>
 */
@Configuration
public class TestVectorStoreConfig {

    @Bean
    @Primary
    public VectorStore vectorStore() {
        return Mockito.mock(VectorStore.class);
    }
}
