package com.grim.contextos.container.model.type;

public record BookMetadata(
    String isbn,
    String author,
    Integer pageCount,
    Integer currentPage,
    String genre,
    String readingStatus
) {}
