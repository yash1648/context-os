# WebSocket Architecture

## Overview

ContextOS uses **Spring WebSocket with STOMP** for real-time bidirectional communication. WebSocket enables:

- Real-time container updates across multiple devices
- AI enrichment progress streaming
- Notifications and alerts
- User presence tracking

## Architecture

```
┌──────────┐     ┌──────────┐     ┌────────────────┐
│  Browser │────▶│  Nginx   │────▶│  Spring Boot   │
│  (STOMP  │     │ (Upgrade │     │  WebSocket     │
│   over   │     │  Proxy)  │     │  Handler       │
│  SockJS) │◀────│          │◀────│                │
└──────────┘     └──────────┘     └───────┬────────┘
                                          │
                    ┌─────────────────────┤
                    │                     │
               ┌────▼────┐          ┌─────▼─────┐
               │  Redis   │          │  RabbitMQ  │
               │  Pub/Sub │          │  (Events)  │
               └─────────┘          └───────────┘
```

## Configuration

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Simple broker for development, Redis for production
        config.enableSimpleBroker("/topic", "/queue");
        
        // Application destination prefix for @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
        
        // User-specific queue prefix
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS()
            .setClientLibraryUrl("/webjars/sockjs-client/1.5.1/sockjs.min.js");
    }

    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
        messageConverters.add(converter);
        return false;
    }
}
```

## WebSocket Controller

```java
@Controller
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketSessionManager sessionManager;

    public WebSocketController(SimpMessagingTemplate messagingTemplate,
                                WebSocketSessionManager sessionManager) {
        this.messagingTemplate = messagingTemplate;
        this.sessionManager = sessionManager;
    }

    /**
     * Broadcast container update to subscribers
     */
    @MessageMapping("/container.update")
    @SendTo("/topic/containers/{containerId}")
    public ContainerUpdateMessage broadcastUpdate(
            @Payload ContainerUpdateMessage message,
            @Header("simpSessionId") String sessionId) {
        message.setTimestamp(Instant.now());
        return message;
    }

    /**
     * Send enrichment progress to specific user
     */
    public void sendEnrichmentProgress(UUID userId, EnrichmentProgressMessage progress) {
        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/queue/enrichment",
            progress
        );
    }

    /**
     * Send notification to specific user
     */
    public void sendNotification(UUID userId, NotificationMessage notification) {
        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/queue/notifications",
            notification
        );
    }

    /**
     * Handle user presence heartbeat
     */
    @MessageMapping("/presence")
    public void handlePresence(@Payload PresenceMessage presence,
                                @Header("simpSessionId") String sessionId,
                                Principal principal) {
        sessionManager.updatePresence(principal.getName(), sessionId, presence);
    }
}
```

## WebSocket Session Management

```java
@Service
public class WebSocketSessionManager {

    private final ConcurrentHashMap<String, UserSession> sessions = new ConcurrentHashMap<>(); // sessionId -> UserSession
    private final ConcurrentHashMap<String, Set<String>> userSessions = new ConcurrentHashMap<>(); // userId -> Set<sessionId>

    public void registerSession(String sessionId, String userId) {
        sessions.put(sessionId, new UserSession(sessionId, userId, Instant.now()));
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
    }

    public void removeSession(String sessionId) {
        UserSession session = sessions.remove(sessionId);
        if (session != null) {
            Set<String> userSess = userSessions.get(session.userId());
            if (userSess != null) {
                userSess.remove(sessionId);
                if (userSess.isEmpty()) {
                    userSessions.remove(session.userId());
                }
            }
        }
    }

    public boolean isUserOnline(String userId) {
        return userSessions.containsKey(userId) && !userSessions.get(userId).isEmpty();
    }

    public int getActiveSessionCount() {
        return sessions.size();
    }

    public record UserSession(String sessionId, String userId, Instant connectedAt) {}
}
```

## WebSocket Event Listener

```java
@Component
public class WebSocketEventListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);
    private final WebSocketSessionManager sessionManager;

    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headers.getSessionId();
        log.info("WebSocket connected: {}", sessionId);
    }

    @EventListener
    public void handleSessionDisconnected(SessionDisconnectEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headers.getSessionId();
        sessionManager.removeSession(sessionId);
        log.info("WebSocket disconnected: {}", sessionId);
    }

    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        log.debug("Subscription: {} -> {}", headers.getSessionId(), headers.getDestination());
    }
}
```

## Client Integration (React)

```typescript
// src/lib/websocket.ts
import { Client, IMessage, IFrame } from '@stomp/stompjs';
import { useAuthStore } from './stores/auth';

