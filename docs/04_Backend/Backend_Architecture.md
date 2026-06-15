# Backend Architecture

## Technology Stack

| Component | Technology | Purpose |
|---|---|---|
| Framework | Spring Boot 3.x | Web framework, DI, configuration |
| Language | Java 21 | LTS, virtual threads, records, pattern matching |
| Build | Maven 3.9+ | Dependency management, build lifecycle |
| Database | Spring Data JPA / Hibernate 6 | ORM, repository pattern |
| Migrations | Flyway 10 | Versioned database migrations |
| Security | Spring Security 6 | Authentication, authorization |
| API Docs | SpringDoc OpenAPI 2 | OpenAPI 3.0 generation |
| Messaging | Spring AMQP (RabbitMQ) | Async event-driven communication |
| WebSocket | Spring WebSocket / STOMP | Real-time bidirectional communication |
| Caching | Spring Cache + Redis | Distributed caching |
| Vector Search | pgvector | Embedding storage and similarity search |
| AI | Spring AI + Ollama | LLM integration, embeddings |
| Validation | Jakarta Validation 3 | Bean validation |
| Mapping | MapStruct | Entity-DTO mapping |
| Testing | JUnit 5 + Testcontainers | Integration testing |

## Module Structure

```
com.contextos
├── ContextosApplication.java
├── config/
│   ├── SecurityConfig.java
│   ├── WebSocketConfig.java
│   ├── RabbitMQConfig.java
│   ├── RedisConfig.java
│   ├── OpenAPIConfig.java
│   ├── CorsConfig.java
│   ├── JacksonConfig.java
│   ├── FlywayConfig.java
│   └── AsyncConfig.java
│
├── common/
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── BusinessException.java
│   │   ├── ResourceNotFoundException.java
│   │   ├── ValidationException.java
│   │   └── UnauthorizedException.java
│   ├── response/
│   │   ├── ApiResponse.java
│   │   ├── PageResponse.java
│   │   └── ErrorResponse.java
│   ├── audit/
│   │   └── AuditAwareImpl.java
│   └── util/
│       ├── SecurityUtil.java
│       └── ValidationUtil.java
│
├── auth/
│   ├── controller/
│   │   └── AuthController.java
│   ├── dto/
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── AuthResponse.java
│   │   ├── RefreshTokenRequest.java
│   │   └── TokenRefreshResponse.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── JwtService.java
│   │   └── RefreshTokenService.java
│   ├── security/
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── JwtTokenProvider.java
│   │   ├── UserDetailsServiceImpl.java
│   │   └── SecurityUtils.java
│   ├── model/
│   │   └── UserPrincipal.java
│   └── event/
│       ├── UserRegisteredEvent.java
│       └── UserLoggedInEvent.java
│
├── user/
│   ├── controller/
│   │   └── UserController.java
│   ├── dto/
│   │   ├── UserProfileResponse.java
│   │   ├── UpdateUserRequest.java
│   │   └── UserSettingsResponse.java
│   ├── service/
│   │   └── UserService.java
│   └── model/
│       └── User.java
│
├── container/
│   ├── controller/
│   │   └── ContainerController.java
│   ├── dto/
│   │   ├── CreateContainerRequest.java
│   │   ├── UpdateContainerRequest.java
│   │   ├── ContainerResponse.java
│   │   ├── ContainerListResponse.java
│   │   └── ContainerSearchCriteria.java
│   ├── service/
│   │   ├── ContainerService.java
│   │   ├── ContainerMapper.java
│   │   └── ContainerValidator.java
│   ├── model/
│   │   ├── Container.java
│   │   ├── ContainerType.java
│   │   ├── ContainerStatus.java
│   │   └── type/
│   │       ├── BookContainer.java
│   │       ├── MovieContainer.java
│   │       ├── GoalContainer.java
│   │       └── ... (all 12 types)
│   ├── repository/
│   │   └── ContainerRepository.java
│   ├── event/
│   │   ├── ContainerCreatedEvent.java
│   │   ├── ContainerUpdatedEvent.java
│   │   ├── ContainerDeletedEvent.java
│   │   └── ContainerStatusChangedEvent.java
│   └── search/
│       ├── ContainerSearchService.java
│       └── SearchSpecification.java
│
├── tag/
│   ├── controller/
│   │   └── TagController.java
│   ├── dto/
│   │   ├── CreateTagRequest.java
│   │   ├── TagResponse.java
│   │   └── TagSearchRequest.java
│   ├── service/
│   │   └── TagService.java
│   ├── model/
│   │   ├── Tag.java
│   │   └── ContainerTag.java
│   └── repository/
│       └── TagRepository.java
│
├── snapshot/
│   ├── controller/
│   │   └── SnapshotController.java
│   ├── dto/
│   │   ├── CreateSnapshotRequest.java
│   │   ├── SnapshotResponse.java
│   │   └── SnapshotDiffResponse.java
│   ├── service/
│   │   └── SnapshotService.java
│   ├── model/
│   │   └── Snapshot.java
│   └── repository/
│       └── SnapshotRepository.java
│
├── timeline/
│   ├── controller/
│   │   └── TimelineController.java
│   ├── dto/
│   │   ├── TimelineEventResponse.java
│   │   └── TimelineQueryRequest.java
│   ├── service/
│   │   └── TimelineService.java
│   ├── model/
│   │   └── TimelineEvent.java
│   └── repository/
│       └── TimelineEventRepository.java
│
├── pin/
│   ├── controller/
│   │   └── PinController.java
│   ├── dto/
│   │   ├── PinResponse.java
│   │   └── PinRequest.java
│   ├── service/
│   │   └── PinService.java
│   └── model/
│       └── Pin.java
│
├── ai/
│   ├── enrichment/
│   │   ├── EnrichmentService.java
│   │   ├── EnrichmentConsumer.java
│   │   ├── SummaryGenerator.java
│   │   ├── AutoTagger.java
│   │   └── RelationshipDiscoverer.java
│   ├── embedding/
│   │   ├── EmbeddingService.java
│   │   ├── EmbeddingConsumer.java
│   │   └── VectorSearchService.java
│   ├── rag/
│   │   ├── RAGService.java
│   │   ├── ContextRetriever.java
│   │   └── PromptBuilder.java
│   ├── recommendation/
│   │   ├── RecommendationService.java
│   │   └── RecommendationEngine.java
│   ├── knowledge/
│   │   ├── KnowledgeGraphService.java
│   │   ├── EntityExtractor.java
│   │   └── GraphBuilder.java
│   ├── context/
│   │   ├── AIContextService.java
│   │   └── ContextAggregator.java
│   ├── dto/
│   │   ├── EnrichmentResult.java
│   │   ├── SearchResult.java
│   │   ├── RecommendationResult.java
│   │   └── RAGResponse.java
│   └── client/
│       ├── OllamaClient.java
│       └── ModelConfig.java
│
├── search/
│   ├── controller/
│   │   └── SearchController.java
│   ├── service/
│   │   ├── HybridSearchService.java
│   │   ├── FullTextSearchService.java
│   │   └── SearchRanker.java
│   ├── dto/
│   │   ├── SearchRequest.java
│   │   └── SearchResponse.java
│   └── model/
│       └── SearchResult.java
│
├── websocket/
│   ├── WebSocketController.java
│   ├── WebSocketEventListener.java
│   └── dto/
│       ├── ContainerUpdateMessage.java
│       ├── EnrichmentProgressMessage.java
│       └── NotificationMessage.java
│
├── activity/
│   ├── service/
│   │   └── ActivityService.java
│   ├── model/
│   │   └── ActivityFeedItem.java
│   └── event/
│       └── ActivityEvent.java
│
├── dashboard/
│   ├── controller/
│   │   └── DashboardController.java
│   ├── dto/
│   │   ├── DashboardResponse.java
│   │   ├── ContainerSummary.java
│   │   └── ActivitySummary.java
│   └── service/
│       └── DashboardService.java
│
└── analytics/
    ├── controller/
    │   └── AnalyticsController.java
    ├── service/
    │   └── AnalyticsService.java
    ├── dto/
    │   └── UserStatsResponse.java
    └── event/
        └── SearchLoggedEvent.java
```

