# Day 4 AI 工作台重构与本机 JDK 稳定启动 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 把当前前端首页重构成一个可扩展、可演示、可继续承接 Day 5 / Day 6 的 AI 工作台，并补齐固定使用本机稳定 JDK 的后端启动脚本与中文说明文档。

**Architecture:** 前端保留单页应用形态，但把首页从“步骤演示页”重构为“AI 工作台”，使用统一的能力卡片组织 ping、summary、extract、rag placeholder，并把 `/ai/extract` 做成本次最完整的业务能力卡。后端启动不改业务代码，只在后端项目内新增 `scripts` 启动脚本，由脚本在当前进程显式设置 `JAVA_HOME` 与 `PATH`，再执行 Maven 启动命令。

**Tech Stack:** React 18、TypeScript、Vite 5、Spring Boot、Maven、PowerShell、Windows cmd

---

## File Structure

### Backend docs / scripts

- Create: `D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo\scripts\run-backend.ps1`
- Create: `D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo\scripts\run-backend.cmd`
- Create: `D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo\docs\guides\2026-07-03-ai-workbench-and-local-startup.md`

### Frontend API / types / components

- Modify: `D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo-web\src\types\api.ts`
- Modify: `D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo-web\src\api\aiApi.ts`
- Create: `D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo-web\src\components\CapabilityCard.tsx`
- Create: `D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo-web\src\components\ExtractPanel.tsx`
- Create: `D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo-web\src\components\ExtractResultCard.tsx`
- Modify: `D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo-web\src\App.tsx`
- Modify: `D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo-web\src\styles\app.css`

### Verification

- Reuse backend tests already present for `/ai/extract`
- Use frontend type/build verification:
  - `npm run build`
- Use runtime verification:
  - 启动后端脚本
  - 启动前端 dev server
  - 打开页面验证 AI 工作台交互

### Stable local facts to preserve during implementation

- 后端接口 `POST /ai/extract` 已存在，返回字段为 `title / category / priority / keywords`
- API Key 已在本地替换，**不得提交**
- 已验证可用的稳定 JDK 路径是：
  - `D:\SoftWare\IntelliJ IDEA 2026.1.3\jbr`

## Task 1: 扩展前端类型与 API 调用

**Files:**
- Modify: `D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo-web\src\types\api.ts`
- Modify: `D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo-web\src\api\aiApi.ts`

- [ ] **Step 1: 在 `src/types/api.ts` 中新增结构化抽取类型**

加入：

```ts
export type ExtractResponse = {
  title: string;
  category: "BUG" | "FEATURE" | "QUESTION" | "COMPLAINT";
  priority: "LOW" | "MEDIUM" | "HIGH";
  keywords: string[];
};
```

- [ ] **Step 2: 在 `src/api/aiApi.ts` 中扩展 import 并新增 `extractText`**

把 import 改成：

```ts
import { type AiPingResponse, type SummaryResponse, type ExtractResponse } from "../types/api";
```

新增：

```ts
export async function extractText(text: string): Promise<ExtractResponse> {
  const response = await fetch(`${import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080"}/ai/extract`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ text })
  });

  const payload = await response.json();

  if (!response.ok || !payload.success) {
    throw new Error(payload.message || `Request failed with status ${response.status}`);
  }

  return payload.data as ExtractResponse;
}
```

- [ ] **Step 3: 运行前端构建，确认类型与 API 扩展阶段没有语法级错误**

Run:

```bash
npm run build
```

Workdir:

```bash
D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo-web
```

Expected:

- 允许因为 UI 尚未接线继续失败
- 不应出现 `types/api.ts` 或 `api/aiApi.ts` 的语法错误

- [ ] **Step 4: Commit**

```bash
git add src/types/api.ts src/api/aiApi.ts
git commit -m "feat: add extract api contract for ai workbench"
```

## Task 2: 新增工作台通用能力卡与结构化抽取组件

**Files:**
- Create: `D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo-web\src\components\CapabilityCard.tsx`
- Create: `D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo-web\src\components\ExtractPanel.tsx`
- Create: `D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo-web\src\components\ExtractResultCard.tsx`
- Modify: `D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo-web\src\styles\app.css`

- [ ] **Step 1: 创建通用能力卡组件 `CapabilityCard.tsx`**

```tsx
import { type ReactNode } from "react";

type CapabilityCardProps = {
  kicker: string;
  title: string;
  description: string;
  children: ReactNode;
};

export function CapabilityCard({ kicker, title, description, children }: CapabilityCardProps) {
  return (
    <section className="panel capability-card">
      <div className="panel-header capability-card__header">
        <div>
          <p className="panel-kicker">{kicker}</p>
          <h2>{title}</h2>
          <p className="capability-card__description">{description}</p>
        </div>
      </div>
      {children}
    </section>
  );
}
```

- [ ] **Step 2: 创建 `ExtractPanel.tsx`，只负责输入与操作**

```tsx
import { useState } from "react";

