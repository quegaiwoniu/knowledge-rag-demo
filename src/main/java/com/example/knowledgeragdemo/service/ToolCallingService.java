package com.example.knowledgeragdemo.service;

import com.example.knowledgeragdemo.dto.ToolCallResponse;
import org.springframework.stereotype.Service;

@Service
public class ToolCallingService {

    private final AiService aiService;

    public ToolCallingService(AiService aiService) {
        this.aiService = aiService;
    }

    public ToolCallResponse answer(String question) {
        return aiService.toolCall(question);
    }
}