class WebSocketManager {
  private client: Client | null = null;
  private subscriptions: Map<string, () => void> = new Map();

  connect() {
    const token = useAuthStore.getState().accessToken;
    
    this.client = new Client({
      brokerURL: `wss://contextos.app/ws`,
      connectHeaders: {
        Authorization: `Bearer ${token}`
      },
      debug: (str) => console.debug('WS:', str),
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => this.onConnected(),
      onDisconnect: () => this.onDisconnected(),
      onStompError: (frame) => this.onError(frame),
    });

    this.client.activate();
  }

  disconnect() {
    this.subscriptions.forEach(unsubscribe => unsubscribe());
    this.subscriptions.clear();
    this.client?.deactivate();
  }

  subscribe(destination: string, callback: (message: any) => void): () => void {
    if (!this.client?.connected) {
      console.warn('WebSocket not connected, queueing subscription:', destination);
      return () => {};
    }

    const subscription = this.client.subscribe(destination, (message: IMessage) => {
      const payload = JSON.parse(message.body);
      callback(payload);
    });

    const unsubscribe = () => subscription.unsubscribe();
    this.subscriptions.set(destination, unsubscribe);
    
    return unsubscribe;
  }

  subscribeToContainer(containerId: string, callback: (update: any) => void): () => void {
    return this.subscribe(`/topic/containers/${containerId}`, callback);
  }

  subscribeToNotifications(userId: string, callback: (notification: any) => void): () => void {
    return this.subscribe(`/user/${userId}/queue/notifications`, callback);
  }

  subscribeToEnrichment(userId: string, callback: (progress: any) => void): () => void {
    return this.subscribe(`/user/${userId}/queue/enrichment`, callback);
  }

  send(destination: string, payload: any) {
    this.client?.publish({
      destination: `/app/${destination}`,
      body: JSON.stringify(payload)
    });
  }

  private onConnected() {
    console.log('WebSocket connected');
    // Re-establish subscriptions
  }

  private onDisconnected() {
    console.log('WebSocket disconnected');
  }

  private onError(frame: IFrame) {
    console.error('WebSocket error:', frame);
  }
}

export const wsManager = new WebSocketManager();
```

## TanStack Query Integration

```typescript
// src/hooks/useRealtimeContainer.ts
import { useQueryClient } from '@tanstack/react-query';
import { useEffect } from 'react';
import { wsManager } from '@/lib/websocket';

export function useRealtimeContainer(containerId: string) {
  const queryClient = useQueryClient();

  useEffect(() => {
    const unsubscribe = wsManager.subscribeToContainer(
      containerId,
      (update) => {
        // Update query cache with real-time data
        queryClient.setQueryData(
          ['container', containerId],
          (old: any) => ({
            ...old,
            ...update.payload,
            updatedAt: update.timestamp,
          })
        );
        
        // Invalidate list queries to reflect changes
        queryClient.invalidateQueries({ queryKey: ['containers'] });
      }
    );

    return unsubscribe;
  }, [containerId, queryClient]);
}
```

## Message Types

```json
{
  "CONTAINER_CREATED": { "containerId": "uuid", "type": "BOOK", "title": "..." },
  "CONTAINER_UPDATED": { "containerId": "uuid", "field": "progressPercentage", "oldValue": 25, "newValue": 50 },
  "CONTAINER_DELETED": { "containerId": "uuid" },
  "ENRICHMENT_STARTED": { "containerId": "uuid", "model": "mistral:7b" },
  "ENRICHMENT_PROGRESS": { "containerId": "uuid", "stage": "SUMMARIZATION", "progress": 50 },
  "ENRICHMENT_COMPLETED": { "containerId": "uuid", "summary": "...", "tags": [...] },
  "ENRICHMENT_FAILED": { "containerId": "uuid", "error": "Model timeout" },
  "RECOMMENDATION_READY": { "count": 5, "message": "New recommendations available" },
  "NOTIFICATION": { "type": "INFO", "title": "...", "body": "..." }
}
```

## Performance Considerations

```yaml
WebSocket Performance:
  max_connections_per_instance: 10000
  heartbeat_interval: 10 seconds
  message_size_limit: 256 KB
  
  scaling:
    single_instance: Simple broker (default)
    multi_instance: Redis pub/sub broker relay
  
  monitoring:
    - Track active connections via Actuator
    - Alert on connection drops > 5%
    - Log message size and frequency by topic
```
