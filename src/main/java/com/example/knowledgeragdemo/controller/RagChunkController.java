package com.example.knowledgeragdemo.controller;

import com.example.knowledgeragdemo.dto.ApiResponse;
import com.example.knowledgeragdemo.dto.RagChunksResponse;
import com.example.knowledgeragdemo.service.RagChunkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RagChunkController {

    private final RagChunkService ragChunkService;

    public RagChunkController(RagChunkService ragChunkService) {
        this.ragChunkService = ragChunkService;
    }

    /**
     * 查看当前内存知识库的切片结果。
     *
     * <p>这是 Day 10 的调试接口，帮助我们在接 embedding 前先确认 chunk 是否可追溯。</p>
     */
    @GetMapping("/rag/chunks")
    public ResponseEntity<ApiResponse<RagChunksResponse>> chunks() {
        try {
            return ResponseEntity.ok(ApiResponse.success(ragChunkService.listChunks()));
        } catch (IllegalStateException e) {
            // 配置不合法、源文件读取失败等可预期问题统一返回 400，方便前端展示明确错误。
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));
        }
    }
}