type ExtractPanelProps = {
  onSubmit: (text: string) => Promise<void>;
  loading: boolean;
};

const EXAMPLE_TEXT =
  "支付接口上线后，部分订单提交失败，用户反馈需要优先处理，并检查超时日志和订单状态回写。";

export function ExtractPanel({ onSubmit, loading }: ExtractPanelProps) {
  const [text, setText] = useState(EXAMPLE_TEXT);

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    await onSubmit(text);
  }

  function fillExample() {
    setText(EXAMPLE_TEXT);
  }

  function clearText() {
    setText("");
  }

  return (
    <form className="stack" onSubmit={handleSubmit}>
      <label className="field">
        <span className="field-label">结构化抽取输入</span>
        <textarea
          rows={6}
          value={text}
          onChange={(event) => setText(event.target.value)}
          placeholder="输入一段业务文本，例如故障反馈、需求描述、客户投诉或问题咨询。"
          disabled={loading}
        />
      </label>

      <div className="actions">
        <button className="primary-button" type="submit" disabled={loading}>
          {loading ? "正在抽取..." : "发送 /ai/extract"}
        </button>
        <button className="secondary-button" type="button" onClick={fillExample} disabled={loading}>
          填充示例
        </button>
        <button className="secondary-button" type="button" onClick={clearText} disabled={loading}>
          清空文本
        </button>
      </div>
    </form>
  );
}
```

- [ ] **Step 3: 创建 `ExtractResultCard.tsx`，只负责展示结果**

```tsx
import { type ExtractResponse } from "../types/api";

type ExtractResultCardProps = {
  data: ExtractResponse | null;
  loading: boolean;
};

const categoryLabels: Record<ExtractResponse["category"], string> = {
  BUG: "缺陷",
  FEATURE: "需求",
  QUESTION: "咨询",
  COMPLAINT: "投诉"
};

const priorityLabels: Record<ExtractResponse["priority"], string> = {
  LOW: "低",
  MEDIUM: "中",
  HIGH: "高"
};

export function ExtractResultCard({ data, loading }: ExtractResultCardProps) {
  if (loading) {
    return <div className="answer-card"><div className="placeholder-block">正在抽取结构化信息，我们马上就回来。</div></div>;
  }

  if (!data) {
    return (
      <div className="answer-card">
        <div className="empty-card">
          <h4>还没有抽取结果</h4>
          <p>发送一段业务文本到 /ai/extract，页面会在这里展示标题、分类、优先级和关键词。</p>
        </div>
      </div>
    );
  }

  return (
    <div className="answer-card">
      <div className="answer-card__header">
        <h3>结构化抽取结果</h3>
      </div>

      <div className="extract-title-block">
        <span className="meta-label">标题</span>
        <p className="extract-title">{data.title}</p>
      </div>

      <div className="meta-grid">
        <div className="meta-card">
          <span className="meta-label">分类</span>
          <strong>{categoryLabels[data.category]}</strong>
        </div>
        <div className="meta-card">
          <span className="meta-label">优先级</span>
          <strong className={`priority-badge priority-badge--${data.priority.toLowerCase()}`}>
            {priorityLabels[data.priority]}
          </strong>
        </div>
      </div>

      <div className="keyword-block">
        <span className="meta-label">关键词</span>
        <div className="tag-list">
          {data.keywords.map((keyword) => (
            <span key={keyword} className="tag-chip">{keyword}</span>
          ))}
        </div>
      </div>
    </div>
  );
}
```

- [ ] **Step 4: 在 `src/styles/app.css` 加入工作台卡片与抽取结果样式**

追加：

```css
.capability-card__header {
  align-items: flex-start;
}

.capability-card__description {
  margin: 12px 0 0;
  color: var(--muted);
  line-height: 1.7;
}

.extract-title-block,
.keyword-block {
  margin-top: 16px;
}

.extract-title {
  margin: 8px 0 0;
  font-size: 1.15rem;
  font-weight: 700;
  line-height: 1.6;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.tag-chip,
.priority-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 0.92rem;
  font-weight: 700;
}

.tag-chip {
  background: #f4efe2;
  border: 1px solid var(--line);
}

.priority-badge--low {
  background: #eef6ff;
  color: #245ea9;
}

.priority-badge--medium {
  background: #fff4d9;
  color: #8a5a00;
}

