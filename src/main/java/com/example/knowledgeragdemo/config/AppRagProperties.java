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
        @DefaultValue("docs/sample-docs") String sampleDocsPath,

        /**
         * 每个 chunk 的目标字符数。
         *
         * <p>Day 10 先使用字符数做简单切片，后续接入 tokenizer 后可以替换成 token 数。</p>
         */
        @DefaultValue("500") int chunkSize,

        /**
         * 相邻 chunk 之间保留的重叠字符数。
         *
         * <p>overlap 可以减少切片边界丢失上下文的问题，但必须小于 chunkSize。</p>
         */
        @DefaultValue("80") int chunkOverlap) {
}
