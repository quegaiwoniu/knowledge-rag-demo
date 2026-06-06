package com.example.knowledgeragdemo.controller;

import com.example.knowledgeragdemo.config.AppAiProperties;
import com.example.knowledgeragdemo.dto.ApiResponse;
import com.example.knowledgeragdemo.dto.ClassifyRequest;
import com.example.knowledgeragdemo.dto.ClassifyResponse;
import com.example.knowledgeragdemo.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文本分类接口控制器。
 *
 * <p>这个接口用于把用户输入稳定映射到固定枚举类别，
 * 适合后续挂到工单分类、反馈分流、客服辅助等场景。</p>
 */
@RestController
public class AiClassifyController {

    private final AiService aiService;
    private final AppAiProperties appAiProperties;

    public AiClassifyController(AiService aiService, AppAiProperties appAiProperties) {
        this.aiService = aiService;
        this.appAiProperties = appAiProperties;
    }

    /**
     * 对输入文本进行固定分类。
     *
     * <p>当前沿用 summary 的长度限制，避免过长文本直接进入模型导致响应不稳定或成本升高。</p>
     */
    @PostMapping("/ai/classify")
    public ResponseEntity<ApiResponse<ClassifyResponse>> classify(@RequestBody ClassifyRequest request) {
        String text = request == null ? null : request.getText();
        if (text == null || text.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.failure("text must not be blank"));
        }

        if (text.length() > appAiProperties.summaryMaxInputLength()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("text exceeds max length " + appAiProperties.summaryMaxInputLength()));
        }

        return ResponseEntity.ok(ApiResponse.success(aiService.classify(text)));
    }
}
