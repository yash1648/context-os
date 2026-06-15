# Architecture Decision Records (ADR)

## ADR-001: Modular Monolith over Microservices

**Status:** Accepted
**Date:** 2026-01-15

### Context
We need to decide between a modular monolith and microservices architecture for ContextOS V1-V2.

### Decision
Adopt a **modular monolith** architecture with well-defined bounded contexts, explicit module boundaries, and event-driven communication between modules.

### Rationale
- **Team size**: Initial team of 5 engineers — microservices overhead is too high
- **Deployment simplicity**: Single JAR deployment via Docker Compose
- **Development velocity**: Faster iteration with unified codebase
- **Extraction path**: Bounded contexts with event-driven boundaries allow future extraction to microservices
- **Data consistency**: Single database with transactional guarantees where needed

### Consequences
- **Positive**: Faster development, simpler deployment, strong consistency
- **Negative**: Scaling requires vertical scaling initially; must maintain module discipline
- **Mitigation**: Enforce module boundaries with package structure and ArchUnit tests

### Future Consideration
Re-evaluate at V3+ when user base exceeds 10,000 and team exceeds 10 engineers.

---

## ADR-002: PostgreSQL with pgvector over Specialized Vector Database

**Status:** Accepted
**Date:** 2026-01-15

### Context
We need vector storage for semantic search and AI features. Options: pgvector (PostgreSQL extension) or dedicated vector database (Qdrant, Pinecone, Weaviate).

### Decision
Use **PostgreSQL with pgvector** for V1-V2. Maintain abstraction layer to allow future migration to Qdrant if needed.

### Rationale
- **Operational simplicity**: One less infrastructure component
- **Transactional consistency**: Vectors stay consistent with relational data
- **Backup simplicity**: Single backup strategy
- **Adequate performance**: pgvector with IVFFlat indexes handles up to 1M vectors
- **Cost**: No additional infrastructure cost

### Consequences
- **Positive**: Simplified operations, ACID guarantees, single backup
- **Negative**: Performance degrades beyond 1M+ vectors; limited index types
- **Mitigation**: Abstraction layer via `VectorStoreRepository` interface; monitor query performance

---

## ADR-003: Ollama for Local AI Inference

**Status:** Accepted
**Date:** 2026-01-15

### Context
We need LLM and embedding model inference. Options: Ollama (local), OpenAI API, Anthropic API, or self-hosted model.

### Decision
Use **Ollama** for both LLM and embedding generation. Cloud APIs are optional fallbacks.

### Rationale
- **Privacy**: User data never leaves their machine
- **Offline capability**: No internet dependency for core AI features
- **Cost**: Free, no API usage costs
- **Model flexibility**: Support for 100+ models, easy to switch
- **Performance**: Sufficient for single-user and small-team scenarios

### Consequences
- **Positive**: Privacy, offline capability, zero API costs, model flexibility
- **Negative**: Requires Docker container for Ollama; GPU recommended for larger models
- **Mitigation**: Support remote Ollama instances; use quantized models (e.g., Mistral 7B Q4)

---

## ADR-004: React + TypeScript over Next.js

**Status:** Accepted
**Date:** 2026-01-15

### Context
Frontend framework decision for the SPA. Options: React with Vite, Next.js, or Remix.

### Decision
Use **React 18+ with TypeScript** bundled with **Vite**. No SSR/SSG framework for V1-V2.

### Rationale
- **Simplicity**: SPA is sufficient for an authenticated dashboard application
- **Bundle size**: Vite produces smaller bundles than Next.js
- **Build speed**: Vite's HMR is significantly faster
- **Deployment**: Static files served by Nginx — simpler than Node SSR
- **SEO not required**: Authenticated application, no public pages

### Consequences
- **Positive**: Fast builds, simple deployment, smaller bundle
- **Negative**: No SSR for potential public landing pages
- **Mitigation**: Add landing page as separate Next.js app if needed

---

## ADR-005: TanStack Query for Server State

**Status:** Accepted
**Date:** 2026-01-15

### Context
State management strategy for frontend. Need to handle server cache, real-time updates, and optimistic updates.

