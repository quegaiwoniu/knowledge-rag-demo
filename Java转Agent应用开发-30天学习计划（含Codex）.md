# Java转Agent应用开发-30天学习计划（含Codex）

## 1. 目标定义

本文档面向已有多年 Java 后端经验、希望在 1 个月内完成 Agent 应用开发转型的人。

目标不是转成算法研究员，也不是系统学习所有大模型理论，而是在较短时间内具备以下能力：

- 能基于 `Spring Boot + Spring AI` 独立完成 AI 应用 PoC
- 能完成一个企业知识库 `RAG` 项目
- 能完成一个 `Tool Calling / MCP Agent` 项目
- 能为两个项目补齐最小前端演示页面
- 能把 `Codex` 作为日常编码助手，提升交付速度
- 能围绕项目结果应对 Java AI 应用开发岗位的基础面试

## 2. 学习边界

本计划刻意收窄范围，只保留高 ROI 内容。

本月重点学习：

- `Spring AI`
- `Structured Output`
- `Tool Calling`
- `RAG`
- `MCP`
- `React + Vite` 最小演示层
- 最小评测与可观测

本月不做：

- 模型训练
- 微调
- 多智能体复杂协作
- 过多框架横向对比
- 长周期理论深挖

## 3. 最终交付物

30 天结束时，应至少拿到以下交付物：

### 3.1 项目一：`knowledge-rag-demo`

一个企业知识库问答最小闭环项目，至少包含：

- 文档导入
- 文档切片
- 向量化与检索
- 问答接口
- 引用来源
- 无答案拒答
- 最小演示页面
- 最小评测集

### 3.2 项目二：`ticket-agent-demo`

一个工单/排障辅助 Agent，至少包含：

- 用户问题输入
- Tool Calling
- 至少 3 个工具
- 多工具结果汇总
- 工具失败处理
- 至少 1 个 MCP Server
- 调用日志与证据输出
- 最小演示页面
- 最小评测集

### 3.3 方法沉淀

- 一套可复用的 Codex 提示词模板
- 一套自己的日常开发工作流
- 一份面试问答提纲

## 4. 技术栈建议

主栈：

- `Java 17+`
- `Spring Boot 3.x`
- `Spring AI`
- `Maven`

最小前端演示层：

- `React`
- `Vite`
- `TypeScript`

辅助能力：

- `LangChain4j`：只做了解，不作为主开发框架
- `MCP`：重点学习 server/client 基本集成方式

存储与检索：

- 向量库优先选择你最容易接入的轻量方案
- 不在本月花大量时间做向量库选型对比

## 5. Codex 在本计划中的角色

Codex 不是替你完成学习，而是替你节省样板代码、重构和测试上的时间。

### 5.1 适合交给 Codex 的任务

- 生成 Spring Boot 项目骨架
- 生成 DTO、Controller、Service、Config
- 生成统一响应体和异常处理
- 接入 SDK
- 编写单元测试
- 重构重复代码
- 生成 React 页面骨架
- 生成 API 调用层和 TS 类型
- 处理 loading / error / empty 状态
- 生成 README 初稿
- 整理接口示例

### 5.2 不适合完全交给 Codex 的任务

- 场景取舍
- 架构判断
- 数据切片策略判断
- 工具边界设计
- 结果验收
- 面试表达
- 错误归因

### 5.3 使用原则

- 一次只让 Codex 完成一个小闭环
- 先让 Codex 设计，再让它写代码
- 关键模块必须自己通读
- 运行和验收不能外包给 Codex

## 6. 每日固定节奏

如果你是在职学习，建议每天投入 `2-3 小时`，按固定节奏执行：

### 6.1 第 1 段：15 分钟

只看当天任务需要的资料，不做泛读。

### 6.2 第 2 段：60-90 分钟

让 Codex 帮你实现当天的小目标，完成一个最小闭环。

### 6.3 第 3 段：20-30 分钟

自己运行、调试、验证、读代码。

### 6.4 第 4 段：10-15 分钟

记录当天笔记：