.priority-badge--high {
  background: #ffe5e5;
  color: #b42318;
}
```

- [ ] **Step 5: 运行前端构建，确认新组件无 TypeScript 语法错误**

Run:

```bash
npm run build
```

Expected:

- 允许因为 `App.tsx` 尚未接线继续失败
- 不应出现新增组件的语法错误

- [ ] **Step 6: Commit**

```bash
git add src/components/CapabilityCard.tsx src/components/ExtractPanel.tsx src/components/ExtractResultCard.tsx src/styles/app.css
git commit -m "feat: add ai workbench capability components"
```

## Task 3: 把首页重构为 AI 工作台

**Files:**
- Modify: `D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo-web\src\App.tsx`
- Create: `D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo-web\src\components\CapabilityCard.tsx`
- Create: `D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo-web\src\components\ExtractPanel.tsx`
- Create: `D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo-web\src\components\ExtractResultCard.tsx`

- [ ] **Step 1: 在 `App.tsx` 中新增结构化抽取状态与 handler**

加入：

```tsx
const [extractData, setExtractData] = useState<ExtractResponse | null>(null);
const [extractLoading, setExtractLoading] = useState(false);
const [extractError, setExtractError] = useState<string | null>(null);

async function handleExtract(text: string) {
  setExtractLoading(true);
  setExtractError(null);

  try {
    const result = await extractText(text);
    setExtractData(result);
  } catch (error) {
    setExtractError(error instanceof Error ? error.message : "结构化抽取请求失败");
  } finally {
    setExtractLoading(false);
  }
}
```

- [ ] **Step 2: 重写首页 hero 区，让文案体现“AI 工作台”**

目标文案：

```tsx
<header className="hero">
  <div>
    <p className="eyebrow">Knowledge RAG Demo</p>
    <h1>AI 能力工作台</h1>
    <p className="hero-copy">
      这个工作台用于承接当前学习阶段的 AI 能力验证，包括模型连通性、文本总结、结构化抽取和后续 RAG 问答能力。
      页面组织方式尽量贴近企业内部 AI 控制台，便于继续扩展 Day 5 / Day 6。
    </p>
  </div>
  <HealthBadge data={health} loading={healthLoading} error={healthError} />
</header>
```

- [ ] **Step 3: 使用统一能力卡片重组首页 4 块能力**

目标卡片：

- AI 连通性验证
- 文本总结
- 结构化抽取
- RAG 能力预留

结构化抽取卡参考：

```tsx
<CapabilityCard
  kicker="Structured Extraction"
  title="结构化抽取"
  description="把业务文本治理成可消费的标题、分类、优先级和关键词，贴近企业后台里的文本预处理场景。"
>
  <ExtractPanel onSubmit={handleExtract} loading={extractLoading} />
  <ExtractResultCard data={extractData} loading={extractLoading} />
  <StatusNotice tone="error" message={extractError} />
  <StatusNotice
    tone="info"
    message="推荐用故障反馈、需求描述、投诉记录和咨询文本来观察结构化抽取效果。"
  />
</CapabilityCard>
```

- [ ] **Step 4: 运行前端构建，验证工作台重构后的首页已完整接线**

Run:

```bash
npm run build
```

Expected:

- PASS

- [ ] **Step 5: 如果构建失败，优先修复以下问题后再次构建**

排查项：

- `ExtractResponse` import 是否缺失
- `extractText` 是否正确导出
- `CapabilityCard` 是否正确导入 `ReactNode`
- 首页是否还残留旧 Step 文案和旧布局拼写
- `priority-badge--${...}` 模板字符串是否正确

再次运行：

```bash
npm run build
```

Expected:

- PASS

- [ ] **Step 6: Commit**

```bash
git add src/App.tsx src/components/CapabilityCard.tsx src/components/ExtractPanel.tsx src/components/ExtractResultCard.tsx src/api/aiApi.ts src/types/api.ts src/styles/app.css
git commit -m "feat: refactor homepage into ai workbench"
```

## Task 4: 新增本机 JDK 固定启动脚本

**Files:**
- Create: `D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo\scripts\run-backend.ps1`
- Create: `D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo\scripts\run-backend.cmd`

- [ ] **Step 1: 创建 PowerShell 启动脚本**

`scripts/run-backend.ps1`：

```powershell
$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$jdkHome = "D:\SoftWare\IntelliJ IDEA 2026.1.3\jbr"
$mavenArgs = @("-s", ".mvn/settings.xml", "spring-boot:run")

if (-not (Test-Path -LiteralPath $jdkHome)) {
    throw "Stable JDK not found: $jdkHome"
}

$env:JAVA_HOME = $jdkHome
$env:Path = "$jdkHome\bin;$env:Path"

Set-Location $projectRoot

Write-Host "Using JAVA_HOME=$env:JAVA_HOME"
Write-Host "Starting backend with stable local JDK..."

mvn @mavenArgs
```

- [ ] **Step 2: 创建 cmd 启动脚本**

`scripts/run-backend.cmd`：

```bat
@echo off
setlocal

