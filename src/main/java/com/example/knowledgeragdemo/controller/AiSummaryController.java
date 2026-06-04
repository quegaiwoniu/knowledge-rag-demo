package com.example.knowledgeragdemo.controller;

import com.example.knowledgeragdemo.config.AppAiProperties;
import com.example.knowledgeragdemo.dto.ApiResponse;
import com.example.knowledgeragdemo.dto.SummaryRequest;
import com.example.knowledgeragdemo.dto.SummaryResponse;
import com.example.knowledgeragdemo.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
/**
 * 文本总结接口控制器。
 *
 * <p>这是当前 demo 里第一个真正处理文本内容的接口。
 * 它负责做输入校验、长度限制检查，并把总结逻辑委托给 AI service 层。</p>
 */
public class AiSummaryController {

    private final AiService aiService;
    private final AppAiProperties appAiProperties;

    public AiSummaryController(AiService aiService, AppAiProperties appAiProperties) {
        this.aiService = aiService;
        this.appAiProperties = appAiProperties;
    }

    /**
     * 对输入文本执行总结。
     *
     * <p>校验规则：
     * 1. text 不能为空白
     * 2. text 不能超过配置中的最大输入长度
     *
     * <p>如果校验通过，再把请求交给 {@link AiService#summarize(String)} 处理。</p>
     */
    @PostMapping("/ai/summary")
    public ResponseEntity<ApiResponse<SummaryResponse>> summarize(@RequestBody SummaryRequest request) {
        String text = request == null ? null : request.getText();
        if (text == null || text.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.failure("text must not be blank"));
        }

        if (text.length() > appAiProperties.summaryMaxInputLength()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("text exceeds max length " + appAiProperties.summaryMaxInputLength()));
        }

        return ResponseEntity.ok(ApiResponse.success(aiService.summarize(text)));
    }
}
