# Day 7 Stage Review

> Scope: skip Day 6 and review the first-stage work from Day 1 to Day 5.

## What We Have Built

The current `knowledge-rag-demo` project has completed the first Spring AI learning loop:

- Basic Spring Boot project structure with `controller / service / config / dto`.
- Unified API response model through `ApiResponse<T>`.
- Health check endpoint: `GET /health`.
- Model connectivity endpoint: `GET /ai/ping`.
- Text summarization endpoint: `POST /ai/summary`.
- Text classification endpoint: `POST /ai/classify`.
- Structured extraction endpoint: `POST /ai/extract`.
- Weather Tool Calling endpoint: `POST /ai/tool-call`.
- Minimal React workbench in `knowledge-rag-demo-web`.

The most important shift is that the project is no longer just "calling a model".
It now has several enterprise-style AI capabilities with stable request and response boundaries.

## Why Structured Output Fits Enterprise Systems Better Than Free Text

Free text is easy for humans to read, but hard for systems to consume reliably.
Enterprise systems usually need stable fields, fixed categories, predictable validation, and clear failure behavior.

For example, `/ai/extract` returns fields like:

- `title`
- `category`
- `priority`
- `keywords`

These fields can be stored in a database, shown in a UI, routed to workflow rules, or used by another backend service.
If the model only returns a paragraph, every downstream system has to parse natural language again, which is fragile.

Structured output is useful because it gives us:

- Stable contracts between frontend and backend.
- Easier validation and fallback handling.
- Better testability.
- Less dependence on the model's writing style.
- More reliable integration with existing Java business systems.

The key lesson is: in enterprise applications, the model should not be treated as a free-form writer only.
It should often be treated as a component that produces controlled data for the rest of the system.

## Tool Calling vs Normal Chat APIs

A normal chat API mainly answers from the model's internal knowledge and prompt context.
It can sound fluent, but it cannot reliably know private system state, order status, ticket details, weather, logs, or database values unless we provide that data.

Tool Calling changes the flow:

1. The user asks a question.
2. The model decides whether a tool is needed.
3. The backend executes a real function, such as `getWeather`.
4. The model uses the tool result to produce the final answer.
5. The API returns both the answer and structured tool metadata.

In this project, `/ai/tool-call` demonstrates that pattern with weather data.
The response tells the frontend whether a tool was called, which tool was used, which provider supplied the data, and what structured result came back.

The important difference is responsibility:

- Normal chat: the model directly answers.
- Tool Calling: the model plans, the system executes, and the final answer is grounded in tool output.

For enterprise systems, Tool Calling is valuable because the model can interact with controlled backend capabilities without inventing facts.

## What Is Still Not Clear Enough

These areas are worth revisiting before moving deeper into RAG:

- Error model: current endpoints handle basic validation, but the error format and exception strategy can still be unified more cleanly.
- Prompt organization: prompts are currently embedded in service methods; later they may need a small prompt builder or template layer.
- Observability: the project can run and test, but still lacks useful request logs, tool call traces, and model latency records.
- Real provider behavior: tests cover provider parsing and stub behavior, but live model and live tool behavior still need manual verification.
- Secret hygiene: `OPENAI_API_KEY` is now environment-based, but we should keep using secret scanning before every commit.
- RAG boundaries: we have not yet decided the exact document format, chunk size, metadata schema, or vector store choice.

Skipping Day 6 is acceptable for learning momentum, but these cleanup items should not be forgotten.
Some of them will naturally return during the RAG implementation.

## What To Prepare For Next Week's RAG Work

Day 8 starts the RAG project phase, so the next useful preparation is not code first.
It is a small, clean document set.

Recommended document set:

- 10 to 20 Markdown files under `docs/sample-docs/`.
- One consistent business domain, such as order troubleshooting, account FAQ, payment support, or internal deployment guide.
- Each document should have a clear title and 3 to 8 short sections.
- Documents should contain real-looking facts, procedures, constraints, and failure cases.
- Avoid mixing too many unrelated domains in the first RAG dataset.

Recommended metadata to preserve later:

- `docId`
- `fileName`
- `sourcePath`
- `chunkIndex`
- `title`

Recommended candidate questions:

- 5 direct questions whose answer appears clearly in one document.
- 5 paraphrased questions that require semantic retrieval.
- 5 no-answer questions where the system should refuse or say context is insufficient.
- 5 confusing questions that are close to the domain but not directly answered.

The goal for next week is to prove the full RAG loop:

`documents -> chunks -> retrieval -> answer -> citations -> refusal when context is insufficient`

## Personal Takeaways

This first stage already shows the basic shape of Java AI application development:

- Use Spring Boot to keep enterprise engineering structure stable.
- Use Spring AI to connect model capabilities.
- Use DTOs and enums to control model output.
- Use Tool Calling when the answer must depend on external or system data.
- Use frontend cards only as a thin demonstration layer, not as the core logic.

The next stage should move from "model capability endpoints" to "knowledge-grounded answering".
That is where RAG begins to feel different from a normal chatbot.
