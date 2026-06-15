# Indexing Strategy

## Index Design Principles

1. **Query-driven**: Indexes are designed based on actual query patterns, not theoretical ones
2. **Coverage monitoring**: Track which indexes are actually used via `pg_stat_user_indexes`
3. **Write impact awareness**: Each index adds overhead to writes; balance read vs write performance
4. **Progressive indexing**: Start with essential indexes, add as needed based on slow query log
5. **Periodic maintenance**: Regular `REINDEX` and `ANALYZE` to maintain index performance

## Current Indexes

### High-Priority Indexes (Critical Path)

```sql
-- ============================================
-- Container queries (most frequent operations)
-- ============================================

-- 1. Find containers by owner (dashboard, list views)
CREATE INDEX idx_containers_owner ON containers(owner_id);
-- Type: B-tree
-- Size estimate: ~20MB per 1M containers
-- Query: SELECT * FROM containers WHERE owner_id = ?
-- Impact: Critical - without this, every user query is a full table scan

-- 2. Filter by container type
CREATE INDEX idx_containers_type ON containers(type);
-- Type: B-tree
-- Use case: Filtering by type in search/dashboard
-- Query: SELECT * FROM containers WHERE type = 'BOOK' AND owner_id = ?

-- 3. Filter by status (active, completed, etc.)
CREATE INDEX idx_containers_status ON containers(status);
-- Type: B-tree (low selectivity, but combined in queries)

-- 4. Sort by creation date (default sort order)
CREATE INDEX idx_containers_created ON containers(created_at DESC);
-- Type: B-tree DESC
-- Query: SELECT * FROM containers ORDER BY created_at DESC LIMIT 20

-- 5. Composite: Owner + Type (common filter combination)
CREATE INDEX idx_containers_owner_type ON containers(owner_id, type);
-- Type: B-tree composite
-- Benefit: Index-only scans when filtering by owner and type
-- Query: SELECT id, title, status FROM containers WHERE owner_id = ? AND type = 'BOOK'

-- 6. Composite: Owner + Status (common filter)
CREATE INDEX idx_containers_owner_status ON containers(owner_id, status);
-- Type: B-tree composite
-- Query: SELECT * FROM containers WHERE owner_id = ? AND status = 'ACTIVE'

-- ============================================
-- Full-Text Search
-- ============================================

-- 7. Full-text search on title and description
CREATE INDEX idx_containers_search ON containers
    USING GIN (to_tsvector('english', title || ' ' || COALESCE(description, '')));
-- Type: GIN
-- Size estimate: ~50MB per 1M containers
-- Query: SELECT * FROM containers WHERE to_tsvector('english', title || ' ' || description) @@ to_tsquery('english', 'search_term')
-- Note: Requires trigger to keep in sync

-- ============================================
-- JSONB Metadata Queries
-- ============================================

-- 8. JSONB metadata queries (e.g., find books by ISBN)
CREATE INDEX idx_containers_metadata ON containers USING GIN (metadata jsonb_path_ops);
-- Type: GIN (jsonb_path_ops is faster and smaller than default)
-- Query: SELECT * FROM containers WHERE metadata @> '{"isbn": "9780143127741"}'
-- Note: jsonb_path_ops does not support all JSONB operators; use default GIN if needed

-- ============================================
-- Vector Search (V2)
-- ============================================

-- 9. Vector similarity search
CREATE INDEX idx_containers_embedding ON containers
    USING IVFFLAT (embedding vector_cosine_ops) WITH (lists = 100);
-- Type: IVFFLAT (Inverted File with Flat Compression)
-- Lists: 100 (sqrt(n) rule for 10k vectors, adjust based on actual count)
-- Build time: ~5 minutes per 100k vectors
-- Query: SELECT * FROM containers ORDER BY embedding <=> '[0.1, 0.2, ...]' LIMIT 10
-- Trade-off: IVFFLAT probes = sqrt(100) = 10 for good recall; adjust probes for speed vs accuracy
```

### Medium-Priority Indexes

