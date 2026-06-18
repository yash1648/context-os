package com.grim.contextos.container.model.type;

public record CourseMetadata(
    String platform,
    String instructor,
    Integer totalModules,
    Integer completedModules,
    Integer totalDurationHours,
    String certificateUrl
) {}
