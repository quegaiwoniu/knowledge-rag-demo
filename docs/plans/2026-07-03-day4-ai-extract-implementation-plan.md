# Day 4 `/ai/extract` Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 `knowledge-rag-demo` 增加企业风格的结构化抽取接口 `POST /ai/extract`，返回稳定的 `title/category/priority/keywords` 结构。

**Architecture:** 沿用当前项目的 `controller -> service -> dto` 分层。模型先返回内部候选结构 `ExtractResult`，再由 `ProviderAiService` 做归一化、枚举映射和结构校验，最终输出稳定的 `ExtractResponse`。

**Tech Stack:** Java 17, Spring Boot 3.5.0, Spring AI 1.0.0, Maven, MockMvc, JUnit 5, Mockito

---

## 文件结构

### 新增文件

- `src/main/java/com/example/knowledgeragdemo/controller/AiExtractController.java`
  - `/ai/extract` 接口入口，负责基础输入校验和响应包装
- `src/main/java/com/example/knowledgeragdemo/dto/ExtractRequest.java`
  - 请求体 DTO，只承载 `text`
- `src/main/java/com/example/knowledgeragdemo/dto/ExtractResponse.java`
  - 对外稳定返回对象，字段为 `title/category/priority/keywords`
- `src/main/java/com/example/knowledgeragdemo/dto/ExtractResult.java`
  - 仅供 service 内部接收模型候选结构
- `src/main/java/com/example/knowledgeragdemo/dto/ExtractionPriority.java`
  - 固定优先级枚举 `LOW/MEDIUM/HIGH`
- `src/test/java/com/example/knowledgeragdemo/controller/AiExtractControllerTest.java`
  - controller 行为测试

### 修改文件

- `src/main/java/com/example/knowledgeragdemo/service/AiService.java`
  - 增加 `extract(String text)` 抽象方法
- `src/main/java/com/example/knowledgeragdemo/service/StubAiService.java`
  - 提供稳定的 `extract` stub 结果，支撑测试和本地兜底
- `src/main/java/com/example/knowledgeragdemo/service/ProviderAiService.java`
  - 新增结构化抽取 prompt、候选结构映射、归一化与异常校验逻辑
- `src/test/java/com/example/knowledgeragdemo/service/ProviderAiServiceTest.java`
  - 补充 `extract` 的 service 层兜底测试

---

### Task 1: 搭建 `/ai/extract` 的 DTO 与接口骨架

**Files:**
- Create: `src/main/java/com/example/knowledgeragdemo/dto/ExtractRequest.java`
- Create: `src/main/java/com/example/knowledgeragdemo/dto/ExtractResponse.java`
- Create: `src/main/java/com/example/knowledgeragdemo/dto/ExtractResult.java`
- Create: `src/main/java/com/example/knowledgeragdemo/dto/ExtractionPriority.java`
- Create: `src/main/java/com/example/knowledgeragdemo/controller/AiExtractController.java`
- Modify: `src/main/java/com/example/knowledgeragdemo/service/AiService.java`
- Test: `src/test/java/com/example/knowledgeragdemo/controller/AiExtractControllerTest.java`

- [ ] **Step 1: 写失败的 controller 测试**

```java
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
                .andExpect(jsonPath("$.data.title").isNotEmpty())
                .andExpect(jsonPath("$.data.category").isNotEmpty())
                .andExpect(jsonPath("$.data.priority").isNotEmpty())
                .andExpect(jsonPath("$.data.keywords").isArray());
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
```

- [ ] **Step 2: 运行测试，确认接口尚未实现**

Run: `mvn test -Dtest=AiExtractControllerTest`

Expected: FAIL，出现 `404` 或 Spring 容器中缺少 `/ai/extract` 对应 controller / service 方法

- [ ] **Step 3: 新增请求与响应 DTO、优先级枚举、controller 骨架，并补齐 `AiService` 抽象**

```java
package com.example.knowledgeragdemo.dto;

public class ExtractRequest {

    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
```

```java
package com.example.knowledgeragdemo.dto;

public enum ExtractionPriority {
    LOW,
    MEDIUM,
    HIGH
}
```

```java
package com.example.knowledgeragdemo.dto;

import java.util.List;

public class ExtractResponse {

    private final String title;
    private final ClassificationCategory category;
    private final ExtractionPriority priority;
    private final List<String> keywords;

    public ExtractResponse(String title,
                           ClassificationCategory category,
                           ExtractionPriority priority,
                           List<String> keywords) {
        this.title = title;
        this.category = category;
        this.priority = priority;
        this.keywords = keywords;
    }

    public String getTitle() {
        return title;
    }

    public ClassificationCategory getCategory() {
        return category;
    }

    public ExtractionPriority getPriority() {
        return priority;
    }

    public List<String> getKeywords() {
        return keywords;
    }
}
```

