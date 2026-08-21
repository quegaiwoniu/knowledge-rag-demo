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
- `全链路可观测性`（traceId、耗时明细、prompt 调试模式）——贯穿全计划的新增主线
- `边界防御设计`（阈值过滤、超时控制、失败 fallback、拒答原因细分）

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
- 一份全景复盘清单（分主题沉淀每类知识点）

## 3a. 全景复盘清单（推荐每周日更新）

全景复盘的目的是**防止学完就忘**。每完成一个主题，把关键产出物填入对应清单，30 天结束时你就有了一份完整的知识索引。

| 主题 | 产出物 | 状态 |
|---|---|---|
| Spring AI 基础能力 | 接口清单 + 关键配置 + 测试策略 | □ |
| 结构化输出 | 枚举设计 + 解析校验逻辑 | □ |
| Tool Calling | 工具注册方式 + 结果回传模式 | □ |
| 可观测性 | traceId 集成 + 耗时明细 + prompt 调试模式 | □ |
| RAG 链路 | Ingestion → Chunking → Embedding → Retrieval → QA → 拒答 | □ |
| 边界防御 | 阈值过滤 + 超时控制 + 失败 fallback + 拒答原因细分 | □ |
| Agent 编排 | 单工具 → 多工具串联 → 失败处理 | □ |
| MCP | 原生 Tool vs MCP 对比 + 最小 Server 实现 | □ |
| 前端演示 | 证据高亮 + Tool Calling 时间线 + 状态全覆盖 | □ |
| 评测 | 问题集 + 结果记录 + 错误分类方法 | □ |
| 面试准备 | 项目自述 + 问答提纲 + 演示脚本 | □ |

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
- 【新增】记录工具调用的入参、出参和执行耗时（为后续可观测性铺垫）

验收：

- 问工具相关问题时，会触发工具
- 最终回答来自工具结果
- 能说出「模型调了哪个工具、传了什么参数、工具返回了什么」

### Day 6：可观测基建 + 补测试与重构

目标：

- 在现有项目中引入最小可观测性基础设施

任务：

- 新增 `TraceIdFilter`：每个 HTTP 请求自动生成 traceId，通过响应头返回
- 在关键 Service 方法中加入 traceId 日志（ingestion / chunking / tool call 等）
- 统一异常处理
- 抽公共 prompt builder
- 增加日志
- 拆分过长方法
- 补 controller/service 测试

验收：

- 每个接口响应头携带 X-Trace-Id
- 通过 traceId 能在日志中串联一次请求的完整路径
- 代码结构清晰
- 测试能覆盖主要分支

### Day 7：阶段复盘

输出一页笔记，至少回答：

- 结构化输出为什么比自由文本更适合企业系统
- Tool Calling 和普通聊天接口的区别
- 哪些模块还讲不清楚
- 下周 RAG 项目需要准备什么文档

## 第 2 周：RAG 项目

目标：完成一个更接近企业真实场景的知识库问答系统，而不是只做一个“能聊天”的 demo。

本周核心学习价值：

- 文档可追溯：每个答案都能追到原始文档、章节和 chunk
- 切片可解释：能看懂为什么这样切、哪里可能切坏
- 检索可调试：能通过 `/rag/search` 判断问题出在召回还是生成
- 回答有引用：citation 必须来自检索结果，不能让模型凭空编来源
- 资料不足会拒答：没有足够上下文时明确拒答，而不是硬编
- 评测可复现：用固定问题集持续观察 RAG 质量
- 可观测可定位：traceId 贯穿链路 + 耗时明细 + prompt 调试模式
- 边界有防御：score 阈值过滤 + 超时控制 + 拒答原因细分

本周最终闭环：

```text
sample docs -> ingestion -> chunking -> embedding -> retrieval -> answer -> citations -> evaluation
```

### Day 8：企业语料与评测集设计

价值点：

- 企业 RAG 的质量从语料开始，不是从模型开始
- 语料如果太散、太假、太短，后面检索和问答都学不到真实问题
- 先设计评测问题，可以避免后面只为了接口 200 而写代码

建议业务域：

- 优先选择一个固定领域，例如“订单与支付排障”
- 这个领域后续还能复用到 Agent / 工单分析项目里，学习收益更高

任务：

- 准备 10-15 篇同一领域 Markdown 文档
- 准备 15-20 条候选问题
- 把问题分成四类：
  - 直接命中
  - 近义表达
  - 资料中没有答案
  - 容易混淆的问题