- 今天完成了什么
- 哪些 prompt 有效
- 哪些代码讲不清楚
- 明天做什么

## 7. 30 天详细学习计划

## 时间分配原则

本计划改为“后端主线 + 最小前端演示层”的组合：

- 后端能力建设约占 `70%`
- 前端演示层约占 `20%`
- 文档、评测、面试包装约占 `10%`

前端不是单独成项目，而是服务于两个后端项目的可演示化。

## 第 1 周：Spring AI 最小闭环

目标：掌握模型调用、结构化输出、Tool Calling 的最小工程闭环。

本周只做后端，不做前端。

### Day 1：环境与骨架

目标：

- 创建 `knowledge-rag-demo`
- 接入 `Spring Boot + Spring AI`
- 建立统一工程结构

任务：

- 初始化 Maven 项目
- 配置 `application.yml`
- 增加 `/health`
- 增加 `/ai/ping`
- 增加统一响应体 `ApiResponse<T>`

验收：

- 项目能启动
- 基础接口可访问
- README 有最小运行说明

建议给 Codex 的提示词：

```text
基于 Spring Boot 3 和 Spring AI，生成一个最小可运行项目。
要求：
1. 包结构为 controller/service/config/dto
2. 提供 /health 和 /ai/ping 接口
3. 统一响应体为 ApiResponse<T>
4. 使用 application.yml 配置模型参数
5. 给出 pom.xml 和关键配置类
```

### Day 2：文本总结接口

目标：

- 完成 `/ai/summary`

任务：

- 定义请求 DTO 和返回 DTO
- 增加文本总结 service
- 增加空输入校验
- 增加超长输入限制
- 补充单测

验收：

- 正常输入可返回摘要
- 空输入有明确错误
- 超长文本处理行为可解释

### Day 3：文本分类接口

目标：

- 完成 `/ai/classify`

建议分类：

- `bug`
- `feature`
- `question`
- `complaint`

任务：

- 设计固定枚举
- 保证输出稳定
- 用结构化字段返回结果

验收：

- 相似输入的输出风格稳定
- 返回字段固定

### Day 4：结构化抽取接口

目标：

- 完成 `/ai/extract`

建议抽取字段：

- `title`
- `category`
- `priority`
- `keywords`

任务：

- 设计结构化输出对象
- 增加格式校验
- 补模型输出异常处理

验收：

- 返回固定 JSON
- 输出结构稳定
- 输出错误可定位

### Day 5：Tool Calling 入门

目标：

- 完成一个简单工具调用闭环

建议工具：

- `getOrderStatus`
- 或 `getTicketDetail`

任务：

- 设计工具接口
- 用 mock 数据实现工具
- 增加模型调用工具逻辑
- 输出自然语言汇总结果

验收：

- 问工具相关问题时，会触发工具
- 最终回答来自工具结果

### Day 6：补测试与重构

目标：

- 清理第 1 周代码

任务：

- 统一异常处理
- 抽公共 prompt builder
- 增加日志
- 拆分过长方法
- 补 controller/service 测试

验收：

- 代码结构清晰
- 测试能覆盖主要分支

### Day 7：阶段复盘

输出一页笔记，至少回答：

- 结构化输出为什么比自由文本更适合企业系统
- Tool Calling 和普通聊天接口的区别
- 哪些模块还讲不清楚
- 下周 RAG 项目需要准备什么文档

## 第 2 周：RAG 项目

目标：完成一个可演示、可测试的企业知识库问答系统，并补最小前端页面。

### Day 8：准备语料

任务：

- 收集 10-20 篇同一领域文档
- 优先选择自己熟悉的企业内容

建议文档类型：

- FAQ
- 排障手册
- 接口说明
- 产品说明

产出：

- `docs/sample-docs/`
- 文档清单
- 10 条候选问题

### Day 9：文档导入

目标：

- 完成 ingestion 基础流程

任务：

- 实现文件读取
- 保存元数据：
  - `docId`
  - `fileName`
  - `chunkIndex`
  - `sourcePath`

