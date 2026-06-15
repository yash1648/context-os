# RAG Pipeline Design

## Overview

The RAG (Retrieval-Augmented Generation) pipeline enables users to ask natural language questions about their knowledge base and receive answers synthesized from their containers.

```
┌────────────┐    ┌──────────────┐    ┌──────────────┐
│  User      │    │  Retrieval   │    │  Generation   │
│  Question  │───▶│  Phase       │───▶│  Phase        │───▶ Answer
│            │    │              │    │              │     + Citations
│ "What have │    │ 1. Embed     │    │ 1. Build     │
│  I learned │    │    Query     │    │    Prompt    │
│  about ML?"│    │ 2. Vector    │    │ 2. LLM Call  │
│            │    │    Search    │    │ 3. Format    │
│            │    │ 3. Text      │    │    Response  │
│            │    │    Search    │    └──────────────┘
│            │    │ 4. Rerank    │
│            │    └──────────────┘
```

## RAG Service

```java
@Service
public class RAGService {

    private final OllamaClient ollamaClient;
    private final ContextRetrievalService contextRetrieval;
    private final PromptBuilder promptBuilder;
    private final ResponseFormatter responseFormatter;

    public CompletableFuture<RAGResponse> ask(RAGRequest request, UUID userId) {
        return CompletableFuture.supplyAsync(() -> {
            // 1. Retrieve relevant context
            List<ContextSource> sources = contextRetrieval.retrieveContext(
                request.question(),
                userId,
                ContextRetrievalRequest.builder()
                    .maxResults(5)
                    .vectorWeight(0.7f)
                    .textWeight(0.3f)
                    .build()
            );

            // 2. Build prompt with context
            String prompt = promptBuilder.buildPrompt(
                request.question(),
                sources
            );

            // 3. Generate answer
            String answer = ollamaClient.generate(prompt, request.temperature());

            // 4. Format response with citations
            return responseFormatter.format(answer, sources);
        });
    }

    public SseEmitter streamAsk(RAGRequest request, UUID userId) {
        SseEmitter emitter = new SseEmitter(30000L); // 30s timeout

        // 1. Retrieve context (sync)
        List<ContextSource> sources = contextRetrieval.retrieveContext(
            request.question(),
            userId,
            ContextRetrievalRequest.builder().build()
        );

        // 2. Stream response via WebSocket
        String prompt = promptBuilder.buildPrompt(request.question(), sources);
        
        ollamaClient.generateStream(prompt, 0.3, (chunk) -> {
            try {
                emitter.send(SseEmitter.event()
                    .name("token")
                    .data(chunk));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        });

        // 3. Send sources after completion
        try {
            emitter.send(SseEmitter.event()
                .name("sources")
                .data(sources));
            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }
}
```

## Prompt Builder

```java
@Component
public class PromptBuilder {

    private static final String SYSTEM_PROMPT = """
        You are ContextOS, an AI assistant that helps users understand and explore their personal knowledge base.
        
        Rules:
        - Answer based ONLY on the provided context. If the context doesn't contain relevant information, say so.
        - Always cite sources using [Source N] notation.
        - Be concise but thorough. Use bullet points for lists.
        - If the user asks about something not in their knowledge base, suggest how they might add it.
        - Maintain a helpful, knowledgeable tone.
        """;

    private static final String NO_CONTEXT_RESPONSE = """
        I couldn't find any information in your knowledge base related to your question.
        
        You might want to:
        1. Try rephrasing your question
        2. Add containers related to this topic
        3. Check if you have relevant containers with different tags
        
        What would you like to know more about?
        """;

    public String buildPrompt(String question, List<ContextSource> sources) {
        if (sources.isEmpty()) {
            return SYSTEM_PROMPT + "\n\nUser: " + question + "\n\n" + NO_CONTEXT_RESPONSE;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(SYSTEM_PROMPT).append("\n\n");
        sb.append("Context from user's knowledge base:\n\n");

        for (int i = 0; i < sources.size(); i++) {
            ContextSource source = sources.get(i);
            sb.append("[Source ").append(i + 1).append("] ");
            sb.append(source.getTitle()).append(" (").append(source.getContainerType()).append(")\n");
            sb.append("Relevance: ").append(String.format("%.0f%%", source.getRelevance() * 100)).append("\n");
            sb.append(source.getContent()).append("\n\n");
        }

        sb.append("User Question: ").append(question).append("\n\n");
        sb.append("Answer (cite sources as [Source N]):");

        return sb.toString();
    }

    public String buildFollowUpPrompt(String conversationHistory, String newQuestion) {
        return SYSTEM_PROMPT + "\n\n" +
            "Conversation history:\n" + conversationHistory + "\n\n" +
            "New Question: " + newQuestion + "\n\n" +
            "Answer:";
    }
}
```

