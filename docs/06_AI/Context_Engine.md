# Context Engine

## Overview

The Context Engine is the central orchestrator that manages AI-powered understanding of user data. It integrates embedding generation, semantic search, enrichment, and knowledge graph construction into a cohesive pipeline.

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   Context Engine                         │
│                                                          │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────┐  │
│  │ Ingestion   │  │ Processing   │  │ Retrieval      │  │
│  │             │  │              │  │                │  │
│  │ Container   │→ │ Embedding    │→ │ Semantic Search│  │
│  │ Create/     │  │ Enrichment   │  │ RAG Query      │  │
│  │ Update      │  │ KG Update    │  │ Recommendation │  │
│  │ Web Import  │  │              │  │                │  │
│  └─────────────┘  └──────┬───────┘  └────────────────┘  │
│                          │                               │
│                    ┌─────▼─────┐                         │
│                    │ Ollama     │                         │
│                    │ Service    │                         │
│                    └───────────┘                         │
└─────────────────────────────────────────────────────────┘
```

## Ingestion Pipeline

```java
@Service
public class ContextIngestionPipeline {

    private final EmbeddingService embeddingService;
    private final EnrichmentOrchestrator enrichmentOrchestrator;
    private final KnowledgeGraphService knowledgeGraphService;

    /**
     * Full ingestion pipeline for a new or updated container.
     * Runs asynchronously after container save.
     */
    @Async
    public CompletableFuture<Void> ingestContainer(Container container) {
        // 1. Generate embedding
        float[] embedding = embeddingService.generateEmbedding(
            buildContent(container)
        );
        container.setEmbedding(embedding);

        // 2. Enrich (summary, tags, relationships)
        enrichmentOrchestrator.enrichContainer(container.getId());

        // 3. Update knowledge graph
        knowledgeGraphService.extractAndLink(container);

        return CompletableFuture.completedFuture(null);
    }

    private String buildContent(Container container) {
        return String.format("""
            Title: %s
            Description: %s
            Type: %s
            Metadata: %s
            Tags: %s
            """,
            container.getTitle(),
            container.getDescription(),
            container.getType(),
            container.getMetadata(),
            container.getTags().stream().map(Tag::getName).collect(Collectors.joining(", "))
        );
    }
}
```

## Context Retrieval

```java
@Service
public class ContextRetrievalService {

    private final VectorSearchService vectorSearch;
    private final FullTextSearchService fullTextSearch;
    private final SearchRanker ranker;

    /**
     * Retrieve context for RAG or recommendations.
     * Implements hybrid retrieval with configurable weights.
     */
    public List<ContextSource> retrieveContext(
        String query,
        UUID userId,
        ContextRetrievalRequest request
    ) {
        // 1. Generate query embedding
        float[] queryEmbedding = embeddingService.generateEmbedding(query);

        // 2. Vector search
        List<VectorSearchResult> vectorResults = vectorSearch.search(
            queryEmbedding, userId, request.getVectorLimit()
        );

        // 3. Full-text search (parallel)
        List<FullTextResult> textResults = fullTextSearch.search(
            query, userId, request.getTextLimit()
        );

        // 4. Merge and rerank
        List<ContextSource> merged = ranker.mergeAndRank(
            vectorResults, textResults,
            request.getVectorWeight(),
            request.getTextWeight()
        );

        // 5. Apply filters
        if (request.getTypeFilter() != null) {
            merged = merged.stream()
                .filter(s -> request.getTypeFilter().contains(s.getContainerType()))
                .collect(Collectors.toList());
        }

        // 6. Limit and return
        return merged.stream()
            .limit(request.getMaxResults())
            .collect(Collectors.toList());
    }
}

@Data
@Builder
public class ContextRetrievalRequest {
    private int vectorLimit = 20;
    private int textLimit = 20;
    private float vectorWeight = 0.7f;
    private float textWeight = 0.3f;
    private List<ContainerType> typeFilter;
    private int maxResults = 10;
}
```

## Context Cache

```java
@Service
@CacheConfig(cacheNames = "ai-context")
public class ContextCacheService {

    /**
     * Cache AI context results to avoid redundant LLM calls.
     * TTL: 1 hour for summaries, 24 hours for embeddings.
     */

    @Cacheable(key = "'summary:' + #containerId")
    public String getCachedSummary(UUID containerId) {
        return null; // Cache miss triggers enrichment
    }

    @Cacheable(key = "'embedding:' + #containerId")
    public float[] getCachedEmbedding(UUID containerId) {
        return null;
    }

    @CacheEvict(key = "'summary:' + #containerId")
    public void invalidateSummary(UUID containerId) {}

    @CacheEvict(key = "'embedding:' + #containerId")
    public void invalidateEmbedding(UUID containerId) {}
}
```

## Batch Processing

```java
@Service
public class BatchContextProcessor {

    /**
     * Periodic batch processing for unenriched containers.
     * Runs daily at 3 AM via scheduler.
     */
    @Scheduled(cron = "${contextos.ai.enrichment.schedule}")
    @Transactional
    public void processPendingEnrichments() {
        List<Container> pending = containerRepository
            .findByAiContextEnrichmentStatus(EnrichmentStatus.PENDING);

        log.info("Processing {} pending enrichments", pending.size());

        for (Container container : pending) {
            try {
                enrichmentOrchestrator.enrichContainer(container.getId());
            } catch (Exception e) {
                log.error("Batch enrichment failed for container: {}", container.getId(), e);
            }
        }
    }

    /**
     * Weekly full re-indexing for stale embeddings.
     */
    @Scheduled(cron = "0 0 4 * * SUN")
    public void reindexStaleEmbeddings() {
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        List<Container> stale = containerRepository
            .findByUpdatedAtAfter(weekAgo);

        log.info("Reindexing {} stale embeddings", stale.size());

        for (Container container : stale) {
            try {
                float[] embedding = embeddingService.generateEmbedding(
                    buildContent(container)
                );
                container.setEmbedding(embedding);
                containerRepository.save(container);
            } catch (Exception e) {
                log.error("Reindex failed for container: {}", container.getId(), e);
            }
        }
    }
}
```

## Context Window Management

```java
/**
 * Manages the context window for LLM calls.
 * Ensures prompts fit within model's context limits.
 */
@Service
public class ContextWindowManager {

    private static final int MAX_CONTEXT_TOKENS = 4096;
    private static final int MAX_SOURCES = 5;

    public String buildContextPrompt(String query, List<ContextSource> sources) {
        // Truncate sources to fit within context window
        List<ContextSource> truncated = truncateSources(sources);
        
        StringBuilder context = new StringBuilder();
        context.append("Based on the following information from the user's knowledge base:\n\n");

        for (int i = 0; i < truncated.size(); i++) {
            ContextSource source = truncated.get(i);
            String entry = String.format(
                "[Source %d] %s (%s)\n%s\n\n",
                i + 1,
                source.getTitle(),
                source.getContainerType(),
                truncateContent(source.getContent(), 500) // Truncate each source
            );
            
            if (context.length() + entry.length() > MAX_CONTEXT_TOKENS * 4) {
                break; // Approximate token limit
            }
            context.append(entry);
        }

        context.append("Question: ").append(query);
        return context.toString();
    }

    private List<ContextSource> truncateSources(List<ContextSource> sources) {
        return sources.stream()
            .sorted(Comparator.comparingDouble(ContextSource::getRelevance).reversed())
            .limit(MAX_SOURCES)
            .collect(Collectors.toList());
    }

    private String truncateContent(String content, int maxChars) {
        if (content.length() <= maxChars) return content;
        return content.substring(0, maxChars) + "...";
    }
}
```
