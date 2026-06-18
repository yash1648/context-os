package com.grim.contextos.container.validation;

import tools.jackson.databind.ObjectMapper;
import com.grim.contextos.container.model.ContainerType;
import com.grim.contextos.container.model.type.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ContainerValidationService {

    private static final Pattern ISBN_13_PATTERN = Pattern.compile("^(?:978|979)[0-9]{10}$");
    private static final Pattern URL_PATTERN = Pattern.compile("^https?://.+\\..+");

    private final ObjectMapper objectMapper;

    public ContainerValidationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<String> validate(String metadata, ContainerType type) {
        List<String> errors = new ArrayList<>();
        if (metadata == null || metadata.isBlank()) {
            return errors;
        }
        try {
            return switch (type) {
                case BOOK -> validateBook(metadata);
                case MOVIE -> validateMovie(metadata);
                case TV_SERIES -> validateTVSeries(metadata);
                case COURSE -> validateCourse(metadata);
                case LEARNING_PROGRESS -> validateLearningProgress(metadata);
                case SOFTWARE_PROJECT -> validateSoftwareProject(metadata);
                case GOAL -> validateGoal(metadata);
                case HABIT -> validateHabit(metadata);
                case NOTE -> validateNote(metadata);
                case PINNED_CONTENT -> validatePinnedContent(metadata);
                default -> errors;
            };
        } catch (Exception e) {
            errors.add("Invalid JSON format for " + type + " metadata: " + e.getMessage());
            return errors;
        }
    }

    private List<String> validateBook(String json) {
        List<String> errors = new ArrayList<>();
        BookMetadata meta = objectMapper.readValue(json, BookMetadata.class);
        if (meta.isbn() != null && !ISBN_13_PATTERN.matcher(meta.isbn()).matches()) {
            errors.add("ISBN must be a valid 13-digit ISBN-13 (978/979 prefix)");
        }
        if (meta.pageCount() != null && meta.pageCount() <= 0) {
            errors.add("pageCount must be positive");
        }
        if (meta.currentPage() != null && meta.currentPage() < 0) {
            errors.add("currentPage must be non-negative");
        }
        if (meta.pageCount() != null && meta.currentPage() != null && meta.currentPage() > meta.pageCount()) {
            errors.add("currentPage cannot exceed pageCount");
        }
        return errors;
    }

    private List<String> validateMovie(String json) {
        List<String> errors = new ArrayList<>();
        MovieMetadata meta = objectMapper.readValue(json, MovieMetadata.class);
        if (meta.releaseYear() != null && (meta.releaseYear() < 1888 || meta.releaseYear() > 2030)) {
            errors.add("releaseYear must be between 1888 and 2030");
        }
        if (meta.durationMinutes() != null && meta.durationMinutes() <= 0) {
            errors.add("durationMinutes must be positive");
        }
        return errors;
    }

    private List<String> validateTVSeries(String json) {
        List<String> errors = new ArrayList<>();
        TVSeriesMetadata meta = objectMapper.readValue(json, TVSeriesMetadata.class);
        if (meta.seasons() != null && meta.seasons() <= 0) {
            errors.add("seasons must be positive");
        }
        if (meta.episodes() != null && meta.episodes() <= 0) {
            errors.add("episodes must be positive");
        }
        return errors;
    }

    private List<String> validateCourse(String json) {
        List<String> errors = new ArrayList<>();
        CourseMetadata meta = objectMapper.readValue(json, CourseMetadata.class);
        if (meta.totalModules() != null && meta.totalModules() <= 0) {
            errors.add("totalModules must be positive");
        }
        if (meta.completedModules() != null && meta.completedModules() < 0) {
            errors.add("completedModules must be non-negative");
        }
        if (meta.totalModules() != null && meta.completedModules() != null
            && meta.completedModules() > meta.totalModules()) {
            errors.add("completedModules cannot exceed totalModules");
        }
        if (meta.totalDurationHours() != null && meta.totalDurationHours() <= 0) {
            errors.add("totalDurationHours must be positive");
        }
        return errors;
    }

    private List<String> validateLearningProgress(String json) {
        List<String> errors = new ArrayList<>();
        LearningProgressMetadata meta = objectMapper.readValue(json, LearningProgressMetadata.class);
        if (meta.skill() != null && meta.skill().isBlank()) {
            errors.add("skill must not be blank");
        }
        return errors;
    }

    private List<String> validateSoftwareProject(String json) {
        List<String> errors = new ArrayList<>();
        SoftwareProjectMetadata meta = objectMapper.readValue(json, SoftwareProjectMetadata.class);
        if (meta.repositoryUrl() != null && !meta.repositoryUrl().isBlank()
            && !URL_PATTERN.matcher(meta.repositoryUrl()).matches()) {
            errors.add("repositoryUrl must be a valid HTTP(S) URL");
        }
        return errors;
    }

    private List<String> validateGoal(String json) {
        List<String> errors = new ArrayList<>();
        GoalMetadata meta = objectMapper.readValue(json, GoalMetadata.class);
        if (meta.keyResults() != null && meta.keyResults().isEmpty()) {
            errors.add("keyResults must not be empty");
        }
        if (meta.keyResults() != null) {
            for (var kr : meta.keyResults()) {
                if (kr.description() != null && kr.description().isBlank()) {
                    errors.add("keyResult description must not be blank");
                }
            }
        }
        return errors;
    }

    private List<String> validateHabit(String json) {
        List<String> errors = new ArrayList<>();
        HabitMetadata meta = objectMapper.readValue(json, HabitMetadata.class);
        if (meta.targetCount() != null && meta.targetCount() <= 0) {
            errors.add("targetCount must be positive");
        }
        if (meta.currentStreak() != null && meta.currentStreak() < 0) {
            errors.add("currentStreak must be non-negative");
        }
        if (meta.longestStreak() != null && meta.longestStreak() < 0) {
            errors.add("longestStreak must be non-negative");
        }
        return errors;
    }

    private List<String> validateNote(String json) {
        List<String> errors = new ArrayList<>();
        NoteMetadata meta = objectMapper.readValue(json, NoteMetadata.class);
        if (meta.content() != null && meta.content().isBlank()) {
            errors.add("content must not be blank");
        }
        return errors;
    }

    private List<String> validatePinnedContent(String json) {
        List<String> errors = new ArrayList<>();
        PinnedContentMetadata meta = objectMapper.readValue(json, PinnedContentMetadata.class);
        if (meta.url() != null && !meta.url().isBlank() && !URL_PATTERN.matcher(meta.url()).matches()) {
            errors.add("url must be a valid HTTP(S) URL");
        }
        return errors;
    }
}
