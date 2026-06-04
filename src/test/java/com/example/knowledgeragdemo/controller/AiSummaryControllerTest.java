package com.example.knowledgeragdemo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AiSummaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void summaryReturnsSummaryForNormalInput() throws Exception {
        mockMvc.perform(post("/ai/summary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "Spring AI helps us build AI features in Java services with consistent abstractions."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.summary").isNotEmpty())
                .andExpect(jsonPath("$.data.originalLength").value(83))
                .andExpect(jsonPath("$.data.truncated").value(false));
    }

    @Test
    void summaryRejectsBlankInput() throws Exception {
        mockMvc.perform(post("/ai/summary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "   "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("text must not be blank"));
    }

    @Test
    void summaryRejectsInputThatExceedsConfiguredLimit() throws Exception {
        String longText = "x".repeat(1201);
        String payload = "{\"text\":\"" + longText + "\"}";

        mockMvc.perform(post("/ai/summary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("text exceeds max length 1200"));
    }
}
