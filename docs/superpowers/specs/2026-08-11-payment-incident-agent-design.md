# Payment Incident Agent Enterprise Design

## Status

- Date: 2026-08-11
- Status: design approved
- Scope: refine Week 3 and later tasks in `studyplan.md`
- Primary audience: experienced Java backend developers moving into enterprise AI application development

## 1. Context

The current learning plan completes a useful RAG foundation in Week 2, but the original Week 3 Agent project is mostly a tool-calling demonstration. It lacks a coherent business incident, persisted workflow, evidence model, authorization boundary, controlled execution, audit trail, measurable quality, and production-readiness work.

The revised project must remain achievable as a local portfolio project while using the same engineering patterns and core technologies expected in production. Mock implementations may replace unavailable enterprise systems, but they must sit behind production-shaped interfaces. Core workflow, persistence, reliability controls, security checks, and observability must execute for real.

## 2. Product Definition

Rename the second project from `ticket-agent-demo` to `payment-incident-agent`.

Product statement:

> An enterprise payment incident investigation and controlled remediation platform. It correlates ticket, payment, monitoring, deployment, and knowledge evidence; produces traceable diagnosis candidates; proposes an allowlisted runbook; and executes it only after human approval.

The project is not a general chatbot. Its unit of work is a persisted production incident.

### 2.1 Business Domain

- Business systems: order, payment, and refund services.
- Operations systems: ticketing, observability, deployment history, knowledge base, and runbooks.
- Primary actors: on-call responder, approver, and auditor.
- Fixed incident scenarios:
  - Payment succeeded but order status was not updated.
  - Duplicate charge risk.
  - Batch refund failures.
  - Payment latency increased after a deployment.

### 2.2 Business Value

- Reduce repetitive evidence collection during the first minutes of an incident.
- Separate confirmed facts from model-generated hypotheses.
- Make every diagnosis claim traceable to persisted evidence.
- Prevent autonomous execution of high-risk actions.
- Preserve a complete timeline for review and compliance.
- Make incident quality measurable through stable evaluation cases.

### 2.3 Success Metrics

- Time from incident creation to first diagnosis.
- Correct tool selection rate.
- Evidence citation completeness.
- Unsupported conclusion rate.
- High-risk action approval coverage.
- Runbook execution success rate.
- Model calls, token use, and total latency per investigation.

## 3. Goals and Non-Goals

### 3.1 Goals

- Build a stable end-to-end incident workflow, not an unbounded autonomous agent.
- Reuse the existing payment knowledge base through a real MCP boundary.
- Persist incidents, investigations, evidence, approvals, executions, and audit events in PostgreSQL.
- Enforce human approval and role checks for state-changing runbooks.
- Provide deterministic validation around all model-generated plans and recommendations.
- Produce a local demo that is resource-efficient and can be explained as a production architecture.
- Establish repeatable RAG and Agent evaluation baselines.

### 3.2 Non-Goals

- Connecting to a real production environment.
- Allowing the model to invoke arbitrary commands or bypass approval.
- Complex multi-agent collaboration.
- Infinite planning or reflection loops.
- Deploying Redis, Prometheus, Grafana, an OpenTelemetry Collector, Kafka, or an identity provider locally.
- Claiming that the model can determine a unique root cause without sufficient evidence.

## 4. Repository and Deployment Shape

Use sibling repositories under the existing workspace:

```text
ragdemo/
  knowledge-rag-demo/             # existing RAG service and future MCP server
  knowledge-rag-demo-web/         # existing RAG workbench
  payment-incident-agent/         # new incident and Agent backend
  payment-incident-agent-web/     # new incident operations frontend
```

The workspace root remains a container directory rather than a Git repository. Each application can be built, tested, versioned, and run independently.

## 5. Architecture

Editable Chinese architecture diagram: [`payment-incident-agent.drawio`](../../architecture/payment-incident-agent.drawio)

### 5.1 Runtime Components

- `payment-incident-agent-web`: incident operations UI.
- `payment-incident-agent`: modular Spring Boot application for incident workflow and Agent orchestration.
- `knowledge-rag-demo`: existing RAG service extended with a stateless Streamable HTTP MCP server.
- PostgreSQL/pgvector: the only required external infrastructure; reused by the local projects with separate tables or schemas.
- External system adapters: local mock providers implementing production-shaped interfaces for tickets, payments, metrics, deployments, and runbook execution.

### 5.2 Technical Baseline

