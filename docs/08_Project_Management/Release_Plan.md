# Release Plan

## Release Overview

| Release | Date | Theme | Key Features | Effort |
|---|---|---|---|---|
| V1.0 | 2026-05-15 | MVP | Container management, tags, search, auth | 166 pts |
| V1.1 | 2026-06-01 | Bugfix & Polish | V1 bug fixes, performance, UX improvements | 20 pts |
| V2.0 | 2026-08-15 | AI Integration | Semantic search, enrichment, RAG, KG | 71 pts |
| V2.1 | 2026-09-01 | AI Improvements | Better recommendations, model config UI | 15 pts |
| V3.0 | 2026-10-15 | Browser Extension | Web capture, auto-detection | 39 pts |
| V3.1 | 2026-11-15 | Extension Enhancements | Highlight capture, sidebar | 15 pts |
| V4.0 | 2027-01-15 | VSCode Integration | Code snippets, project tracking | 31 pts |
| V5.0 | 2027-06-01 | Personal OS | Proactive AI, sync, plugins | 97 pts |

## V1.0 — MVP (2026-05-15)

### Goals
- Core container management working
- 12 container types implemented
- User authentication and basic UI
- API stable and documented

### Feature Set
```
✅ User registration & login (JWT)
✅ Container CRUD for 12 types
✅ Tag management
✅ Full-text search
✅ Timeline events
✅ Snapshots
✅ Pins
✅ Dashboard
✅ WebSocket notifications
✅ Docker Compose deployment
✅ CI/CD pipeline
✅ API documentation (OpenAPI)
```

### Release Criteria
- [ ] All P0 and P1 features complete
- [ ] Backend test coverage > 85%
- [ ] Frontend test coverage > 70%
- [ ] API p95 latency < 200ms
- [ ] No critical or high security issues
- [ ] Load test: 100 concurrent users stable
- [ ] Documentation reviewed and published
- [ ] Staging deployment verified
- [ ] Rollback procedure tested

### Known Limitations
- No AI features (summaries, semantic search)
- No browser extension
- No offline support
- Polling for updates (WebSocket basic)

## V2.0 — AI Integration (2026-08-15)

### Goals
- AI-powered semantic search
- Automatic container enrichment
- Question answering over knowledge base
- Content recommendations

### Feature Set
```
✅ Ollama integration
✅ Embedding pipeline
✅ Vector search (pgvector)
✅ Hybrid search (vector + full-text)
✅ Auto-summarization
✅ Auto-tagging
✅ RAG question answering
✅ Recommendations
✅ Knowledge graph (basic)
✅ AI context view in UI
```

### Upgrade Path from V1
```yaml
Database:
  - Run V2 migration (add ai_contexts table, embedding column)
  - Backfill embeddings for existing containers
  
Configuration:
  - Add Ollama service to Docker Compose
  - Configure Spring AI properties
  
API Changes:
  - New endpoints: /api/v1/search (hybrid), /api/v1/ai/**
  - Existing endpoints unchanged
```

## V3.0 — Browser Extension (2026-10-15)

### Goals
- One-click web content capture
- Auto-detection of page types
- Browser sidebar for quick access

### Feature Set
```
✅ Chrome extension
✅ Firefox extension
✅ Page metadata extraction
✅ Auto-detection (article, video, product, doc)
✅ One-click save to container
✅ Extension popup UI
✅ Context sidebar
```

## V4.0 — VSCode Integration (2027-01-15)

### Goals
- Developer context tracking
- Code snippet management
- In-editor knowledge access

### Feature Set
```
✅ VSCode extension
✅ Project auto-detection
✅ Tech stack tracking
✅ Code snippet capture
✅ In-editor search
✅ Git commit linking
```

## V5.0 — Personal OS (2027-06-01)

### Goals
- Proactive intelligence
- Cross-device synchronization
- Plugin ecosystem

### Feature Set
```
✅ Proactive suggestions and alerts
✅ Daily context brief
✅ Cross-device sync (CRDT)
✅ Plugin SDK v1
✅ Plugin marketplace
✅ Personal analytics
✅ Offline-first architecture
✅ Performance for 10M+ containers
```

## Release Process

```yaml
Release Process:
  1. Feature Freeze (2 weeks before release)
     - No new features merged
     - Only bug fixes and documentation
  
  2. Release Candidate (1 week before release)
     - RC branch created from main
     - Deployed to staging
     - Full regression test suite
     - Load testing
  
  3. Release Day
     - Final approval from QA
     - Deploy to production (main branch)
     - Monitor for 2 hours
     - Rollback if error rate > 5%
  
  4. Post-Release (1 week after)
     - Bugfix hotfixes as needed
     - Performance review
     - User feedback collection
     - Retrospective
```

## Release Checklist

```markdown
## Pre-Release Checklist

### Code Quality
- [ ] All tests passing (backend + frontend)
- [ ] No lint errors
- [ ] No TypeScript errors
- [ ] Code review completed for all changes
- [ ] Security scan passed

### Documentation
- [ ] API documentation updated
- [ ] Release notes written
- [ ] Upgrade guide written (if applicable)
- [ ] Known issues documented

### Infrastructure
- [ ] Docker images built and pushed
- [ ] Database migrations tested on staging
- [ ] Backup verified
- [ ] Monitoring dashboards updated
- [ ] Alerts configured

### Verification
- [ ] Smoke tests passed on staging
- [ ] Load test passed (target throughput)
- [ ] Rollback procedure tested
- [ ] Communication sent to team

### Go/No-Go
- [ ] All critical bugs fixed
- [ ] Performance targets met
- [ ] Security requirements met
- [ ] Stakeholder approval received
```
