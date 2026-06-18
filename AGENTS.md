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

V1 complete — 12 packages, 37 API endpoints, 236 tests.

| Module | Status | What's inside |
|--------|--------|--------------|
| `config/` | done | `SecurityConfig`, `JpaConfig`, `OpenAPIConfig` |
| `common/` | done | Exception hierarchy (6 classes), response envelope (`ApiResponse`, `ErrorResponse`, `PageResponse`), `BaseEntity`, `SecurityUtil` |
| `auth/` | done | JWT register/login/refresh/logout/forgot-password/reset-password, `jjwt 0.12.6`, HMAC-SHA512 |
| `user/` | done | `User` entity, `UserController` (GET /me) |
| `health/` | done | `/api/v1/health`, `/api/v1/info` |
| `container/` | done | CRUD + search + pin/unpin + 6-state lifecycle + type system (12 types) + validation |
| `tag/` | done | CRUD + search/autocomplete + merge + ManyToMany with containers |
| `snapshot/` | done | CRUD + restore from snapshot |
| `timeline/` | done | Event recording + query by containerId/eventType |
| `dashboard/` | done | Summary endpoint (counts by status, tag count, recent activity) |
| `websocket/` | done | STOMP over `/ws`, in-memory broker `/topic/events`, domain events on all mutations |

**V2+ (not yet built):** AI features, analytics, integration, Redis/RabbitMQ code, OAuth2, RBAC, progress tracking, snapshot diff, bulk operations.

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
- **Controller tests**: `@WebMvcTest(ControllerClass.class)` with `@MockitoBean` for service + JWT deps
- **Imports**: `tools.jackson.*` (not `com.fasterxml.jackson.*`), `spring-boot-starter-webmvc-test` in POM
- **No Testcontainers** — H2 only
- All **236 tests** pass with `mvn test`

## Gotchas

- `JwtTokenProvider` field has typo `acceessExpiration` (3 e's) — compiles fine, don't rename unless
  you update all call sites
- `WebMvcTest` controllers need both `JwtTokenProvider` + `CustomUserDetailsService` mocked via `@MockitoBean`
- For `@AuthenticationPrincipal` in `@WebMvcTest` tests, use `with(user(principal))` from `SecurityMockMvcRequestPostProcessors`
- Refresh token is hardcoded to **7 days** expiry (not 30 as the config suggests)
- BCrypt strength 12 — slow in tests, be mindful
- `UserService.getUserById()` throws plain `RuntimeException` (not `ResourceNotFoundException`)
- `Container.StatusUpdateRequest` is a local record inside `ContainerController` — imports need
  to reference `ContainerController.StatusUpdateRequest`
- Container status transitions: destroyed is terminal; same-status is idempotent; any other
  forward transition is allowed (strict enforcement coming in Stage 2+)
- No `@PreAuthorize` anywhere — only URL-based auth in `SecurityConfig`
- Flyway runs in production (`ddl-auto: validate`); tests use H2 with `create-drop` and Flyway disabled via `@ActiveProfiles("test")`
- `@MockitoBean` is the Spring Boot 4.x equivalent of `@MockBean` (in `org.springframework.test.context.bean.override.mockito`)

## API surface — 37 endpoints

### Public (8)

| Endpoint | Method | Notes |
|----------|--------|-------|
| `/api/v1/auth/register` | POST | |
| `/api/v1/auth/login` | POST | |
| `/api/v1/auth/refresh` | POST | |
| `/api/v1/auth/logout` | POST | revokes refresh token |
| `/api/v1/auth/forgot-password` | POST | returns temp token |
| `/api/v1/auth/reset-password` | POST | |
| `/api/v1/health` | GET | raw Map, not ApiResponse |
| `/api/v1/info` | GET | raw Map, not ApiResponse |

### JWT Required (29)

**User:**
| Endpoint | Method | Notes |
|----------|--------|-------|
| `/api/v1/users/me` | GET | |

**Containers:**
| Endpoint | Method | Notes |
|----------|--------|-------|
| `/api/v1/containers` | GET | list |
| `/api/v1/containers` | POST | create |
| `/api/v1/containers/{id}` | PUT | update (partial) |
| `/api/v1/containers/{id}` | GET | |
| `/api/v1/containers/{id}` | DELETE | |
| `/api/v1/containers/{id}/status` | PATCH | transition status |
| `/api/v1/containers/{id}/pin` | POST | |
| `/api/v1/containers/{id}/pin` | DELETE | |
| `/api/v1/containers/search` | GET | query: `q`, `status`, `type`, `tagId` |
| `/api/v1/containers/pinned` | GET | list pinned |

**Tags:**
| Endpoint | Method | Notes |
|----------|--------|-------|
| `/api/v1/tags` | GET | list |
| `/api/v1/tags` | POST | create |
| `/api/v1/tags/{id}` | GET | |
| `/api/v1/tags/{id}` | PUT | |
| `/api/v1/tags/{id}` | DELETE | |
| `/api/v1/tags/search` | GET | query: `q` |
| `/api/v1/tags/autocomplete` | GET | query: `q`, top 10 |
| `/api/v1/tags/merge` | POST | body: `sourceTagId`, `targetTagId` |
| `/api/v1/tags/assign` | POST | query: `containerId`, body: `Set<UUID>` |
| `/api/v1/tags/{tagId}/containers/{containerId}` | DELETE | unassign |

**Snapshots:**
| Endpoint | Method | Notes |
|----------|--------|-------|
| `/api/v1/containers/{containerId}/snapshots` | POST | create |
| `/api/v1/containers/{containerId}/snapshots` | GET | list |
| `/api/v1/snapshots/{id}` | GET | |
| `/api/v1/snapshots/{id}` | DELETE | |
| `/api/v1/snapshots/{id}/restore` | POST | restore container from snapshot |

**Timeline:**
| Endpoint | Method | Notes |
|----------|--------|-------|
| `/api/v1/timeline` | GET | query: `containerId`, `eventType` |
| `/api/v1/timeline/{id}` | GET | |

**Dashboard:**
| Endpoint | Method | Notes |
|----------|--------|-------|
| `/api/v1/dashboard/summary` | GET | counts + recent activity |

**WebSocket:**
| Endpoint | Protocol | Notes |
|----------|----------|-------|
| `/ws` | STOMP | subscribe to `/topic/events` |

All protected endpoints return 401 via SecurityConfig (no custom handler wired yet).