- Java 17 or later.
- Spring Boot 3.5.x.
- Spring AI 1.1.8, retaining Spring Boot 3 compatibility while adding current MCP support.
- Spring AI MCP Client and Server with stateless Streamable HTTP.
- Spring Data JPA, Flyway, PostgreSQL, and existing pgvector.
- Spring Security role-based authorization.
- Resilience4j TimeLimiter, Retry, and CircuitBreaker for external reads.
- Spring Boot Actuator and Micrometer for production-standard metrics exposure.
- Structured logging with `traceId`, `incidentId`, and `toolCallId`.
- React, Vite, and TypeScript for the operations UI.

### 5.3 Lightweight Production Equivalence

Local development does not run observability backends. The application exposes metrics and trace-compatible identifiers exactly as a production deployment would, while exporters remain disabled by default. PostgreSQL is the only persisted state source. Redis is intentionally excluded because the defined workflow does not require distributed cache, session storage, or distributed locking.

Expected incremental local resource use is approximately 500-800 MB for the Agent backend and frontend, excluding the already running PostgreSQL/pgvector instance.

## 6. Module Boundaries

`payment-incident-agent` is a modular monolith with explicit dependencies:

- `incident`: incident aggregate, severity, ownership, and state transitions.
- `investigation`: bounded planning, orchestration, budgets, and diagnosis generation.
- `tooling`: normalized tool contracts, execution records, and evidence conversion.
- `policy`: risk classification, parameter allowlists, and action authorization.
- `approval`: approval requests, separation of duties, expiration, and parameter freezing.
- `runbook`: precondition checks, idempotent execution, results, and rollback guidance.
- `audit`: immutable business timeline events.
- `evaluation`: dataset loading, case replay, deterministic scoring, and reports.
- `integration`: ticket, payment, monitoring, deployment, MCP, and executor adapters.

Controllers call application use cases. They do not contain Agent loops, approval decisions, provider calls, or state transition logic.

## 7. Control Boundary

The model may:

- Select allowlisted read-only investigation tools.
- Produce a structured investigation plan within a fixed budget.
- Generate diagnosis candidates grounded in evidence IDs.
- Recommend an allowlisted runbook and parameters.

Deterministic application code must:

- Validate all model output schemas and referenced evidence IDs.
- Enforce maximum tool calls, time, tokens, and repeated-call limits.
- Classify action risk and determine whether approval is required.
- Validate roles, separation of duties, state transitions, and frozen parameters.
- Generate and enforce idempotency keys.
- Invoke the runbook executor.
- Persist all workflow and audit records.

The runbook executor is not exposed as a model-callable tool. Approval completion invokes it through a deterministic application service.

## 8. Investigation Tools

### 8.1 Read-Only Tools

- `getTicketDetail(ticketId)`
- `getPaymentSnapshot(orderId, paymentId)`
- `getServiceMetrics(serviceName, timeRange, metrics)`
- `getRecentDeployments(serviceName, timeRange)`
- `searchKnowledgeBase(query, topK)` through MCP
- `getRunbookDocument(runbookCode)` through MCP

### 8.2 Normalized Tool Result

```json
{
  "toolCallId": "tc_xxx",
  "status": "SUCCESS",
  "sourceSystem": "monitoring",
  "observedAt": "2026-08-11T10:15:00+08:00",
  "durationMs": 83,
  "data": {},
  "evidence": [],
  "error": null
}
```

Errors distinguish no data, stale data, invalid parameters, forbidden access, timeout, schema incompatibility, retryable dependency failure, and permanent dependency failure.

Retries apply only to idempotent reads and explicitly retryable failures. Backoff and maximum attempts are bounded. A tool failure never produces successful evidence.

## 9. Evidence and Diagnosis

Each evidence record includes:

- `evidenceId`
- source system and source type
- business reference
- collection time
- concise summary
- raw payload hash
- freshness or staleness status
- optional RAG document, section, chunk, score, and index version

The structured diagnosis contains:

- incident summary and impact
- confirmed facts
- diagnosis candidates with confidence
- evidence IDs for every candidate
- missing evidence and uncertainties
- recommended allowlisted runbook
- risk level, parameters, and preconditions
- manual next steps when execution is not appropriate

Any unknown evidence ID invalidates the diagnosis result. The system must never silently remove unsupported citations and present the remaining text as valid.

## 10. Incident and Approval State

Primary flow:

```text
OPEN
  -> INVESTIGATING
  -> DIAGNOSIS_READY
  -> PENDING_APPROVAL
  -> APPROVED
  -> EXECUTING
  -> RESOLVED
```

Alternative terminal or recovery states:

- `NEEDS_MORE_EVIDENCE`
- `REJECTED`
- `EXECUTION_FAILED`
- `CANCELLED`

State transitions use optimistic locking. Invalid and stale transitions return a conflict error rather than overwriting newer state.

## 11. Controlled Runbooks

Initial allowlist:

- `RECONCILE_PAYMENT_ORDER`
- `RETRY_REFUND_JOB`
- `ROLLBACK_PAYMENT_DEPLOYMENT`

Execution flow:

1. Validate incident state, diagnosis, action risk, parameter schema, and preconditions.
2. Create an approval request containing a frozen parameter snapshot and hash.
3. Require an `APPROVER`; high-risk actions enforce separation between requester and approver.
4. Generate a single-use authorization after approval.
5. Execute with `incidentId + runbookCode + parameterHash` as the idempotency key.
6. Persist execution progress, result, failure category, and rollback guidance.
7. Append every action to the audit timeline.

The mock executor supports success, rejection, timeout, permanent failure, and partial success. Automatic retries are never unbounded and are disabled for actions that cannot prove idempotency.

## 12. Persistence

Core tables:

- `incident`
- `investigation`
- `tool_execution`
- `evidence`
- `diagnosis`
- `approval_request`
- `runbook_execution`
- `audit_event`
- `evaluation_case`
- `evaluation_run`

Flyway owns schema evolution. JSON columns may store immutable request and result snapshots, but searchable workflow fields remain typed columns with indexes and constraints.

## 13. API Surface

Initial API groups:

- Incidents: create, list, detail, assign, and cancel.
- Investigation: start, status, diagnosis, evidence, and tool timeline.
- Approval: submit, list pending, approve, reject, and inspect frozen parameters.
- Runbook: execution status and result.
- Audit: incident timeline.
- Evaluation: run dataset and inspect report.

Mutating endpoints require an idempotency key where duplicate submission is plausible. API responses use stable machine-readable error codes and include `traceId`.

## 14. Frontend Experience

The first screen is the incident operations workspace, not a marketing page or generic chat interface.

Required views:

- Incident queue with severity, service, state, owner, and age filters.
- Incident detail with impact summary and investigation controls.
- Confirmed facts and diagnosis candidates shown separately.
- Evidence drawer with source, freshness, timestamp, and raw reference.
- Tool execution timeline with status, duration, and failure category.
- Runbook proposal with risk, frozen parameters, preconditions, and rollback guidance.
- Approval inbox and approve/reject actions.
- Execution progress and audit timeline.

The UI displays structured business decisions, not hidden chain-of-thought. Refreshing the page reconstructs state from the backend.

## 15. Security and Audit

- Roles: `RESPONDER`, `APPROVER`, and `AUDITOR`.
- Demo authentication uses Spring Security-backed local users; production replacement is OAuth2/OIDC without changing application authorization rules.
- External tool data and RAG content are untrusted data, never system instructions.
- Prompt templates delimit tool content and prohibit instructions found inside evidence.
- Sensitive identifiers are minimized and masked before prompts and logs.
- Tool and runbook parameters use explicit allowlists and typed validation.
- Business audit events are separate from diagnostic logs and cannot be omitted by individual adapters.
- MCP production guidance documents OAuth2 client credentials or API-key protection, even when local demo transport uses a development credential.

## 16. Observability

Expose at least:

- investigation duration
- tool calls and failures by tool/error class
- tool latency
- diagnosis validation failures
- approval wait duration
- runbook execution outcomes
- model calls, tokens, and estimated cost
- active incidents by severity/state

All log events include the relevant correlation IDs. Local use requires only Actuator endpoints and structured logs; external collectors are optional deployment concerns.

## 17. Evaluation and Testing

### 17.1 RAG Evaluation

- At least 30 payment-domain questions.
- Expected sources, keywords, refusal expectations, and minimum relevance.
- Recall@K, citation correctness, refusal accuracy, and latency.
- Model, prompt, index, and dataset versions recorded with every run.

### 17.2 Agent Evaluation

- At least 20 incident cases.
- Expected tools, required evidence, prohibited conclusions, recommended runbook, and approval requirement.
- Deterministic scoring for tool selection, parameter validity, evidence coverage, schema validity, and execution blocking.
- LLM-as-Judge may supplement but never replace deterministic checks.

### 17.3 Test Layers

- Unit tests for state transitions, policy, evidence validation, and idempotency.
- Provider contract tests shared by mock and future real adapters.
- MCP contract and compatibility tests.
- PostgreSQL integration tests for migrations, locking, and workflow persistence.
- Controller tests for authorization, validation, conflict, and error mapping.
- End-to-end tests for the four fixed incident scenarios.
- Failure injection tests for timeout, stale data, partial failure, duplicate requests, and concurrent approval.
- Security tests for prompt injection, role bypass, parameter tampering, and sensitive-data leakage.

## 18. Revised Week 3

### Day 15: Domain and Scenario Baseline