建议文档类型：

- FAQ
- 排障手册
- 接口说明
- 配置说明
- 已知问题 / 事故复盘

产出：

- `docs/sample-docs/`
- `docs/evaluation/rag-questions.md`
- `docs/evaluation/rag-dataset.md`

验收：

- 文档属于同一个业务域
- 每篇文档都有标题和清晰章节
- 每个可回答问题都标记期望来源文档
- 至少包含 3 条“应该拒答”的问题

Codex 话术：

```text
我们现在进入 Week 2 RAG 项目，目标是做一个企业知识库问答系统。
请帮我设计一组“订单与支付排障”领域的企业风格样例语料。
要求：
1. 规划 docs/sample-docs 目录下 10 篇 Markdown 文档
2. 每篇文档要有标题、章节、具体事实、排障步骤和可引用内容
3. 生成 15 条评测问题，分为直接命中、近义表达、无答案拒答、容易混淆四类
4. 暂时不要写后端代码
5. 输出文档清单、问题清单和每个问题的期望来源
```

复查话术：

```text
请从企业 RAG 的角度 review 这批样例语料。
重点检查：
1. 文档是否有明确的业务价值
2. 内容是否能支撑 citation
3. 问题集是否覆盖直接命中、近义表达、无答案和混淆问题
4. 哪些文档太空泛或太相似
请给出具体修改建议，不要泛泛而谈。
```

### Day 9：文档导入与元数据建模

目标：

- 完成 ingestion 基础流程，并建立企业 RAG 所需的可追溯 metadata

任务：

- 实现 Markdown 文件读取
- 提取标题和基础内容
- 保存文档级 metadata：
  - `docId`
  - `fileName`
  - `sourcePath`
  - `title`
  - `contentHash`
  - `ingestedAt`
- 增加导入结果统计

验收：

- 能批量读取文档
- 缺失文件有清晰错误
- 重复文档可以通过 `contentHash` 识别
- metadata 足够支撑后续 citation

建议接口：

```text
POST /rag/ingest
```

Codex 话术：

```text
实现 Day 9 文档导入能力。
要求：
1. 在现有 Spring Boot 项目中新增 RAG ingestion service
2. 从 docs/sample-docs 读取 Markdown 文件
3. 保留 docId、fileName、sourcePath、title、contentHash、ingestedAt
4. 新增 POST /rag/ingest
5. 暂时不要实现 chunking 和 vector search
6. 补充成功导入、文件不存在、重复 contentHash 的测试
7. 沿用当前 controller/service/config/dto 分层风格
```

复查话术：

```text
请 review 文档导入实现。
重点检查：
1. metadata 是否足够支撑企业级可追溯
2. 文件路径处理是否安全清晰
3. 重复导入行为是否可解释
4. 测试是否覆盖主要分支
请先指出风险，再给修改建议。
```

### Day 10：文档切片与调试视图

目标：

- 实现 chunking，并让切片结果可检查、可解释

任务：

- 固定 chunk size
- 增加 overlap
- 保留原始元数据
- 尽量保留章节标题
- 增加 chunk 调试接口

验收：

- chunk 可追溯
- 分片粒度合理
- chunk 顺序稳定
- 能直接查看每篇文档被切成了什么

建议 chunk 字段：

```text
chunkId
docId
fileName
sourcePath
title
sectionTitle
chunkIndex
content
contentHash
tokenEstimate
```

建议接口：

```text
GET /rag/chunks
```

Codex 话术：

```text
实现 Day 10 文档切片能力。
要求：
1. 将已导入的 Markdown 文档切成可追溯 chunks
2. 保留 docId、fileName、sourcePath、title、sectionTitle、chunkIndex、content
3. 使用简单可配置的 chunk size 和 overlap
4. 增加 GET /rag/chunks 调试接口
5. 暂时不要加入 embedding
6. 添加测试验证 chunk metadata、chunk 顺序和空文档行为
```

复查话术：

```text
请从企业 RAG 角度 review chunking 实现。
重点检查：
1. chunk 是否能追溯回原文档和章节
2. 分片是否容易破坏语义
3. chunk size 和 overlap 是否容易解释
4. GET /rag/chunks 是否足够用于排查召回问题
```

### Day 11：向量化、入库与索引重建

目标：

- 完成 embedding + vector store，并支持可重复重建索引

任务：

- 接入 embedding
- 完成 chunk 入库
- 抽离 ingestion / chunking / indexing pipeline
- 增加索引重建能力
- 增加索引状态查询

