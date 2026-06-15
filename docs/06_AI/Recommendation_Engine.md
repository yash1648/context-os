# Recommendation Engine

## Overview

The Recommendation Engine provides personalized suggestions for containers the user might find valuable. It uses content-based filtering (similar content based on embeddings) and collaborative signals (usage patterns, ratings).

## Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                    Recommendation Engine                       │
│                                                               │
│  ┌──────────────┐    ┌──────────────┐    ┌────────────────┐  │
│  │ Content-Based │    │ Usage-Based  │    │ Hybrid         │  │
│  │ Recommender   │    │ Recommender  │    │ Ranker         │  │
│  │               │    │              │    │                │  │
│  │ • Embedding   │    │ • Recent     │    │ • Weighted     │  │
│  │   Similarity  │    │ • Frequent   │    │   Combination  │  │
│  │ • Tag Overlap │    │ • Related    │    │ • Diversity    │  │
│  │ • Type Match  │    │ • Sequential │    │ • Freshness    │  │
│  └──────┬───────┘    └──────┬───────┘    └───────┬────────┘  │
│         │                  │                     │            │
│         └─────────┬────────┴─────────────────────┘            │
│                   │                                           │
│            ┌──────▼──────┐                                    │
│            │  Cache      │                                    │
│            │  (Redis)    │                                    │
│            └─────────────┘                                    │
└──────────────────────────────────────────────────────────────┘
```

## Recommendation Service

```java
@Service
public class RecommendationService {

    private final ContentBasedRecommender contentBased;
    private final UsageBasedRecommender usageBased;
    private final HybridRanker hybridRanker;
    private final RecommendationRepository recommendationRepository;
    private final RecommendationCache cache;

    /**
     * Get recommendations for a user.
     */
    public List<Recommendation> getRecommendations(UUID userId, UUID contextContainerId, int limit) {
        // Check cache first
        List<Recommendation> cached = cache.get(userId, contextContainerId);
        if (cached != null) return cached;

        // 1. Get content-based recommendations
        List<Recommendation> contentBasedRecs = contentBased.recommend(
            userId, contextContainerId, limit * 2
        );

        // 2. Get usage-based recommendations
        List<Recommendation> usageBasedRecs = usageBased.recommend(
            userId, limit * 2
        );

        // 3. Hybrid ranking
        List<Recommendation> hybrid = hybridRanker.rank(
            contentBasedRecs, usageBasedRecs, limit
        );

        // 4. Remove already-owned recommendations
        hybrid = filterAlreadyRecommended(userId, hybrid);

        // 5. Cache results
        cache.put(userId, contextContainerId, hybrid);

        return hybrid;
    }

