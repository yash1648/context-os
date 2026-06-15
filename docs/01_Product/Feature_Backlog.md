# Feature Backlog

## Backlog Structure

Features are organized by:
- **Epic**: High-level feature area
- **Priority**: P0 (must-have), P1 (should-have), P2 (nice-to-have), P3 (future)
- **Phase**: V1-V5
- **Story Points**: Estimated effort (1 = trivial, 13 = very complex)

---

## V1 MVP Features

### Epic: User Authentication

| ID | Feature | Priority | Points | Description |
|---|---|---|---|---|
| AUTH-01 | User Registration | P0 | 3 | Email/password registration with validation |
| AUTH-02 | User Login | P0 | 2 | JWT-based login with access + refresh tokens |
| AUTH-03 | Token Refresh | P0 | 2 | Automatic token refresh mechanism |
| AUTH-04 | User Logout | P0 | 1 | Token invalidation and session end |
| AUTH-05 | Password Reset | P1 | 3 | Email-based password reset flow |
| AUTH-06 | OAuth2 Login (Google, GitHub) | P1 | 5 | Social login integration |
| AUTH-07 | Email Verification | P1 | 2 | Verify email on registration |
| AUTH-08 | MFA Setup | P2 | 5 | TOTP-based multi-factor authentication |
| AUTH-09 | Session Management | P1 | 3 | View and revoke active sessions |

### Epic: Container Core

| ID | Feature | Priority | Points | Description |
|---|---|---|---|---|
| CON-01 | Container Domain Model | P0 | 5 | Core entity, abstract base, type registry |
| CON-02 | Container CRUD API | P0 | 8 | Create, read, update, delete endpoints |
| CON-03 | Container Type System | P0 | 5 | 12 typed container implementations |
| CON-04 | Container Validation | P0 | 3 | Type-specific validation rules |
| CON-05 | Container Search | P1 | 8 | Full-text search with filters |
| CON-06 | Bulk Container Operations | P2 | 5 | Batch create, archive, delete |
| CON-07 | Container Import/Export | P2 | 5 | JSON, CSV, Markdown |
| CON-08 | Container Duplication | P2 | 2 | Copy containers with metadata |

### Epic: Container Types

| ID | Feature | Priority | Points | Description |
|---|---|---|---|---|
| CT-01 | Book Container | P0 | 5 | ISBN, author, pages, genre, reading status |
| CT-02 | Movie Container | P0 | 5 | Director, year, duration, genre, watch status |
| CT-03 | TV Series Container | P0 | 5 | Seasons, episodes, network, watch status |
| CT-04 | Course Container | P0 | 5 | Platform, instructor, duration, completion |
| CT-05 | Learning Progress | P0 | 3 | Skill tracking, resources, level |
| CT-06 | Software Project | P0 | 5 | Tech stack, repo, status, milestones |
| CT-07 | Goal Container | P0 | 3 | Target, deadline, key results |
| CT-08 | Habit Container | P0 | 3 | Frequency, streak, reminder |
| CT-09 | Note Container | P0 | 3 | Rich text, attachments |
| CT-10 | Snapshot Container | P0 | 3 | Timestamped state capture |
| CT-11 | Pinned Content | P0 | 2 | Bookmark with context |
| CT-12 | Knowledge Asset | P0 | 5 | Snippet, reference, source |

### Epic: Tag System

| ID | Feature | Priority | Points | Description |
|---|---|---|---|---|
| TAG-01 | Tag CRUD | P0 | 3 | Create, read, update, delete tags |
| TAG-02 | Tag Assignment | P0 | 2 | Assign/remove tags to/from containers |
| TAG-03 | Tag Search | P1 | 2 | Search and filter by tags |
| TAG-04 | Tag Autocomplete | P1 | 2 | Typeahead tag suggestions |
| TAG-05 | Tag Merge | P2 | 2 | Merge duplicate tags |
| TAG-06 | Tag Hierarchy | P3 | 5 | Parent-child tag relationships |

### Epic: Snapshot System

| ID | Feature | Priority | Points | Description |
|---|---|---|---|---|
| SNAP-01 | Snapshot Creation | P0 | 5 | Capture container state at a point in time |
| SNAP-02 | Snapshot List | P1 | 3 | View snapshot history for a container |
| SNAP-03 | Snapshot Diff | P2 | 5 | Compare two snapshots |
| SNAP-04 | Snapshot Restore | P2 | 3 | Restore container to snapshot state |

### Epic: Timeline

| ID | Feature | Priority | Points | Description |
|---|---|---|---|---|
| TIM-01 | Timeline Event Recording | P0 | 3 | Auto-record container lifecycle events |
| TIM-02 | Timeline Query | P1 | 3 | Query timeline with filters |
| TIM-03 | Timeline Visualization | P2 | 5 | Visual timeline in UI |

