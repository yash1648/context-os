# AI Architecture

## Overview

ContextOS features a comprehensive AI layer that provides semantic understanding, intelligent retrieval, and proactive recommendations — all powered by local LLMs via Ollama.

```
┌────────────────────────────────────────────────────────────┐
│                    AI Layer Overview                        │
│                                                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │  Embedding    │  │  Enrichment  │  │  RAG Pipeline    │  │
│  │  Pipeline     │  │  Pipeline    │  │                  │  │
│  │               │  │              │  │  Query →         │  │
│  │  Content →    │  │  Content →   │  │  Retrieve →      │  │
│  │  Vector →     │  │  Summary     │  │  Augment →       │  │
│  │  Store        │  │  Auto-tags   │  │  Generate        │  │
│  │               │  │  Relations   │  │                  │  │
│  └──────┬───────┘  └──────┬───────┘  └────────┬─────────┘  │
│         │                 │                    │            │
│         └────────┬────────┴────────────────────┘            │
│                  │                                          │
│         ┌────────▼────────┐   ┌─────────────────────┐      │
│         │  Ollama Service │   │  Knowledge Graph    │      │
│         │                 │   │                     │      │
│         │  • Mistral 7B   │   │  • Entity Extract   │      │
│         │  • nomic-embed  │   │  • Relation Discover │      │
│         │  • Llama 3.2    │   │  • Graph Query       │      │
│         └─────────────────┘   └─────────────────────┘      │
└────────────────────────────────────────────────────────────┘
```

## Key Components

### 1. Embedding Pipeline
Converts container content into vector embeddings for semantic search.

### 2. Enrichment Pipeline
AI-powered summarization, auto-tagging, and relationship discovery.

### 3. RAG Pipeline
Retrieval-Augmented Generation for answering questions about user's knowledge base.

### 4. Recommendation Engine
Content-based and collaborative filtering for suggesting relevant containers.

### 5. Knowledge Graph
Entity extraction, relationship mapping, and graph traversal.

## Ollama Integration

```java
@Service
public class OllamaClient {

    private final OllamaApi ollamaApi;
    private final String model;
    private final String embeddingModel;

    public OllamaClient(
        @Value("${spring.ai.ollama.base-url}") String baseUrl,
        @Value("${spring.ai.ollama.model:mistral:7b-q4_K_M}") String model,
        @Value("${spring.ai.ollama.embedding-model:nomic-embed-text}") String embeddingModel
    ) {
        this.ollamaApi = new OllamaApi(baseUrl);
        this.model = model;
        this.embeddingModel = embeddingModel;
    }

    public String generate(String prompt, float temperature) {
        var request = GenerateRequest.builder(model)
            .prompt(prompt)
            .temperature(temperature)
            .stream(false)
            .options(Map.of("num_ctx", 4096))
            .build();

        var response = ollamaApi.generate(request);
        return response.getResponse();
    }

    public String generateWithContext(String systemPrompt, String userPrompt, List<String> context) {
        String contextualizedPrompt = buildContextualizedPrompt(systemPrompt, userPrompt, context);
        return generate(contextualizedPrompt, 0.3);
    }

    public float[] generateEmbedding(String text) {
        var request = EmbeddingRequest.builder(embeddingModel, List.of(text)).build();
        var response = ollamaApi.embed(request);
        return response.getEmbeddings().get(0).stream()
            .mapToDouble(d -> d)
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll)
            .stream()
            .mapToDouble(d -> d)
            .toArray();
    }

    private String buildContextualizedPrompt(String system, String user, List<String> context) {
        StringBuilder sb = new StringBuilder();
        sb.append("System: ").append(system).append("\n\n");
        sb.append("Context:\n");
        for (int i = 0; i < context.size(); i++) {
            sb.append("[").append(i + 1).append("] ").append(context.get(i)).append("\n");
        }
        sb.append("\nUser: ").append(user);
        return sb.toString();
    }
}
```

## Model Configuration

```yaml
# application-ai.yml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      model: mistral:7b-q4_K_M
      embedding-model: nomic-embed-text
      chat:
        options:
          temperature: 0.3
          top-k: 40
          top-p: 0.9
          num-predict: 2048
          stop: ["User:", "\n\n"]

contextos:
  ai:
    embedding:
      dimension: 768  # nomic-embed-text output dimension
      batch-size: 10
    enrichment:
      schedule: "0 0 3 * * ?"  # Daily at 3 AM
      max-retries: 3
    recommendation:
      batch-size: 50
      cache-ttl: 3600
    rag:
      max-context-sources: 5
      max-tokens: 1024
```

## Ollama Docker Setup