```java
package com.example.knowledgeragdemo.dto;

import java.util.List;

public class ExtractResult {

    private String title;
    private String category;
    private String priority;
    private List<String> keywords;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
}
```

```java
package com.example.knowledgeragdemo.controller;

import com.example.knowledgeragdemo.config.AppAiProperties;
import com.example.knowledgeragdemo.dto.ApiResponse;
import com.example.knowledgeragdemo.dto.ExtractRequest;
import com.example.knowledgeragdemo.dto.ExtractResponse;
import com.example.knowledgeragdemo.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiExtractController {

    private final AiService aiService;
    private final AppAiProperties appAiProperties;

    public AiExtractController(AiService aiService, AppAiProperties appAiProperties) {
        this.aiService = aiService;
        this.appAiProperties = appAiProperties;
    }

    @PostMapping("/ai/extract")
    public ResponseEntity<ApiResponse<ExtractResponse>> extract(@RequestBody ExtractRequest request) {
        String text = request == null ? null : request.getText();
        if (text == null || text.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.failure("text must not be blank"));
        }

        if (text.length() > appAiProperties.summaryMaxInputLength()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("text exceeds max length " + appAiProperties.summaryMaxInputLength()));
        }

        return ResponseEntity.ok(ApiResponse.success(aiService.extract(text)));
    }
}
```

```java
package com.example.knowledgeragdemo.service;

import com.example.knowledgeragdemo.dto.AiPingResponse;
import com.example.knowledgeragdemo.dto.ClassifyResponse;
import com.example.knowledgeragdemo.dto.ExtractResponse;
import com.example.knowledgeragdemo.dto.SummaryResponse;

public interface AiService {

    AiPingResponse ping(String message);

    SummaryResponse summarize(String text);

    ClassifyResponse classify(String text);

    ExtractResponse extract(String text);
}
```

- [ ] **Step 4: 在 `StubAiService` 里补一个最小可用的稳定实现**

```java
@Override
public ExtractResponse extract(String text) {
    String normalized = text == null ? "" : text.trim().replaceAll("\\s+", " ");
    ClassificationCategory category = resolveCategory(normalized.toLowerCase());
    ExtractionPriority priority = containsAny(normalized.toLowerCase(),
            "优先", "紧急", "立即", "失败", "异常") ? ExtractionPriority.HIGH : ExtractionPriority.MEDIUM;

    return new ExtractResponse(
            "结构化抽取预览",
            category,
            priority,
            java.util.List.of("结构化抽取", "接口设计", "学习项目")
    );
}
```

- [ ] **Step 5: 运行 controller 测试，确认接口骨架通过**

Run: `mvn test -Dtest=AiExtractControllerTest`

Expected: PASS

- [ ] **Step 6: 提交这一小步**

```bash
git add src/main/java/com/example/knowledgeragdemo/controller/AiExtractController.java src/main/java/com/example/knowledgeragdemo/dto/ExtractRequest.java src/main/java/com/example/knowledgeragdemo/dto/ExtractResponse.java src/main/java/com/example/knowledgeragdemo/dto/ExtractResult.java src/main/java/com/example/knowledgeragdemo/dto/ExtractionPriority.java src/main/java/com/example/knowledgeragdemo/service/AiService.java src/main/java/com/example/knowledgeragdemo/service/StubAiService.java src/test/java/com/example/knowledgeragdemo/controller/AiExtractControllerTest.java
git commit -m "feat: add ai extract endpoint skeleton"
```

### Task 2: 实现 `ProviderAiService` 的结构化抽取与治理逻辑

**Files:**
- Modify: `src/main/java/com/example/knowledgeragdemo/service/ProviderAiService.java`
- Modify: `src/test/java/com/example/knowledgeragdemo/service/ProviderAiServiceTest.java`

- [ ] **Step 1: 先写 service 层失败测试，锁定归一化与异常行为**

