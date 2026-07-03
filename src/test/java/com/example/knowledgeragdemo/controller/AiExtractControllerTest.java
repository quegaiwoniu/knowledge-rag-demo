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
class AiExtractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void extractReturnsStructuredFieldsForNormalInput() throws Exception {
        mockMvc.perform(post("/ai/extract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "支付接口上线后，部分订单提交失败，用户反馈优先处理，并检查超时日志。"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("订单提交失败排查"))
                .andExpect(jsonPath("$.data.category").value("BUG"))
                .andExpect(jsonPath("$.data.priority").value("HIGH"))
                .andExpect(jsonPath("$.data.keywords").isArray())
                .andExpect(jsonPath("$.data.keywords.length()").value(3));
    }

    @Test
    void extractRejectsBlankInput() throws Exception {
        mockMvc.perform(post("/ai/extract")
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
    void extractRejectsInputThatExceedsConfiguredLimit() throws Exception {
        String longText = "x".repeat(1201);
        String payload = "{\"text\":\"" + longText + "\"}";

        mockMvc.perform(post("/ai/extract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("text exceeds max length 1200"));
    }
}
