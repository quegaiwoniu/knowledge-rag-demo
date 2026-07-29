package com.example.knowledgeragdemo.controller;

import com.example.knowledgeragdemo.dto.ApiResponse;
import com.example.knowledgeragdemo.dto.RagIndexStatusResponse;
import com.example.knowledgeragdemo.dto.RagSearchResponse;
import com.example.knowledgeragdemo.service.RagIndexService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * RAG 索引管理接口。
 *
 * <p>Day 11 提供索引重建和状态查询能力。</p>
 */
@RestController
@RequestMapping("/rag/index")
public class RagIndexController {

    private final RagIndexService ragIndexService;

    public RagIndexController(RagIndexService ragIndexService) {
        this.ragIndexService = ragIndexService;
    }

    /**
     * 触发一次完整的索引重建。
     *
     * <p>流程：导入文档 → 切片 → 向量化 → 写入 pgvector。</p>
     */
    @PostMapping("/rebuild")
    public ResponseEntity<ApiResponse<RagIndexStatusResponse>> rebuild() {
        try {
            RagIndexStatusResponse status = ragIndexService.rebuildIndex();
            return ResponseEntity.ok(ApiResponse.success(status));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.failure("索引重建失败: " + e.getMessage()));
        }
    }

    /**
     * 查询当前索引状态。
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<RagIndexStatusResponse>> status() {
        return ResponseEntity.ok(ApiResponse.success(ragIndexService.getStatus()));
    }

    /**
     * 基于 pgvector 的相似度检索。
     *
     * @param query 用户问题
     * @param topK  返回最相关的条数（默认 5）
     * @return 检索结果
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<RagSearchResponse>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topK) {
        try {
            RagSearchResponse results = ragIndexService.search(query, topK);
            return ResponseEntity.ok(ApiResponse.success(results));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.failure("检索失败: " + e.getMessage()));
        }
    }
}