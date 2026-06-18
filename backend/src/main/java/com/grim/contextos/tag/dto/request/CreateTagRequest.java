package com.grim.contextos.tag.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTagRequest(
    @NotBlank @Size(min = 1, max = 100) String name,
    @Size(max = 7) String color
) {}
