package com.grim.contextos.container.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ProgressUpdateRequest(
    @Min(0) @Max(100) int progress
) {}
