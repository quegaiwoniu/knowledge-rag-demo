package com.example.knowledgeragdemo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RagIngestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void ingestImportsMarkdownDocumentsFromSampleDocsDirectory() throws Exception {
        mockMvc.perform(post("/rag/ingest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.importedCount").value(10))
                .andExpect(jsonPath("$.data.duplicateCount").value(0))
                .andExpect(jsonPath("$.data.documents[0].docId").isNotEmpty())
                .andExpect(jsonPath("$.data.documents[0].fileName").isNotEmpty())
                .andExpect(jsonPath("$.data.documents[0].sourcePath").isNotEmpty())
                .andExpect(jsonPath("$.data.documents[0].title").isNotEmpty())
                .andExpect(jsonPath("$.data.documents[0].contentHash").isNotEmpty())
                .andExpect(jsonPath("$.data.documents[0].ingestedAt").isNotEmpty());
    }
}
