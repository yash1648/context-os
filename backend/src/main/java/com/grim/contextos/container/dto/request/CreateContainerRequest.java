package com.grim.contextos.container.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateContainerRequest(
    @NotBlank @Size(min = 1, max = 255) String name,
    @Size(max = 5000) String description,
    @NotBlank @Size(max = 100) String type,
    String envVars,
    String resourceLimits,
    String labels
) {}
