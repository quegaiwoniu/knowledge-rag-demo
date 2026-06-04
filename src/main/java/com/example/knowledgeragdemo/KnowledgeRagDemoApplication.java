package com.example.knowledgeragdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
/**
 * Spring Boot 应用启动入口。
 *
 * <p>这个项目当前刻意保持为一个小型骨架，先提供最小可运行后端，
 * 方便我们从简单的 AI 调试接口逐步演进到真正的 RAG 服务。</p>
 */
public class KnowledgeRagDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnowledgeRagDemoApplication.class, args);
    }
}
