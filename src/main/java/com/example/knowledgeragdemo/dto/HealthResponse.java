package com.example.knowledgeragdemo.dto;

/**
 * /health 接口的响应体。
 *
 * <p>用于告诉调用方：
 * 当前服务是否存活，以及是哪一个应用实例返回了响应。</p>
 */
public record HealthResponse(String status, String application) {
}
