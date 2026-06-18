package com.grim.contextos.container.model.type;

import java.util.Set;

public record NoteMetadata(
    String content,
    Set<String> attachments,
    String source
) {}
