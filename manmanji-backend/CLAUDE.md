# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Compile
mvn compile

# Run tests (only one test file exists)
mvn test

# Run a single test class
mvn test -pl . -Dtest=LlmProviderRegistryTest

# Start the app (requires PostgreSQL + Redis)
mvn spring-boot:run

# Swagger UI after startup
# http://localhost:8080/swagger-ui.html
```

## Tech Stack

- Java 21, Spring Boot 4.0.1, Maven
- PostgreSQL + PGVector (vector embeddings), Redis (Redisson 4.0.0)
- MyBatis-Plus 3.5.16 (ORM with `BaseMapper<T>`, `LambdaQueryWrapper`, auto-fill, logical delete)
- Spring AI 2.0.0-M4 (OpenAI-compatible chat/embedding, multi-vendor)
- Spring Security + JWT (RS256 asymmetric, jjwt 0.12.6)
- SpringDoc OpenAPI 3.0.2 (Swagger UI with Bearer JWT auth)
- Lombok 1.18.36

## Architecture

**Package layout per module** — each business module follows this structure under `com.yunbian27.<module>`:

| Package | Role |
|---------|------|
| `entity` | Database table mapping (`@TableName`, `@TableId`, MyBatis-Plus auto-fill) |
| `model` | HTTP transport: `XxxDTO` (request body), `XxxVO` (response body) |
| `mapper` | MyBatis-Plus `BaseMapper<T>` interfaces |
| `service` | Business logic |
| `controller` | REST endpoints (currently being flattened up one level) |

**Modules:**

| Module | Status | Purpose |
|--------|--------|---------|
| `auth` | Working | Register/login/refresh with JWT + Spring Security |
| `article` | Partial | Article CRUD (create done, list/detail/update stubbed) |
| `ai` | Partial | LLM provider management, chat routing, article generation |
| `interaction` | Skeleton | Likes, bookmarks, comments (entities + mappers only) |
| `flashsale` | Skeleton | Flash sales + coupons (entities + mappers only) |
| `points` | Skeleton | Points transactions (entity + mapper only) |
| `user` | Skeleton | User follows (entity + mapper only) |
| `ranking` | Empty | Directory exists, no code yet |

**LLM Provider routing** — `LlmProviderRegistry` in `ai/utils` handles dynamic provider selection:

1. Checks DB (`llm_providers` table via `LlmProviderMapper`) for enabled provider by ID
2. Falls back to YAML config (`ai.providers.*` in application.yml)
3. Builds `OpenAiChatModel` → `ChatClient` with AES-GCM decrypted API key
4. Caches `ChatClient` instances in `ConcurrentHashMap` by provider ID

Spring AI auto-configuration is **disabled** (`ManmanjiApplication` excludes all `OpenAi*AutoConfiguration` classes). All chat/embedding models are built manually through `LlmProviderRegistry`.

## Key Conventions

- **Unified response**: `Result<T>` (fields: `code`, `message`, `data`). Use `Result.success(data)` / `Result.error(ErrorCode.xxx)`.
- **Error codes**: `ErrorCode` enum in `common/exception/`. Provider-related errors are `11xxx`.
- **Business exceptions**: `throw new BusinessException(ErrorCode.XXX)` — caught by `GlobalExceptionHandler`.
- **Pagination**: `PageDTO<T>` with `total/page/size/records`.
- **Current user**: `SecurityUtils.getCurrentUserId()` returns the authenticated user's ID.
- **DI style**: Lombok `@RequiredArgsConstructor` (constructor injection), no `@Autowired` on fields.
- **JPA-free**: MyBatis-Plus with `LambdaQueryWrapper` for type-safe queries. No JPA/Hibernate.
- **Entity auto-fill**: `createdAt` and `updatedAt` use MyBatis-Plus `FieldFill.INSERT` / `INSERT_UPDATE`.
- **Logical delete**: MyBatis-Plus `logic-delete-field: deleted` — `deleted=true` marks soft-deleted rows.
- **Redis keys**: Managed via `RedisKeys` constants class.
- **Jackson**: `non_null` property inclusion, date format `yyyy-MM-dd HH:mm:ss`, timezone `Asia/Shanghai`.

## Security

- JWT RS256 (private/public key pair under `classpath:keys/`)
- Access token: 15 min, Refresh token: 7 days
- `JwtAuthFilter` is added before `UsernamePasswordAuthenticationFilter`
- Public endpoints: auth (register/login/refresh), GET for articles/users/rankings/flash-sales/coupons, Swagger/docs
- Admin endpoints (`/api/admin/**`) require `SCOPE_ROLE_ADMIN`
- All other requests require authentication
- Swagger UI: click "Authorize" button, enter `Bearer <token>`

## Important Notes

- `ToolCallingManager` and `ObservationRegistry` are injected `@Autowired(required = false)` in `LlmProviderRegistry`, but `OpenAiChatModel` constructor validates `toolCallingManager` as non-null via `Assert.notNull`. This is a known potential bug — runtime will fail if Spring doesn't provide the bean.
- Spring AI's `ChatClient.builder()` is a static method — unit tests must use `MockedStatic<ChatClient>`.
- `application.yml` uses env-var placeholders (`${POSTGRES_HOST:localhost}`) for all external services.
