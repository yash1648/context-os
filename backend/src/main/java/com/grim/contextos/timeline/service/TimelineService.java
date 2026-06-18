package com.grim.contextos.timeline.service;

import com.grim.contextos.common.exception.ResourceNotFoundException;
import com.grim.contextos.container.model.ContainerStatus;
import com.grim.contextos.timeline.dto.response.TimelineEventResponse;
import com.grim.contextos.timeline.model.TimelineEvent;
import com.grim.contextos.timeline.model.TimelineEventType;
import com.grim.contextos.timeline.repository.TimelineEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TimelineService {

    private final TimelineEventRepository timelineEventRepository;

    public TimelineService(TimelineEventRepository timelineEventRepository) {
        this.timelineEventRepository = timelineEventRepository;
    }

    @Transactional
    public TimelineEventResponse recordEvent(UUID containerId, TimelineEventType eventType, String description) {
        return recordEvent(containerId, eventType, null, null, description, null);
    }

    @Transactional
    public TimelineEventResponse recordStatusChange(UUID containerId, ContainerStatus previous, ContainerStatus next) {
        String description = "Status changed from " + previous + " to " + next;
        return recordEvent(containerId, TimelineEventType.STATUS_CHANGED, previous, next, description, null);
    }

    @Transactional
    public TimelineEventResponse recordEvent(UUID containerId, TimelineEventType eventType,
                                              ContainerStatus previousStatus, ContainerStatus newStatus,
                                              String description, String metadata) {
        TimelineEvent event = new TimelineEvent(containerId, eventType, description);
        event.setPreviousStatus(previousStatus);
        event.setNewStatus(newStatus);
        event.setMetadata(metadata);
        event = timelineEventRepository.save(event);
        return TimelineEventResponse.from(event);
    }

    @Transactional(readOnly = true)
    public List<TimelineEventResponse> listEvents(UUID containerId, TimelineEventType eventType) {
        List<TimelineEvent> events;
        if (containerId != null && eventType != null) {
            events = timelineEventRepository.findByContainerIdAndEventTypeOrderByCreatedAtDesc(containerId, eventType);
        } else if (containerId != null) {
            events = timelineEventRepository.findByContainerIdOrderByCreatedAtDesc(containerId);
        } else if (eventType != null) {
            events = timelineEventRepository.findByEventTypeOrderByCreatedAtDesc(eventType);
        } else {
            events = timelineEventRepository.findAllByOrderByCreatedAtDesc();
        }
        return events.stream()
            .map(TimelineEventResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public TimelineEventResponse getEvent(UUID id) {
        TimelineEvent event = timelineEventRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("TimelineEvent", id));
        return TimelineEventResponse.from(event);
    }
}
