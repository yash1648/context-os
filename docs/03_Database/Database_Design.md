# Database Design

## Schema Overview

```
Database: contextos
Engine: PostgreSQL 16
Extensions: pgvector, pgcrypto, pg_stat_statements
Encoding: UTF-8
Collation: en_US.UTF-8
```

## Entity Relationship Overview

### Core Tables

```sql
-- ============================================
-- USERS & AUTHENTICATION
-- ============================================

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    avatar_url VARCHAR(500),
    email_verified BOOLEAN DEFAULT FALSE,
    mfa_enabled BOOLEAN DEFAULT FALSE,
    mfa_secret VARCHAR(255),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    settings JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_login_at TIMESTAMPTZ
);

CREATE INDEX idx_users_email ON users(email);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);

-- ============================================
-- CONTAINERS (Core Entity)
-- ============================================

CREATE TABLE containers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    progress_percentage INTEGER DEFAULT 0,
    progress_data JSONB DEFAULT '{}',
    metadata JSONB DEFAULT '{}',
    ai_context_id UUID,
    embedding VECTOR(1536),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT valid_type CHECK (type IN (
        'BOOK', 'MOVIE', 'TV_SERIES', 'COURSE',
        'LEARNING_PROGRESS', 'SOFTWARE_PROJECT', 'GOAL',
        'HABIT', 'NOTE', 'SNAPSHOT', 'PINNED_CONTENT',
        'KNOWLEDGE_ASSET'
    )),
    CONSTRAINT valid_status CHECK (status IN (
        'DRAFT', 'ACTIVE', 'COMPLETED', 'ARCHIVED', 'DELETED'
    )),
    CONSTRAINT valid_progress CHECK (progress_percentage BETWEEN 0 AND 100)
);

CREATE INDEX idx_containers_owner ON containers(owner_id);
CREATE INDEX idx_containers_type ON containers(type);
CREATE INDEX idx_containers_status ON containers(status);
CREATE INDEX idx_containers_created ON containers(created_at DESC);
CREATE INDEX idx_containers_owner_type ON containers(owner_id, type);
CREATE INDEX idx_containers_owner_status ON containers(owner_id, status);

-- Full-text search index
CREATE INDEX idx_containers_search ON containers
    USING GIN (to_tsvector('english', title || ' ' || COALESCE(description, '')));

-- GIN index for JSONB metadata queries
CREATE INDEX idx_containers_metadata ON containers USING GIN (metadata jsonb_path_ops);

-- Vector index for embeddings
CREATE INDEX idx_containers_embedding ON containers
    USING IVFFLAT (embedding vector_cosine_ops) WITH (lists = 100);

-- ============================================
-- TAGS
-- ============================================

CREATE TABLE tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    color VARCHAR(7) DEFAULT '#6366f1',
    description VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(owner_id, name)
);

CREATE INDEX idx_tags_owner ON tags(owner_id);
CREATE INDEX idx_tags_name ON tags(owner_id, name);

CREATE TABLE container_tags (
    container_id UUID NOT NULL REFERENCES containers(id) ON DELETE CASCADE,
    tag_id UUID NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (container_id, tag_id)
);

CREATE INDEX idx_container_tags_tag ON container_tags(tag_id);

-- ============================================
-- TIMELINE
-- ============================================

CREATE TABLE timeline_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    container_id UUID NOT NULL REFERENCES containers(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL,
    description TEXT,
    old_value TEXT,
    new_value TEXT,
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_timeline_container ON timeline_events(container_id, created_at DESC);
CREATE INDEX idx_timeline_type ON timeline_events(event_type);
CREATE INDEX idx_timeline_created ON timeline_events(created_at DESC);

-- ============================================
-- SNAPSHOTS
-- ============================================

CREATE TABLE snapshots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    container_id UUID NOT NULL REFERENCES containers(id) ON DELETE CASCADE,
    label VARCHAR(200),
    snapshot_data JSONB NOT NULL,
    metadata JSONB DEFAULT '{}',
    version INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(container_id, version)
);

CREATE INDEX idx_snapshots_container ON snapshots(container_id, version DESC);

-- ============================================
-- PINS
-- ============================================

CREATE TABLE pins (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    container_id UUID NOT NULL REFERENCES containers(id) ON DELETE CASCADE,
    pinned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(owner_id, container_id)
);

CREATE INDEX idx_pins_owner ON pins(owner_id, pinned_at DESC);

-- ============================================
-- AI CONTEXT
-- ============================================

CREATE TABLE ai_contexts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    container_id UUID NOT NULL UNIQUE REFERENCES containers(id) ON DELETE CASCADE,
    summary TEXT,
    summary_model VARCHAR(100),
    summary_generated_at TIMESTAMPTZ,
    auto_tags TEXT[],
    auto_tag_scores FLOAT[],
    entities JSONB DEFAULT '[]',
    relationships JSONB DEFAULT '[]',
    enrichment_version INTEGER DEFAULT 1,
    enrichment_status VARCHAR(20) DEFAULT 'PENDING',
    last_enriched_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT valid_enrichment_status CHECK (enrichment_status IN (
        'PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'
    ))
);

CREATE INDEX idx_ai_contexts_status ON ai_contexts(enrichment_status);
CREATE INDEX idx_ai_contexts_container ON ai_contexts(container_id);

-- ============================================
-- KNOWLEDGE GRAPH
-- ============================================

CREATE TABLE knowledge_nodes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    label VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    source_container_id UUID REFERENCES containers(id) ON DELETE SET NULL,
    metadata JSONB DEFAULT '{}',
    embedding VECTOR(1536),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_knowledge_nodes_owner ON knowledge_nodes(owner_id);
CREATE INDEX idx_knowledge_nodes_type ON knowledge_nodes(type);

CREATE TABLE knowledge_edges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_node_id UUID NOT NULL REFERENCES knowledge_nodes(id) ON DELETE CASCADE,
    target_node_id UUID NOT NULL REFERENCES knowledge_nodes(id) ON DELETE CASCADE,
    relationship_type VARCHAR(100) NOT NULL,
    weight FLOAT DEFAULT 1.0,
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(source_node_id, target_node_id, relationship_type)
);

CREATE INDEX idx_knowledge_edges_source ON knowledge_edges(source_node_id);
CREATE INDEX idx_knowledge_edges_target ON knowledge_edges(target_node_id);
CREATE INDEX idx_knowledge_edges_type ON knowledge_edges(relationship_type);

-- ============================================
-- RECOMMENDATIONS
-- ============================================

CREATE TABLE recommendations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    source_container_id UUID REFERENCES containers(id) ON DELETE CASCADE,
    recommended_container_id UUID REFERENCES containers(id) ON DELETE CASCADE,
    score FLOAT NOT NULL,
    reason VARCHAR(500),
    recommendation_type VARCHAR(50) NOT NULL,
    viewed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_recommendations_user ON recommendations(user_id, score DESC);
CREATE INDEX idx_recommendations_viewed ON recommendations(user_id, viewed);

-- ============================================
-- SEARCH LOG
-- ============================================

CREATE TABLE search_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    query TEXT NOT NULL,
    search_type VARCHAR(20) NOT NULL, -- 'FULLTEXT', 'SEMANTIC', 'HYBRID'
    result_count INTEGER DEFAULT 0,
    execution_ms INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_search_log_user ON search_log(user_id, created_at DESC);

-- ============================================
-- ACTIVITY FEED
-- ============================================

CREATE TABLE activity_feed (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    activity_type VARCHAR(50) NOT NULL,
    container_id UUID REFERENCES containers(id) ON DELETE SET NULL,
    description TEXT,
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_activity_user ON activity_feed(user_id, created_at DESC);
```

