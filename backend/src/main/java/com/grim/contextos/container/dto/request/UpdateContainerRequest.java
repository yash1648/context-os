package com.grim.contextos.container.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateContainerRequest(
    @Size(min = 1, max = 255) String name,
    @Size(max = 5000) String description,
    String metadata,
    String envVars,
    String resourceLimits,
    String labels
) {}
