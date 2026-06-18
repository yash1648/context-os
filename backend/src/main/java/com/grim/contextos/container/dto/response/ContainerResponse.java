package com.grim.contextos.container.dto.response;

import com.grim.contextos.container.model.Container;
import com.grim.contextos.container.model.ContainerStatus;
import com.grim.contextos.container.model.ContainerType;

import java.time.LocalDateTime;
import java.util.UUID;

public record ContainerResponse(
    UUID id,
    String name,
    String description,
    ContainerType type,
    String metadata,
    ContainerStatus status,
    String envVars,
    String resourceLimits,
    String labels,
    String errorMessage,
    LocalDateTime startedAt,
    LocalDateTime stoppedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    boolean pinned,
    LocalDateTime pinnedAt
) {
    public static ContainerResponse from(Container c) {
        return new ContainerResponse(
            c.getId(), c.getName(), c.getDescription(), c.getType(),
            c.getMetadata(),
            c.getStatus(), c.getEnvVars(), c.getResourceLimits(), c.getLabels(),
            c.getErrorMessage(), c.getStartedAt(), c.getStoppedAt(),
            c.getCreatedAt(), c.getUpdatedAt(),
            c.isPinned(), c.getPinnedAt()
        );
    }
}
