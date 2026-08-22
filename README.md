# knowledge-rag-demo

一个基于 `Spring Boot 3 + Spring AI` 的最小后端示例项目，用来演示：

- 健康检查接口
- AI 联调接口
- 文本总结接口
- 统一响应体设计
- 真实模型调用与 stub 测试隔离

这个项目当前已经完成 **最小 RAG 后端闭环**：
文档导入 → Markdown 切片 → embedding → pgvector 检索 → grounded QA → 引用与拒答。

## 当前功能

- `GET /health`
- `GET /ai/ping?message=...`
- `POST /ai/summary`
- `POST /ai/classify`
- `POST /ai/extract`
- `POST /ai/tool-call`
- `POST /rag/ingest`
- `GET /rag/chunks`
- `POST /rag/index/rebuild`
- `GET /rag/index/status`
- `GET /rag/index/search`
- `POST /rag/ask`
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
      base-url: https://api.longcat.chat/openai
      api-key: ${OPENAI_API_KEY:}
      chat:
        options:
          model: LongCat-2.0
      embedding:
        base-url: ${OPENAI_EMBEDDING_BASE_URL:https://ai.lazymo.qzz.io}
        api-key: ${OPENAI_EMBEDDING_API_KEY:${OPENAI_API_KEY:}}
        options:
          model: text-embedding-3-small

app:
  ai:
    provider: longcat-openai-compatible
    default-message: Hello from knowledge-rag-demo
    summary-max-input-length: 1200
    use-stub-service: false
```

说明：
- `use-stub-service: false` 表示运行时走真实模型
- 真实密钥不要写进 `application.yml`，请通过环境变量 `OPENAI_API_KEY` 注入
- 索引重建会调用 embedding 接口；如果聊天供应商不支持 `/v1/embeddings`，请通过 `OPENAI_EMBEDDING_BASE_URL` 和 `OPENAI_EMBEDDING_API_KEY` 单独配置支持 embedding 的 OpenAI 兼容供应商
- 如果供应商页面显示的 API 端点是 `https://ai.lazymo.qzz.io/v1`，Spring AI 配置里要填根地址 `https://ai.lazymo.qzz.io`，不要带最后的 `/v1`
- 测试目录下有单独的 `src/test/resources/application.yml`，用于让自动化测试稳定运行

### 在 IDEA 里配置 `OPENAI_API_KEY`

推荐直接在 IDEA 的 Spring Boot 启动配置里加环境变量，这样最适合本地开发，也不会把密钥写进仓库。

操作步骤：

1. 打开 IDEA 顶部菜单：`Run -> Edit Configurations...`
2. 选中你的后端 Spring Boot 启动项
3. 找到 `Environment variables`
4. 点击右侧编辑按钮，新增一条：

```text
OPENAI_API_KEY=你的真实key
OPENAI_EMBEDDING_API_KEY=你的embedding供应商key
```

5. 保存配置后，再从 IDEA 启动后端

补充说明：

- 现在项目里的配置是：

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:}
      embedding:
        api-key: ${OPENAI_EMBEDDING_API_KEY:${OPENAI_API_KEY:}}
```

- 这表示程序启动时会优先读取环境变量 `OPENAI_API_KEY`
- 重建索引时会优先读取环境变量 `OPENAI_EMBEDDING_API_KEY`；如果不配置，就回退使用 `OPENAI_API_KEY`
- 如果这些变量没配，程序就拿不到真实 key
- 不要把真实 key 再写回 `application.yml`

如果你只是临时想在命令行里启动，也可以用 PowerShell 这样写：

```powershell
$env:OPENAI_API_KEY="你的真实key"
$env:OPENAI_EMBEDDING_API_KEY="你的embedding供应商key"
mvn -s .mvn/settings.xml spring-boot:run
```

## pgvector 向量库规则

项目使用真实 PostgreSQL + pgvector 作为向量库，固定规则如下：

- 数据库：`ragdb`
- schema：`public`
- 表名：`vector_store`
- 向量维度：`1536`
- 距离：`COSINE_DISTANCE`
- 索引：`HNSW`
- 重建策略：每次 `/rag/index/rebuild` 先清空 `public.vector_store`，再重新写入当前文档切片的 embedding

Spring AI 会在后端启动时自动执行 pgvector 初始化：

```sql
CREATE EXTENSION IF NOT EXISTS vector;
CREATE TABLE IF NOT EXISTS public.vector_store (...);
CREATE INDEX IF NOT EXISTS spring_ai_vector_index ...;
```

如果你已经有本地 PostgreSQL，请确认它支持 pgvector 扩展，并且连接参数与配置一致：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${PGHOST:localhost}:${PGPORT:5432}/${PGDATABASE:ragdb}
    username: ${PGUSER:rag}
    password: ${PGPASSWORD:ragpass}
```

如果你没有安装 pgvector，可以用项目自带的 Docker Compose 启动一个：

```powershell
docker compose -f docker-compose.pgvector.yml up -d
```

这等价于你当前使用的命令：

```powershell
docker run -d `
  --name pgvector `
  -e POSTGRES_USER=rag `
  -e POSTGRES_PASSWORD=ragpass `
  -e POSTGRES_DB=ragdb `
  -p 5432:5432 `
  -v pgvector_data:/var/lib/postgresql/data `
  pgvector/pgvector:pg17
```

启动后可用下面的 SQL 检查：

```sql
SELECT extname FROM pg_extension WHERE extname = 'vector';
SELECT COUNT(*) FROM public.vector_store;
```

注意：请在数据库客户端里展开 `ragdb -> public -> 表`。如果你看的是 `postgres -> public`，那里为空是正常的，因为应用不会写入 `postgres` 数据库。表只会在后端成功启动并连接到 `ragdb` 后创建；数据只会在调用 `/rag/index/rebuild` 并且 embedding 成功后写入。

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

## 当前阶段与下一步

当前 RAG 阶段已经完成：

- 文档导入与重复检测
- Markdown 章节感知切片
- pgvector 向量索引重建
- 相似度阈值过滤
- 基于证据的问答
- 引用来源与召回片段返回
- 无结果/低相似度拒答
- Trace ID、耗时日志和 Prompt 调试开关

下一步建议：

1. 建立固定的 RAG 评测集
2. 记录召回准确率、拒答准确率和引用正确率
3. 完成 Tool Calling / MCP Agent 项目
4. 补充生产环境异常处理、认证和持久化任务状态
