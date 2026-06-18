package com.grim.contextos.container.model.type;

public record HabitMetadata(
    String habitName,
    String frequency,
    Integer targetCount,
    Integer currentStreak,
    Integer longestStreak,
    String reminderTime
) {}