Create the Agent project, incident domain, Flyway schema, state machine, four coherent incident datasets, and expected investigation paths.

Learning value: enterprise Agent quality begins with business modeling and reproducible data, not prompt writing.

### Day 16: Tool Contracts and Evidence

Implement production-shaped providers, normalized tool results, Resilience4j policies, persisted executions, and evidence records.

Learning value: distinguish raw tool output from evidence that may legally support a conclusion.

### Day 17: Real MCP Knowledge Boundary

Upgrade the RAG service to Spring AI 1.1.8, expose knowledge tools through stateless Streamable HTTP, and consume them from the Agent.

Learning value: understand tool discovery, protocol contracts, versioning, and graceful degradation across application boundaries.

### Day 18: Bounded Investigation

Implement the investigation orchestrator, structured diagnosis, call/token/time budgets, repeated-call suppression, and evidence reference validation.

Learning value: build a controlled Agent workflow rather than an unlimited reasoning loop.

### Day 19: Approval and Idempotent Runbooks

Implement roles, approval states, parameter freezing, separation of duties, preconditions, idempotency, and the mock executor.

Learning value: separate model recommendations from authorized business execution.

### Day 20: Security, Audit, and Observability

Add prompt-injection defenses, masking, allowlists, audit events, metrics, structured logs, and cost tracking.

Learning value: learn the production controls that determine whether an Agent can be trusted in an enterprise workflow.

### Day 21: Incident Operations UI and End-to-End Review

Build the incident queue, diagnosis/evidence view, approval workflow, execution status, and audit timeline; demonstrate one full scenario.

Learning value: translate Agent capabilities into a recoverable, role-based operational workflow.

## 19. Revised Week 4

### Day 22: RAG Regression Baseline

Create the 30-question versioned dataset and collect retrieval, citation, refusal, and latency metrics.

### Day 23: Agent Evaluation

Create the 20-case dataset and score tool selection, parameters, evidence, diagnosis, runbook recommendation, and safety gates.

### Day 24: Failure Injection and Security Red Team

Exercise dependency failure, stale data, duplicate approval, execution timeout, prompt injection, privilege bypass, tampering, and leakage.

### Day 25: Architecture and Contract Refactoring

Enforce module dependencies, remove orchestration from controllers, normalize contracts, version schemas, and add MCP compatibility tests.

### Day 26: Performance, Cost, and Observability Baseline

Measure end-to-end latency, tool P95, model usage, concurrency behavior, and database pool behavior; document bottlenecks and improvements.

### Day 27: Enterprise Documentation and Architecture Assets

Produce README files, editable draw.io architecture, state and sequence diagrams, ADRs, runbooks, security boundaries, and production replacement guidance.

### Day 28: Reproducible Release and Demo

Add environment templates, startup checks, resettable fixtures, health checks, four fixed demo cases, and recovery verification.

### Day 29: Production Readiness Review and Interview Rehearsal

Review security, reliability, data, observability, cost, and operations; rehearse architectural decisions and incident-response questions.

### Day 30: Final Acceptance and Retrospective

Verify all applications, evaluations, diagrams, ADRs, runbooks, demo scripts, and production-readiness findings; document the next evolution steps.

## 20. Study Plan Rewrite Scope

Update these sections of `studyplan.md` consistently:

- goals and learning boundaries
- final deliverables
- technical stack
- Week 3 and Week 4 schedules
- second-project detailed design
- Codex prompts and review prompts
- monthly acceptance criteria
- recommended execution order

Week 1 remains unchanged. Week 2 implementation steps remain unchanged except for forward references to the Agent project and MCP reuse.

## 21. Risks and Mitigations

- Scope risk: keep four fixed incidents, five read tools, three runbooks, and one bounded orchestrator.
- Resource risk: PostgreSQL is the only required infrastructure; no local observability stack.
- Model instability: structured schemas, deterministic validation, stable fixtures, and stubbed tests isolate variance.
- MCP migration risk: upgrade to Spring AI 1.1.8 before adding MCP and protect the boundary with contract tests.
- False production realism: mocks replace only external systems; workflow, database, security, reliability, and audit behavior remain real.
- Unsafe automation: the executor remains outside the model tool set and requires a valid approval artifact.

## 22. Acceptance Criteria

The revised learning plan is complete when it describes a project that is:

- runnable with lightweight local resources
- grounded in one coherent payment incident domain
- traceable from diagnosis to source evidence
- protected by roles, approval, parameter validation, and idempotency
- resilient to dependency failures without fabricating facts
- measurable through stable RAG and Agent evaluations
- observable through production-standard metrics and correlation IDs
- auditable from incident creation through execution
- documented with editable architecture assets and repeatable demo cases
