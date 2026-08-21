package com.example.knowledgeragdemo.filter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 验证 TraceIdFilter 的行为：
 * 1. 每个响应都携带 X-Trace-Id 响应头
 * 2. 不同请求的 traceId 不同
 * 3. 无 MDC 泄漏（请求结束后 ThreadLocal 被清理）
 */
@SpringBootTest
@AutoConfigureMockMvc
class TraceIdFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void everyResponseCarriesTraceIdHeader() throws Exception {
        MvcResult result = mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andReturn();

        String traceId = result.getResponse().getHeader(TraceIdFilter.TRACE_ID_HEADER);
        assertThat(traceId).isNotBlank();
    }

    @Test
    void differentRequestsGetDifferentTraceIds() throws Exception {
        String first = mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getHeader(TraceIdFilter.TRACE_ID_HEADER);

        String second = mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getHeader(TraceIdFilter.TRACE_ID_HEADER);

        assertThat(first).isNotBlank();
        assertThat(second).isNotBlank();
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void threadLocalIsClearedAfterRequest() throws Exception {
        // 先触发一次请求，让 filter 写入 ThreadLocal
        mockMvc.perform(get("/health")).andExpect(status().isOk());

        // 请求结束后，ThreadLocal 应该被清空，回退到默认值
        assertThat(TraceIdContext.get()).isEqualTo("no-trace");
    }
}
