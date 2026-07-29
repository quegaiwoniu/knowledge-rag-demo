package com.example.knowledgeragdemo.controller;

import com.example.knowledgeragdemo.dto.ApiResponse;
import com.example.knowledgeragdemo.dto.RagAskRequest;
import com.example.knowledgeragdemo.dto.RagAskResponse;
import com.example.knowledgeragdemo.service.RagIndexService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * RAG 问答接口。
 *
 * <p>Day 13 提供带引用和拒答的问答能力。</p>
 */
@RestController
@RequestMapping("/rag")
public class RagAskController {

    private final RagIndexService ragIndexService;

    public RagAskController(RagIndexService ragIndexService) {
        this.ragIndexService = ragIndexService;
    }

    /**
     * 基于知识库检索结果生成答案。
     *
     * <p>流程：检索 → 判断是否有足够上下文 → 拼 prompt → 调模型 → 返回答案 + 引用。</p>
     */
    @PostMapping("/ask")
    public ResponseEntity<ApiResponse<RagAskResponse>> ask(@RequestBody RagAskRequest request) {
        try {
            RagAskResponse response = ragIndexService.ask(
                    request.getQuery(),
                    request.getTopK()
            );
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.failure("问答失败: " + e.getMessage()));
        }
    }
}
