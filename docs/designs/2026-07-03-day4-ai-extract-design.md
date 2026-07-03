# Day 4 `/ai/extract` 接口设计

## 1. 背景

`knowledge-rag-demo` 目前已经完成了前 3 个学习步骤：

- `GET /health`
- `GET /ai/ping`
- `POST /ai/summary`
- `POST /ai/classify`

当前项目已经形成了比较明确的实现风格：

- controller 层负责基础输入校验
- `AiService` 作为应用层统一抽象
- `ProviderAiService` 负责 prompt 设计与结果归一化
- 测试同时覆盖 controller 行为和 service 层兜底逻辑

Day 4 的设计应该延续这套风格，而不是重新引入一套新的实现方式。

## 2. 目标

新增结构化抽取接口 `POST /ai/extract`，把用户输入的非结构化文本转换成稳定、可直接被后端系统消费的结构化对象。

这个接口的设计目标要尽量贴近企业真实业务场景中的“文本预处理接口”：

- 输出契约固定
- 枚举值由后端掌控
- 模型输出只视为候选结构，不直接作为最终可信结果
- 结构错误可定位、可测试、可解释

## 3. 范围

本次设计覆盖：

- `/ai/extract` 的请求与响应契约
- 结构化输出对象设计
- prompt 策略
- 归一化与校验策略
- 错误处理行为
- 测试策略

本次设计不覆盖：

- 持久化落库
- 数据库表结构
- RAG 集成
- 前端联动改造
- 全局异常体系重构

## 4. 方案选择

本次考虑 3 种实现路线。

### 方案 A：自由文本返回 + Java 手工解析

让模型返回文本或 JSON 风格文本，再由 Java 手工解析和校验。

优点：

- 初看比较直观
- 不依赖结构化映射能力

缺点：

- 解析脆弱
- 字符串处理噪音多，学习价值偏低
- 不够贴近现代 Spring AI 的结构化输出使用方式

### 方案 B：结构化输出映射 + Java 后置治理

先让模型返回候选结构对象，再由 Java 对结果进行归一化、校验和契约治理，最后对外返回稳定对象。

优点：

- 最符合 Day 4 的 Structured Output 学习目标
- 更贴近企业后端真实集成方式
- 既保留模型能力，又保证接口契约稳定
- 测试层次清晰，适合后续扩展

缺点：

- 比直接字符串解析多一点设计工作

### 方案 C：先做固定字段 demo，内部约束较弱

先实现一个看起来固定、但内部校验较弱的 demo 接口。

优点：

- 实现速度快

缺点：

- 学习价值偏低
- 后面大概率返工
- 不够贴近真实业务系统

### 推荐方案

采用 **方案 B：结构化输出映射 + Java 后置治理**。

这是学习价值、企业真实感、与当前项目风格兼容性最平衡的方案。

## 5. 接口契约

### 接口

`POST /ai/extract`

### 请求示例

```json
{
  "text": "支付接口上线后，部分订单提交失败，用户反馈优先处理，并检查超时日志。"
}
```

### 响应示例

```json
{
  "success": true,
  "data": {
    "title": "订单提交失败排查",
    "category": "BUG",
    "priority": "HIGH",
    "keywords": ["支付接口", "订单提交", "超时日志"]
  },
  "message": "OK"
}
```

## 6. 输出字段设计

### `title`

`title` 由模型生成，用于概括文本的核心问题或主题。

约束规则：

- 必填
- 去除首尾空白
- 不允许换行
- 归一化后长度控制在 1 到 20 个字符
- 尽量去掉无意义的结尾标点

设计意图：

- 保留“抽取”而不是“截取”的学习价值
- 又避免标题过长、过散、不可消费

### `category`

复用 Day 3 已有分类枚举：

- `BUG`
- `FEATURE`
- `QUESTION`
- `COMPLAINT`

约束规则：

- 必填
- 对外只暴露固定枚举值
- 后端允许模型返回 `bug`、`feature` 这类大小写不一致的候选值，再映射为固定枚举
- 未知值视为结构错误

