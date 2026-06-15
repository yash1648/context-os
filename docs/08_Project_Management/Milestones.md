# Milestones

## Release Milestones

### M1: Foundation (Week 8 — 2026-03-01)

**Theme:** Core infrastructure and authentication

**Deliverables:**
- [x] Spring Boot project scaffolded
- [x] React + Vite project scaffolded
- [x] Docker Compose setup (PostgreSQL, Redis, RabbitMQ)
- [x] User registration and login API
- [x] JWT authentication with refresh tokens
- [x] User profile management
- [x] Database schema (users, containers, tags)
- [x] CI/CD pipeline (build, test, lint)
- [x] API documentation setup (OpenAPI)

**Technical Debt:**
- Basic error handling (no custom exceptions yet)
- No rate limiting
- No caching

---

### M2: Container Engine (Week 12 — 2026-04-01)

**Theme:** Core container management

**Deliverables:**
- [x] Container domain model with all 12 types
- [x] Container CRUD API
- [x] Container type-specific metadata validation
- [x] Tag system (CRUD, assignment, search)
- [x] Full-text search on containers
- [x] Pagination and filtering
- [x] Timeline event recording
- [x] Snapshot system (create, list, diff)
- [x] Pin system
- [ ] Soft delete with restore

**Technical Debt:**
- No WebSocket (polling for updates)
- No AI features yet
- Basic search only (no vector/semantic)

---

### M3: UI Foundation (Week 16 — 2026-05-01)

**Theme:** User interface for container management

**Deliverables:**
- [x] Dashboard layout with sidebar
- [x] Container list page with filters
- [x] Container detail page with tabs
- [x] Container create/edit forms
- [x] Tag management page
- [x] Global search bar (Cmd+K)
- [x] User settings page
- [x] Responsive design (mobile, tablet, desktop)
- [x] Dark mode support
- [x] Form validation
- [ ] WebSocket real-time updates

---

### V1 Release (Week 18 — 2026-05-15)

**Theme:** MVP Launch

**Deliverables:**
- [x] All M1-M3 deliverables
- [x] WebSocket real-time updates
- [x] Docker Compose production config
- [x] API v1 stable release
- [x] Performance testing (100 concurrent users)
- [x] Security audit
- [x] Documentation complete
- [x] Deployment to staging

**Quality Gate:**
- 85%+ test coverage (backend)
- 70%+ test coverage (frontend)
- p95 API latency < 200ms
- Zero critical security issues
- Lighthouse score > 85

---

### M4: AI Foundation (Week 22 — 2026-06-15)

**Theme:** AI integration

**Deliverables:**
- [x] Ollama integration (embedding + LLM)
- [x] Embedding pipeline for new containers
- [x] pgvector setup and vector search
- [x] AI enrichment pipeline (summary, tags)
- [x] AIContext model and storage
- [x] Async enrichment via RabbitMQ
- [x] WebSocket enrichment progress
- [ ] Re-enrichment scheduler

---

### M5: AI Search (Week 26 — 2026-07-15)

**Theme:** Intelligent search and retrieval

**Deliverables:**
- [x] Hybrid search (vector + full-text)
- [x] Semantic search API
- [x] RAG pipeline for question answering
- [x] AI Ask page in UI
- [x] Streaming answers via WebSocket
- [x] Search filters and facets
- [ ] Follow-up questions
- [ ] Search analytics

---

### V2 Release (Week 30 — 2026-08-15)

**Theme:** AI Integration Launch

**Deliverables:**
- [x] All M4-M5 deliverables
- [x] Recommendation engine
- [x] Knowledge graph (basic)
- [x] AI enrichment status UI
- [x] Model configuration UI
- [x] Performance optimization (vector search)

**Quality Gate:**
- Search p95 latency < 500ms
- Enrichment success rate > 95%
- Embedding quality (precision@10) > 0.85
- RAG accuracy > 80%

---

### M6: Browser Extension (Week 34 — 2026-09-15)

**Theme:** Web capture

**Deliverables:**
- [x] Chrome extension skeleton
- [x] Page metadata extraction
- [x] Auto-detection (article, video, product)
- [x] One-click save to container
- [x] Extension popup UI
- [ ] Highlight capture

---

### V3 Release (Week 42 — 2026-10-15)

**Theme:** Browser Extension Launch

---

### M7: VSCode Integration (Week 50 — 2026-12-15)

**Theme:** Developer tools

**Deliverables:**
- [ ] VSCode extension skeleton
- [ ] Project auto-detection
- [ ] Tech stack tracking
- [ ] Code snippet capture to Knowledge Asset
- [ ] In-editor container search

---

### V4 Release (Week 4 — 2027-02-01)

**Theme:** VSCode Integration Launch

---

### M8: Personal OS (Week 16 — 2027-04-15)

**Theme:** Proactive intelligence

**Deliverables:**
- [ ] Proactive suggestion engine
- [ ] Daily context brief
- [ ] Cross-device sync (CRDT)
- [ ] Plugin SDK v1

---

### V5 Release (Week 26 — 2027-06-01)

**Theme:** Personal OS Launch

**Full feature set:**
- [ ] Proactive intelligence
- [ ] Daily context brief
- [ ] Cross-device sync
- [ ] Plugin SDK & marketplace
- [ ] Personal analytics
- [ ] Offline-first architecture
- [ ] Performance optimization for 10M+ containers
