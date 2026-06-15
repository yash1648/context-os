# Package Structure

## Complete Package Tree

```
com.contextos/
├── ContextosApplication.java
│
├── config/
│   ├── SecurityConfig.java           # Spring Security configuration
│   ├── WebSocketConfig.java          # WebSocket & STOMP configuration
│   ├── RabbitMQConfig.java           # RabbitMQ exchange, queue, binding setup
│   ├── RedisConfig.java              # Redis cache manager configuration
│   ├── OpenAPIConfig.java            # Swagger/OpenAPI configuration
│   ├── CorsConfig.java               # CORS policy configuration
│   ├── JacksonConfig.java            # Jackson ObjectMapper customization
│   ├── FlywayConfig.java             # Flyway migration configuration
│   ├── AsyncConfig.java              # Virtual thread task executor
│   └── ModelConfig.java              # Spring AI model configuration
│
├── common/
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java     # @RestControllerAdvice
│   │   ├── BusinessException.java          # Base business exception
│   │   ├── ResourceNotFoundException.java   # 404 errors
│   │   ├── ValidationException.java        # Validation errors
│   │   ├── UnauthorizedException.java       # 401 errors
│   │   └── ForbiddenException.java          # 403 errors
│   ├── response/
│   │   ├── ApiResponse.java                # Generic API response wrapper
│   │   ├── PageResponse.java               # Paginated response
│   │   └── ErrorResponse.java              # Error response with code + message
│   ├── audit/
│   │   ├── AuditAwareImpl.java             # Spring Data JPA auditor
│   │   └── BaseEntity.java                 # Abstract entity with createdAt/updatedAt
│   └── util/
│       ├── SecurityUtil.java               # Current user context helper
│       └── ValidationUtil.java             # Common validation helpers
│
├── auth/
│   ├── controller/
│   │   └── AuthController.java             # /api/auth/**
│   ├── dto/
│   │   ├── request/
│   │   │   ├── LoginRequest.java
│   │   │   ├── RegisterRequest.java
│   │   │   └── RefreshTokenRequest.java
│   │   └── response/
│   │       ├── AuthResponse.java
│   │       ├── TokenRefreshResponse.java
│   │       └── UserInfoResponse.java
│   ├── service/
│   │   ├── AuthService.java                # Auth orchestration
│   │   ├── JwtService.java                 # JWT create/validate
│   │   ├── RefreshTokenService.java        # Refresh token lifecycle
│   │   └── OAuth2Service.java              # OAuth2 login handling
│   ├── security/
│   │   ├── JwtAuthenticationFilter.java    # OncePerRequestFilter
│   │   ├── JwtTokenProvider.java           # Token generation/validation
│   │   ├── CustomUserDetailsService.java   # UserDetailsService impl
│   │   └── OAuth2SuccessHandler.java       # OAuth2 callback handler
│   ├── model/
│   │   ├── UserPrincipal.java              # UserDetails implementation
│   │   └── Role.java                       # Role enum
│   └── event/
│       ├── UserRegisteredEvent.java
│       └── UserLoggedInEvent.java
│
├── user/
│   ├── controller/
│   │   └── UserController.java             # /api/users/**
│   ├── dto/
│   │   ├── UserProfileResponse.java
│   │   ├── UpdateUserRequest.java
│   │   ├── UserSettingsRequest.java
│   │   └── UserSettingsResponse.java
│   ├── service/
│   │   └── UserService.java
│   ├── model/
│   │   └── User.java                       # @Entity
│   ├── repository/
│   │   └── UserRepository.java
│   └── mapper/
│       └── UserMapper.java
│
├── container/
│   ├── controller/
│   │   └── ContainerController.java        # /api/containers/**
│   ├── dto/
│   │   ├── request/
│   │   │   ├── CreateContainerRequest.java
│   │   │   ├── UpdateContainerRequest.java
│   │   │   └── ContainerSearchRequest.java
│   │   └── response/
│   │       ├── ContainerResponse.java
│   │       ├── ContainerListResponse.java
│   │       └── ContainerSummaryResponse.java
│   ├── service/
│   │   ├── ContainerService.java           # Interface
│   │   ├── ContainerServiceImpl.java       # Implementation
│   │   ├── ContainerValidator.java         # Create/update validation
│   │   └── ContainerAuthorizationService.java # Ownership check
│   ├── model/
│   │   ├── Container.java                  # @Entity (base)
│   │   ├── ContainerType.java              # Enum: BOOK, MOVIE, etc.
│   │   ├── ContainerStatus.java            # Enum: DRAFT, ACTIVE, etc.
│   │   ├── MetadataEntry.java              # @Embeddable K-V pair
│   │   └── type/
│   │       ├── BookContainer.java
│   │       ├── MovieContainer.java
│   │       ├── TVSeriesContainer.java
│   │       ├── CourseContainer.java
│   │       ├── LearningProgressContainer.java
│   │       ├── SoftwareProjectContainer.java
│   │       ├── GoalContainer.java
│   │       ├── HabitContainer.java
│   │       ├── NoteContainer.java
│   │       ├── SnapshotContainer.java
│   │       ├── PinnedContentContainer.java
│   │       └── KnowledgeAssetContainer.java
│   ├── repository/
│   │   ├── ContainerRepository.java
│   │   └── ContainerCustomRepository.java  # Custom queries
│   ├── event/
│   │   ├── ContainerCreatedEvent.java
│   │   ├── ContainerUpdatedEvent.java
│   │   ├── ContainerDeletedEvent.java
│   │   ├── ContainerArchivedEvent.java
│   │   └── ContainerStatusChangedEvent.java
│   ├── mapper/
│   │   └── ContainerMapper.java            # MapStruct
│   ├── search/
│   │   ├── ContainerSpecification.java     # JPA Specification builder
│   │   └── ContainerSearchService.java     # Search orchestrator
│   └── listener/
│       └── ContainerEventListener.java     # Event handlers
│
├── tag/
│   ├── controller/
│   │   └── TagController.java              # /api/tags/**
│   ├── dto/
│   │   ├── CreateTagRequest.java
│   │   ├── UpdateTagRequest.java
│   │   ├── TagResponse.java
│   │   └── TagSummaryResponse.java
│   ├── service/
│   │   └── TagService.java
│   ├── model/
│   │   ├── Tag.java                        # @Entity
│   │   └── ContainerTag.java               # @Entity (join table)
│   ├── repository/
│   │   └── TagRepository.java
│   └── mapper/
│       └── TagMapper.java
│
├── snapshot/
│   ├── controller/
│   │   └── SnapshotController.java         # /api/containers/{id}/snapshots
│   ├── dto/
│   │   ├── CreateSnapshotRequest.java
│   │   ├── SnapshotResponse.java
│   │   └── SnapshotDiffResponse.java
│   ├── service/
│   │   └── SnapshotService.java
│   ├── model/
│   │   └── Snapshot.java                   # @Entity
│   ├── repository/
│   │   └── SnapshotRepository.java
│   ├── mapper/
│   │   └── SnapshotMapper.java
│   └── util/
│       └── DiffCalculator.java             # JSON diff logic
│
├── timeline/
│   ├── controller/
│   │   └── TimelineController.java         # /api/containers/{id}/timeline
│   ├── dto/
│   │   ├── TimelineEventResponse.java
│   │   └── TimelineQueryRequest.java
│   ├── service/
│   │   └── TimelineService.java
│   ├── model/
│   │   ├── TimelineEvent.java              # @Entity
│   │   └── TimelineEventType.java          # Enum
│   ├── repository/
│   │   └── TimelineEventRepository.java
│   └── mapper/
│       └── TimelineMapper.java
│
├── pin/
│   ├── controller/
│   │   └── PinController.java              # /api/pins/**
│   ├── dto/
│   │   ├── PinRequest.java
│   │   └── PinResponse.java
│   ├── service/
│   │   └── PinService.java
│   ├── model/
│   │   └── Pin.java                        # @Entity
│   └── repository/
│       └── PinRepository.java
│
├── ai/
│   ├── enrichment/
│   │   ├── EnrichmentOrchestrator.java     # Main enrichment coordinator
│   │   ├── EnrichmentConsumer.java         # RabbitMQ consumer
│   │   ├── SummaryGenerator.java           # LLM-based summarization
│   │   ├── AutoTagger.java                 # LLM-based tag suggestion
│   │   ├── RelationshipDiscoverer.java     # Cross-container link discovery
│   │   └── EnrichmentScheduler.java        # Periodic re-enrichment
│   ├── embedding/
│   │   ├── EmbeddingService.java           # Embedding generation
│   │   ├── EmbeddingConsumer.java          # Async embedding consumer
│   │   └── VectorSearchService.java        # pgvector similarity search
│   ├── rag/
│   │   ├── RAGService.java                 # RAG orchestration
│   │   ├── ContextRetriever.java           # Retrieve relevant context
│   │   ├── PromptBuilder.java              # Construct LLM prompts
│   │   └── ResponseFormatter.java          # Format LLM responses
│   ├── recommendation/
│   │   ├── RecommendationService.java      # Interface
│   │   ├── RecommendationEngine.java       # Collaborative + content-based
│   │   └── RecommendationScheduler.java    # Periodic batch generation
│   ├── knowledge/
│   │   ├── KnowledgeGraphService.java      # Graph CRUD + query
│   │   ├── EntityExtractor.java            # NER using LLM
│   │   ├── GraphBuilder.java               # Edge discovery
│   │   └── GraphTraversalService.java      # Path finding, connected components
│   ├── context/
│   │   ├── AIContextService.java           # AI context CRUD
│   │   └── ContextAggregator.java          # Combine multiple AI results
│   ├── client/
│   │   ├── OllamaClient.java               # HTTP client for Ollama API
│   │   ├── OllamaEmbeddingClient.java      # Embedding-specific client
│   │   └── ModelRegistry.java              # Available models
│   ├── dto/
│   │   ├── EnrichmentRequest.java
│   │   ├── EnrichmentResult.java
│   │   ├── SearchResult.java
│   │   ├── RecommendationResult.java
│   │   ├── RAGRequest.java
│   │   └── RAGResponse.java
│   └── model/
│       ├── AIContext.java                  # @Entity
│       └── EnrichmentStatus.java           # Enum
│
├── search/
│   ├── controller/
│   │   └── SearchController.java           # /api/search/**
│   ├── service/
│   │   ├── SearchService.java              # Interface
│   │   ├── HybridSearchService.java        # Full-text + vector merge
│   │   ├── FullTextSearchService.java      # PostgreSQL full-text
│   │   └── SearchRanker.java               # Result ranking
│   ├── dto/
│   │   ├── SearchRequest.java
│   │   └── SearchResponse.java
│   └── model/
│       └── SearchResultItem.java
│
├── websocket/
│   ├── WebSocketController.java            # STOMP message handlers
│   ├── WebSocketEventListener.java         # Connect/disconnect events
│   ├── WebSocketSessionManager.java        # Track active sessions
│   └── dto/
│       ├── ContainerUpdateMessage.java
│       ├── EnrichmentProgressMessage.java
│       ├── NotificationMessage.java
│       └── PresenceMessage.java
│
├── activity/
│   ├── service/
│   │   └── ActivityService.java
│   ├── model/
│   │   └── ActivityFeedItem.java           # @Entity
│   ├── repository/
│   │   └── ActivityFeedRepository.java
│   ├── event/
│   │   └── ActivityEvent.java              # Generic activity event
│   └── listener/
│       └── ActivityEventListener.java
│
├── dashboard/
│   ├── controller/
│   │   └── DashboardController.java        # /api/dashboard/**
│   ├── dto/
│   │   ├── DashboardResponse.java
│   │   ├── ContainerSummary.java
│   │   ├── RecentActivity.java
│   │   └── ProgressSummary.java
│   └── service/
│       └── DashboardService.java
│
├── analytics/
│   ├── controller/
│   │   └── AnalyticsController.java        # /api/analytics/**
│   ├── service/
│   │   └── AnalyticsService.java
│   ├── dto/
│   │   ├── UserStatsResponse.java
│   │   ├── ContainerStats.java
│   │   ├── ActivityStats.java
│   │   └── TrendResponse.java
│   └── event/
│       └── AnalyticsEvent.java
│
├── integration/
│   ├── browser/
│   │   └── BrowserExtensionController.java # /api/integration/browser/**
│   ├── vscode/
│   │   └── VSCodeExtensionController.java  # /api/integration/vscode/**
│   └── webhook/
│       ├── WebhookController.java          # /api/webhooks/**
│       ├── WebhookService.java
│       └── WebhookConfig.java
│
└── health/
    └── HealthController.java               # /api/health, /api/info
```

## Build Configuration

```xml
<!-- pom.xml (key dependencies) -->
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-amqp</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    
    <!-- Spring AI -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-ollama-spring-boot-starter</artifactId>
        <version>1.0.0-M6</version>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-database-postgresql</artifactId>
    </dependency>
    
    <!-- Mapping -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.6.3</version>
    </dependency>
    
    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.6</version>
    </dependency>
    
    <!-- Testing -->
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>testcontainers</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```
