# ContextOS

Single-module Spring Boot 4.1.0 backend at `backend/`. Container-based knowledge management
("AI-powered Personal Operating System").

## Quick start

```bash
# dev services (PostgreSQL 16, Redis 7, RabbitMQ 3)
docker compose -f docker-compose.dev.yml up -d

# build & test (no services needed — H2 in-memory for tests)
mvn test -f backend/pom.xml

# run (requires PostgreSQL)
mvn spring-boot:run -f backend/pom.xml
```

## Architecture (what's actually built)

Only these modules are implemented. The `docs/` folder contains aspirational architecture
that differs from reality in many places.

| Module | Status | What's inside |
|--------|--------|--------------|
| `config/` | done | `SecurityConfig`, `JpaConfig` |
| `common/` | done | Exception hierarchy (6 classes), response envelope (`ApiResponse`, `ErrorResponse`, `PageResponse`), `BaseEntity`, `SecurityUtil` |
| `auth/` | done | JWT register/login/refresh/logout, `jjwt 0.12.6`, HMAC-SHA512 |
| `user/` | done | `User` entity, `UserController` (GET /me) |
| `health/` | done | `/api/v1/health`, `/api/v1/info` |
| `container/` | Stage 1 | CRUD with 6-state lifecycle (PENDING→BUILDING→RUNNING→STOPPED→FAILED→DESTROYED) |

**Not yet implemented** despite being in docs: tag, snapshot, timeline, pin, ai, search,
websocket, activity, dashboard, analytics, integration. Also no MapStruct, no Flyway
(disabled with `ddl-auto: update`), no Redis/RabbitMQ integration code.

## Package conventions (verify against actual code, not docs)

- **DTOs**: Java records with `jakarta.validation` constraints, `static from(Entity)` factory
- **Entities**: extend `BaseEntity` (UUID PK auto-generated, `@PrePersist` timestamps), no Lombok
- **Repositories**: `extends JpaRepository<Entity, UUID>`
- **Services**: concrete classes (no interfaces/`*Impl` pattern), constructor injection
- **Controllers**: `ResponseEntity<ApiResponse<T>>` with `ApiResponse.ok(data)`
- **Exception handler**: returns `Map<String,Object>` (not `ErrorResponse` record), with `success`, `error.code`, `error.message`, `timestamp`
  - `BadCredentialsException` → 401 `INVALID_CREDENTIALS`
  - `RuntimeException("Email already*")` → 409 `EMAIL_EXISTS`
  - `RuntimeException` (other) → 400 `BAD_REQUEST`
- **Error shape**: `{ "success": false, "error": { "code": "...", "message": "...", "details": [] }, "timestamp": "..." }`

## Spring Boot 4.x import quirks (different from 3.x)

| Thing | Spring Boot 4.x import |
|-------|----------------------|
| ObjectMapper | `tools.jackson.databind.ObjectMapper` |
| MockMvc auto-config | `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc` |
| Web starter | `spring-boot-starter-webmvc` (not `-web`) |

## `.env` loading

`spring.dotenv.location: file:../.env` — `.env` lives at project root, relative to `backend/`.
Never at `backend/.env`. Contains `JWT_SECRET`, `APP_PORT=6969`, DB creds.

## Testing conventions

- **Unit tests**: `@ExtendWith(MockitoExtension.class)`, `@Mock` repository, instantiate service in `@BeforeEach`
- **Integration tests**: `@SpringBootTest(properties = {...})` overriding datasource to H2 in-memory,
  `@AutoConfigureMockMvc`, `@ActiveProfiles("test")`
- **Imports**: `tools.jackson.*` (not `com.fasterxml.jackson.*`), `spring-boot-starter-webmvc-test` in POM
- **No Testcontainers** — H2 only
- All 101 tests pass with `mvn test`

## Gotchas

- `JwtTokenProvider` field has typo `acceessExpiration` (3 e's) — compiles fine, don't rename unless
  you update all call sites
- Refresh token is hardcoded to **7 days** expiry (not 30 as the config suggests)
- BCrypt strength 12 — slow in tests, be mindful
- `UserService.getUserById()` throws plain `RuntimeException` (not `ResourceNotFoundException`)
- `Container.StatusUpdateRequest` is a local record inside `ContainerController` — imports need
  to reference `ContainerController.StatusUpdateRequest`
- Container status transitions: destroyed is terminal; same-status is idempotent; any other
  forward transition is allowed (strict enforcement coming in Stage 2+)
- No `@PreAuthorize` anywhere — only URL-based auth in `SecurityConfig`

## API surface

| Endpoint | Method | Auth | Notes |
|----------|--------|------|-------|
| `/api/v1/auth/register` | POST | public | |
| `/api/v1/auth/login` | POST | public | |
| `/api/v1/auth/refresh` | POST | public | |
| `/api/v1/auth/logout` | POST | public | revokes refresh token |
| `/api/v1/health` | GET | public | raw Map, not ApiResponse |
| `/api/v1/info` | GET | public | raw Map, not ApiResponse |
| `/api/v1/users/me` | GET | JWT | |
| `/api/v1/containers` | GET | JWT | |
| `/api/v1/containers` | POST | JWT | create |
| `/api/v1/containers/{id}` | GET | JWT | |
| `/api/v1/containers/{id}` | DELETE | JWT | |
| `/api/v1/containers/{id}/status` | PATCH | JWT | transition status |

All protected endpoints return 401/403 via SecurityConfig (no custom handler wired yet).
