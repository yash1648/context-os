package com.grim.contextos.tag;

import com.grim.contextos.tag.model.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TagModelTest {

    @Test
    void tagConstructorSetsFields() {
        UUID ownerId = UUID.randomUUID();
        Tag tag = new Tag("fiction", "#ff0000", ownerId);
        assertEquals("fiction", tag.getName());
        assertEquals("#ff0000", tag.getColor());
        assertEquals(ownerId, tag.getOwnerId());
    }

    @Test
    void tagSettersAndGetters() {
        UUID ownerId = UUID.randomUUID();
        Tag tag = new Tag();
        tag.setId(UUID.randomUUID());
        tag.setName("science");
        tag.setColor("#00ff00");
        tag.setOwnerId(ownerId);

        assertEquals("science", tag.getName());
        assertEquals("#00ff00", tag.getColor());
        assertEquals(ownerId, tag.getOwnerId());
    }

    @Test
    void tagAllowsNullColor() {
        UUID ownerId = UUID.randomUUID();
        Tag tag = new Tag("untagged", null, ownerId);
        assertNull(tag.getColor());
    }
}
