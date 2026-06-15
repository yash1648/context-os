# Product Goals

## Strategic Goals (Year 1)

### G1: Universal Container Engine
Build a robust, extensible container system that supports all core entity types with consistent CRUD, metadata management, progress tracking, and snapshot capabilities.

**Success Criteria:**
- All 12 container types (Book, Movie, TV Series, Course, Learning Progress, Software Project, Goal, Habit, Note, Snapshot, Pinned Content, Knowledge Asset) fully implemented
- Container CRUD API response time < 100ms (p95)
- Field validation and custom metadata support
- Snapshot creation with diff tracking

### G2: Semantic Search & Retrieval
Implement vector embeddings for all containers and enable natural language semantic search across the entire knowledge base.

**Success Criteria:**
- Embedding generation for all container types on create/update
- Semantic search returning top-10 results in < 500ms
- Hybrid search (semantic + keyword) with configurable weights
- Filterable by container type, tags, date ranges

### G3: AI Context Enrichment
Automatically enrich containers with AI-generated summaries, related topics, and cross-container connections.

**Success Criteria:**
- AI summary generation for Book, Movie, Course, and Project containers
- Auto-tagging with confidence scores
- Cross-container relationship discovery
- Weekly enrichment cycle for existing containers

### G4: Real-Time Collaboration Foundation
Establish WebSocket-based real-time updates for multi-device and multi-user scenarios.

**Success Criteria:**
- Real-time sync of container changes across devices (< 1s latency)
- WebSocket reconnection with state recovery
- Presence indicators for multi-user mode
- Offline change queue with conflict resolution

### G5: Extensibility Framework
Create the plugin and custom container type system that enables third-party extensions.

**Success Criteria:**
- Custom container type registration API
- Plugin lifecycle management (install, enable, disable, remove)
- Plugin sandboxing for security
- Example plugin with documentation

## Secondary Goals (Year 1)

### G6: User Onboarding Excellence
First-time user experience that demonstrates value within 5 minutes.

### G7: Mobile-Responsive Web UI
Full functionality on tablet and mobile browsers.

### G8: Data Portability
Import/export in JSON, CSV, and Markdown formats.

## OKR Framework

### Q1 OKRs
| Objective | Key Result | Target |
|---|---|---|
| Launch container engine | Container types implemented | 12/12 |
| Launch container engine | API p95 latency | < 100ms |
| Launch container engine | Test coverage | > 85% |
| Deploy infrastructure | CI/CD pipeline green | > 95% |
| Deploy infrastructure | Docker Compose setup | Production-ready |

### Q2 OKRs
| Objective | Key Result | Target |
|---|---|---|
| AI search live | Semantic query latency | < 500ms |
| AI search live | Search precision@10 | > 0.85 |
| AI enrichment | Auto-tag accuracy | > 80% |
| Real-time sync | Change propagation | < 1s |
| Real-time sync | Offline queue | Working |

### Q3 OKRs
| Objective | Key Result | Target |
|---|---|---|
| Browser extension | Web page capture | Working |
| Browser extension | Auto-container detection | > 70% accuracy |
| Plugin system | Plugin API stable | v1.0 |
| Plugin system | Community plugins | 3+ |

### Q4 OKRs
| Objective | Key Result | Target |
|---|---|---|
| VSCode extension | Code context capture | Working |
| VSCode extension | In-editor search | Working |
| Knowledge graph | Nodes indexed | 100K+ |
| Knowledge graph | Relationship accuracy | > 85% |
