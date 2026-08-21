package com.example.knowledgeragdemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * 应用层自定义 AI 配置（绑定 application.yml 里 {@code app.ai.*} 前缀）。
 *
 * <p>设计意图：业务代码只依赖这个"应用自己的配置对象"，
 * 不直接接触某个模型供应商 SDK 的参数。这样以后切换供应商时，
 * 只需改配置和 AiConfig 装配，Controller/Service 层完全不用动——依赖倒置思想的体现。</p>
 */
@ConfigurationProperties(prefix = "app.ai")
public record AppAiProperties(
        /**
         * 当前模型供应商标识（如 longcat-openai-compatible）。
         * 主要用于响应里回显，方便联调确认走的是哪条链路。
         */
        String provider,

        /** ping 接口在未传 message 时的默认问候语。 */
        String defaultMessage,

        /** 文本总结接口允许的最大输入长度（字符数），超长会被拒绝。 */
        int summaryMaxInputLength,

        /**
         * 是否使用 Stub（桩）服务。
         *
         * <p>true → 使用 {@code StubAiService}（不调真实模型，测试用）；
         * false → 使用 {@code ProviderAiService}（调真实模型，运行用）。
         * 这是"测试隔离"的关键开关：自动化测试不依赖外部网络和模型输出稳定性。</p>
         */
        boolean useStubService,

        /**
         * Prompt 调试模式。
         *
         * <p>开启后，RAG 问答接口会额外返回最终发给模型的完整 prompt 文本，
         * 方便排查 prompt 质量问题（比如上下文太长、指令不清）。
         * 生产环境建议保持关闭，避免把内部 prompt 暴露给客户端。</p>
         */
        @DefaultValue("false") boolean promptDebug
) {
}