验收：

- 能批量读取文档
- 元数据完整

### Day 10：文档切片

目标：

- 实现 chunking

任务：

- 固定 chunk size
- 增加 overlap
- 保留原始元数据

验收：

- chunk 可追溯
- 分片粒度合理

### Day 11：向量化与入库

目标：

- 完成 embedding + vector store

任务：

- 接入 embedding
- 完成 chunk 入库
- 抽离 ingestion pipeline

验收：

- 文档可重复导入
- 检索前准备流程完整

### Day 12：纯检索接口

目标：

- 完成 `/rag/search`

任务：

- 输入 query
- 返回 top-k chunks
- 附带 source metadata

验收：

- 检索结果相关
- source 信息完整

### Day 13：问答接口

目标：

- 完成 `/rag/ask`

任务：

- 根据 query 检索文档片段
- 构造回答 prompt
- 返回答案和引用
- 处理空检索结果

验收：

- 回答可用
- 有 citation
- 资料不足时明确拒答

### Day 14：RAG 评测

任务：

- 准备 15-20 条问题
- 手工校验结果
- 分类错误原因

同时补充前端第一版：

- 创建 `knowledge-rag-demo-web`
- 完成文档导入区
- 完成问题输入区
- 完成答案展示区
- 完成引用来源区
- 处理 loading / error / empty 状态

至少区分以下错误类型：

- 召回失败
- 召回偏题
- 回答幻觉
- 引用错误
- 拒答失败

## 第 3 周：Agent + MCP 项目

目标：完成一个排障/工单辅助 Agent，并补最小前端页面。

### Day 15：定义场景和工具

项目名建议：

- `ticket-agent-demo`

工具至少 3 个：

- `getTicketDetail(ticketId)`
- `searchKnowledgeBase(query)`
- `getServiceHealth(serviceName)`

任务：

- 写接口契约
- 准备 mock 数据
- 画工具调用流程

### Day 16：普通 Tool Calling Agent

目标：

- 不使用 MCP，先完成 Agent 基础流程

任务：

- 构建 `AgentOrchestratorService`
- 根据用户问题决定是否调用工具
- 调用单工具并汇总结果

验收：

- 能回答单工具类问题
- 最终回答基于工具输出

### Day 17：多工具串联

目标：

- 支持两步或三步工具调用

任务：

- 先查工单
- 再查服务状态
- 再查知识库
- 汇总为最终建议

验收：

- 最终回答包含证据
- 无证据不下结论

### Day 18：失败处理

目标：

- 提高可靠性

任务：

- 设计失败场景：
  - 工具超时
  - 工具无数据
  - 参数非法
  - 目标工单不存在
- 补异常处理
- 补错误返回
- 增加日志

验收：

- 工具失败时不瞎编
- 有明确错误说明

### Day 19：第一个 MCP Server

目标：

- 将 `getServiceHealth` 封成 MCP Server

任务：

- 建立最小 MCP server
- 给出注册和调用示例
- 补 README

验收：

- 能通过 MCP 暴露能力
- Agent 能消费该能力

### Day 20：第二个 MCP 工具

目标：

- 封装第二个工具为 MCP

建议：

- `searchKnowledgeBase`
- 或 `getTicketDetail`

任务：

- 统一 schema
- 统一返回结构

验收：

- 多个工具的风格一致

### Day 21：阶段复盘

输出一页笔记，回答：

- Tool Calling 和 MCP 的区别
- 为什么 Java 企业系统适合通过 MCP 暴露工具
- Agent 项目最大风险是什么
- 当前系统如何防止幻觉

同时补充前端第一版：

- 创建 `ticket-agent-demo-web`
- 完成问题输入区
- 完成最终结论区
- 完成工具调用日志区
- 完成证据区和下一步建议区
- 处理 loading / error / empty 状态

## 第 4 周：评测、重构、面试包装

目标：把两个 PoC 变成可演示、可讲解、可面试的工程样例，并完成前后端联调。

### Day 22：RAG 评测集整理

任务：

