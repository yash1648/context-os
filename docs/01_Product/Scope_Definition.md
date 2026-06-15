# Scope Definition

## In Scope — V1 MVP

### Core Platform
- User registration and authentication (email/password, OAuth2)
- JWT-based session management with refresh tokens
- User profile management
- Role-based access control (admin, user)

### Container System
- **Container** — universal entity with id, type, metadata, status, progress, timeline
- **12 Container Types**: Book, Movie, TV Series, Course, Learning Progress, Software Project, Goal, Habit, Note, Snapshot, Pinned Content, Knowledge Asset
- Container CRUD operations (create, read, update, delete)
- Container status lifecycle (draft, active, completed, archived, deleted)
- Progress tracking (percentage, milestones, checklists)
- Timeline events (created, updated, status change, milestone achieved)
- Snapshot management (versioned captures of container state)
- Tag system (create, assign, search, merge)

### Metadata System
- Type-specific metadata schemas (ISBN for books, director for movies, etc.)
- Custom metadata fields per container
- Metadata validation rules
- Metadata versioning

### Search
- Full-text search on title, description, tags
- Filter by container type, status, date range
- Sort by relevance, date, title, progress
- Pagination with cursor-based navigation

### User Interface
- Dashboard with container overview
- Container list with filtering and search
- Container detail view with tabs (overview, timeline, snapshots, AI context)
- Container create/edit forms with type-specific fields
- Tag management UI
- User settings page
- Responsive design (mobile, tablet, desktop)

### API & Infrastructure
- RESTful JSON API
- OpenAPI 3.0 documentation
- WebSocket for real-time notifications
- PostgreSQL database
- Redis caching
- Docker Compose deployment
- GitHub Actions CI/CD

## In Scope — V2 AI Integration
- Ollama integration for local LLM
- Embedding pipeline with vector storage
- Semantic search (hybrid vector + keyword)
- RAG pipeline for context-aware queries
- AI enrichment (auto-summarization, auto-tagging)
- Recommendation engine
- Knowledge graph construction
- AI context view in container details

## In Scope — V3 Browser Extension
- Chrome + Firefox extensions
- Page type auto-detection
- One-click container creation from web page
- Content extraction (title, description, metadata)
- Context sidebar in browser

## In Scope — V4 VSCode Extension
- VSCode extension
- Project auto-detection
- Tech stack tracking
- Code snippet as Knowledge Asset
- In-editor container search

## In Scope — V5 Personal OS
- Proactive intelligence engine
- Daily context brief
- Cross-device sync
- Plugin SDK and marketplace
- Personal analytics
- Offline-first architecture

## Out of Scope (Explicit)
- General-purpose file storage (use Dropbox, Google Drive)
- Note-taking as primary feature (use Obsidian, Notion)
- Real-time collaborative editing (use Google Docs)
- Social network features (no following, sharing, commenting)
- Email integration (use your email client)
- Calendar integration (use Google Calendar)
- Mobile native apps (PWA sufficient for V1-V4)
- Cloud sync as requirement (offline-first always)
- AI model training (use pre-trained models via Ollama)
- Multi-language support (English-only for V1)

## Boundaries & Constraints

### Technical Constraints
| Constraint | Rationale |
|---|---|
| Local-first AI via Ollama | Privacy, no cloud dependency, offline capable |
| PostgreSQL over NoSQL | Relational integrity for container metadata |
| React + Spring Boot | Proven stack, strong typing at both ends |
| Docker-only deployment | Simplified infrastructure, no Kubernetes needed for MVP |
| 12 container types max (V1) | Focus, scope management |

### UX Constraints
| Constraint | Rationale |
|---|---|
| No more than 3 clicks to any feature | Keep friction low |
| All actions available via API and UI | Power users and automation |
| Offline mode must never lose data | Reliability requirement |
| AI features must be non-blocking | UI never waits for AI |