## Layered Architecture

```
Controller Layer       (@RestController)
    ↓
Service Layer          (@Service, @Transactional)
    ↓
Repository Layer       (@Repository, Spring Data JPA)
    ↓
Database Layer         (PostgreSQL)

Cross-cutting:
    - Security (@PreAuthorize, JWT filter)
    - Events (ApplicationEventPublisher / RabbitMQ)
    - Cache (@Cacheable, @CacheEvict)
    - Validation (@Valid, @Validated)
    - Audit (@CreatedDate, @LastModifiedDate)
```

## Key Architectural Decisions

### 1. Virtual Threads (Java 21)
```java
@Configuration
public class AsyncConfig {
    @Bean
    public TaskExecutor taskExecutor() {
        return new VirtualThreadTaskExecutor("contextos-");
    }
}
```

### 2. Record-based DTOs
```java
public record CreateContainerRequest(
    @NotBlank @Size(max = 500) String title,
    @NotNull ContainerType type,
    String description,
    Map<String, String> metadata,
    Set<String> tags,
    @Min(0) @Max(100) Integer initialProgress
) {}
```

### 3. MapStruct for Entity-DTO Mapping
```java
@Mapper(componentModel = "spring", unmappedTargetPolicy = ERROR)
public interface ContainerMapper {
    Container toEntity(CreateContainerRequest request);
    ContainerResponse toResponse(Container container);
    List<ContainerListResponse> toListResponse(List<Container> containers);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(@MappingTarget Container container, UpdateContainerRequest request);
}
```