- 为 `knowledge-rag-demo` 准备 20 条问题
- 标记期望来源
- 标记答案范围

### Day 23：Agent 评测集整理

任务：

- 为 `ticket-agent-demo` 准备 20 条问题
- 重点检查：
  - 工具选得对不对
  - 工具失败时是否乱答
  - 最终证据是否真实

### Day 24：重构 RAG 项目

任务：

- 抽 prompt builder
- 拆大类
- 清理命名
- 提升可测试性
- 联调 `knowledge-rag-demo-web`
- 清理答案区与引用区展示逻辑

### Day 25：重构 Agent 项目

任务：

- 统一 tool schema
- 统一 error model
- 增加 trace id
- 增强日志可读性
- 联调 `ticket-agent-demo-web`
- 清理工具日志区与 evidence 展示逻辑

### Day 26：补 README 和架构说明

两个项目的 README 至少包含：

- 项目目标
- 技术栈
- 核心流程
- 关键难点
- 风险控制
- 运行方式
- 示例输入输出

### Day 27：整理面试问答

必须准备的题目：

- RAG 和微调有什么区别
- 为什么企业问答优先做 RAG
- Tool Calling 的价值是什么
- MCP 解决了什么问题
- 如何降低幻觉
- 为什么要结构化输出
- 如何做最小评测
- 你的两个项目中最难的点是什么

### Day 28：整理演示脚本

任务：

- 为每个项目准备 3 个固定 demo case
- 固定演示顺序
- 整理 API 调用示例
- 确保页面可稳定完成演示流程

### Day 29：模拟面试

任务：

- 做 30 分钟项目自述
- 重点讲清：
  - 项目背景
  - 架构设计
  - 风险控制
  - 技术取舍

可让 Codex 扮演面试官反向追问。

### Day 30：最终复盘

最终输出：

- 两个项目代码
- 两个 README
- 一份面试问答提纲
- 一份 Codex 使用经验总结

总结至少回答：

- 哪类任务最适合交给 Codex
- 哪类任务必须自己判断
- 哪类提示词最有效
- 哪类错误最常返工

## 8. 项目一详细设计：`knowledge-rag-demo`

## 8.1 项目定位

定位为企业知识库问答最小闭环，不追求平台化，只解决以下核心问题：

- 文档如何导入
- 文档如何切片
- 如何召回相关内容
- 如何生成带来源的答案
- 当知识不足时如何拒答

## 8.2 推荐目录结构

```text
knowledge-rag-demo/
  src/main/java/com/example/rag/
    controller/
      RagController.java
      IngestionController.java
    service/
      DocumentIngestionService.java
      ChunkingService.java
      VectorStoreService.java
      RagQueryService.java
      AnswerGenerationService.java
    config/
      AiConfig.java
      VectorStoreConfig.java
      JacksonConfig.java
    dto/
      ApiResponse.java
      IngestRequest.java
      IngestResponse.java
      AskRequest.java
      AskResponse.java
      SearchRequest.java
      SearchResponse.java
      CitationDto.java
      ChunkDto.java
    domain/
      DocumentChunk.java
      Citation.java
    util/
      PromptTemplateBuilder.java
      DocumentParser.java
      IdGenerator.java
    exception/
      GlobalExceptionHandler.java
      BizException.java
  src/test/java/com/example/rag/
  docs/
    sample-docs/
  README.md
```

## 8.3 核心接口

### `POST /ingest`

作用：导入文档并切片入库

请求示例：

```json
{
  "paths": [
    "docs/sample-docs/faq.md",
    "docs/sample-docs/troubleshooting.md"
  ]
}
```

### `POST /rag/search`

作用：只做检索，便于调试召回效果

请求示例：

```json
{
  "query": "服务启动失败怎么办",
  "topK": 5
}
```

### `POST /rag/ask`

作用：检索 + 问答

请求示例：

```json
{
  "question": "系统启动时报数据库连接失败怎么排查？"
}
```

返回示例：

