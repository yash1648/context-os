package com.grim.contextos.container.validation;

import tools.jackson.databind.ObjectMapper;
import com.grim.contextos.container.model.ContainerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContainerValidationServiceTest {

    private ContainerValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ContainerValidationService(new ObjectMapper());
    }

    @Test
    void nullMetadataReturnsNoErrors() {
        List<String> errors = validationService.validate(null, ContainerType.BOOK);
        assertTrue(errors.isEmpty());
    }

    @Test
    void blankMetadataReturnsNoErrors() {
        List<String> errors = validationService.validate("  ", ContainerType.BOOK);
        assertTrue(errors.isEmpty());
    }

    @Test
    void validBookMetadataReturnsNoErrors() {
        String json = """
            {"isbn":"9783161484100","author":"Test Author","pageCount":300,"currentPage":50}
            """;
        List<String> errors = validationService.validate(json, ContainerType.BOOK);
        assertTrue(errors.isEmpty());
    }

    @Test
    void invalidBookIsbnReturnsError() {
        String json = """
            {"isbn":"12345","author":"Test"}
            """;
        List<String> errors = validationService.validate(json, ContainerType.BOOK);
        assertFalse(errors.isEmpty());
        assertTrue(errors.getFirst().contains("ISBN"));
    }

    @Test
    void bookPageCountExceededReturnsError() {
        String json = """
            {"isbn":"9783161484100","pageCount":100,"currentPage":150}
            """;
        List<String> errors = validationService.validate(json, ContainerType.BOOK);
        assertFalse(errors.isEmpty());
        assertTrue(errors.getFirst().contains("currentPage"));
    }

    @Test
    void validMovieMetadataReturnsNoErrors() {
        String json = """
            {"director":"Spielberg","releaseYear":1993,"durationMinutes":120}
            """;
        List<String> errors = validationService.validate(json, ContainerType.MOVIE);
        assertTrue(errors.isEmpty());
    }

    @Test
    void invalidMovieYearReturnsError() {
        String json = """
            {"releaseYear":1800}
            """;
        List<String> errors = validationService.validate(json, ContainerType.MOVIE);
        assertFalse(errors.isEmpty());
        assertTrue(errors.getFirst().contains("releaseYear"));
    }

    @Test
    void validTVSeriesMetadataReturnsNoErrors() {
        String json = """
            {"seasons":5,"episodes":50}
            """;
        List<String> errors = validationService.validate(json, ContainerType.TV_SERIES);
        assertTrue(errors.isEmpty());
    }

    @Test
    void invalidSeasonCountReturnsError() {
        String json = """
            {"seasons":-1}
            """;
        List<String> errors = validationService.validate(json, ContainerType.TV_SERIES);
        assertFalse(errors.isEmpty());
    }

    @Test
    void validCourseMetadataReturnsNoErrors() {
        String json = """
            {"totalModules":10,"completedModules":5,"totalDurationHours":40}
            """;
        List<String> errors = validationService.validate(json, ContainerType.COURSE);
        assertTrue(errors.isEmpty());
    }

    @Test
    void courseCompletedExceedsTotalReturnsError() {
        String json = """
            {"totalModules":5,"completedModules":10}
            """;
        List<String> errors = validationService.validate(json, ContainerType.COURSE);
        assertFalse(errors.isEmpty());
        assertTrue(errors.getFirst().contains("completedModules"));
    }

    @Test
    void validSoftwareProjectReturnsNoErrors() {
        String json = """
            {"repositoryUrl":"https://github.com/user/repo"}
            """;
        List<String> errors = validationService.validate(json, ContainerType.SOFTWARE_PROJECT);
        assertTrue(errors.isEmpty());
    }

    @Test
    void invalidUrlReturnsError() {
        String json = """
            {"repositoryUrl":"not-a-url"}
            """;
        List<String> errors = validationService.validate(json, ContainerType.SOFTWARE_PROJECT);
        assertFalse(errors.isEmpty());
        assertTrue(errors.getFirst().contains("repositoryUrl"));
    }

    @Test
    void validHabitMetadataReturnsNoErrors() {
        String json = """
            {"targetCount":5,"currentStreak":3,"longestStreak":10}
            """;
        List<String> errors = validationService.validate(json, ContainerType.HABIT);
        assertTrue(errors.isEmpty());
    }

    @Test
    void invalidTargetCountReturnsError() {
        String json = """
            {"targetCount":0}
            """;
        List<String> errors = validationService.validate(json, ContainerType.HABIT);
        assertFalse(errors.isEmpty());
    }

    @Test
    void invalidJsonReturnsError() {
        List<String> errors = validationService.validate("{invalid}", ContainerType.BOOK);
        assertFalse(errors.isEmpty());
        assertTrue(errors.getFirst().contains("Invalid JSON"));
    }

    @Test
    void pinnedContentInvalidUrlReturnsError() {
        String json = """
            {"url":"ftp://bad"}
            """;
        List<String> errors = validationService.validate(json, ContainerType.PINNED_CONTENT);
        assertFalse(errors.isEmpty());
    }

    @Test
    void goalEmptyKeyResultsReturnsError() {
        String json = """
            {"keyResults":[]}
            """;
        List<String> errors = validationService.validate(json, ContainerType.GOAL);
        assertFalse(errors.isEmpty());
    }

    @Test
    void learningProgressValidReturnsNoErrors() {
        String json = """
            {"skill":"Java","proficiencyLevel":"intermediate"}
            """;
        List<String> errors = validationService.validate(json, ContainerType.LEARNING_PROGRESS);
        assertTrue(errors.isEmpty());
    }

    @Test
    void snapshotTypeReturnsNoErrors() {
        String json = """
            {"sourceContainerId":"abc","label":"test"}
            """;
        List<String> errors = validationService.validate(json, ContainerType.SNAPSHOT);
        assertTrue(errors.isEmpty());
    }

    @Test
    void knowledgeAssetValidReturnsNoErrors() {
        String json = """
            {"assetType":"snippet","content":"code","language":"java"}
            """;
        List<String> errors = validationService.validate(json, ContainerType.KNOWLEDGE_ASSET);
        assertTrue(errors.isEmpty());
    }

    @Test
    void noteValidReturnsNoErrors() {
        String json = """
            {"content":"My note","source":"manual"}
            """;
        List<String> errors = validationService.validate(json, ContainerType.NOTE);
        assertTrue(errors.isEmpty());
    }
}
