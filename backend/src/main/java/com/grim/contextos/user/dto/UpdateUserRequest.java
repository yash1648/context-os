package com.grim.contextos.user.dto;

import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
    @Size(min = 1, max = 100) String displayName,
    @Size(max = 500) String avatarUrl,
    String settings
) {}