验收：

- 文档可重复导入
- 检索前准备流程完整
- 索引可从样例文档重建
- 能看到文档数、chunk 数、向量化数量
- embedding 供应商异常时有可读错误
- 【新增】索引重建时记录每步耗时（ingestion / chunking / embedding 各多少 ms）

建议接口：

```text
POST /rag/index/rebuild
GET /rag/index/status
```

建议状态字段：

```text
documentCount
chunkCount
embeddedChunkCount
lastRebuildAt
```

Codex 话术：

```text
实现 Day 11 embedding 与向量索引重建能力。
要求：
1. 优先使用 Spring AI 已支持的 embedding / vector store 能力
2. 新增索引重建接口，将样例文档重新导入、切片、向量化
3. 新增索引状态接口，返回 documentCount、chunkCount、embeddedChunkCount、lastRebuildAt
4. 密钥只能来自环境变量，不能写进配置文件
5. 外部模型调用相关逻辑要便于测试隔离
6. 添加不依赖真实模型调用的 orchestration 测试
```

复查话术：

```text
请 review 向量索引设计。
重点检查：
1. 索引是否可重建
2. 状态是否足够调试
3. 是否有隐藏的外网依赖假设
4. 是否存在密钥泄漏风险
5. 测试是否避开了真实模型调用的不稳定性
```

### Day 12：纯检索接口与召回调试

目标：

- 完成 `/rag/search`，把它做成 RAG 的调试窗口

任务：

- 输入 query
- 返回 top-k chunks
- 附带 source metadata
- 返回 score
- 返回 chunk 内容摘要
- 处理空 query 和非法 topK
- 【新增】增加 traceId 记录，每次检索打出耗时明细（embedding 耗时 / SQL 查询耗时 / 结果组装耗时）
- 【新增】增加 score 阈值过滤：低于 `app.rag.min-score-threshold` 的 chunk 不返回

验收：

- 检索结果相关
- source 信息完整
- 每个 hit 都有 score
- 每个 hit 都能追溯到 fileName、sectionTitle、chunkIndex
- 至少手工验证 5 条评测问题
- 【新增】能通过 traceId 定位一次检索的耗时分布
- 【新增】低分结果被过滤的行为可解释

建议响应字段：

```text
query
topK
hits[]
score
content
fileName
sourcePath
sectionTitle
chunkIndex
```

Codex 话术：

```text
实现 Day 12 纯检索接口 /rag/search。
要求：
1. 新增 POST /rag/search
2. 请求包含 query 和 topK
3. 返回带 score 的 chunk hits
4. 每个 hit 必须包含 fileName、sourcePath、sectionTitle、chunkIndex、content snippet
5. 暂时不要生成最终答案
6. 增加 blank query、非法 topK、正常检索响应映射的测试
```

复查话术：

```text
请把 /rag/search 当成 RAG 调试接口来 review。
重点检查：
1. 开发者能否通过响应判断召回是否正确
2. score 是否可见
3. metadata 是否足够生成 citation
4. 空结果行为是否清晰
```

### Day 13：带引用和拒答的问答接口

目标：

- 完成 `/rag/ask`，并强制回答基于检索证据

任务：

- 根据 query 检索文档片段
- 构造回答 prompt
- 返回答案和引用
- 处理空检索结果
- 返回 `enoughContext`
- 返回 retrieved chunks 供调试
- 明确禁止模型编造 citation
- 【新增】拒答原因细化：`refusalReason` 字段区分「检索为空 / 最高分低于阈值 / 内容不相关」
- 【新增】增加 Prompt 调试模式：当 `app.ai.prompt-debug=true` 时，接口额外返回最终发给模型的完整 prompt 文本

验收：

- 回答可用
- 有 citation
- 资料不足时明确拒答
- citation 必须来自 retrieved chunks
- answer 和 citations 分字段返回
- 无上下文问题返回 `enoughContext=false`
- 【新增】能通过 `refusalReason` 精确判断拒答原因
- 【新增】prompt-debug 模式可辅助排查 prompt 质量问题

建议响应结构：

```json
{
  "answer": "...",
  "enoughContext": true,
  "citations": [],
  "retrievedChunks": []
}
```

Codex 话术：

```text
实现 Day 13 RAG 问答接口 /rag/ask。
要求：
1. 复用 /rag/search 的检索逻辑
2. 根据 retrieved chunks 构造 grounded prompt
3. 返回 answer、enoughContext、citations、retrievedChunks
4. 检索不到有效上下文时直接拒答，不要让模型猜
5. citation 只能来自 retrieved chunks
6. 添加 no-context refusal、citation construction、response mapping 的测试
```

