package com.grim.contextos.tag;

import com.grim.contextos.common.exception.ResourceNotFoundException;
import com.grim.contextos.container.model.Container;
import com.grim.contextos.container.model.ContainerType;
import com.grim.contextos.container.repository.ContainerRepository;
import com.grim.contextos.tag.dto.request.CreateTagRequest;
import com.grim.contextos.tag.dto.request.UpdateTagRequest;
import com.grim.contextos.tag.dto.response.TagResponse;
import com.grim.contextos.tag.model.Tag;
import com.grim.contextos.tag.repository.TagRepository;
import com.grim.contextos.tag.service.TagService;
import com.grim.contextos.timeline.model.TimelineEventType;
import com.grim.contextos.timeline.service.TimelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ContainerRepository containerRepository;

    @Mock
    private TimelineService timelineService;

    private TagService tagService;
    private final UUID ownerId = UUID.randomUUID();
    private final UUID tagId = UUID.randomUUID();
    private Tag testTag;

    @BeforeEach
    void setUp() {
        tagService = new TagService(tagRepository, containerRepository, timelineService);
        testTag = new Tag("fiction", "#ff0000", ownerId);
        testTag.setId(tagId);
    }

    @Test
    void createTagReturnsResponse() {
        var request = new CreateTagRequest("fiction", "#ff0000");
        when(tagRepository.existsByNameAndOwnerId("fiction", ownerId)).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenReturn(testTag);

        TagResponse response = tagService.createTag(request, ownerId);

        assertEquals(tagId, response.id());
        assertEquals("fiction", response.name());
        assertEquals("#ff0000", response.color());
        assertEquals(ownerId, response.ownerId());
    }

    @Test
    void createTagThrowsWhenDuplicateName() {
        var request = new CreateTagRequest("fiction", "#ff0000");
        when(tagRepository.existsByNameAndOwnerId("fiction", ownerId)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> tagService.createTag(request, ownerId));
        verify(tagRepository, never()).save(any());
    }

    @Test
    void listTagsReturnsOwnerTags() {
        when(tagRepository.findByOwnerId(ownerId)).thenReturn(List.of(testTag));

        List<TagResponse> responses = tagService.listTags(ownerId);

        assertEquals(1, responses.size());
        assertEquals("fiction", responses.getFirst().name());
    }

    @Test
    void getTagReturnsResponseWhenFound() {
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(testTag));

        TagResponse response = tagService.getTag(tagId);

        assertEquals(tagId, response.id());
        assertEquals("fiction", response.name());
    }

    @Test
    void getTagThrowsWhenNotFound() {
        when(tagRepository.findById(tagId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tagService.getTag(tagId));
    }

    @Test
    void updateTagUpdatesFields() {
        var request = new UpdateTagRequest("non-fiction", "#0000ff");
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(testTag));
        when(tagRepository.save(any(Tag.class))).thenReturn(testTag);

        TagResponse response = tagService.updateTag(tagId, request);

        assertEquals("non-fiction", response.name());
        assertEquals("#0000ff", response.color());
    }

    @Test
    void updateTagPartialUpdate() {
        var request = new UpdateTagRequest(null, "#0000ff");
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(testTag));
        when(tagRepository.save(any(Tag.class))).thenReturn(testTag);

        TagResponse response = tagService.updateTag(tagId, request);

        assertEquals("fiction", response.name()); // unchanged
        assertEquals("#0000ff", response.color());
    }

    @Test
    void deleteTagDeletesWhenExists() {
        when(tagRepository.existsById(tagId)).thenReturn(true);

        tagService.deleteTag(tagId);

        verify(tagRepository).deleteById(tagId);
    }

    @Test
    void deleteTagThrowsWhenNotFound() {
        when(tagRepository.existsById(tagId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> tagService.deleteTag(tagId));
        verify(tagRepository, never()).deleteById(any());
    }

    @Test
    void assignTagsToContainerAddsTags() {
        UUID containerId = UUID.randomUUID();
        Container container = new Container("test", "desc", ContainerType.BOOK);
        container.setId(containerId);

        when(containerRepository.findById(containerId)).thenReturn(Optional.of(container));
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(testTag));
        when(containerRepository.save(any(Container.class))).thenReturn(container);

        tagService.assignTagsToContainer(containerId, Set.of(tagId));

        assertTrue(container.getTags().contains(testTag));
        verify(containerRepository).save(container);
    }

    @Test
    void assignTagsToContainerThrowsWhenContainerNotFound() {
        UUID containerId = UUID.randomUUID();
        when(containerRepository.findById(containerId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> tagService.assignTagsToContainer(containerId, Set.of(tagId)));
    }

    @Test
    void removeTagFromContainerRemovesTag() {
        UUID containerId = UUID.randomUUID();
        Container container = new Container("test", "desc", ContainerType.BOOK);
        container.setId(containerId);
        container.getTags().add(testTag);

        when(containerRepository.findById(containerId)).thenReturn(Optional.of(container));
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(testTag));
        when(containerRepository.save(any(Container.class))).thenReturn(container);

        tagService.removeTagFromContainer(containerId, tagId);

        assertFalse(container.getTags().contains(testTag));
        verify(containerRepository).save(container);
    }

    @Test
    void removeTagFromContainerThrowsWhenContainerNotFound() {
        UUID containerId = UUID.randomUUID();
        when(containerRepository.findById(containerId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> tagService.removeTagFromContainer(containerId, tagId));
    }

    @Test
    void searchTagsReturnsMatchingTags() {
        when(tagRepository.findByNameContainingIgnoreCaseAndOwnerId("fict", ownerId))
            .thenReturn(List.of(testTag));

        var results = tagService.searchTags("fict", ownerId);

        assertEquals(1, results.size());
        assertEquals("fiction", results.getFirst().name());
    }

    @Test
    void searchTagsReturnsEmptyWhenNoMatch() {
        when(tagRepository.findByNameContainingIgnoreCaseAndOwnerId("nonexistent", ownerId))
            .thenReturn(List.of());

        var results = tagService.searchTags("nonexistent", ownerId);

        assertTrue(results.isEmpty());
    }

    @Test
    void autocompleteTagsReturnsLimitedResults() {
        when(tagRepository.findTop10ByNameContainingIgnoreCaseAndOwnerIdOrderByName("fict", ownerId))
            .thenReturn(List.of(testTag));

        var results = tagService.autocompleteTags("fict", ownerId);

        assertEquals(1, results.size());
        assertEquals("fiction", results.getFirst().name());
    }

    @Test
    void mergeTagsMergesSourceIntoTarget() {
        UUID targetId = UUID.randomUUID();
        Tag targetTag = new Tag("books", "#00ff00", ownerId);
        targetTag.setId(targetId);

        UUID containerId = UUID.randomUUID();
        Container container = new Container("test", "desc", ContainerType.BOOK);
        container.setId(containerId);
        container.getTags().add(testTag);

        when(tagRepository.findById(tagId)).thenReturn(Optional.of(testTag));
        when(tagRepository.findById(targetId)).thenReturn(Optional.of(targetTag));
        when(containerRepository.findByTagsId(tagId)).thenReturn(List.of(container));
        when(containerRepository.save(any(Container.class))).thenReturn(container);

        TagResponse result = tagService.mergeTags(tagId, targetId, ownerId);

        assertFalse(container.getTags().contains(testTag));
        assertTrue(container.getTags().contains(targetTag));
        assertEquals("books", result.name());
        verify(tagRepository).delete(testTag);
        verify(timelineService).recordEvent(eq(containerId), eq(TimelineEventType.TAG_ASSIGNED), anyString());
    }

    @Test
    void mergeTagsThrowsWhenSourceNotFound() {
        UUID targetId = UUID.randomUUID();
        when(tagRepository.findById(tagId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> tagService.mergeTags(tagId, targetId, ownerId));
    }

    @Test
    void mergeTagsThrowsWhenTargetNotFound() {
        UUID targetId = UUID.randomUUID();
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(testTag));
        when(tagRepository.findById(targetId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> tagService.mergeTags(tagId, targetId, ownerId));
    }

    @Test
    void mergeTagsThrowsWhenWrongOwner() {
        UUID targetId = UUID.randomUUID();
        Tag targetTag = new Tag("books", "#00ff00", UUID.randomUUID()); // different owner
        targetTag.setId(targetId);
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(testTag));
        when(tagRepository.findById(targetId)).thenReturn(Optional.of(targetTag));

        assertThrows(IllegalArgumentException.class,
            () -> tagService.mergeTags(tagId, targetId, ownerId));
    }

    @Test
    void mergeTagsThrowsWhenSameTag() {
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(testTag));

        assertThrows(IllegalArgumentException.class,
            () -> tagService.mergeTags(tagId, tagId, ownerId));
    }

    @Test
    void mergeTagsRecordsTimelineForEachContainer() {
        UUID targetId = UUID.randomUUID();
        Tag targetTag = new Tag("books", "#00ff00", ownerId);
        targetTag.setId(targetId);

        Container c1 = new Container("c1", "d", ContainerType.BOOK);
        c1.setId(UUID.randomUUID());
        Container c2 = new Container("c2", "d", ContainerType.BOOK);
        c2.setId(UUID.randomUUID());

        when(tagRepository.findById(tagId)).thenReturn(Optional.of(testTag));
        when(tagRepository.findById(targetId)).thenReturn(Optional.of(targetTag));
        when(containerRepository.findByTagsId(tagId)).thenReturn(List.of(c1, c2));
        when(containerRepository.save(any(Container.class))).thenReturn(c1, c2);

        tagService.mergeTags(tagId, targetId, ownerId);

        verify(timelineService, times(2)).recordEvent(any(), eq(TimelineEventType.TAG_ASSIGNED), anyString());
        verify(tagRepository).delete(testTag);
    }
}