```sql
-- ============================================
-- Timeline queries
-- ============================================

-- 10. Timeline events by container (chronological)
CREATE INDEX idx_timeline_container ON timeline_events(container_id, created_at DESC);
-- Type: B-tree composite DESC
-- Query: Timeline view for a specific container

-- 11. Timeline events by type (analytics queries)
CREATE INDEX idx_timeline_type ON timeline_events(event_type);
-- Type: B-tree

-- 12. Recent events (activity feed)
CREATE INDEX idx_timeline_created ON timeline_events(created_at DESC);
-- Type: B-tree DESC

-- ============================================
-- Snapshot queries
-- ============================================

-- 13. Snapshots by container (version order)
CREATE INDEX idx_snapshots_container ON snapshots(container_id, version DESC);
-- Type: B-tree composite DESC

-- ============================================
-- Tag queries
-- ============================================

-- 14. Tags by owner
CREATE INDEX idx_tags_owner ON tags(owner_id);
-- Type: B-tree

-- 15. Tags by name (search/autocomplete)
CREATE INDEX idx_tags_name ON tags(owner_id, name);
-- Type: B-tree composite

-- ============================================
-- AI Context
-- ============================================

-- 16. AI contexts by enrichment status (for background jobs)
CREATE INDEX idx_ai_contexts_status ON ai_contexts(enrichment_status);
-- Type: B-tree
-- Query: SELECT * FROM ai_contexts WHERE enrichment_status = 'PENDING'
-- Use case: AI enrichment worker picks up pending items
```

### Low-Priority / Future Indexes

```sql
-- 17. Knowledge graph edges by type (graph traversal)
CREATE INDEX idx_knowledge_edges_type ON knowledge_edges(relationship_type);

-- 18. Recommendations by score (ranking)
CREATE INDEX idx_recommendations_user_score ON recommendations(user_id, score DESC);

-- 19. Unviewed recommendations
CREATE INDEX idx_recommendations_unviewed ON recommendations(user_id, viewed);
```

## Index Maintenance

```sql
-- ============================================
-- Monitoring queries
-- ============================================

-- Find unused indexes
SELECT schemaname, tablename, indexname, idx_scan
FROM pg_stat_user_indexes
WHERE idx_scan = 0 AND indexname NOT LIKE '%_pkey'
ORDER BY tablename;

-- Find missing indexes (sequential scans on large tables)
SELECT schemaname, relname, seq_scan, seq_tup_read, idx_scan
FROM pg_stat_user_tables
WHERE seq_scan > 1000 AND seq_tup_read > 100000
ORDER BY seq_tup_read DESC;

-- Index size
SELECT indexname, pg_size_pretty(pg_relation_size(indexname::regclass))
FROM pg_indexes
WHERE tablename = 'containers';

-- ============================================
-- Rebuilding indexes (low-traffic window)
-- ============================================

-- Concurrent rebuild (non-blocking)
REINDEX INDEX CONCURRENTLY idx_containers_search;

-- Full table reindex (blocking, but more thorough)
REINDEX TABLE CONCURRENTLY containers;
```

## Index Sizing Estimates

| Index | Size per 1M rows | Notes |
|---|---|---|
| containers_pkey (PK) | ~25MB | Primary key B-tree |
| idx_containers_owner | ~30MB | UUID B-tree |
| idx_containers_type | ~15MB | Low cardinality B-tree |
| idx_containers_created | ~30MB | Descending B-tree |
| idx_containers_embedding | ~600MB | IVFFLAT (largest index) |
| idx_containers_search | ~200MB | GIN inverted index |
| idx_timeline_container | ~40MB | Composite B-tree |
| idx_knowledge_edges | ~20MB | Composite B-tree |

**Total index size estimate for 1M containers + related data: ~1.5GB**

## Partial Indexes

```sql
-- Only index active containers (most queries filter by status != DELETED)
CREATE INDEX idx_containers_active ON containers(owner_id)
    WHERE status != 'DELETED';
-- Benefit: Smaller index, faster reads for common case

-- Only index pending AI enrichment (worker queries)
CREATE INDEX idx_ai_contexts_pending ON ai_contexts(created_at)
    WHERE enrichment_status = 'PENDING';
-- Benefit: Worker picks up pending items quickly
```

## Index Strategy by Query Pattern

| Query Pattern | Index Strategy | Example |
|---|---|---|
| Owner's containers list | Composite B-tree (owner + sort_field) | `WHERE owner_id = ? ORDER BY created_at DESC` |
| Type filtering | Composite B-tree (owner + type) | `WHERE owner_id = ? AND type = 'BOOK'` |
| Full-text search | GIN on tsvector | `WHERE search_vector @@ to_tsquery(?)` |
| Semantic search | IVFFLAT on embedding | `ORDER BY embedding <=> ? LIMIT 10` |
| JSONB metadata lookup | GIN on metadata (jsonb_path_ops) | `WHERE metadata @> ?` |
| Timeline view | Composite B-tree (container_id + created_at) | `WHERE container_id = ? ORDER BY created_at DESC` |
| Tag filtering | JOIN via container_tags with indexes | `WHERE tag_id IN (?)` |
| Status filtering | Composite B-tree (owner + status) | `WHERE owner_id = ? AND status = ?` |
