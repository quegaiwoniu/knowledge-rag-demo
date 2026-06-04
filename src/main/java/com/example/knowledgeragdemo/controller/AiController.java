package com.example.knowledgeragdemo.controller;

import com.example.knowledgeragdemo.dto.AiPingResponse;
import com.example.knowledgeragdemo.dto.ApiResponse;
import com.example.knowledgeragdemo.service.AiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
/**
 * 简单的 AI 调试控制器。
 *
 * <p>这个控制器的目的，是提供一条最短的调试链路，
 * 让我们快速确认 request -> controller -> service -> response 这一整条流程是通的。</p>
 */
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    /**
     * 一个类似 echo 的 AI 调试接口。
     *
     * <p>当前 stub 实现并不会真的调用模型，
     * 它主要用于验证 Bean 装配、请求响应契约以及前后端联调是否正常。</p>
     */
    @GetMapping("/ai/ping")
    public ApiResponse<AiPingResponse> ping(@RequestParam(defaultValue = "") String message) {
        return ApiResponse.success(aiService.ping(message));
    }
}
