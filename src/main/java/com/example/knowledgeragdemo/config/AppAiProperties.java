package com.example.knowledgeragdemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 应用层自定义 AI 配置。
 *
 * <p>这里放的是当前 demo 自己关心的配置，而不是直接暴露某个供应商 SDK 的全部参数。
 * 这样后面切换模型供应商时，controller 和 service 层的业务代码可以保持稳定。</p>
 */
@ConfigurationProperties(prefix = "app.ai")
public record AppAiProperties(
        String provider,
        String defaultMessage,
        int summaryMaxInputLength,
        boolean useStubService
) {
}
