# knowledge-rag-demo

Minimal Spring Boot 3 + Spring AI skeleton.

## Features

- `GET /health`
- `GET /ai/ping?message=...`
- Unified response type: `ApiResponse<T>`
- `application.yml` based AI placeholder configuration

## Provider Placeholder Configuration

The project is preconfigured for an OpenAI-compatible provider:

```yaml
spring:
  ai:
    model:
      chat: openai
    openai:
      base-url: https://api.zetatechs.com
      api-key: aaaa
      chat:
        options:
          model: replace-me-model-name
```

Notes:

- `api-key` is intentionally a placeholder.
- Replace `replace-me-model-name` with the real model id from your provider.
- The current `/ai/ping` endpoint still uses `StubAiService`; switch the service implementation when you are ready to call the real provider.

## Run

```bash
mvn spring-boot:run
```

## Notes

- This project intentionally starts with a stub AI implementation.
- Replace `StubAiService` with your real provider integration later.
