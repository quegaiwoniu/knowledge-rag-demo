package com.example.knowledgeragdemo.controller;

import com.example.knowledgeragdemo.dto.ApiResponse;
import com.example.knowledgeragdemo.dto.ToolCallRequest;
import com.example.knowledgeragdemo.dto.ToolCallResponse;
import com.example.knowledgeragdemo.service.ToolCallingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiToolCallingController {

    private final ToolCallingService toolCallingService;

    public AiToolCallingController(ToolCallingService toolCallingService) {
        this.toolCallingService = toolCallingService;
    }

    @PostMapping("/ai/tool-call")
    public ResponseEntity<ApiResponse<ToolCallResponse>> toolCall(@RequestBody ToolCallRequest request) {
        String question = request == null ? null : request.getQuestion();
        if (question == null || question.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.failure("question must not be blank"));
        }

        return ResponseEntity.ok(ApiResponse.success(toolCallingService.answer(question)));
    }
}
