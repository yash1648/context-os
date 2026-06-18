package com.grim.contextos.snapshot.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateSnapshotRequest(

    @NotBlank(message = "Snapshot name is required")
    String name,

    String description
) {}
