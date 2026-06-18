package com.grim.contextos.container.model.type;

public record SnapshotMetadata(
    String sourceContainerId,
    String snapshotData,
    String label
) {}
