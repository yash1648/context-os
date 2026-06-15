# Migration Strategy

## Technology

**Flyway** with versioned SQL migrations. All migrations are forward-only with optional repeatable migrations for views and functions.

## Migration Structure

```
backend/src/main/resources/db/migration/
├── V1__initial_schema.sql
├── V2__add_embeddings.sql
├── V3__add_knowledge_graph.sql
├── V4__add_recommendations.sql
├── V5__add_search_log.sql
├── V6__add_activity_feed.sql
├── V7__add_container_partitions.sql
├── R__container_stats_view.sql
└── R__search_functions.sql
```

## Migration Design Principles

1. **Forward-only**: Never modify a migration that has been applied to production
2. **Idempotent where possible**: Use `IF NOT EXISTS` / `IF EXISTS` for safety
3. **Backward compatible**: Old application versions should work with new schema
4. **Zero-downtime capable**: Migrations should not lock tables for extended periods
5. **Rollback plans**: Each migration must have a documented rollback strategy

## Core Migrations

### V1: Initial Schema

```sql
-- 001: Core tables (users, containers, tags, timeline, snapshots, pins)
-- Applied at: V1 MVP launch
-- Rollback: Restore from backup

CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS pgvector;

-- Users table
CREATE TABLE users ( ... );  -- As defined in Database_Design.md

-- Containers table  
CREATE TABLE containers ( ... );

-- Tags + container_tags
CREATE TABLE tags ( ... );
CREATE TABLE container_tags ( ... );

-- Timeline
CREATE TABLE timeline_events ( ... );

-- Snapshots
CREATE TABLE snapshots ( ... );

-- Pins
CREATE TABLE pins ( ... );
```

### V2: AI Embeddings

```sql
-- 002: Add AI context and embedding support
-- Applied at: V2 AI launch
-- Rollback: DROP TABLE ai_contexts, DROP COLUMN embedding

CREATE TABLE ai_contexts ( ... );  -- As defined in Database_Design.md
ALTER TABLE containers ADD COLUMN embedding VECTOR(1536);
CREATE INDEX idx_containers_embedding ON containers USING IVFFLAT (embedding vector_cosine_ops);
```

### V3: Knowledge Graph

```sql
-- 003: Knowledge graph tables
-- Applied at: V2 AI launch (post-embedding)
-- Rollback: DROP TABLE knowledge_edges, knowledge_nodes

CREATE TABLE knowledge_nodes ( ... );
CREATE TABLE knowledge_edges ( ... );
```

### V4: Recommendations

```sql
-- 004: Recommendation tracking
-- Applied at: V2 AI launch
-- Rollback: DROP TABLE recommendations

CREATE TABLE recommendations ( ... );
```

## Migration Execution

```yaml
# application.yml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    baseline-version: 0
    validate-on-migrate: true
    out-of-order: false
```

## Zero-Downtime Migration Pattern

```sql
-- Pattern for adding a nullable column
-- Step 1: Add column (nullable, no index)
ALTER TABLE containers ADD COLUMN IF NOT EXISTS new_field VARCHAR(100);

-- Step 2: Deploy application code that populates the column

-- Step 3: Add NOT NULL constraint and index (separate migration)
-- Do NOT do this in production until Step 2 has populated all rows
ALTER TABLE containers ALTER COLUMN new_field SET NOT NULL;
CREATE INDEX idx_containers_new_field ON containers(new_field);
```

## Migration Testing

```bash
# Test migrations in CI
./mvnw flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/contextos_test

# Verify migration state
./mvnw flyway:info -Dspring.profiles.active=test

# Repair if checksums change (development only)
./mvnw flyway:repair -Dspring.profiles.active=dev
```

## Rollback Strategy

| Migration | Rollback Method | Time |
|---|---|---|
| V1 (Initial Schema) | Restore from backup | 1 hour |
| V2 (Embeddings) | `DROP TABLE ai_contexts`, `ALTER TABLE containers DROP COLUMN embedding` | 5 min |
| V3 (Knowledge Graph) | `DROP TABLE knowledge_edges, knowledge_nodes` | 2 min |
| V4 (Recommendations) | `DROP TABLE recommendations` | 1 min |
| Later migrations | Application-level revert + schema revert | 15 min |
