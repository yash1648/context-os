package com.grim.contextos.timeline.dto.response;

import com.grim.contextos.container.model.ContainerStatus;
import com.grim.contextos.timeline.model.TimelineEvent;
import com.grim.contextos.timeline.model.TimelineEventType;

import java.time.LocalDateTime;
import java.util.UUID;

public record TimelineEventResponse(
    UUID id,
    UUID containerId,
    TimelineEventType eventType,
    ContainerStatus previousStatus,
    ContainerStatus newStatus,
    String description,
    String metadata,
    LocalDateTime createdAt
) {
    public static TimelineEventResponse from(TimelineEvent event) {
        return new TimelineEventResponse(
            event.getId(),
            event.getContainerId(),
            event.getEventType(),
            event.getPreviousStatus(),
            event.getNewStatus(),
            event.getDescription(),
            event.getMetadata(),
            event.getCreatedAt()
        );
    }
}