```json
{
  "success": true,
  "data": {
    "answer": "建议先检查数据库地址、账号密码和网络连通性。",
    "citations": [
      {
        "fileName": "troubleshooting.md",
        "chunkIndex": 3,
        "snippet": "当数据库连接失败时，应先检查连接串..."
      }
    ],
    "enoughContext": true
  }
}
```

## 8.4 核心流程

1. 接收文档路径
2. 读取文件
3. 切片并附带 metadata
4. 写入向量库
5. 根据问题检索相关 chunks
6. 组装 prompt
7. 返回答案和引用

## 8.5 工程关键点

- citation 必须来自检索结果，不能由模型自由生成
- 空检索结果要拒答
- 返回 answer 和 citations 时要分离字段
- chunk 设计优先简单稳定，不追求复杂策略

## 8.7 最小前端演示层：`knowledge-rag-demo-web`

目标：让用户能完成“导入文档 -> 输入问题 -> 查看答案 -> 查看引用”的完整体验。

推荐目录结构：

```text
knowledge-rag-demo-web/
  src/
    api/
      ragApi.ts
    components/
      UploadPanel.tsx
      QuestionInput.tsx
      AnswerCard.tsx
      CitationList.tsx
      EmptyState.tsx
      ErrorAlert.tsx
      LoadingBlock.tsx
    pages/
      RagDemoPage.tsx
    styles/
      variables.css
      app.css
    types/
      rag.ts
    App.tsx
    main.tsx
  package.json
  vite.config.ts
```

页面最小功能：

- 文档导入
- 问题输入
- 答案展示
- 引用来源展示
- 空状态、加载态、错误态

核心组件职责：

- `UploadPanel`：文档导入与导入结果
- `QuestionInput`：问题输入和提交
- `AnswerCard`：答案与上下文充足状态
- `CitationList`：引用来源列表
- `EmptyState`：无内容时展示
- `ErrorAlert`：错误提示
- `LoadingBlock`：加载态反馈

页面风格要求：

- 清晰分区
- 浅色背景
- 不使用默认 AI 紫色风格
- 重点突出答案和引用，不做复杂交互

## 8.6 最小测试集建议

- 5 条直接命中问题
- 5 条近义表达问题
- 5 条资料中没有答案的问题
- 5 条易混淆问题

## 9. 项目二详细设计：`ticket-agent-demo`

## 9.1 项目定位

定位为排障/工单辅助 Agent，不追求全自动执行，只做只读查询和分析。

核心目标：

- 模型决定是否调用工具
- 调用一个或多个工具
- 汇总结果
- 输出证据和下一步建议
- 演示 MCP 的企业接入价值

## 9.2 推荐目录结构

```text
ticket-agent-demo/
  src/main/java/com/example/agent/
    controller/
      AgentController.java
      TicketController.java
      HealthController.java
    service/
      AgentOrchestratorService.java
      ToolRoutingService.java
      TicketToolService.java
      KnowledgeToolService.java
      ServiceHealthToolService.java
      ResponseComposerService.java
    mcp/
      ServiceHealthMcpServer.java
      KnowledgeSearchMcpServer.java
    config/
      AiConfig.java
      ToolConfig.java
      McpConfig.java
    dto/
      ApiResponse.java
      AgentAskRequest.java
      AgentAnswerResponse.java
      ToolExecutionLogDto.java
      TicketDetailDto.java
      ServiceHealthDto.java
      KnowledgeHitDto.java
    domain/
      AgentContext.java
      ToolExecutionRecord.java
      AgentEvidence.java
    mock/
      MockTicketRepository.java
      MockServiceHealthRepository.java
      MockKnowledgeBaseRepository.java
    exception/
      GlobalExceptionHandler.java
      BizException.java
  src/test/java/com/example/agent/
  README.md
```

## 9.3 最小工具集

### `getTicketDetail(ticketId)`

返回：

- 工单状态
- 优先级
- 负责人
- 描述
- 最近更新时间

### `getServiceHealth(serviceName)`

返回：

- 服务状态
- 最近错误数
- 最近报警时间

### `searchKnowledgeBase(query)`

