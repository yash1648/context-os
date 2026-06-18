package com.grim.contextos.container.model.type;

public record SoftwareProjectMetadata(
    String repositoryUrl,
    String techStack,
    String projectType,
    String license,
    String startDate,
    String targetDate
) {}