set "PROJECT_ROOT=%~dp0.."
set "JDK_HOME=D:\SoftWare\IntelliJ IDEA 2026.1.3\jbr"

if not exist "%JDK_HOME%" (
  echo Stable JDK not found: %JDK_HOME%
  exit /b 1
)

set "JAVA_HOME=%JDK_HOME%"
set "PATH=%JDK_HOME%\bin;%PATH%"

cd /d "%PROJECT_ROOT%"
echo Using JAVA_HOME=%JAVA_HOME%
echo Starting backend with stable local JDK...

mvn -s .mvn/settings.xml spring-boot:run
```

- [ ] **Step 3: 运行 PowerShell 脚本验证后端可启动**

Run:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-backend.ps1
```

Workdir:

```bash
D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo
```

Expected:

- 输出 `Using JAVA_HOME=...jbr`
- Spring Boot 开始启动

- [ ] **Step 4: 检查 8080 监听**

Run:

```powershell
Get-NetTCPConnection -LocalPort 8080 -State Listen
```

Expected:

- 能看到 8080 监听

- [ ] **Step 5: Commit**

```bash
git add scripts/run-backend.ps1 scripts/run-backend.cmd
git commit -m "chore: add stable local jdk backend startup scripts"
```

## Task 5: 补充中文说明文档并做联调验收

**Files:**
- Create: `D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo\docs\guides\2026-07-03-ai-workbench-and-local-startup.md`
- Modify: `D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo-web\src\App.tsx`
- Create: `D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo\scripts\run-backend.ps1`
- Create: `D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo\scripts\run-backend.cmd`

- [ ] **Step 1: 编写中文指南文档**

文档最少包含：

```md
# Day 4 AI 工作台与本机 JDK 启动说明

## 1. 这次做了什么
- 首页重构为 AI 工作台
- 新增结构化抽取能力卡
- 后端新增固定 JDK 启动脚本

## 2. 如何启动后端
- PowerShell: `.\scripts\run-backend.ps1`
- cmd: `scripts\run-backend.cmd`

## 3. 如何启动前端
- `npm run dev -- --host 0.0.0.0`

## 4. 如何验证结构化抽取
- 打开 AI 工作台
- 在结构化抽取卡输入业务文本
- 查看标题、分类、优先级、关键词结果

## 5. 注意事项
- API Key 仅保留本地
- 不提交真实密钥
```

- [ ] **Step 2: 启动后端**

Run:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-backend.ps1
```

Expected:

- 后端成功启动

- [ ] **Step 3: 启动前端**

Run:

```bash
npm run dev -- --host 0.0.0.0
```

Workdir:

```bash
D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo-web
```

Expected:

- Vite 输出可访问地址

- [ ] **Step 4: 在浏览器中手工验收 AI 工作台**

检查项：

- 首页标题已体现 AI 工作台
- AI 连通性、文本总结、结构化抽取、RAG 预留 4 张卡片均可见
- 结构化抽取卡默认示例文本可提交
- 结果卡可展示标题、分类、优先级、关键词
- 清空按钮工作正常
- 输入空文本时能看到后端错误提示

- [ ] **Step 5: 再次运行前端构建，确保静态构建通过**

Run:

```bash
npm run build
```

Expected:

- PASS

- [ ] **Step 6: Commit**

```bash
git add D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo\docs\guides\2026-07-03-ai-workbench-and-local-startup.md
git add D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo\scripts\run-backend.ps1
git add D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo\scripts\run-backend.cmd
git add D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo-web\src\App.tsx
git add D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo-web\src\api\aiApi.ts
git add D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo-web\src\types\api.ts
git add D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo-web\src\components\CapabilityCard.tsx
git add D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo-web\src\components\ExtractPanel.tsx
git add D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo-web\src\components\ExtractResultCard.tsx
git add D:\SoftWare\IdeaProjects\ragdemo\knowledge-rag-demo-web\src\styles\app.css
git commit -m "feat: deliver ai workbench and stable backend startup"
```

## Self-Review

### Spec coverage

- AI 工作台首页重构：Task 2-3 覆盖
- `/ai/extract` 重点能力卡：Task 1-3 覆盖
- 本机 JDK 固定启动脚本：Task 4 覆盖
- 中文文档与联调验收：Task 5 覆盖

### Placeholder scan

- 未使用 `TODO / TBD / similar to previous`
- 每个代码步骤都给了明确文件和代码块
- 每个验证步骤都给了明确命令

### Type consistency

- 前端类型统一使用 `ExtractResponse`
- API 函数统一命名为 `extractText`
- UI handler 统一命名为 `handleExtract`
- 首页组织统一围绕 `CapabilityCard`