设计意图：

- 保证前后端契约稳定
- 为后续工单分流、知识入库预标注等真实场景做准备

### `priority`

新增固定优先级枚举：

- `LOW`
- `MEDIUM`
- `HIGH`

约束规则：

- 必填
- 后端允许模型返回 `low`、`medium`、`high` 等大小写变体
- 未知值视为结构错误

设计意图：

- 体现结构化抽取不仅是“识别类别”，还要能服务业务排序和处理优先级

### `keywords`

`keywords` 用于抽取后续可用于检索、聚类、展示的关键词。

约束规则：

- 必填
- 最终返回 3 到 5 个关键词
- 每个关键词应为简短短语
- 去掉空项
- 去重
- 尽量控制每个关键词长度在 2 到 10 个字符
- 归一化后少于 3 个有效关键词，视为结构错误

设计意图：

- 兼顾演示价值和可测试性
- 贴近真实业务里“后续可检索、可推荐、可打标”的用途

## 7. 对象分层设计

建议把模型候选结果和接口最终返回对象分开。

### 对外响应对象

这个对象是 API 的稳定契约：

```java
public class ExtractResponse {
    private final String title;
    private final ClassificationCategory category;
    private final ExtractionPriority priority;
    private final List<String> keywords;
}
```

### 内部候选结果对象

这个对象只在 service 内部使用，用于承接模型返回结果：

```java
public class ExtractResult {
    private String title;
    private String category;
    private String priority;
    private List<String> keywords;
}
```

### 为什么要拆两层

这很像企业系统里的常见模式：

- 模型输出不是最终外部契约
- 内部候选对象可以相对宽松
- 对外响应对象必须严格、稳定、可控
- 未来接数据库、消息队列、工单系统时更容易演进

这也是 Day 4 最有学习价值的一点：模型不是最终裁决者，后端代码才是契约守门员。

## 8. 分层职责设计

### Controller 层职责

新增 `AiExtractController`，职责尽量保持轻量，并和现有 controller 风格一致。

职责包括：

- 从请求中读取 `text`
- 拒绝空输入
- 复用现有长度配置做超长校验
- 调用 `AiService.extract(text)`
- 用 `ApiResponse.success(...)` 包装返回结果

### Service 抽象层职责

在 `AiService` 中新增方法：

```java
ExtractResponse extract(String text);
```

### ProviderAiService 职责

`ProviderAiService` 负责两件事：

1. 调用模型获取候选结构结果
2. 对候选结果做归一化、校验和治理，再产出最终响应对象

这部分是 Day 4 学习价值最高的地方，因为它体现了企业系统里常见的模式：

- 模型负责“生成候选结构”
- 应用层负责“把候选结构治理成可交付契约”

## 9. Prompt 设计

system prompt 不应该只是“帮我抽字段”，而应该明确告诉模型，它现在处在一个企业应用的文本预处理场景里。

建议 system prompt 风格如下：

```text
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
```

user prompt 建议保持简单：

```text
请从以下文本中提取结构化信息：

%s
```

### 这套 prompt 的设计意图

- 强调固定结构，而不是自由发挥
- 强调后端消费场景，而不是自然语言回答场景
- 鼓励模型在语义模糊时保守输出，而不是直接失败

这里有一个偏企业真实的取舍：

- 语义不确定，不一定报错
- 结构不合法，才应该报错

也就是说：

- 如果文本比较模糊，模型仍然应该尽量给出保守分类，如 `QUESTION` 或 `MEDIUM`
- 如果模型返回了非法值，比如 `priority = urgent`，才视为结构错误

## 10. 归一化与校验策略

建议把后置治理拆成 4 个聚焦的方法。

### `normalizeTitle`

职责：

- 去首尾空格
- 去换行
- 清理明显格式噪音
- 去除无意义结尾标点
- 校验长度是否合法

非法场景：

- 归一化后为空
- 归一化后长度超限

### `parseCategory`

职责：

- 把大小写不一致的候选值映射到固定枚举
- 拒绝非法分类

