package com.grim.contextos.container.model.type;

public record TVSeriesMetadata(
    Integer seasons,
    Integer episodes,
    String network,
    String genre,
    String watchStatus
) {}
