package com.grim.contextos.snapshot.dto.response;

import com.grim.contextos.container.model.ContainerStatus;
import com.grim.contextos.container.model.ContainerType;
import com.grim.contextos.snapshot.model.Snapshot;

import java.time.LocalDateTime;
import java.util.UUID;

public record SnapshotResponse(
    UUID id,
    UUID containerId,
    String name,
    String description,
    String capturedName,
    String capturedDescription,
    ContainerType capturedType,
    ContainerStatus capturedStatus,
    String capturedMetadata,
    String capturedEnvVars,
    String capturedResourceLimits,
    String capturedLabels,
    LocalDateTime createdAt
) {
    public static SnapshotResponse from(Snapshot snapshot) {
        return new SnapshotResponse(
            snapshot.getId(),
            snapshot.getContainerId(),
            snapshot.getName(),
            snapshot.getDescription(),
            snapshot.getCapturedName(),
            snapshot.getCapturedDescription(),
            snapshot.getCapturedType(),
            snapshot.getCapturedStatus(),
            snapshot.getCapturedMetadata(),
            snapshot.getCapturedEnvVars(),
            snapshot.getCapturedResourceLimits(),
            snapshot.getCapturedLabels(),
            snapshot.getCreatedAt()
        );
    }
}
