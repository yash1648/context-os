package com.grim.contextos.container.dto.response;

import com.grim.contextos.container.model.Container;
import com.grim.contextos.container.model.ContainerStatus;
import com.grim.contextos.container.model.ContainerType;
import com.grim.contextos.tag.dto.response.TagResponse;

import java.time.LocalDateTime;
import java.util.List;
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
    List<TagResponse> tags,
    String errorMessage,
    LocalDateTime startedAt,
    LocalDateTime stoppedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    boolean pinned,
    LocalDateTime pinnedAt,
    Integer progress
) {
    public static ContainerResponse from(Container c) {
        List<TagResponse> tags = c.getTags() != null
            ? c.getTags().stream().map(TagResponse::from).toList()
            : List.of();
        return new ContainerResponse(
            c.getId(), c.getName(), c.getDescription(), c.getType(),
            c.getMetadata(),
            c.getStatus(), c.getEnvVars(), c.getResourceLimits(), c.getLabels(), tags,
            c.getErrorMessage(), c.getStartedAt(), c.getStoppedAt(),
            c.getCreatedAt(), c.getUpdatedAt(),
            c.isPinned(), c.getPinnedAt(),
            c.getProgress()
        );
    }
}
