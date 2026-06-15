# High-Level Architecture

## Architecture Overview

ContextOS follows a **modular monolith** architecture with clear domain boundaries, designed to be extracted into microservices when scale warrants it.

```plantuml
@startuml ContextOS_HLD_Overview
!define RECTANGLE class

skinparam backgroundColor #f8f9fa
skinparam componentStyle rectangle

layer "Presentation Layer" as PRES {
  [React SPA] as SPA
  [Browser Extension] as EXT
  [VSCode Extension] as VS
}

layer "API Gateway Layer" as GATEWAY {
  [Nginx Reverse Proxy] as NGINX
  [Spring Security] as SEC
}

layer "Application Layer" as APP {
  [Container Service] as CONT
  [Tag Service] as TAG
  [Snapshot Service] as SNAP
  [Timeline Service] as TIM
  [Search Service] as SEARCH
  [Auth Service] as AUTH
  [User Service] as USER
}

layer "AI Layer" as AI {
  [Context Engine] as CTX
  [Embedding Service] as EMBED
  [RAG Pipeline] as RAG
  [Recommendation Engine] as REC
  [Knowledge Graph] as KG
}

layer "Infrastructure Layer" as INFRA {
  [PostgreSQL] as PG
  [Redis] as RD
  [Vector Store] as VS_DB
  [RabbitMQ / Kafka] as MQ
  [Ollama] as OLLAMA
}

SPA --> NGINX
EXT --> NGINX
VS --> NGINX
NGINX --> SEC
SEC --> CONT
SEC --> TAG
SEC --> SNAP
SEC --> TIM
SEC --> SEARCH
SEC --> AUTH
SEC --> USER
SEC --> CTX

CONT --> PG
TAG --> PG
SNAP --> PG
TIM --> PG
SEARCH --> VS_DB
AUTH --> PG
USER --> PG

CTX --> EMBED
CTX --> RAG
CTX --> REC
CTX --> KG
EMBED --> OLLAMA
EMBED --> VS_DB
RAG --> OLLAMA
RAG --> VS_DB
RAG --> PG
REC --> VS_DB
KG --> PG
KG --> VS_DB

CONT -.-> MQ
SNAP -.-> MQ
MQ -.-> CTX

CONT --> RD
TAG --> RD
SEARCH --> RD

@enduml
```

## Architecture Principles

| Principle | Description |
|---|---|
| **Domain-Driven Design** | Each bounded context has its own domain model, services, and data |
| **Event-Driven Communication** | Async events between domains via message broker |
| **API-First** | All functionality accessible via REST API |
| **Offline-Capable** | Core features work without network connectivity |
| **AI-Native** | AI enrichment is baked into data lifecycle, not bolted on |
| **Observable by Default** | Metrics, logs, and traces for every component |

## System Context

```plantuml
@startuml System_Context
!define RECTANGLE class

actor "User" as User
actor "Admin" as Admin

system "ContextOS" as COS {
  [Web Application]
  [REST API]
  [AI Engine]
}

system "Ollama" as OLL {
  [LLM]
  [Embeddings]
}

system "Email Service" as SMTP
system "GitHub" as GH
system "Browser" as BR

User --> COS : "Manages containers"
User --> COS : "Searches knowledge"
User --> COS : "Asks AI questions"
Admin --> COS : "Manages users"
COS --> OLL : "Generates embeddings"
COS --> OLL : "LLM inference"
COS --> SMTP : "Sends emails"
BR --> COS : "Web page capture (extension)"
GH --> COS : "OAuth2 authentication"
@enduml
```

## Component Overview

### 1. Presentation Layer
- **React SPA**: Single-page application with TypeScript, Tailwind CSS
- **Browser Extension**: Chrome/Firefox for web content capture
- **VSCode Extension**: IDE integration for developers

### 2. API Gateway Layer
- **Nginx**: Reverse proxy, TLS termination, rate limiting
- **Spring Security**: Authentication, authorization, CSRF protection

### 3. Application Layer (Bounded Contexts)
| Context | Responsibility | Key Entities |
|---|---|---|
| Container | Core container CRUD, type system, validation | Container, ContainerType, Metadata |
| Tag | Tag management, assignment, search | Tag, ContainerTag |
| Snapshot | Versioning, diff, restore | Snapshot, SnapshotDiff |
| Timeline | Event recording, queries | TimelineEvent |
| Search | Full-text + semantic search | SearchIndex |
| Auth | Authentication, authorization, sessions | User, Role, Session |
| User | Profile, preferences, settings | UserProfile, UserSettings |

### 4. AI Layer
- **Context Engine**: Orchestrates AI enrichment pipeline
- **Embedding Service**: Generates and stores vector embeddings
- **RAG Pipeline**: Retrieval-Augmented Generation for Q&A
- **Recommendation Engine**: Suggests related containers
- **Knowledge Graph**: Entity extraction and relationship mapping

### 5. Infrastructure Layer
- **PostgreSQL**: Primary data store
- **Redis**: Caching, session store, rate limiting
- **Vector Store**: pgvector or Qdrant for embeddings
- **Message Queue**: RabbitMQ or Kafka for async events
- **Ollama**: Local LLM for AI inference

## Request Flow Patterns

### Synchronous Flow (CRUD)
```
Client → Nginx → Spring Security → Controller → Service → Repository → Database
                                                                    → Redis (cache)
                                                                    → Message Queue (event)
```

### Asynchronous Flow (AI Enrichment)
```
Service → Message Queue → Consumer → AI Service → Embedding Service → Vector Store
                                               → Context Engine → Database
                                               → WebSocket → Client
```

### Search Flow
```
Client → Controller → Search Service → Full-Text Search (PostgreSQL)
                                     → Vector Search (Vector Store)
                                     → Hybrid Ranker
                                     → Response
```

## Deployment Architecture

```plantuml
@startuml Deployment_HLD
!define RECTANGLE class

node "User Device" {
  [Browser] as BR
  [VSCode] as VS
}

node "Server (Docker Host)" {
  docker_container "Nginx" as NGX
  docker_container "ContextOS API" as API {
    [Spring Boot JAR]
  }
  docker_container "ContextOS Frontend" as FE {
    [Nginx + React Build]
  }
  docker_container "Redis" as RD
  docker_container "PostgreSQL" as PG
  docker_container "Ollama" as OLL
  docker_container "RabbitMQ" as MQ
}

BR --> NGX : HTTPS
VS --> NGX : HTTPS
NGX --> API : Proxy
NGX --> FE : Static Files
API --> RD : Cache
API --> PG : Persistence
API --> MQ : Events
API --> OLL : AI Inference
@enduml
```
