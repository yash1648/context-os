package com.grim.contextos.container.dto.request;

import com.grim.contextos.container.model.ContainerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateContainerRequest(
    @NotBlank @Size(min = 1, max = 255) String name,
    @Size(max = 5000) String description,
    @NotNull ContainerType type,
    String metadata,
    String envVars,
    String resourceLimits,
    String labels
) {}
