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
class AiClassifyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void classifyReturnsStableCategoryForBugLikeInput() throws Exception {
        mockMvc.perform(post("/ai/classify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "系统升级后，提交订单一直报空指针异常，页面也无法保存。"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.category").value("BUG"))
                .andExpect(jsonPath("$.data.description").isNotEmpty());
    }

    @Test
    void classifyReturnsStableCategoryForQuestionLikeInput() throws Exception {
        mockMvc.perform(post("/ai/classify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "请问这个接口应该怎么调用，是否需要先登录获取 token？"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.category").value("QUESTION"))
                .andExpect(jsonPath("$.data.description").isNotEmpty());
    }

    @Test
    void classifyRejectsBlankInput() throws Exception {
        mockMvc.perform(post("/ai/classify")
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
}