    /**
     * Generate recommendations in batch for all users.
     * Runs weekly via scheduler.
     */
    @Scheduled(cron = "0 0 5 * * MON")
    @Transactional
    public void generateBatchRecommendations() {
        List<UUID> userIds = userRepository.findAllActiveUserIds();
        
        for (UUID userId : userIds) {
            try {
                // Get user's top containers as context
                List<Container> topContainers = containerRepository
                    .findTopByOwnerIdOrderByUpdatedAtDesc(userId, PageRequest.of(0, 5));

                Set<UUID> recommended = new HashSet<>();
                
                for (Container context : topContainers) {
                    List<Recommendation> recs = getRecommendations(
                        userId, context.getId(), 10
                    );
                    
                    for (Recommendation rec : recs) {
                        if (!recommended.contains(rec.getRecommendedContainerId())) {
                            recommendationRepository.save(rec);
                            recommended.add(rec.getRecommendedContainerId());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Batch recommendation failed for user: {}", userId, e);
            }
        }
    }
}
```

## Content-Based Recommender

```java
@Component
public class ContentBasedRecommender {

    private final VectorSearchRepository vectorSearch;
    private final ContainerRepository containerRepository;

    /**
     * Recommend containers similar to a given context container.
     * Uses embedding similarity + tag overlap.
     */
    public List<Recommendation> recommend(UUID userId, UUID contextContainerId, int limit) {
        Optional<Container> context = containerRepository.findById(contextContainerId);
        if (context.isEmpty() || context.get().getEmbedding() == null) {
            return List.of();
        }

        Container ctx = context.get();

        // 1. Vector similarity search
        List<VectorSearchProjection> similar = vectorSearch.similaritySearch(
            ctx.getEmbedding(), userId, limit * 3
        );

        // 2. Filter out the context container itself
        similar = similar.stream()
            .filter(s -> !s.getId().equals(contextContainerId))
            .collect(Collectors.toList());

        // 3. Compute combined score (embedding + tag overlap)
        List<Recommendation> recommendations = new ArrayList<>();
        for (VectorSearchProjection proj : similar) {
            Optional<Container> candidate = containerRepository.findById(proj.getId());
            if (candidate.isEmpty()) continue;

            Container cand = candidate.get();

            double tagOverlap = calculateTagOverlap(ctx, cand);
            double combinedScore = (proj.getSimilarity() * 0.7) + (tagOverlap * 0.3);

            String reason = buildReason(proj.getSimilarity(), tagOverlap, ctx.getType(), cand.getType());

            recommendations.add(new Recommendation(
                UUID.randomUUID(),
                userId,
                contextContainerId,
                cand.getId(),
                combinedScore,
                reason,
                "CONTENT_BASED"
            ));
        }

        return recommendations.stream()
            .sorted(Comparator.comparingDouble(Recommendation::getScore).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }

    private double calculateTagOverlap(Container a, Container b) {
        Set<String> tagsA = a.getTags().stream().map(Tag::getName).collect(Collectors.toSet());
        Set<String> tagsB = b.getTags().stream().map(Tag::getName).collect(Collectors.toSet());

        if (tagsA.isEmpty() || tagsB.isEmpty()) return 0.0;

        Set<String> intersection = new HashSet<>(tagsA);
        intersection.retainAll(tagsB);

        Set<String> union = new HashSet<>(tagsA);
        union.addAll(tagsB);

        return (double) intersection.size() / union.size();
    }

    private String buildReason(double similarity, double tagOverlap, ContainerType ctxType, ContainerType candType) {
        if (similarity > 0.85) {
            return "Very similar to what you're viewing";
        } else if (tagOverlap > 0.5) {
            return "Shares many tags with your current content";
        } else if (ctxType == candType) {
            return "Same category as your current content";
        } else {
            return "Related content you might find valuable";
        }
    }
}
```

## Usage-Based Recommender

```java
@Component
public class UsageBasedRecommender {

    private final ContainerRepository containerRepository;
    private final TimelineEventRepository timelineRepository;

    /**
     * Recommend based on user's usage patterns.
     * - Recently active containers
     * - Frequently accessed types
     * - Sequential patterns (if you read X, you might like Y)
     */
    public List<Recommendation> recommend(UUID userId, int limit) {
        // 1. Get user's most active container types
        List<Object[]> typeCounts = timelineRepository
            .countEventsByTypeAndUser(userId, PageRequest.of(0, 3));

        List<ContainerType> preferredTypes = typeCounts.stream()
            .map(row -> (ContainerType) row[0])
            .collect(Collectors.toList());

        // 2. Find popular containers in preferred types that user hasn't seen
        List<Container> candidates = containerRepository
            .findByOwnerIdAndTypeInAndStatus(
                userId, preferredTypes, ContainerStatus.ACTIVE,
                PageRequest.of(0, limit)
            );

        // 3. Score by recency of similar type activity
        return candidates.stream()
            .map(c -> {
                double score = 0.5 + (Math.random() * 0.3); // Base + noise
                return new Recommendation(
                    UUID.randomUUID(),
                    userId,
                    null,
                    c.getId(),
                    score,
                    "Based on your recent activity in " + c.getType().name().toLowerCase().replace('_', ' '),
                    "USAGE_BASED"
                );
            })
            .sorted(Comparator.comparingDouble(Recommendation::getScore).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }
}
```

## Hybrid Ranker

```java
@Component
public class HybridRanker {

    /**
     * Combine and rank recommendations from multiple sources.
     */
    public List<Recommendation> rank(
        List<Recommendation> contentBased,
        List<Recommendation> usageBased,
        int limit
    ) {
        Map<UUID, Recommendation> merged = new HashMap<>();

        // Content-based: weight 0.6
        for (Recommendation rec : contentBased) {
            merged.merge(rec.getRecommendedContainerId(), rec,
                (existing, incoming) -> {
                    existing.setScore(Math.max(existing.getScore(), incoming.getScore() * 0.6));
                    return existing;
                });
        }

        // Usage-based: weight 0.4
        for (Recommendation rec : usageBased) {
            merged.merge(rec.getRecommendedContainerId(), rec,
                (existing, incoming) -> {
                    existing.setScore(Math.max(existing.getScore(), incoming.getScore() * 0.4));
                    return existing;
                });
        }

        // Apply diversity: limit to 2 per container type
        Map<ContainerType, Integer> typeCount = new HashMap<>();
        List<Recommendation> diversified = new ArrayList<>();

        merged.values().stream()
            .sorted(Comparator.comparingDouble(Recommendation::getScore).reversed())
            .forEach(rec -> {
                // Need the container type
                containerRepository.findById(rec.getRecommendedContainerId())
                    .ifPresent(container -> {
                        ContainerType type = container.getType();
                        typeCount.merge(type, 1, Integer::sum);
                        if (typeCount.get(type) <= 2) {
                            diversified.add(rec);
                        }
                    });
            });

        return diversified.stream()
            .limit(limit)
            .collect(Collectors.toList());
    }
}
```

## Recommendation Cache

```java
@Component
@CacheConfig(cacheNames = "recommendations")
public class RecommendationCache {

    @Cacheable(key = "'rec:' + #userId + ':' + #contextId")
    public List<Recommendation> get(UUID userId, UUID contextId) {
        return null; // Cache miss
    }

    @CachePut(key = "'rec:' + #userId + ':' + #contextId")
    public List<Recommendation> put(UUID userId, UUID contextId, List<Recommendation> recs) {
        return recs;
    }

    @CacheEvict(key = "'rec:' + #userId + ':*'")
    public void invalidateUser(UUID userId) {}
}
```

## Recommendation Types

| Type | Description | Algorithm | Example |
|---|---|---|---|
| SIMILAR_CONTENT | Based on embedding similarity | Content-based | "Similar to your current book" |
| TAG_OVERLAP | Based on shared tags | Content-based | "Shares tags with your project" |
| SAME_TYPE | Same container type | Content-based | "Another book you might like" |
| POPULAR_IN_TYPE | Frequently used type | Usage-based | "Popular in your favorite category" |
| RECENT_ACTIVITY | Based on recent actions | Usage-based | "Based on your recent reading" |
| SEQUENTIAL | Common follow-up pattern | Usage-based | "Readers of X also added Y" |

## Recommendation Feedback

```java
@PostMapping("/api/v1/ai/recommendations/{id}/feedback")
public ResponseEntity<Void> submitFeedback(
    @PathVariable UUID id,
    @RequestBody RecommendationFeedback feedback
) {
    recommendationService.recordFeedback(id, feedback);
    return ResponseEntity.ok().build();
}

public record RecommendationFeedback(
    boolean helpful,
    String reason,  // "ALREADY_HAVE", "NOT_INTERESTED", "SAVED", etc.
    Integer rating  // 1-5
) {}
```