## Response Formatter

```java
@Component
public class ResponseFormatter {

    public RAGResponse format(String rawAnswer, List<ContextSource> sources) {
        // Parse citations from answer
        List<Citation> citations = extractCitations(rawAnswer, sources);

        // Build structured response
        return new RAGResponse(
            cleanAnswer(rawAnswer),
            citations,
            calculateConfidence(citations, sources),
            sources.stream()
                .map(s -> new SourceInfo(
                    s.getContainerId(),
                    s.getTitle(),
                    s.getContainerType().name(),
                    s.getRelevance()
                ))
                .collect(Collectors.toList())
        );
    }

    private List<Citation> extractCitations(String answer, List<ContextSource> sources) {
        List<Citation> citations = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\[Source (\\d+)\\]");
        Matcher matcher = pattern.matcher(answer);

        while (matcher.find()) {
            int sourceNum = Integer.parseInt(matcher.group(1));
            if (sourceNum > 0 && sourceNum <= sources.size()) {
                ContextSource source = sources.get(sourceNum - 1);
                citations.add(new Citation(
                    source.getContainerId(),
                    source.getTitle(),
                    sourceNum
                ));
            }
        }

        return citations;
    }

    private String cleanAnswer(String rawAnswer) {
        // Remove any instruction leakage
        return rawAnswer
            .replaceAll("(?s)System:.*?(?=User:|$)", "")
            .replaceAll("(?s)Context from user's knowledge base:.*?(?=User Question:|$)", "")
            .trim();
    }

    private double calculateConfidence(List<Citation> citations, List<ContextSource> sources) {
        if (sources.isEmpty()) return 0.0;
        
        // Average relevance of cited sources
        double avgRelevance = sources.stream()
            .mapToDouble(ContextSource::getRelevance)
            .average()
            .orElse(0.0);

        // Boost if we have citations
        double citationBoost = citations.isEmpty() ? 0.0 : 0.15;

        return Math.min(1.0, avgRelevance + citationBoost);
    }
}
```

## RAG Request/Response Models

```java
public record RAGRequest(
    @NotBlank String question,
    float temperature,
    int maxTokens,
    List<String> containerTypeFilter,
    boolean includeSources
) {
    public RAGRequest {
        temperature = Math.max(0.1f, Math.min(0.5f, temperature));
        maxTokens = maxTokens > 0 ? Math.min(maxTokens, 2048) : 512;
        includeSources = true;
    }
}

public record RAGResponse(
    String answer,
    List<Citation> citations,
    double confidence,
    List<SourceInfo> sources,
    String modelUsed,
    long processingTimeMs
) {
    public RAGResponse(String answer, List<Citation> citations, double confidence, List<SourceInfo> sources) {
        this(answer, citations, confidence, sources, "mistral:7b-q4_K_M", 0);
    }
}

public record Citation(
    UUID containerId,
    String title,
    int sourceNumber
) {}

public record SourceInfo(
    UUID containerId,
    String title,
    String containerType,
    double relevance
) {}
```

## RAG Quality Metrics

| Metric | Target | Measurement |
|---|---|---|
| Answer relevance | > 4.0/5.0 | User feedback rating |
| Citation accuracy | > 90% | Manual audit |
| Source relevance | > 0.7 mean | Embedding cosine similarity |
| Response time | < 8s p95 | Server timing |
| Hallucination rate | < 5% | Automatic fact-checking |
| User satisfaction | > 85% | Post-query survey |

## Follow-up Questions

```java
@Component
public class FollowUpGenerator {

    /**
     * Generates suggested follow-up questions based on the current answer.
     */
    public List<String> generateFollowUps(String question, String answer, List<ContextSource> sources) {
        String prompt = """
            Based on this Q&A, suggest 3 follow-up questions the user might want to ask.
            
            Original Question: %s
            Answer: %s
            
            Suggested follow-up questions (one per line):
            """.formatted(question, truncate(answer, 500));

        String response = ollamaClient.generate(prompt, 0.4);
        return Arrays.stream(response.split("\n"))
            .map(String::trim)
            .filter(s -> s.endsWith("?"))
            .limit(3)
            .collect(Collectors.toList());
    }
}
```