示例：

- `bug` -> `BUG`
- `feature` -> `FEATURE`

非法场景：

- 返回值不在允许范围内

### `parsePriority`

职责：

- 把大小写不一致的候选值映射到固定优先级枚举
- 拒绝非法优先级

示例：

- `high` -> `HIGH`
- `medium` -> `MEDIUM`

非法场景：

- 返回值不在允许范围内

### `normalizeKeywords`

职责：

- 去空白
- 去空项
- 去重
- 限制数量为 3 到 5 个
- 保持短语简洁可读

非法场景：

- 模型没有返回关键词列表
- 归一化后有效关键词不足 3 个

## 11. 错误处理设计

### 输入错误

以下情况继续返回 `400`，行为保持和现有 controller 一致：

- `text` 为空
- `text` 超过配置长度

### 模型结构错误

以下情况属于“模型返回结构不合法”：

- `category` 非法
- `priority` 非法
- `title` 归一化后为空
- `keywords` 归一化后不满足最少数量要求

对当前这个学习型项目，我建议保留清晰、可定位的错误信息，而不是一律模糊成通用异常。

推荐错误信息风格：

- `invalid extract category: urgent-bug`
- `invalid extract priority: p0`
- `invalid extract keywords: empty result`
- `invalid extract title: blank after normalization`

这样设计的原因是：

- Day 4 的验收目标强调“输出错误可定位”
- 当前阶段更需要理解问题来源，而不是过早做完全隐藏式错误包装

## 12. 测试策略

### Controller 测试

建议覆盖：

- 正常输入返回 `200`
- 返回体中包含固定字段
- 空输入返回 `400`
- 超长输入返回 `400`

### Service 测试

建议覆盖：

- 模型返回合法结构时，能正确映射成固定枚举和关键词列表
- `priority` 大小写变体能归一化成功
- 重复关键词会被去重
- 非法 `category` 会抛出明确异常
- 非法 `priority` 会抛出明确异常
- 空 `title` 会抛出明确异常
- 关键词数量不足会抛出明确异常

### 为什么这组测试有学习价值

真正有价值的不是“接口能返回 200”，而是验证下面这件事：

- 模型轻微跑偏时，应用层能纠正
- 模型结构真的非法时，系统能阻止坏数据流出
- 对外契约不会因为模型风格波动而漂移

这比单纯证明“模型被调用了一次”更接近企业后端的真实要求。

## 13. 对后续阶段的复用价值

这套设计不只是服务于 Day 4。

它对后面的能力建设也有直接复用价值，比如：

- RAG 中 citation 结构治理
- Agent 中 evidence 结构治理
- Tool Calling 结果归一化
- 最终对前端暴露的稳定响应对象设计

换句话说，Day 4 是这个项目里第一次系统性地把“模型输出治理”落到代码设计层面。

## 14. 文件落点建议

预计新增：

- `controller/AiExtractController.java`
- `dto/ExtractRequest.java`
- `dto/ExtractResponse.java`
- `dto/ExtractResult.java`
- `dto/ExtractionPriority.java`

预计修改：

- `service/AiService.java`
- `service/ProviderAiService.java`

实现时的一个合理取舍：

- `category` 直接复用已有 `ClassificationCategory`
- 不额外重复定义第二套分类枚举

这样更符合当前项目“小闭环、少重复、可维护”的原则。

## 15. 验收标准

Day 4 完成的标准是：

- `/ai/extract` 返回固定 JSON 字段
- `category` 和 `priority` 是固定枚举
- `keywords` 经过归一化后稳定输出
- 模型结构错误可定位
- controller 和 service 测试覆盖主要分支

## 16. 最终结论

`/ai/extract` 应该被实现为一个“强结构化、强约束、后端负责治理”的接口：

- 枚举优先
- `title` 由模型生成短标题
- `keywords` 固定 3 到 5 个
- 模型输出只作为候选结构
- Java 应用层负责契约安全

这套设计既符合企业后端真实业务接口的思路，也能最大化 Day 4 的学习价值。
