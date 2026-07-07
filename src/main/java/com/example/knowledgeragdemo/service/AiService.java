package com.example.knowledgeragdemo.service;

import com.example.knowledgeragdemo.dto.AiPingResponse;
import com.example.knowledgeragdemo.dto.ClassifyResponse;
import com.example.knowledgeragdemo.dto.ExtractResponse;
import com.example.knowledgeragdemo.dto.SummaryResponse;
import com.example.knowledgeragdemo.dto.ToolCallResponse;

/**
 * 应用内 AI 能力的统一 service 抽象。
 */
public interface AiService {

    AiPingResponse ping(String message);

    SummaryResponse summarize(String text);

    ClassifyResponse classify(String text);

    ExtractResponse extract(String text);

    ToolCallResponse toolCall(String question);
}
