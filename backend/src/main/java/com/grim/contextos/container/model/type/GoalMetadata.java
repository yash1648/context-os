package com.grim.contextos.container.model.type;

import java.util.Set;

public record GoalMetadata(
    String objective,
    Set<KeyResult> keyResults,
    String deadline,
    String category
) {
    public record KeyResult(String description, Boolean achieved) {}
}
