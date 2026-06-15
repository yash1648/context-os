# Risk Register

## Risk Scoring

| Score | Likelihood | Impact |
|---|---|---|
| 1 | Very Unlikely | Negligible |
| 2 | Unlikely | Minor |
| 3 | Possible | Moderate |
| 4 | Likely | Major |
| 5 | Very Likely | Critical |

**Risk Level = Likelihood × Impact**

| Level | Score | Response |
|---|---|---|
| Low | 1-6 | Accept, monitor |
| Medium | 7-14 | Mitigate, contingency plan |
| High | 15-25 | Active mitigation required, escalate |

## Current Risks

### R1: Ollama Performance on Consumer Hardware

| Field | Value |
|---|---|
| **Description** | Ollama LLM (Mistral 7B) may be too slow on CPU-only machines (no GPU) |
| **Category** | Technical |
| **Phase** | V2 |
| **Likelihood** | 4 (Likely) |
| **Impact** | 4 (Major) |
| **Risk Level** | 16 (High) |
| **Owner** | AI Lead |

**Mitigation:**
- Support smaller quantized models (Q4, Q3) for CPU-only deployments
- Always run enrichment asynchronously (never block UI)
- Support remote Ollama instances (user can run on GPU server)
- Default to nomic-embed-text for embeddings (2GB, fast on CPU)
- Provide clear hardware requirements documentation

**Contingency:**
- Cloud AI fallback option (OpenAI API) for users without local Ollama
- Batch processing during low-usage hours

---

### R2: Vector Search Performance Degradation

| Field | Value |
|---|---|
| **Description** | pgvector performance degrades with > 1M vectors per user |
| **Category** | Technical / Scalability |
| **Phase** | V3+ |
| **Likelihood** | 3 (Possible) |
| **Impact** | 3 (Moderate) |
| **Risk Level** | 9 (Medium) |
| **Owner** | Backend Lead |

**Mitigation:**
- IVFFlat indexes with proper `lists` and `probes` configuration
- Table partitioning by user_id for vector searches
- Monitor query performance with pg_stat_statements
- Abstraction layer for vector store (can migrate to Qdrant)

**Contingency:**
- Migrate to Qdrant or Pinecone dedicated vector database
- Shard by user_id across multiple pgvector instances

---

### R3: AI Enrichment Quality

| Field | Value |
|---|---|
| **Description** | LLM-generated summaries and tags may be low quality or incorrect |
| **Category** | Quality |
| **Phase** | V2 |
| **Likelihood** | 3 (Possible) |
| **Impact** | 3 (Moderate) |
| **Risk Level** | 9 (Medium) |
| **Owner** | AI Lead |

**Mitigation:**
- User feedback mechanism (thumbs up/down on AI content)
- Allow manual override of AI-generated content
- Confidence scores on auto-tags
- Regular model evaluation against test dataset
- A/B test different models and prompts

**Contingency:**
- Fallback to simpler NLP techniques if LLM quality is insufficient
- Allow users to disable AI enrichment entirely

---

### R4: Data Privacy Concerns

| Field | Value |
|---|---|
| **Description** | Users may be concerned about their data being processed by AI, even locally |
| **Category** | Legal / Trust |
| **Phase** | V2 |
| **Likelihood** | 2 (Unlikely) |
| **Impact** | 4 (Major) |
| **Risk Level** | 8 (Medium) |
| **Owner** | Product Manager |

**Mitigation:**
- Clear privacy documentation (no data leaves user's machine)
- Open-source code for transparency
- Local-only processing with Ollama
- Opt-in for any cloud features
- GDPR compliance documentation

**Contingency:**
- Offline-only mode for security-conscious users
- SOC2 certification for enterprise customers

---

### R5: Scope Creep on Container Types

| Field | Value |
|---|---|
| **Description** | Users request many more container types, expanding scope beyond V1 plan |
| **Category** | Product |
| **Phase** | V1 |
| **Likelihood** | 4 (Likely) |
| **Impact** | 3 (Moderate) |
| **Risk Level** | 12 (Medium) |
| **Owner** | Product Manager |

**Mitigation:**
- Strictly limit V1 to 12 predefined container types
- Clear "Out of Scope" documentation
- Custom metadata fields allow flexibility without new types
- Feature voting for community requests
- Plugin architecture planned for V5 to allow custom types

**Contingency:**
- Add custom container type support earlier (V3 instead of V5)

---

### R6: Team Member Unavailability

| Field | Value |
|---|---|
| **Description** | Key team member (especially AI specialist) may be unavailable |
| **Category** | Resource |
| **Phase** | All |
| **Likelihood** | 3 (Possible) |
| **Impact** | 4 (Major) |
| **Risk Level** | 12 (Medium) |
| **Owner** | Engineering Manager |

**Mitigation:**
- Cross-train team members on AI components
- Comprehensive documentation (this vault)
- Code reviews ensure shared ownership
- Modular architecture allows parallel work

**Contingency:**
- Contractor network for specialized AI work
- Reduce AI scope for the affected sprint

---

### R7: Browser Extension API Limitations

| Field | Value |
|---|---|
| **Description** | Chrome/Firefox extension APIs may limit auto-detection capabilities |
| **Category** | Technical |
| **Phase** | V3 |
| **Likelihood** | 3 (Possible) |
| **Impact** | 2 (Minor) |
| **Risk Level** | 6 (Low) |
| **Owner** | Frontend Lead |

**Mitigation:**
- Research extension capabilities early
- Fallback to manual type selection if auto-detection fails
- Content script extraction + metadata parsing

---

### R8: Database Migration Issues

| Field | Value |
|---|---|
| **Description** | Schema changes may cause downtime or data loss during migrations |
| **Category** | Technical |
| **Phase** | All |
| **Likelihood** | 2 (Unlikely) |
| **Impact** | 5 (Critical) |
| **Risk Level** | 10 (Medium) |
| **Owner** | Backend Lead |

**Mitigation:**
- Flyway versioned migrations with thorough testing
- Backward-compatible schema changes
- Migrations tested on staging first
- Rollback plan for each migration
- Database backup before every migration

**Contingency:**
- Point-in-time recovery from WAL
- Blue-green deployment for zero-downtime migrations

## Risk Matrix

```
Impact
 5 |    |    |    |    | R8 |
 4 |    |    | R1 | R6 |    |
 3 |    | R2 | R4 |    |    |
   |    | R3 | R5 |    |    |
 2 |    |    | R7 |    |    |
 1 |    |    |    |    |    |
   +----------------------------
     1    2    3    4    5
              Likelihood
```

## Risk Dashboards

### Weekly Monitoring

```yaml
Review Schedule:
  - R1 (Ollama): Check enrichment latency, success rate
  - R2 (pgvector): Check query execution time
  - R3 (AI Quality): Review user feedback on AI content
  - R5 (Scope): Review feature requests, backlog
  - R8 (Migrations): Review migration test results
```

### Risk Owner Responsibilities

- Track risk indicators weekly
- Update mitigation progress
- Escalate if risk level increases
- Document any risk events and responses
