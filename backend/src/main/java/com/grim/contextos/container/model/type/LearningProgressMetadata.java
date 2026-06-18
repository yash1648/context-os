package com.grim.contextos.container.model.type;

import java.util.Set;

public record LearningProgressMetadata(
    String skill,
    String proficiencyLevel,
    Set<String> resources,
    String targetDate
) {}
