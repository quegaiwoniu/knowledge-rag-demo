package com.example.knowledgeragdemo.controller;

import com.example.knowledgeragdemo.config.AppAiProperties;
import com.example.knowledgeragdemo.dto.ApiResponse;
import com.example.knowledgeragdemo.dto.ExtractRequest;
import com.example.knowledgeragdemo.dto.ExtractResponse;
import com.example.knowledgeragdemo.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文本结构化抽取接口控制器。
 */
@RestController
public class AiExtractController {

    private final AiService aiService;
    private final AppAiProperties appAiProperties;

    public AiExtractController(AiService aiService, AppAiProperties appAiProperties) {
        this.aiService = aiService;
        this.appAiProperties = appAiProperties;
    }

    @PostMapping("/ai/extract")
    public ResponseEntity<ApiResponse<ExtractResponse>> extract(@RequestBody ExtractRequest request) {
        String text = request == null ? null : request.getText();
        if (text == null || text.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.failure("text must not be blank"));
        }

        if (text.length() > appAiProperties.summaryMaxInputLength()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("text exceeds max length " + appAiProperties.summaryMaxInputLength()));
        }

        return ResponseEntity.ok(ApiResponse.success(aiService.extract(text)));
    }
}
