package com.example.knowledgeragdemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * RAG 模块配置（绑定 application.yml 里 {@code app.rag.*} 前缀的所有配置）。
 *
 * <h3>学习要点：Spring Boot 配置绑定</h3>
 * <ul>
 *   <li><b>@ConfigurationProperties</b>：把 yml 里的配置自动绑定到 Java 对象，
 *       比到处写 {@code @Value} 更整洁，且支持类型校验和 IDE 提示；</li>
 *   <li><b>record + @DefaultValue</b>：Java record 天然不可变，适合做配置载体；
 *       @DefaultValue 表示"配置缺失时用什么默认值"，保证应用总能启动；</li>
 *   <li>这个类的实例由 Spring 自动创建（配合主类上的 {@code @ConfigurationPropertiesScan}），
 *       Service 层注入它即可读取配置。</li>
 * </ul>
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
         * <p>Day 10 先使用字符数做简单切片，后续接入 tokenizer 后可以替换成 token 数。
         * 切片太大：检索粒度粗、浪费 token；切片太小：片段失去上下文、召回质量差。</p>
         */
        @DefaultValue("500") int chunkSize,

        /**
         * 相邻 chunk 之间保留的重叠字符数。
         *
         * <p>overlap 可以减少切片边界丢失上下文的问题（比如一句话被从中间切断），
         * 但必须小于 chunkSize。</p>
         */
        @DefaultValue("80") int chunkOverlap,

        /**
         * 检索结果的最低相似度阈值。
         *
         * <p>低于该阈值的 chunk 会被过滤掉，避免把与问题无关的片段送入 prompt。
         * 相似度范围约 [0, 1]，1 表示完全相关。
         * 阈值太高：容易误杀相关内容导致拒答；阈值太低：无关内容混进 prompt 引发幻觉。
         * 需要根据实际 embedding 效果调优。</p>
         */
        @DefaultValue("0.5") double minScoreThreshold) {
}
