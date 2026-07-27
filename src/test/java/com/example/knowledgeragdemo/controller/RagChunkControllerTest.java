package com.example.knowledgeragdemo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RagChunkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void chunksEndpointReturnsChunksAfterIngest() throws Exception {
        mockMvc.perform(post("/rag/ingest"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/rag/chunks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.documentCount").value(10))
                .andExpect(jsonPath("$.data.chunkCount").isNumber())
                .andExpect(jsonPath("$.data.chunks[0].docId").isNotEmpty())
                .andExpect(jsonPath("$.data.chunks[0].fileName").isNotEmpty())
                .andExpect(jsonPath("$.data.chunks[0].sourcePath").isNotEmpty())
                .andExpect(jsonPath("$.data.chunks[0].title").isNotEmpty())
                .andExpect(jsonPath("$.data.chunks[0].sectionTitle").isNotEmpty())
                .andExpect(jsonPath("$.data.chunks[0].chunkIndex").isNumber())
                .andExpect(jsonPath("$.data.chunks[0].content").isNotEmpty());
    }
}
