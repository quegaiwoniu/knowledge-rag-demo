package com.example.knowledgeragdemo.dto;

import java.time.LocalDateTime;

/**
 * RAG 索引状态响应。
 */
public record RagIndexStatusResponse(
        int documentCount,
        int chunkCount,
        int embeddedChunkCount,
        LocalDateTime lastRebuildAt,
        boolean hasIndex
) {
}
