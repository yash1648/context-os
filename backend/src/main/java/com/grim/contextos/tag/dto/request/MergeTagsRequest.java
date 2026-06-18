package com.grim.contextos.tag.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record MergeTagsRequest(
    @NotNull UUID sourceTagId,
    @NotNull UUID targetTagId
) {}