### Decision
Use **TanStack Query** for server state management and **Zustand** for client state only.

### Rationale
- **TanStack Query**: Automatic caching, refetching, pagination, optimistic updates, WebSocket integration
- **Zustand**: Minimal boilerplate for client-only state (theme, sidebar, modals)
- **No Redux**: Overkill for this application's complexity level
- **Separation of concerns**: Server state (TanStack Query) vs client state (Zustand)

### Consequences
- **Positive**: Clean separation, excellent DX, built-in WebSocket integration
- **Negative**: Learning curve for team unfamiliar with TanStack Query
- **Mitigation**: Documentation and patterns in codebase

---

## ADR-006: WebSocket for Real-Time Updates

**Status:** Accepted
**Date:** 2026-01-15

### Context
Need real-time updates for multi-device sync, AI enrichment progress, and collaboration.

### Decision
Use **Spring WebSocket** with STOMP protocol for real-time communication. SockJS as fallback.

### Rationale
- **Spring ecosystem**: Native integration with Spring Boot and Spring Security
- **STOMP**: Simple text-oriented protocol, easy to use from JavaScript
- **Topic-based routing**: Natural fit for container-specific and user-specific channels
- **SockJS fallback**: Handles environments where WebSocket is blocked

### Consequences
- **Positive**: Tight Spring integration, topic routing, browser compatibility
- **Negative**: Stateful; requires sticky sessions or external broker for multi-instance
- **Mitigation**: Use Redis as external message broker for multi-instance deployment

---

## ADR-007: Soft Delete for Containers

**Status:** Accepted
**Date:** 2026-01-15

### Context
Data retention strategy. Options: hard delete, soft delete with tombstone, or soft delete with archive.

### Decision
Use **soft delete** with a `deleted_at` timestamp. Data is permanently purged after 30 days via scheduled job.

### Rationale
- **Recovery**: Users can restore accidentally deleted containers
- **Audit**: Deletion events are recorded in timeline
- **Data integrity**: Related data (snapshots, timeline) remains valid
- **Compliance**: GDPR right to erasure handled via hard delete endpoint

### Consequences
- **Positive**: Undo capability, audit trail, data integrity
- **Negative**: Additional storage for deleted records
- **Mitigation**: Scheduled purge job after 30 days; filtered queries exclude deleted

---

## ADR-008: Event-Driven AI Enrichment

**Status:** Accepted
**Date:** 2026-01-15

### Context
How to trigger AI enrichment (summarization, tagging, embedding) when containers are created or updated.

### Decision
Use **event-driven architecture** with a message queue (RabbitMQ). AI enrichment is fully asynchronous and non-blocking.

### Rationale
- **Non-blocking UI**: Container CRUD returns immediately, enrichment happens in background
- **Retry and resilience**: Failed enrichment can be retried independently
- **Load management**: Enrichment queue prevents overwhelming Ollama
- **Decoupling**: AI layer can be scaled independently

### Consequences
- **Positive**: Fast API responses, resilient to AI failures, load management
- **Negative**: Added infrastructure complexity (RabbitMQ)
- **Mitigation**: Use Redis pub/sub as simpler alternative for V1 if RabbitMQ is overkill

---

## ADR-009: Metadata as JSONB for Flexibility

**Status:** Accepted
**Date:** 2026-01-15

### Context
Container types have different metadata schemas (book needs ISBN, movie needs director, etc.). Need flexible schema storage.

### Decision
Store type-specific metadata as **JSONB columns** in PostgreSQL with validation at the application layer.

### Rationale
- **Schema flexibility**: Each container type has different metadata without schema changes
- **JSONB indexing**: GIN indexes for efficient JSONB queries
- **Application validation**: TypeScript types + Java validation ensure data quality
- **No EAV anti-pattern**: Avoid entity-attribute-value tables

### Consequences
- **Positive**: Flexible schema, queryable JSONB, no migrations for new metadata fields
- **Negative**: Validation must be enforced at application layer
- **Mitigation**: Comprehensive validation on create/update; OpenAPI docs for metadata schemas