## Entity Relationship Diagram (Text)

```
┌──────────┐        ┌──────────────┐        ┌─────────────┐
│  users   │────1:N→│  containers  │────1:N→│  snapshots  │
└──────────┘        └──────────────┘        └─────────────┘
     │                     │1:1                      │
     │                     │                         │
     │1:N           ┌──────┴──────┐                  │
     │              │             │                   │
     ▼              ▼             ▼                   │
┌──────────┐  ┌──────────┐  ┌──────────┐            │
│  tags    │  │  timeline│  │pins      │            │
└──────────┘  └──────────┘  └──────────┘            │
     │                                              │
     │M:N                                            │
     ▼                                              │
┌──────────────┐        ┌──────────────┐            │
│ container_tags│      │ ai_contexts  │◄───────────┘
└──────────────┘        └─────┬────────┘
                              │
                              ▼
                    ┌─────────────────┐
                    │ knowledge_nodes  │
                    └────────┬────────┘
                             │
                             │M:N
                             ▼
                    ┌─────────────────┐
                    │ knowledge_edges │
                    └─────────────────┘
```

## Container Type-Specific Metadata Schemas

### Book Container
```json
{
  "isbn": "9780143127741",
  "author": "Daniel Kahneman",
  "pageCount": 499,
  "currentPage": 250,
  "genre": "Psychology",
  "publisher": "Farrar, Straus and Giroux",
  "publishedYear": 2011,
  "readingStatus": "READING"
}
```

### Movie Container
```json
{
  "director": "Christopher Nolan",
  "releaseYear": 2010,
  "durationMinutes": 148,
  "genre": "Sci-Fi",
  "imdbId": "tt1375666",
  "imdbRating": "8.8",
  "watchStatus": "COMPLETED"
}
```

### Goal Container
```json
{
  "objective": "Run a marathon",
  "keyResults": [
    {"description": "Run 5K without stopping", "target": 1, "current": 1},
    {"description": "Run 10K without stopping", "target": 1, "current": 0},
    {"description": "Complete half marathon", "target": 1, "current": 0},
    {"description": "Complete full marathon", "target": 1, "current": 0}
  ],
  "deadline": "2026-12-31",
  "category": "HEALTH"
}
```

### Software Project Container
```json
{
  "repositoryUrl": "https://github.com/user/project",
  "techStack": ["React", "TypeScript", "Spring Boot", "PostgreSQL"],
  "projectType": "WEB_APP",
  "license": "MIT",
  "startDate": "2026-01-15",
  "targetDate": "2026-06-30",
  "milestones": [
    {"name": "MVP", "date": "2026-03-01", "completed": true},
    {"name": "Beta", "date": "2026-05-01", "completed": false}
  ]
}
```

## Data Migration Considerations

### Seed Data
```sql
-- Default tags for new users
INSERT INTO tags (owner_id, name, color) VALUES
    ($1, 'Important', '#ef4444'),
    ($1, 'Reading', '#3b82f6'),
    ($1, 'Watching', '#8b5cf6'),
    ($1, 'Learning', '#10b981'),
    ($1, 'Building', '#f59e0b'),
    ($1, 'Health', '#ec4899'),
    ($1, 'Reference', '#6366f1');
```
