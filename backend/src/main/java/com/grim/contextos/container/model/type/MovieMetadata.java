package com.grim.contextos.container.model.type;

public record MovieMetadata(
    String director,
    Integer releaseYear,
    Integer durationMinutes,
    String genre,
    String imdbRating,
    String watchStatus
) {}