### Epic: WebSocket

| ID | Feature | Priority | Points | Description |
|---|---|---|---|---|
| WS-01 | Change Notifications | P1 | 5 | WebSocket broadcast of container changes |
| WS-02 | User Presence | P2 | 3 | Online/offline status |
| WS-03 | Connection Recovery | P1 | 3 | Reconnect with state reconciliation |

### Epic: Dashboard

| ID | Feature | Priority | Points | Description |
|---|---|---|---|---|
| DASH-01 | Overview Dashboard | P1 | 5 | Summary counts, recent activity, progress |
| DASH-02 | Activity Feed | P1 | 3 | Recent container events in reverse chronological |
| DASH-03 | Progress Widgets | P1 | 5 | Per-type progress bars and stats |

### Epic: API & Infrastructure

| ID | Feature | Priority | Points | Description |
|---|---|---|---|---|
| INFRA-01 | Docker Compose | P0 | 3 | Backend + DB + Redis + Frontend containers |
| INFRA-02 | CI/CD Pipeline | P1 | 5 | GitHub Actions build/test/deploy |
| INFRA-03 | Database Migrations | P0 | 3 | Flyway-based migration system |
| INFRA-04 | API Documentation | P0 | 3 | Swagger/OpenAPI 3.0 |
| INFRA-05 | Health Endpoints | P1 | 2 | /health, /info, /metrics |

---

## V2 AI Features

| ID | Feature | Priority | Points | Description |
|---|---|---|---|---|
| AI-01 | Ollama Integration | P0 | 5 | Connect to local Ollama instance |
| AI-02 | Embedding Service | P0 | 8 | Generate vector embeddings for containers |
| AI-03 | Vector Search API | P0 | 8 | Semantic search with pgvector/Qdrant |
| AI-04 | Hybrid Search | P1 | 5 | Combine vector + keyword search |
| AI-05 | RAG Pipeline | P0 | 8 | Retrieve context + generate answers |
| AI-06 | Auto-Summarization | P1 | 5 | AI-generated container summaries |
| AI-07 | Auto-Tagging | P1 | 5 | AI-generated tags with confidence scores |
| AI-08 | Recommendation Engine | P1 | 8 | Similar container recommendations |
| AI-09 | Knowledge Graph | P2 | 13 | Entity extraction and relationship mapping |
| AI-10 | AI Context View | P1 | 3 | UI for AI enrichment results |
| AI-11 | AI Configuration UI | P2 | 3 | Model selection, prompt customization |

---

## V3 Browser Extension

| ID | Feature | Priority | Points | Description |
|---|---|---|---|---|
| EXT-01 | Extension Skeleton | P0 | 5 | Chrome/Firefox extension scaffolding |
| EXT-02 | Page Detection | P0 | 8 | Auto-detect page type (article, product, etc.) |
| EXT-03 | One-Click Save | P0 | 8 | Save page content to appropriate container |
| EXT-04 | Metadata Extraction | P1 | 5 | Extract title, description, author, etc. |
| EXT-05 | Extension Sidebar | P1 | 8 | Sidebar UI for quick container access |
| EXT-06 | Highlight Capture | P2 | 5 | Save highlighted text as Note |

---

## V4 VSCode Extension

| ID | Feature | Priority | Points | Description |
|---|---|---|---|---|
| VS-01 | Extension Skeleton | P0 | 5 | VSCode extension scaffolding |
| VS-02 | Project Detection | P0 | 8 | Auto-detect project type, tech stack |
| VS-03 | Code Snippet Capture | P1 | 5 | Save snippets as Knowledge Assets |
| VS-04 | In-Editor Search | P1 | 8 | Search ContextOS from VSCode |
| VS-05 | Commit Linking | P2 | 5 | Link git commits to containers |

---

## V5 Personal OS

| ID | Feature | Priority | Points | Description |
|---|---|---|---|---|
| POS-01 | Proactive Intelligence | P0 | 13 | AI that surfaces relevant content proactively |
| POS-02 | Daily Context Brief | P1 | 8 | Morning digest of relevant items |
| POS-03 | Cross-Device Sync | P0 | 13 | Sync state across devices |
| POS-04 | Plugin SDK | P0 | 13 | Public API for custom container types |
| POS-05 | Plugin Marketplace | P2 | 21 | Registry and distribution |
| POS-06 | Personal Analytics | P1 | 8 | Insights into reading, learning, productivity |
| POS-07 | Offline-First Sync | P0 | 21 | CRDT-based conflict-free sync |

## Total Backlog

| Phase | Features | Total Points |
|---|---|---|
| V1 | 48 | 166 |
| V2 | 11 | 71 |
| V3 | 6 | 39 |
| V4 | 5 | 31 |
| V5 | 7 | 97 |
| **Total** | **77** | **404** |
