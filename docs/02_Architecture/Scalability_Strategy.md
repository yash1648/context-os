# Scalability Strategy

## Scalability Tiers

```
Tier 1: Single User (V1)
├── Docker Compose
├── 1 API instance
├── 1 PostgreSQL instance
└── Runs on laptop / VPS with 4GB RAM

Tier 2: Small Team (V2-V3)
├── Docker Compose / Swarm
├── 2-3 API instances behind Nginx
├── PostgreSQL read replica
├── Redis cluster
└── Dedicated Ollama server

Tier 3: Community (V4)
├── Kubernetes (k3s / EKS)
├── 5-10 API instances (auto-scaled)
├── PostgreSQL with connection pooling (PgBouncer)
├── Qdrant cluster for vector search
├── RabbitMQ cluster
└── GPU node for Ollama

Tier 4: Enterprise (V5)
├── Kubernetes with multi-region
├── 20+ API instances
├── PostgreSQL sharding
├── Dedicated AI service instances
├── CDN for static assets
└── Global Redis replication
```

## Horizontal Scaling Strategies

### API Layer

```yaml
Scaling Strategy:
  approach: Horizontal (add more instances)
  load_balancer: Nginx round-robin
  session: Stateless JWT (no session affinity needed)
  
  Auto-scaling triggers:
    - CPU > 70% for 5 minutes
    - Request latency p95 > 500ms
    - Active connections > 1000 per instance
    
  Instance startup time: 30 seconds (Spring Boot)
```

### Database Layer

```sql
-- Connection pooling with PgBouncer
-- Read replicas for query offloading
-- Table partitioning for large tables

-- Partition containers by creation date
CREATE TABLE containers (
    id UUID,
    type VARCHAR(50),
    title VARCHAR(500),
    owner_id UUID,
    created_at TIMESTAMP,
    -- ...
) PARTITION BY RANGE (created_at);

-- Create monthly partitions
CREATE TABLE containers_2026_01 PARTITION OF containers
    FOR VALUES FROM ('2026-01-01') TO ('2026-02-01');
CREATE TABLE containers_2026_02 PARTITION OF containers
    FOR VALUES FROM ('2026-02-01') TO ('2026-03-01');
```

### Cache Layer

```yaml
Redis Scaling:
  tier_1: Single Redis instance
  tier_2: Redis with replication (1 primary, 1 replica)
  tier_3: Redis Cluster (3 primary, 3 replica)
  tier_4: Redis Cluster with global replication
  
  cache_strategies:
    containers: Cache-aside with TTL 5min
    search_results: Cache-aside with TTL 1min
    user_sessions: Write-through
    rate_limits: Write-through with TTL sliding window
```

### AI Layer

```yaml
Ollama Scaling:
  tier_1: Local Ollama on same machine
  tier_2: Dedicated Ollama server
  tier_3: Multiple Ollama instances with model sharding
  tier_4: GPU cluster with Ollama load balancing
  
  model_strategy:
    embedding: nomic-embed-text (fast, 2GB RAM)
    generation: Mistral 7B Q4 (balanced, 4GB RAM)
    fallback: Llama 3.2 3B (lightweight, 2GB RAM)
```

## Performance Targets

| Metric | V1 Target | V2 Target | V3+ Target |
|---|---|---|---|
| API p95 latency | < 100ms | < 100ms | < 80ms |
| Search p95 latency | < 300ms | < 500ms | < 300ms |
| AI enrichment (per container) | — | < 10s | < 5s |
| WebSocket latency | < 500ms | < 200ms | < 100ms |
| Concurrent users | 10 | 100 | 1000+ |
| Containers per user | 1000 | 5000 | 50000+ |
| Uptime | 99% | 99.5% | 99.9% |

## Bottleneck Mitigation

| Bottleneck | Mitigation | Phase |
|---|---|---|
| Database queries | Indexing, connection pooling, read replicas | V1 |
| Search performance | pgvector IVFFlat indexes, hybrid search optimization | V2 |
| AI enrichment queue | Multiple consumers, priority queue | V2 |
| WebSocket connections | Sticky sessions, Redis pub/sub | V2 |
| File/asset storage | CDN, S3-compatible storage | V3 |
| Heavy AI workloads | GPU instance, batch processing | V3 |
| Cross-region latency | Global Redis, CDN, region-local DB | V5 |
