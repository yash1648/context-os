package com.grim.contextos.tag.service;

import com.grim.contextos.common.exception.ResourceNotFoundException;
import com.grim.contextos.container.model.Container;
import com.grim.contextos.container.repository.ContainerRepository;
import com.grim.contextos.tag.dto.request.CreateTagRequest;
import com.grim.contextos.tag.dto.request.UpdateTagRequest;
import com.grim.contextos.tag.dto.response.TagResponse;
import com.grim.contextos.tag.model.Tag;
import com.grim.contextos.tag.repository.TagRepository;
import com.grim.contextos.timeline.model.TimelineEventType;
import com.grim.contextos.timeline.service.TimelineService;
import com.grim.contextos.websocket.event.DomainEvent;
import com.grim.contextos.websocket.event.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TagService {

    private final TagRepository tagRepository;
    private final ContainerRepository containerRepository;
    private final TimelineService timelineService;
    private final DomainEventPublisher eventPublisher;

    public TagService(TagRepository tagRepository, ContainerRepository containerRepository,
                      TimelineService timelineService, DomainEventPublisher eventPublisher) {
        this.tagRepository = tagRepository;
        this.containerRepository = containerRepository;
        this.timelineService = timelineService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public TagResponse createTag(CreateTagRequest request, UUID ownerId) {
        if (tagRepository.existsByNameAndOwnerId(request.name(), ownerId)) {
            throw new RuntimeException("Tag name already exists");
        }
        Tag tag = new Tag(request.name(), request.color(), ownerId);
        tag = tagRepository.save(tag);
        eventPublisher.publish(new DomainEvent("TAG", "CREATED", tag.getId(),
            "Tag '" + tag.getName() + "' created", null));
        return TagResponse.from(tag);
    }

    public List<TagResponse> listTags(UUID ownerId) {
        return tagRepository.findByOwnerId(ownerId).stream()
            .map(TagResponse::from)
            .collect(Collectors.toList());
    }

    public TagResponse getTag(UUID id) {
        Tag tag = tagRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tag", id));
        return TagResponse.from(tag);
    }

    @Transactional
    public TagResponse updateTag(UUID id, UpdateTagRequest request) {
        Tag tag = tagRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tag", id));
        if (request.name() != null) {
            tag.setName(request.name());
        }
        if (request.color() != null) {
            tag.setColor(request.color());
        }
        tag = tagRepository.save(tag);
        eventPublisher.publish(new DomainEvent("TAG", "UPDATED", id,
            "Tag '" + tag.getName() + "' updated", null));
        return TagResponse.from(tag);
    }

    @Transactional
    public void deleteTag(UUID id) {
        Tag tag = tagRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tag", id));
        String name = tag.getName();
        tagRepository.deleteById(id);
        eventPublisher.publish(new DomainEvent("TAG", "DELETED", id,
            "Tag '" + name + "' deleted", null));
    }

    @Transactional
    public void assignTagsToContainer(UUID containerId, Set<UUID> tagIds) {
        Container container = containerRepository.findById(containerId)
            .orElseThrow(() -> new ResourceNotFoundException("Container", containerId));
        Set<Tag> tags = tagIds.stream()
            .map(id -> tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id)))
            .collect(Collectors.toSet());
        container.getTags().addAll(tags);
        containerRepository.save(container);
        String tagNames = tags.stream().map(Tag::getName).collect(Collectors.joining(", "));
        timelineService.recordEvent(containerId, TimelineEventType.TAG_ASSIGNED,
            "Tags assigned: " + tagNames);
        eventPublisher.publish(new DomainEvent("CONTAINER", "TAG_ASSIGNED", containerId,
            "Tags assigned: " + tagNames, null));
    }

    @Transactional(readOnly = true)
    public List<TagResponse> searchTags(String query, UUID ownerId) {
        return tagRepository.findByNameContainingIgnoreCaseAndOwnerId(query, ownerId).stream()
            .map(TagResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<TagResponse> autocompleteTags(String query, UUID ownerId) {
        return tagRepository.findTop10ByNameContainingIgnoreCaseAndOwnerIdOrderByName(query, ownerId).stream()
            .map(TagResponse::from)
            .toList();
    }

    @Transactional
    public TagResponse mergeTags(UUID sourceTagId, UUID targetTagId, UUID ownerId) {
        Tag source = tagRepository.findById(sourceTagId)
            .orElseThrow(() -> new ResourceNotFoundException("Tag", sourceTagId));
        Tag target = tagRepository.findById(targetTagId)
            .orElseThrow(() -> new ResourceNotFoundException("Tag", targetTagId));

        if (!source.getOwnerId().equals(ownerId) || !target.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Tags must belong to the current user");
        }
        if (sourceTagId.equals(targetTagId)) {
            throw new IllegalArgumentException("Cannot merge a tag with itself");
        }

        List<Container> affectedContainers = containerRepository.findByTagsId(sourceTagId);
        String targetName = target.getName();
        String sourceName = source.getName();

        for (Container container : affectedContainers) {
            container.getTags().remove(source);
            container.getTags().add(target);
            containerRepository.save(container);
            timelineService.recordEvent(container.getId(), TimelineEventType.TAG_ASSIGNED,
                "Tag \"" + targetName + "\" assigned via merge from \"" + sourceName + "\"");
        }

        tagRepository.delete(source);
        eventPublisher.publish(new DomainEvent("TAG", "MERGED", targetTagId,
            "Tag \"" + sourceName + "\" merged into \"" + targetName + "\"", null));
        return TagResponse.from(target);
    }

    @Transactional
    public void removeTagFromContainer(UUID containerId, UUID tagId) {
        Container container = containerRepository.findById(containerId)
            .orElseThrow(() -> new ResourceNotFoundException("Container", containerId));
        Tag tag = tagRepository.findById(tagId)
            .orElseThrow(() -> new ResourceNotFoundException("Tag", tagId));
        String tagName = tag.getName();
        container.getTags().remove(tag);
        containerRepository.save(container);
        timelineService.recordEvent(containerId, TimelineEventType.TAG_REMOVED,
            "Tag removed: " + tagName);
        eventPublisher.publish(new DomainEvent("CONTAINER", "TAG_REMOVED", containerId,
            "Tag removed: " + tagName, null));
    }
}
