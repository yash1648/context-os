# Technology Choices

## Technology Stack Overview

| Layer | Technology | Version | Justification |
|---|---|---|---|
| **Frontend Framework** | React | 18.x | Mature ecosystem, TypeScript support, large community |
| **Frontend Language** | TypeScript | 5.x | Type safety, better DX, catches errors at compile time |
| **Build Tool** | Vite | 5.x | Fast HMR, fast builds, modern ESM support |
| **CSS Framework** | Tailwind CSS | 3.x | Utility-first, rapid prototyping, small bundle size |
| **Server State** | TanStack Query | 5.x | Caching, pagination, optimistic updates, WebSocket |
| **Client State** | Zustand | 4.x | Minimal boilerplate, TypeScript-first, lightweight |
| **Routing** | React Router | 6.x | Standard React routing, loader/action patterns |
| **HTTP Client** | Axios | 1.x | Interceptors, request cancellation, better error handling |
| **UI Components** | Radix UI | Latest | Accessible, unstyled, composable primitives |
| **Forms** | React Hook Form | 7.x | Performant, minimal re-renders, validation integration |
| **Validation** | Zod | 3.x | Schema validation, TypeScript inference |

| **Backend Framework** | Spring Boot | 3.x | Production-grade, vast ecosystem, excellent Java support |
| **Backend Language** | Java | 21 | LTS, virtual threads, pattern matching, records |
| **Build Tool** | Maven | 3.9+ | Standard Java ecosystem, reliable dependency management |
| **ORM** | Spring Data JPA / Hibernate | 6.x | Mature ORM, repository pattern, query methods |
| **Database Migrations** | Flyway | 10.x | Versioned migrations, rollback support, PostgreSQL compatible |
| **API Documentation** | SpringDoc OpenAPI | 2.x | OpenAPI 3.0 generation, Swagger UI |
| **Security** | Spring Security | 6.x | Comprehensive auth, JWT support, OAuth2 |
| **Validation** | Jakarta Validation | 3.x | Bean validation API, annotation-based |
| **Messaging** | Spring AMQP (RabbitMQ) | 3.x | Reliable async messaging, dead-letter queues |
| **WebSocket** | Spring WebSocket / STOMP | 6.x | Native Spring integration, topic routing |
| **Caching** | Spring Cache + Redis | 6.x | Declarative caching, distributed |
| **Vector Search** | pgvector | 0.7+ | PostgreSQL native vector support |
| **AI Client** | Spring AI | 1.x | Unified AI abstraction, Ollama integration |

| **Database** | PostgreSQL | 16.x | Mature, feature-rich, pgvector extension |
| **Cache** | Redis | 7.x | High-performance, pub/sub, session store |
| **Message Broker** | RabbitMQ | 3.x | Reliable, management UI, dead-letter support |
| **Vector Store** | pgvector | 0.7+ | PostgreSQL extension for vector similarity |

| **AI / LLM** | Ollama | Latest | Local LLM, model management, API compatibility |
| **LLM Model** | Mistral / Llama 3 | 7B-13B | Good balance of quality and resource usage |
| **Embedding Model** | nomic-embed-text / mxbai-embed-large | Latest | High-quality embeddings for semantic search |

| **Infrastructure** | Docker Compose | Latest | Single-host orchestration, easy setup |
| **CI/CD** | GitHub Actions | — | Native GitHub integration, generous free tier |
| **Reverse Proxy** | Nginx | 1.x | TLS termination, rate limiting, static file serving |
| **Monitoring** | Prometheus + Grafana | Latest | Metrics collection, visualization, alerting |
| **Logging** | ELK Stack (Filebeat + Elasticsearch + Kibana) | 8.x | Centralized logging, search, dashboards |

## Why This Stack?

### Backend: Spring Boot
- **Maturity**: 20+ years of Java ecosystem development
- **Production readiness**: Battle-tested at enterprise scale
- **Spring AI**: Growing ecosystem for AI integration
- **Virtual threads**: Java 21 virtual threads for high concurrency
- **Security**: Spring Security is the gold standard for Java auth

### Frontend: React + Vite
- **Developer experience**: Vite HMR is instant
- **Type safety**: Full-stack TypeScript reduces bugs
- **Ecosystem**: TanStack Query, React Router, Radix UI are best-in-class
- **Bundle size**: Tree-shaking, code splitting with Vite

### Database: PostgreSQL with pgvector
- **Versatility**: Relational data + vector search in one database
- **Performance**: Handles millions of records with proper indexing
- **Reliability**: ACID compliance, point-in-time recovery
- **Extensions**: pgvector, pg_stat_statements, PostGIS future options

### AI: Ollama (Local)
- **Privacy**: All data stays on user's machine
- **Cost**: Zero API costs
- **Offline**: Full functionality without internet
- **Control**: Model selection, fine-tuning options

## Alternatives Considered

| Technology | Alternative | Why Not Chosen |
|---|---|---|
| Next.js | React + Vite | SSR overhead not needed; static SPA simpler |
| Redux | Zustand | Redux boilerplate excessive for this app |
| MongoDB | PostgreSQL | Relational data model fits better; pgvector for vectors |
| Qdrant | pgvector | Extra infrastructure; pgvector sufficient for launch |
| Kafka | RabbitMQ | Kafka overkill for current scale; RabbitMQ simpler |
| Kubernetes | Docker Compose | K8s complexity not justified for single-host deployment |
| OpenAI API | Ollama | Privacy concerns, cost, offline requirement |
| Python FastAPI | Spring Boot | Team Java expertise; Spring ecosystem for production |