复查话术：

```text
请从幻觉风险角度 review /rag/ask。
重点检查：
1. 模型是否可能引用未检索到的来源
2. 无上下文时是否明确拒答
3. answer 和 citations 是否清晰分离
4. prompt 是否足够约束模型但不过度复杂
```

### Day 14：RAG 评测与最小前端联调

任务：

- 准备 15-20 条问题
- 手工校验结果
- 分类错误原因
- 记录每个失败样例的原因
- 联调前端最小 RAG 演示区

前端第一版：

- 复用现有 `knowledge-rag-demo-web`
- 完成索引重建按钮
- 完成问题输入区
- 完成答案展示区
- 完成引用来源区
- 完成 retrieved chunks 预览区
- 展示 `enoughContext`
- 处理 loading / error / empty 状态
- 【新增】答案下方的「证据高亮」折叠面板：展示每条引用的 fileName、sectionTitle、score，点击可展开原文片段
- 【新增】traceId 展示：页面右上角显示最近一次请求的 traceId，方便后端排查

至少区分以下错误类型：

- 召回失败
- 召回偏题
- 回答幻觉
- 引用错误
- 拒答失败

产出：

- `docs/evaluation/rag-results.md`
- 前端可完成一次完整 RAG demo 流程

验收：

- 15-20 条评测问题完成手工记录
- 每个失败样例都有原因分类
- 页面能展示答案、引用、召回片段、上下文是否充足
- 能解释一次回答为什么可信，或者为什么拒答

Codex 话术：

```text
实现 Day 14 RAG 评测和最小前端联调。
要求：
1. 新增 docs/evaluation/rag-results.md 记录评测结果
2. 扩展现有 React 工作台，而不是创建新前端项目
3. 页面展示 answer、enoughContext、citations、retrievedChunks
4. UI 保持企业调试面板风格，重点突出答案、引用和召回片段
5. 不要过度实现文档上传，除非后端已经支持
6. 完成后运行后端测试和前端 build
```

复查话术：

```text
请 review RAG 前端和评测结果。
重点检查页面是否能让使用者看懂：
1. 问了什么问题
2. 生成了什么答案
3. 哪些来源支撑答案
4. 上下文是否充足
5. 失败样例下一步该怎么改
```

### 第 2 周固定 Codex 协作节奏

每天建议按这个顺序和 Codex 协作：

1. 让 Codex 复述当天目标和验收标准
2. 让 Codex 先检查现有代码结构，不要直接写代码
3. 让 Codex 给最小实现计划
4. 只实现当天闭环，不顺手扩张范围
5. 跑 focused tests，再跑必要的回归测试
6. 让 Codex 总结今天改了什么、你应该重点读哪些文件

通用开工话术：

```text
我们正在做 knowledge-rag-demo 的第 2 周 RAG 项目。
今天目标是 Day X：[当天目标]。
请先检查当前后端和前端结构，再给出最小实现计划。
优先考虑企业 RAG 的可追溯、引用、拒答、可调试和可测试。
不要做无关重构。
```

通用实现话术：

```text
按刚才计划开始实现。
要求：
1. 只做今天 RAG 目标相关改动
2. 关键代码加必要注释，方便我阅读
3. 沿用现有 API 风格、DTO 风格和测试风格
4. 不提交密钥和本地配置
5. 先跑 focused tests，再按风险决定是否跑全量测试
```

通用 review 话术：

```text
请从企业 RAG 角度 review 今天的实现。
优先找 bug、缺失测试、可追溯不足、幻觉风险、错误处理不清晰的问题。
请给具体文件级反馈，不要只给泛泛建议。
```

通用收尾话术：

```text
请总结今天的 RAG 进展。
包括：
1. 实现了什么
2. 我应该重点阅读哪些文件
3. 哪些测试通过了
4. 还剩什么风险
5. 明天应该从哪里开始
```

每天复盘问题：

- 今天构建了 RAG 链路的哪一环？
- 有什么证据证明它能工作？
- 如果放到企业环境，最先可能坏在哪里？
- 我能不能不看代码讲清楚这个模块？
- 我能不能指出答案来自哪里？
- 系统能不能安全地说“资料不足，无法回答”？

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

### Day 16：普通 Tool Calling Agent（含思维链日志）

目标：

- 不使用 MCP，先完成 Agent 基础流程