```java
package com.example.knowledgeragdemo.service;

import com.example.knowledgeragdemo.config.AppAiProperties;
import com.example.knowledgeragdemo.dto.ExtractResponse;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProviderAiServiceTest {

    @Test
    void extractNormalizesPriorityAndDeduplicatesKeywords() {
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec responseSpec = mock(ChatClient.CallResponseSpec.class);
        AppAiProperties properties = new AppAiProperties("provider", "hello", 1200, false);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.entity(com.example.knowledgeragdemo.dto.ExtractResult.class)).thenReturn(buildResult(
                "  订单提交失败排查。  ",
                "bug",
                "high",
                List.of("订单提交", "超时日志", "订单提交", "支付接口")
        ));

        ProviderAiService providerAiService = new ProviderAiService(chatClient, properties);

        ExtractResponse response = providerAiService.extract("支付接口上线后，订单提交失败并出现超时日志");

        assertEquals("订单提交失败排查", response.getTitle());
        assertEquals("BUG", response.getCategory().name());
        assertEquals("HIGH", response.getPriority().name());
        assertEquals(List.of("订单提交", "超时日志", "支付接口"), response.getKeywords());
    }

    @Test
    void extractThrowsWhenCategoryIsUnsupported() {
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec responseSpec = mock(ChatClient.CallResponseSpec.class);
        AppAiProperties properties = new AppAiProperties("provider", "hello", 1200, false);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.entity(com.example.knowledgeragdemo.dto.ExtractResult.class)).thenReturn(buildResult(
                "订单提交失败排查",
                "incident",
                "high",
                List.of("订单提交", "超时日志", "支付接口")
        ));

        ProviderAiService providerAiService = new ProviderAiService(chatClient, properties);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> providerAiService.extract("支付接口上线后，订单提交失败并出现超时日志"));

        assertEquals("invalid extract category: incident", exception.getMessage());
    }

    @Test
    void extractThrowsWhenNormalizedKeywordsAreTooFew() {
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec responseSpec = mock(ChatClient.CallResponseSpec.class);
        AppAiProperties properties = new AppAiProperties("provider", "hello", 1200, false);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.entity(com.example.knowledgeragdemo.dto.ExtractResult.class)).thenReturn(buildResult(
                "订单提交失败排查",
                "bug",
                "high",
                List.of("订单提交", "订单提交", " ")
        ));

        ProviderAiService providerAiService = new ProviderAiService(chatClient, properties);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> providerAiService.extract("支付接口上线后，订单提交失败并出现超时日志"));

        assertEquals("invalid extract keywords: too few valid keywords", exception.getMessage());
    }

    private com.example.knowledgeragdemo.dto.ExtractResult buildResult(
            String title, String category, String priority, List<String> keywords) {
        com.example.knowledgeragdemo.dto.ExtractResult result = new com.example.knowledgeragdemo.dto.ExtractResult();
        result.setTitle(title);
        result.setCategory(category);
        result.setPriority(priority);
        result.setKeywords(keywords);
        return result;
    }
}
```

- [ ] **Step 2: 运行测试，确认 service 新行为尚未实现**

Run: `mvn test -Dtest=ProviderAiServiceTest`

Expected: FAIL，提示 `extract` 方法不存在、`entity(ExtractResult.class)` 尚未接入，或断言不匹配

- [ ] **Step 3: 在 `ProviderAiService` 中实现结构化抽取、映射和后置治理**

```java
@Override
public ExtractResponse extract(String text) {
    ExtractResult result = chatClient.prompt()
            .system("""
                    你是企业应用中的文本结构化抽取助手。
                    你的任务是把用户输入提炼成固定结构，供后端系统直接消费。

                    请严格遵守以下规则：
                    1. title 是 1-20 字的简短中文标题
                    2. category 只能是 bug、feature、question、complaint 之一
                    3. priority 只能是 low、medium、high 之一
                    4. keywords 返回 3 到 5 个中文关键词
                    5. 不要输出解释，不要输出 markdown
                    6. 不要虚构原文中不存在的事实
                    7. 如果信息不足，也必须尽量给出最保守的结构化结果
                    """)
            .user("""
                    请从以下文本中提取结构化信息：

                    %s
                    """.formatted(text))
            .call()
            .entity(ExtractResult.class);

    String title = normalizeTitle(result == null ? null : result.getTitle());
    ClassificationCategory category = parseCategory(result == null ? null : result.getCategory());
    ExtractionPriority priority = parsePriority(result == null ? null : result.getPriority());
    java.util.List<String> keywords = normalizeKeywords(result == null ? null : result.getKeywords());

    return new ExtractResponse(title, category, priority, keywords);
}

private String normalizeTitle(String title) {
    String normalized = title == null ? "" : title.trim().replaceAll("\\s+", " ");
    normalized = normalized.replaceAll("[。！？,.!?\u3002\uff01\uff1f]+$", "");
    if (normalized.isBlank()) {
        throw new IllegalStateException("invalid extract title: blank after normalization");
    }
    if (normalized.length() > 20) {
        normalized = normalized.substring(0, 20);
    }
    return normalized;
}

private ClassificationCategory parseCategory(String rawCategory) {
    String normalized = rawCategory == null ? "" : rawCategory.trim().toLowerCase(java.util.Locale.ROOT);
    return switch (normalized) {
        case "bug" -> ClassificationCategory.BUG;
        case "feature" -> ClassificationCategory.FEATURE;
        case "question" -> ClassificationCategory.QUESTION;
        case "complaint" -> ClassificationCategory.COMPLAINT;
        default -> throw new IllegalStateException("invalid extract category: " + rawCategory);
    };
}

private ExtractionPriority parsePriority(String rawPriority) {
    String normalized = rawPriority == null ? "" : rawPriority.trim().toLowerCase(java.util.Locale.ROOT);
    return switch (normalized) {
        case "low" -> ExtractionPriority.LOW;
        case "medium" -> ExtractionPriority.MEDIUM;
        case "high" -> ExtractionPriority.HIGH;
        default -> throw new IllegalStateException("invalid extract priority: " + rawPriority);
    };
}

private java.util.List<String> normalizeKeywords(java.util.List<String> rawKeywords) {
    if (rawKeywords == null || rawKeywords.isEmpty()) {
        throw new IllegalStateException("invalid extract keywords: empty result");
    }

    java.util.List<String> normalized = rawKeywords.stream()
            .filter(java.util.Objects::nonNull)
            .map(String::trim)
            .filter(keyword -> !keyword.isBlank())
            .map(keyword -> keyword.length() > 10 ? keyword.substring(0, 10) : keyword)
            .distinct()
            .limit(5)
            .toList();

    if (normalized.size() < 3) {
        throw new IllegalStateException("invalid extract keywords: too few valid keywords");
    }

    return normalized;
}
```