```yaml
# docker-compose.override.yml (development)
services:
  ollama:
    image: ollama/ollama:latest
    ports:
      - "11434:11434"
    volumes:
      - ollama-data:/root/.ollama
      - ./ollama-setup.sh:/setup.sh
    environment:
      - OLLAMA_KEEP_ALIVE=24h
    entrypoint: ["/bin/sh", "-c", "sh /setup.sh && ollama serve"]
    deploy:
      resources:
        reservations:
          memory: 8G

# ollama-setup.sh
#!/bin/sh
ollama pull nomic-embed-text
ollama pull mistral:7b-q4_K_M
ollama pull llama3.2:3b
echo "Models downloaded"
```

## AI Service Orchestration

```java
@Service
public class EnrichmentOrchestrator {

    private final OllamaClient ollama;
    private final ContainerRepository containerRepository;
    private final AIContextRepository aiContextRepository;
    private final EventPublisher eventPublisher;

    @Async("taskExecutor")
    @Transactional
    public CompletableFuture<EnrichmentResult> enrichContainer(UUID containerId) {
        Container container = containerRepository.findById(containerId)
            .orElseThrow(() -> new ContainerNotFoundException(containerId));

        AIContext context = aiContextRepository.findByContainerId(containerId)
            .orElse(new AIContext(containerId));

        try {
            context.setEnrichmentStatus(EnrichmentStatus.PROCESSING);

            // 1. Generate embedding
            String contentToEmbed = buildEmbeddingContent(container);
            float[] embedding = ollama.generateEmbedding(contentToEmbed);
            container.setEmbedding(embedding);

            // 2. Generate summary (for supported types)
            if (supportsSummary(container.getType())) {
                String summary = generateSummary(container);
                context.setSummary(summary);
                context.setSummaryModel("mistral:7b-q4_K_M");
                context.setSummaryGeneratedAt(Instant.now());
            }

            // 3. Auto-tag
            List<AutoTagResult> autoTags = generateAutoTags(container);
            context.setAutoTags(autoTags.stream().map(AutoTagResult::tag).toArray(String[]::new));
            context.setAutoTagScores(autoTags.stream().map(AutoTagResult::score).toArray(Float[]::new));

            // 4. Discover relationships
            List<Relationship> relationships = discoverRelationships(container);
            context.setRelationships(relationships);

            context.setEnrichmentVersion(context.getEnrichmentVersion() + 1);
            context.setEnrichmentStatus(EnrichmentStatus.COMPLETED);
            context.setLastEnrichedAt(Instant.now());

            aiContextRepository.save(context);
            containerRepository.save(container);

            eventPublisher.publish(new EnrichmentCompletedEvent(containerId, 
                new EnrichmentResult(summary, autoTags, relationships)));

            return CompletableFuture.completedFuture(
                new EnrichmentResult(summary, autoTags, relationships)
            );

        } catch (Exception e) {
            context.setEnrichmentStatus(EnrichmentStatus.FAILED);
            aiContextRepository.save(context);
            log.error("Enrichment failed for container: {}", containerId, e);
            throw new EnrichmentFailedException("Enrichment failed", e);
        }
    }

    private String generateSummary(Container container) {
        String prompt = """
            Summarize the following content in 2-3 sentences.
            Focus on key themes, main ideas, and why it matters.
            
            Title: %s
            Description: %s
            Type: %s
            Metadata: %s
            """.formatted(
                container.getTitle(),
                container.getDescription(),
                container.getType(),
                container.getMetadata()
            );

        return ollama.generate(prompt, 0.3);
    }

    private List<AutoTagResult> generateAutoTags(Container container) {
        String prompt = """
            Suggest up to 5 relevant tags for this content.
            Return only tag names separated by commas.
            
            Title: %s
            Description: %s
            Type: %s
            """.formatted(
                container.getTitle(),
                container.getDescription(),
                container.getType()
            );

        String response = ollama.generate(prompt, 0.2);
        return Arrays.stream(response.split(","))
            .map(String::trim)
            .filter(tag -> !tag.isEmpty())
            .limit(5)
            .map(tag -> new AutoTagResult(tag, 0.85f))  // Simplified confidence
            .toList();
    }
}
```

## AI Performance Targets

| Operation | Target Latency | Model | Hardware |
|---|---|---|---|
| Embedding generation | < 500ms | nomic-embed-text | CPU (any) |
| Summary generation | < 5s | Mistral 7B Q4 | CPU (8GB RAM) |
| Auto-tagging | < 3s | Mistral 7B Q4 | CPU (8GB RAM) |
| Relationship discovery | < 10s | Mistral 7B Q4 | CPU (8GB RAM) |
| RAG query | < 8s | Mistral 7B Q4 | CPU (8GB RAM) |
| Batch enrichment (50) | < 5 min | All models | CPU (16GB RAM) |
