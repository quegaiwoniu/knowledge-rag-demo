# knowledge-rag-demo

一个基于 `Spring Boot 3 + Spring AI` 的最小后端示例项目，用来演示：

- 健康检查接口
- AI 联调接口
- 文本总结接口
- 统一响应体设计
- 真实模型调用与 stub 测试隔离

这个项目当前定位是 **RAG 后端骨架的第一步**。  
我们先把最小可运行链路跑通，后面再继续补 `/rag/search`、`/rag/ask`、向量检索等能力。

## 当前功能

- `GET /health`
- `GET /ai/ping?message=...`
- `POST /ai/summary`
- 统一响应体：`ApiResponse<T>`
- `application.yml` 管理模型参数
- 测试环境使用 stub，运行环境使用真实模型

## 技术栈

- Java 17+
- Spring Boot 3.5.0
- Spring AI 1.0.0
- Maven

## 目录结构

```text
src/main/java/com/example/knowledgeragdemo
├─ controller   // 对外暴露的接口层
├─ service      // AI 能力抽象与实现
├─ config       // Spring 配置与应用自定义配置
└─ dto          // 请求体、响应体
```

## 接口说明

### 1. 健康检查

`GET /health`

作用：
- 用于确认服务是否正常启动
- 便于前端联调和本地排查

### 2. AI 联调

`GET /ai/ping?message=hello`

作用：
- 快速验证前后端链路是否通
- 快速验证当前模型供应商配置是否可用

### 3. 文本总结

`POST /ai/summary`

请求示例：

```json
{
  "text": "Spring AI 可以帮助 Java 服务以统一抽象接入不同模型供应商，并快速搭建摘要、问答和工具调用能力。"
}
```

返回示例：

```json
{
  "success": true,
  "data": {
    "summary": "Spring AI 可以帮助 Java 服务以统一抽象接入不同模型供应商，并快速搭建摘要、问答和工具调用能力。",
    "originalLength": 55,
    "truncated": false
  },
  "message": "OK"
}
```

校验规则：
- `text` 不能为空
- `text` 不能超过 `app.ai.summary-max-input-length`

## 模型配置

项目当前按 OpenAI 兼容接口方式接入供应商，配置文件在：

- [application.yml](./src/main/resources/application.yml)

关键配置示例：

```yaml
spring:
  ai:
    model:
      chat: openai
    openai:
      base-url: https://api.zetatechs.com
      api-key: your-real-api-key
      chat:
        options:
          model: gpt-4.1-mini

app:
  ai:
    provider: zetatechs-openai-compatible
    default-message: Hello from knowledge-rag-demo
    summary-max-input-length: 1200
    use-stub-service: false
```

说明：
- `use-stub-service: false` 表示运行时走真实模型
- 测试目录下有单独的 `src/test/resources/application.yml`，用于让自动化测试稳定运行

## 本地启动

### 1. 运行测试

```bash
mvn -s .mvn/settings.xml test
```

### 2. 启动服务

```bash
mvn -s .mvn/settings.xml spring-boot:run
```

默认地址：

- 后端服务：[http://localhost:8080](http://localhost:8080)

## 测试策略说明

当前项目把“自动化测试”和“真实模型调用”分开处理：

- **自动化测试**：走 `StubAiService`
- **本地运行/联调**：走 `ProviderAiService`

这样做的目的：

- 测试更稳定
- 不依赖外部网络波动
- 不让模型输出波动影响单测结果

## 下一步建议

这个仓库后面最适合继续补这些能力：

1. `/rag/search` 骨架
2. `/rag/ask` 骨架
3. 文档导入与切片
4. 向量检索
5. 引用来源返回
6. 拒答机制

