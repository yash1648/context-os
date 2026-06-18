package com.grim.contextos.container.dto.search;

import com.grim.contextos.container.model.ContainerStatus;
import com.grim.contextos.container.model.ContainerType;

import java.util.UUID;

public record ContainerSearchCriteria(
    String q,
    ContainerStatus status,
    ContainerType type,
    UUID tagId
) {}