任务：

- 构建 `AgentOrchestratorService`
- 根据用户问题决定是否调用工具
- 调用单工具并汇总结果
- 【新增】记录每次工具调用的思维链日志：`模型收到了什么 → 决定调哪个工具 → 传入什么参数 → 工具返回了什么 → 最终回答`
- 【新增】增加 traceId 贯穿一次 Agent 请求的全流程

验收：

- 能回答单工具类问题
- 最终回答基于工具输出
- 【新增】前端能看到工具调用的全过程日志（时间线视图）

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

### Day 18：失败处理 + 边界防御

目标：

- 提高可靠性，补全边界防御

任务：

- 设计失败场景：
  - 工具超时
  - 工具无数据
  - 参数非法
  - 目标工单不存在
- 补异常处理
- 补错误返回
- 增加日志
- 【新增】工具调用的熔断意识：连续 N 次失败时记录告警级别的日志
- 【新增】前端 Agent 时间线视图：展示每一步 tool call 的入参、出参、耗时和成功/失败状态

验收：

- 工具失败时不瞎编
- 有明确错误说明
- 【新增】前端能通过时间线快速定位哪一步失败

### Day 19：第一个 MCP Server（原生 Tool → MCP 对比实验）

目标：

- 将 `getServiceHealth` 封成 MCP Server，并与原生 @Tool 调用做对比

任务：

- 建立最小 MCP server
- 给出注册和调用示例
- 补 README
- 【新增】整理「原生 Tool vs MCP」对比表，记录两种方式的代码量、启动方式、调用方式差异

验收：

- 能通过 MCP 暴露能力
- Agent 能消费该能力
- 【新增】能说清「什么场景用原生 Tool、什么场景用 MCP」

### Day 20：第二个 MCP 工具

目标：

- 封装第二个工具为 MCP

建议：

- `searchKnowledgeBase`
- 或 `getTicketDetail`

任务：

- 统一 schema
- 统一返回结构
- 【新增】对比两个 MCP 工具的代码量，记录「MCP 的标准化优势」体现在哪里

验收：

- 多个工具的风格一致

### Day 21：阶段复盘 + 前端 Agent 时间线

输出一页笔记，回答：

- Tool Calling 和 MCP 的区别
- 为什么 Java 企业系统适合通过 MCP 暴露工具
- Agent 项目最大风险是什么
- 当前系统如何防止幻觉

同时补充前端 Agent 时间线：

- 创建 `ticket-agent-demo-web`
- 完成问题输入区
- 完成最终结论区
- 完成工具调用日志时间线区（展示每步：入参、出参、耗时、成功/失败）
- 完成证据区和下一步建议区
- 处理 loading / error / empty 状态

前端时间线设计要求：

```text
🕐 09:32:15 → 用户提问 "帮我分析工单 T-1001"
🕑 09:32:16 → 模型决定调用 getTicketDetail("T-1001") ✓ (45ms)
🕒 09:32:17 → 模型决定调用 getServiceHealth("order-service") ✓ (32ms)
🕓 09:32:19 → 模型汇总结果并生成最终回答
```

每个时间线条目可展开查看详细信息。

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

### Day 25：重构 Agent 项目 + 可观测性巩固

任务：

- 统一 tool schema
- 统一 error model
- 确保 traceId 贯穿 Agent 请求全流程
- 增强日志可读性（工具调用的入参/出参/耗时结构化输出）
- 联调 `ticket-agent-demo-web`
- 清理工具日志时间线区与 evidence 展示逻辑

### Day 26：补 README 和架构说明 + 全景复盘清单

任务：

两个项目的 README 至少包含：

- 项目目标
- 技术栈
- 核心流程
- 关键难点
- 风险控制
- 运行方式
- 示例输入输出

全景复盘清单更新：

- 打开 `studyplan.md` 第 3a 节的全景复盘清单
- 把过去 4 周每个主题的状态标记为已完成
- 对每个主题，用 2-3 句话写下你认为最核心的收获
- 如果某个主题讲不清楚，标记为待复习，安排时间回看

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

## 14. 变更记录

| 日期 | 版本 | 变更内容 |
|---|---|---|
| 初始版 | v1.0 | 原始 30 天计划 |
| 当前版 | v2.0 | 融入可观测性主线（traceId/耗时明细/prompt 调试）、边界防御（阈值/超时/拒答原因细分）、前端证据高亮与 Agent 时间线、MCP 对比实验、全景复盘清单 |
```
