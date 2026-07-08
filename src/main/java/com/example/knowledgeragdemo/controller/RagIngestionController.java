package com.example.knowledgeragdemo.controller;

import com.example.knowledgeragdemo.dto.ApiResponse;
import com.example.knowledgeragdemo.dto.RagIngestResponse;
import com.example.knowledgeragdemo.service.RagIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RagIngestionController {

    private final RagIngestionService ragIngestionService;

    public RagIngestionController(RagIngestionService ragIngestionService) {
        this.ragIngestionService = ragIngestionService;
    }

    /**
     * 触发 RAG 文档导入。
     *
     * <p>Day 9 阶段只负责把 docs/sample-docs 下的 Markdown 文件读取成文档元数据，
     * 暂时不做 chunk 切片、embedding 向量化和向量检索。</p>
     */
    @PostMapping("/rag/ingest")
    public ResponseEntity<ApiResponse<RagIngestResponse>> ingest() {
        try {
            return ResponseEntity.ok(ApiResponse.success(ragIngestionService.ingest()));
        } catch (IllegalStateException e) {
            // 例如样例文档目录不存在时，返回 400，方便前端或调用方知道是输入/环境问题。
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));
        }
    }
}