返回：

- 命中文档
- 相关片段
- 建议动作

## 9.4 核心接口

### `POST /agent/ask`

请求示例：

```json
{
  "question": "帮我分析工单 T-1001，看看订单服务异常是不是和它有关"
}
```

返回示例：

```json
{
  "success": true,
  "data": {
    "finalAnswer": "工单 T-1001 与订单服务异常高度相关，建议先检查数据库连接池配置。",
    "toolExecutionLogs": [
      {
        "toolName": "getTicketDetail",
        "success": true
      },
      {
        "toolName": "getServiceHealth",
        "success": true
      },
      {
        "toolName": "searchKnowledgeBase",
        "success": true
      }
    ],
    "evidence": [
      "工单中记录了数据库连接超时",
      "订单服务近5分钟错误数上升",
      "知识库命中了连接池排障文档"
    ],
    "nextActions": [
      "检查连接池参数",
      "确认数据库实例负载",
      "查看最近发布记录"
    ]
  }
}
```

## 9.5 内部处理流程

1. 接收用户问题
2. 构建 AgentContext
3. 由模型决定是否调用工具
4. 执行工具并记录日志
5. 把结果写入上下文
6. 汇总为最终结论
7. 输出证据和下一步建议

## 9.6 工程关键点

- 工具失败时不能编造答案
- evidence 必须来源于工具输出
- 最终回答要和 evidence 对齐
- 第一版只做只读查询，不做写操作
- 第一版不做无限循环推理

## 9.7 MCP 的切入方式

建议先把 `getServiceHealth` 封装为 MCP Server。

理解方式：

- 普通调用：Agent 直接调用 Java service
- MCP 调用：Agent 通过标准协议访问工具

面试时要能讲清：

- 普通调用更直接
- MCP 更适合跨系统、跨语言、标准化集成

## 9.8 最小测试集建议

- 5 条单工具问题
- 5 条双工具问题
- 5 条无数据问题
- 5 条容易误导模型的问题

## 9.9 最小前端演示层：`ticket-agent-demo-web`

目标：让用户看到 Agent 不只是输出最终答案，还会调用工具、汇总证据并给出下一步建议。

推荐目录结构：

```text
ticket-agent-demo-web/
  src/
    api/
      agentApi.ts
    components/
      AgentQuestionBox.tsx
      FinalAnswerCard.tsx
      ToolExecutionPanel.tsx
      EvidencePanel.tsx
      NextActionsPanel.tsx
      StatusBadge.tsx
      ErrorAlert.tsx
      LoadingTimeline.tsx
    pages/
      AgentDemoPage.tsx
    styles/
      variables.css
      app.css
    types/
      agent.ts
    App.tsx
    main.tsx
  package.json
  vite.config.ts
```

页面最小功能：

- 输入用户问题
- 展示最终结论
- 展示工具调用日志
- 展示 evidence
- 展示 next actions
- 空状态、加载态、错误态

核心组件职责：

- `AgentQuestionBox`：问题输入和提交
- `FinalAnswerCard`：最终结论展示
- `ToolExecutionPanel`：工具调用过程和成功/失败状态
- `EvidencePanel`：证据列表
- `NextActionsPanel`：下一步建议
- `StatusBadge`：状态标识
- `ErrorAlert`：错误信息
- `LoadingTimeline`：分析中、调用工具中、汇总中等加载过程

页面风格要求：

- 更像运维/分析面板
- 信息层级清晰
- 结果区比日志区更突出
- 工具调用状态可快速扫读

## 10. 高频 Codex 提示词模板

## 10.1 起骨架

```text
基于 Spring Boot 3 和 Spring AI，生成一个最小可运行项目。
要求：
1. controller/service/config/dto 分层
2. 统一 ApiResponse<T>
3. 使用 application.yml
4. 补基础异常处理
5. 给出 pom.xml 和目录结构
```

## 10.2 做 RAG

