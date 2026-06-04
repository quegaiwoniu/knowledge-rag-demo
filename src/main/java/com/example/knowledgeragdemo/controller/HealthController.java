package com.example.knowledgeragdemo.controller;

import com.example.knowledgeragdemo.dto.ApiResponse;
import com.example.knowledgeragdemo.dto.HealthResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
/**
 * 基础健康检查接口。
 *
 * <p>这个接口不属于 AI 业务本身，它的作用很直接：
 * 回答“后端服务当前是否存活、是否能正常响应请求”。</p>
 */
public class HealthController {

    private final String applicationName;

    public HealthController(@Value("${spring.application.name}") String applicationName) {
        this.applicationName = applicationName;
    }

    /**
     * 返回一个轻量级健康检查结果。
     *
     * <p>前端可以先调用这个接口判断后端是否可达，
     * 再继续测试 AI 相关功能。</p>
     */
    @GetMapping("/health")
    public ApiResponse<HealthResponse> health() {
        return ApiResponse.success(new HealthResponse("UP", applicationName));
    }
}
