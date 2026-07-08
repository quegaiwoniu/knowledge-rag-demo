package com.example.knowledgeragdemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * RAG 模块配置。
 *
 * <p>Day 9 先只关心样例 Markdown 文档所在目录，后续做 chunking、embedding、
 * vector search 时，可以继续在这个配置类里扩展相关参数。</p>
 */
@ConfigurationProperties(prefix = "app.rag")
public record AppRagProperties(
        /**
         * 样例知识库文档目录。
         *
         * <p>默认使用项目根目录下的 docs/sample-docs，运行时会从这里读取 Markdown 文件。</p>
         */
        @DefaultValue("docs/sample-docs") String sampleDocsPath) {
}