```text
在这个 Spring Boot 项目中实现最小 RAG 问答。
要求：
1. 支持文档导入
2. 支持切片和元数据保留
3. 支持检索 top-k
4. 回答时附引用来源
5. 检索为空时明确拒答
6. 先给设计，再分步给代码
```

## 10.2.1 做 RAG 前端

```text
基于 React + Vite + TypeScript，为 knowledge-rag-demo 生成一个最小演示页面。
要求：
1. 页面包含文档导入区、问题输入区、答案展示区、引用来源区
2. 组件拆分为 UploadPanel、QuestionInput、AnswerCard、CitationList、ErrorAlert、LoadingBlock
3. 风格简洁、偏企业演示，不要默认 AI 紫色风格
4. 处理 loading、empty、error 三种状态
5. 先给目录结构，再给代码
```

## 10.3 做 Agent

```text
实现一个基于 Spring AI 的工具调用 Agent。
工具包括：
1. getTicketDetail
2. searchKnowledgeBase
3. getServiceHealth
要求：
1. 支持模型决定是否调用工具
2. 支持多工具结果汇总
3. 工具失败时返回可解释结果
4. 最终回答必须附证据来源
```

## 10.3.1 做 Agent 前端

```text
基于 React + Vite + TypeScript，为 ticket-agent-demo 生成一个最小演示页面。
要求：
1. 页面包含问题输入、最终结论、工具调用日志、证据列表、下一步建议
2. 组件拆分为 AgentQuestionBox、FinalAnswerCard、ToolExecutionPanel、EvidencePanel、NextActionsPanel、StatusBadge
3. 工具调用日志要清晰展示 success/fail
4. 页面风格偏企业运维分析面板，但保持简洁
5. 处理 loading、error、empty 状态
```

## 10.4 做 MCP

```text
把 getServiceHealth 封装成最小 MCP Server。
要求：
1. 给出核心代码
2. 给出如何注册工具
3. 给出调用示例
4. README 写清运行方式
```

## 10.4.1 做前后端联调

```text
根据现有后端接口定义，为前端生成 api 调用层和 TypeScript 类型。
要求：
1. 提取公共请求方法
2. 为 RAG 和 Agent 各自定义独立 api 文件
3. 明确 DTO 类型
4. 所有接口调用都处理错误态
```

## 10.5 做测试

```text
为这个模块补充测试，覆盖：
1. 正常输入
2. 空输入
3. 检索为空
4. 工具超时
5. 模型返回非法结构
6. 返回结果不允许出现未处理异常
```

## 10.6 做 Review

```text
从生产可用性角度 review 这段代码。
重点检查：
1. 幻觉风险
2. 工具失败处理
3. 空结果处理
4. 可测试性
5. 日志和追踪
直接指出问题，不要泛泛而谈。
```

## 11. 本月验收标准

本计划不是“学完了”，而是完成以下验收：

- 能独立讲清 `RAG` 基本链路
- 能独立讲清 `Tool Calling` 基本链路
- 完成两个可运行项目
- 两个项目都具备可演示前端页面
- 能说明 `MCP` 在企业系统中的位置
- 有自己的一套 Codex 协作方式
- 能基于项目应对基础面试

## 12. 推荐执行顺序

必须按以下顺序执行：

1. 先完成 `knowledge-rag-demo`
2. 再完成 `ticket-agent-demo`
3. 最后整理评测、README、面试表达

原因：

- RAG 更简单，适合作为第一阶段
- Agent 项目可复用知识库能力
- 第二个项目天然依赖第一个项目中的检索思路

## 13. 第一条可直接使用的 Codex 提示词

```text
在当前工作区创建一个 Spring Boot 3 + Spring AI 项目 knowledge-rag-demo。
要求：
1. 包结构为 controller/service/config/dto/domain/exception/util
2. 提供统一响应体 ApiResponse<T>
3. 提供 /health、/ingest、/rag/search、/rag/ask 四个接口骨架
4. 先生成可编译的最小代码，不要一次实现全部细节
5. 补充 pom.xml、application.yml 示例和 README 初稿
6. 代码风格偏企业 Java，可维护，命名清晰
```
