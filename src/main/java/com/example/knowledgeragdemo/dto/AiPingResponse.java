package com.example.knowledgeragdemo.dto;

/**
 * /ai/ping 接口的响应体。
 *
 * <p>这个接口主要用于验证：
 * 后端是否能接收输入、是否能走到 AI service 层、是否能按预期返回结构化结果。</p>
 */
public record AiPingResponse(String provider, String input, String output) {
}
