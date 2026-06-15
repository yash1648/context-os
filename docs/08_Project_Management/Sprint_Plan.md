# Sprint Plan

## Sprint Structure

| Aspect | Value |
|---|---|
| Sprint length | 2 weeks |
| Ceremonies | Sprint Planning (Mon), Daily Standup (15min), Review (Fri), Retro (Fri) |
| Team size | 5 engineers |
| Velocity target | 30-40 story points per sprint |
| Backlog refinement | Weekly, 1 hour |

## Sprint 1-2: Foundation (Weeks 1-4)

**Goal:** Project setup and authentication

**Sprint Backlog:**
| ID | Story | Points | Owner |
|---|---|---|---|
| AUTH-01 | User Registration | 3 | BE Dev 1 |
| AUTH-02 | User Login | 2 | BE Dev 1 |
| AUTH-03 | Token Refresh | 2 | BE Dev 1 |
| AUTH-04 | User Logout | 1 | BE Dev 1 |
| INFRA-01 | Docker Compose Setup | 3 | DevOps |
| INFRA-03 | Database Migrations | 3 | BE Dev 2 |
| INFRA-04 | API Documentation | 3 | BE Dev 2 |
| INFRA-05 | Health Endpoints | 2 | BE Dev 2 |
| — | Project Scaffolding (FE/BE) | 5 | All |
| Total | | 24 | |

**Sprint 2 Backlog:**
| ID | Story | Points | Owner |
|---|---|---|---|
| CON-01 | Container Domain Model | 5 | BE Dev 1 |
| CON-02 | Container CRUD API | 8 | BE Dev 1 |
| CON-03 | Container Type System (4 types) | 3 | BE Dev 2 |
| CI-01 | CI/CD Pipeline | 5 | DevOps |
| — | Login/Register UI | 5 | FE Dev |
| Total | | 26 | |

## Sprint 3-4: Container Engine (Weeks 5-8)

**Sprint 3 Backlog:**
| ID | Story | Points | Owner |
|---|---|---|---|
| CON-03 | Container Types (8 remaining) | 5 | BE Dev 1 |
| CT-01 | Book Container | 5 | BE Dev 2 |
| CT-02 | Movie Container | 5 | BE Dev 2 |
| CT-04 | Course Container | 5 | BE Dev 2 |
| TAG-01 | Tag CRUD | 3 | BE Dev 1 |
| TAG-02 | Tag Assignment | 2 | BE Dev 1 |
| UI-01 | Dashboard Layout | 5 | FE Dev |
| Total | | 30 | |

**Sprint 4 Backlog:**
| ID | Story | Points | Owner |
|---|---|---|---|
| CON-05 | Container Search | 8 | BE Dev 1 |
| CON-04 | Container Validation | 3 | BE Dev 2 |
| SNAP-01 | Snapshot Creation | 5 | BE Dev 2 |
| TIM-01 | Timeline Event Recording | 3 | BE Dev 1 |
| DASH-01 | Overview Dashboard | 5 | FE Dev |
| UI-02 | Container List Page | 8 | FE Dev |
| Total | | 32 | |

## Sprint 5-6: Frontend (Weeks 9-12)

**Sprint 5 Backlog:**
| ID | Story | Points | Owner |
|---|---|---|---|
| UI-03 | Container Detail Page | 8 | FE Dev |
| TAG-03 | Tag Search/Autocomplete | 2 | BE |
| TAG-04 | Tag Autocomplete API | 2 | BE |
| SNAP-02 | Snapshot List UI | 3 | FE Dev |
| UI-04 | Container Create Form | 8 | FE Dev |
| TIM-02 | Timeline Query API | 3 | BE |
| Total | | 26 | |

**Sprint 6 Backlog:**
| ID | Story | Points | Owner |
|---|---|---|---|
| UI-05 | Container Edit Form | 5 | FE Dev |
| UI-06 | Tag Management Page | 5 | FE Dev |
| UI-07 | User Settings Page | 3 | FE Dev |
| TAG-05 | Tag Merge | 2 | BE |
| WS-01 | Change Notifications | 5 | BE |
| DASH-02 | Activity Feed | 3 | BE |
| Total | | 23 | |

## Sprint 7-8: V1 Polish (Weeks 13-16)

**Sprint 7 Backlog:**
| ID | Story | Points | Owner |
|---|---|---|---|
| WS-03 | Connection Recovery | 3 | BE |
| INFRA-02 | Production Docker Config | 5 | DevOps |
| — | Performance Testing | 5 | All |
| — | Bug Fixes | 8 | All |
| — | UI Polish | 5 | FE Dev |
| Total | | 26 | |

**Sprint 8 Backlog:**
| ID | Story | Points | Owner |
|---|---|---|---|
| — | Security Audit | 3 | All |
| — | Documentation | 5 | All |
| — | Load Testing | 5 | DevOps |
| — | V1 Release Prep | 3 | All |
| Total | | 16 | |

## Sprint 9-12: AI Integration (Weeks 17-24)

**Sprint 9: AI Foundation**
| ID | Story | Points | Owner |
|---|---|---|---|
| AI-01 | Ollama Integration | 5 | BE AI |
| AI-02 | Embedding Service | 8 | BE AI |
| AI-06 | Auto-Summarization | 5 | BE AI |
| AI-10 | AI Context View | 3 | FE |
| Total | | 21 | |

**Sprint 10: Vector Search**
| ID | Story | Points | Owner |
|---|---|---|---|
| AI-03 | Vector Search API | 8 | BE AI |
| AI-04 | Hybrid Search | 5 | BE AI |
| AI-11 | AI Configuration UI | 3 | FE |
| Total | | 16 | |

**Sprint 11: RAG & Enrichment**
| ID | Story | Points | Owner |
|---|---|---|---|
| AI-05 | RAG Pipeline | 8 | BE AI |
| AI-07 | Auto-Tagging | 5 | BE AI |
| UI-10 | AI Ask Page | 8 | FE |
| Total | | 21 | |

**Sprint 12: Recommendations**
| ID | Story | Points | Owner |
|---|---|---|---|
| AI-08 | Recommendation Engine | 8 | BE AI |
| AI-09 | Knowledge Graph | 13 | BE AI |
| UI-11 | Recommendations Page | 5 | FE |
| Total | | 26 | |
