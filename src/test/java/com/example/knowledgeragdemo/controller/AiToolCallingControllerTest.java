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
class AiToolCallingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void toolCallUsesWeatherToolForWeatherQuestion() throws Exception {
        mockMvc.perform(post("/ai/tool-call")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "question": "北京今天天气怎么样？"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.toolCalled").value(true))
                .andExpect(jsonPath("$.data.toolName").value("getWeather"))
                .andExpect(jsonPath("$.data.toolSource").value("mock-weather"))
                .andExpect(jsonPath("$.data.toolResult.location").value("北京"))
                .andExpect(jsonPath("$.data.toolResult.condition").value("晴"))
                .andExpect(jsonPath("$.data.answer").isNotEmpty());
    }

    @Test
    void toolCallReturnsDirectAnswerForNonWeatherQuestion() throws Exception {
        mockMvc.perform(post("/ai/tool-call")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "question": "请介绍一下你这个项目现在做到哪一步了？"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.toolCalled").value(false))
                .andExpect(jsonPath("$.data.toolName").doesNotExist())
                .andExpect(jsonPath("$.data.toolSource").doesNotExist())
                .andExpect(jsonPath("$.data.toolResult").doesNotExist())
                .andExpect(jsonPath("$.data.answer").isNotEmpty());
    }

    @Test
    void toolCallRejectsBlankQuestion() throws Exception {
        mockMvc.perform(post("/ai/tool-call")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "question": "   "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("question must not be blank"));
    }
}
