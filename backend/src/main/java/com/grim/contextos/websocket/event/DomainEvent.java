package com.grim.contextos.websocket.event;

import java.util.Map;
import java.util.UUID;

public record DomainEvent(
    String entityType,
    String eventType,
    UUID entityId,
    String description,
    Map<String, Object> details
) {}