- [ ] **Step 4: 运行 service 测试，确认治理逻辑通过**

Run: `mvn test -Dtest=ProviderAiServiceTest`

Expected: PASS

- [ ] **Step 5: 提交这一小步**

```bash
git add src/main/java/com/example/knowledgeragdemo/service/ProviderAiService.java src/test/java/com/example/knowledgeragdemo/service/ProviderAiServiceTest.java
git commit -m "feat: add structured extraction normalization"
```

### Task 3: 收尾联通 stub、controller 与完整回归

**Files:**
- Modify: `src/main/java/com/example/knowledgeragdemo/service/StubAiService.java`
- Modify: `src/test/java/com/example/knowledgeragdemo/controller/AiExtractControllerTest.java`
- Modify: `src/test/java/com/example/knowledgeragdemo/service/ProviderAiServiceTest.java`

- [ ] **Step 1: 让 stub 结果更贴近真实接口，保证本地和测试输出稳定**

```java
@Override
public ExtractResponse extract(String text) {
    String normalized = text == null ? "" : text.trim().replaceAll("\\s+", " ");
    ClassificationCategory category = resolveCategory(normalized.toLowerCase());
    ExtractionPriority priority = containsAny(normalized.toLowerCase(),
            "优先", "紧急", "立即", "失败", "异常") ? ExtractionPriority.HIGH : ExtractionPriority.MEDIUM;

    java.util.List<String> keywords = containsAny(normalized.toLowerCase(), "支付", "订单")
            ? java.util.List.of("支付接口", "订单提交", "超时日志")
            : java.util.List.of("结构化抽取", "接口设计", "学习项目");

    String title = category == ClassificationCategory.BUG ? "订单提交失败排查" : "结构化信息提取";
    return new ExtractResponse(title, category, priority, keywords);
}
```

- [ ] **Step 2: 增强 controller 断言，验证枚举值和关键词数量**

```java
.andExpect(jsonPath("$.data.category").value("BUG"))
.andExpect(jsonPath("$.data.priority").value("HIGH"))
.andExpect(jsonPath("$.data.keywords.length()").value(3));
```

- [ ] **Step 3: 运行 Day 4 相关测试**

Run: `mvn test -Dtest=AiExtractControllerTest,ProviderAiServiceTest`

Expected: PASS

- [ ] **Step 4: 运行相关回归测试，避免破坏 Day 2 / Day 3**

Run: `mvn test -Dtest=AiSummaryControllerTest,AiClassifyControllerTest,ProviderAiServiceTest,AiExtractControllerTest`

Expected: PASS

- [ ] **Step 5: 提交收尾**

```bash
git add src/main/java/com/example/knowledgeragdemo/service/StubAiService.java src/test/java/com/example/knowledgeragdemo/controller/AiExtractControllerTest.java src/test/java/com/example/knowledgeragdemo/service/ProviderAiServiceTest.java
git commit -m "test: finalize ai extract endpoint coverage"
```

