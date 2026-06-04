package com.example.knowledgeragdemo.service;

import com.example.knowledgeragdemo.dto.AiPingResponse;
import com.example.knowledgeragdemo.dto.SummaryResponse;

/**
 * 应用内 AI 能力的 service 抽象。
 *
 * <p>controller 只依赖这个接口，不直接依赖具体模型供应商。
 * 这样我们可以在 stub 和真实模型实现之间切换，而不用修改 web 层。</p>
 */
public interface AiService {

    /**
     * 轻量联调用的 AI 调试方法。
     *
     * <p>主要用于快速验证接口、模型装配和前后端联调链路是否可用。</p>
     */
    AiPingResponse ping(String message);

    /**
     * 对输入文本生成摘要，并返回结构化结果。
     */
    SummaryResponse summarize(String text);
}