### 4. Specification-based Search
```java
public class ContainerSpecification {
    public static Specification<Container> withOwner(UUID ownerId) {
        return (root, query, cb) -> cb.equal(root.get("owner").get("id"), ownerId);
    }
    
    public static Specification<Container> withType(ContainerType type) {
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }
    
    public static Specification<Container> withStatus(ContainerStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
    
    public static Specification<Container> withSearch(String query) {
        return (root, query_, cb) -> cb.or(
            cb.like(cb.lower(root.get("title")), "%" + query.toLowerCase() + "%"),
            cb.like(cb.lower(root.get("description")), "%" + query.toLowerCase() + "%")
        );
    }
}
```

### 5. Caching Strategy
```java
@Service
@CacheConfig(cacheNames = "containers")
public class ContainerServiceImpl implements ContainerService {
    
    @Override
    @Cacheable(key = "#id + ':' + #root.target.getCurrentUserId()")
    public ContainerResponse findById(UUID id) {
        // ...
    }
    
    @Override
    @CachePut(key = "#id + ':' + #root.target.getCurrentUserId()")
    @CacheEvict(key = "'list:' + #root.target.getCurrentUserId()")
    public ContainerResponse update(UUID id, UpdateContainerRequest request) {
        // ...
    }
    
    @Override
    @CacheEvict(key = "#id + ':' + #root.target.getCurrentUserId()")
    public void delete(UUID id) {
        // ...
    }
}
```

### 6. Event Publishing
```java
@Service
public class ContainerServiceImpl implements ContainerService {
    private final ApplicationEventPublisher eventPublisher;
    private final RabbitTemplate rabbitTemplate;
    
    @Override
    @Transactional
    public ContainerResponse create(CreateContainerRequest request) {
        // ... save container ...
        
        // Publish synchronous event (Spring)
        eventPublisher.publishEvent(new ContainerCreatedEvent(this, savedContainer));
        
        // Publish async event (RabbitMQ)
        rabbitTemplate.convertAndSend("container.exchange", "container.created", 
            new ContainerCreatedMessage(savedContainer.getId()));
        
        return mapper.toResponse(savedContainer);
    }
}
```

## Configuration

```yaml
# application.yml
spring:
  application:
    name: contextos-api
  
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:contextos}
    username: ${DB_USERNAME:contextos}
    password: ${DB_PASSWORD:contextos}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000
  
  jpa:
    hibernate:
      ddl-auto: validate  # Flyway manages schema
    show-sql: false
    properties:
      hibernate:
        jdbc:
          batch_size: 50
        format_sql: true
        default_schema: public
  
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
  
  cache:
    type: redis
    redis:
      time-to-live: 300000
  
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: 5672
    username: ${RABBITMQ_USER:guest}
    password: ${RABBITMQ_PASSWORD:guest}
  
  ai:
    ollama:
      base-url: http://${OLLAMA_HOST:localhost}:11434
      model: mistral:7b-q4_K_M
      embedding-model: nomic-embed-text

server:
  port: 8080

jwt:
  secret: ${JWT_SECRET}
  access-token-expiration: 900000    # 15 minutes
  refresh-token-expiration: 2592000000  # 30 days

logging:
  level:
    com.contextos: DEBUG
    org.springframework.security: INFO
```
