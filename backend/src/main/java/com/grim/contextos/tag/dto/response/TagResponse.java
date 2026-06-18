package com.grim.contextos.tag.dto.response;

import com.grim.contextos.tag.model.Tag;
import java.util.UUID;

public record TagResponse(
    UUID id,
    String name,
    String color,
    UUID ownerId
) {
    public static TagResponse from(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName(), tag.getColor(), tag.getOwnerId());
    }
}
