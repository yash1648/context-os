package com.grim.contextos.timeline;

import com.grim.contextos.common.exception.ResourceNotFoundException;
import com.grim.contextos.container.model.ContainerStatus;
import com.grim.contextos.timeline.dto.response.TimelineEventResponse;
import com.grim.contextos.timeline.model.TimelineEvent;
import com.grim.contextos.timeline.model.TimelineEventType;
import com.grim.contextos.timeline.repository.TimelineEventRepository;
import com.grim.contextos.timeline.service.TimelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimelineServiceTest {

    @Mock
    private TimelineEventRepository timelineEventRepository;

    private TimelineService timelineService;
    private final UUID eventId = UUID.randomUUID();
    private final UUID containerId = UUID.randomUUID();
    private TimelineEvent testEvent;

    @BeforeEach
    void setUp() {
        timelineService = new TimelineService(timelineEventRepository);

        testEvent = new TimelineEvent(containerId, TimelineEventType.CREATED, "Container created");
        testEvent.setId(eventId);
    }

    @Test
    void recordEventCreatesAndReturnsEvent() {
        when(timelineEventRepository.save(any(TimelineEvent.class))).thenReturn(testEvent);

        TimelineEventResponse response = timelineService.recordEvent(containerId, TimelineEventType.CREATED, "Container created");

        assertEquals(containerId, response.containerId());
        assertEquals(TimelineEventType.CREATED, response.eventType());
        assertEquals("Container created", response.description());
        verify(timelineEventRepository).save(any(TimelineEvent.class));
    }

    @Test
    void recordStatusChangeRecordsStatusTransition() {
        testEvent.setEventType(TimelineEventType.STATUS_CHANGED);
        testEvent.setPreviousStatus(ContainerStatus.PENDING);
        testEvent.setNewStatus(ContainerStatus.RUNNING);
        testEvent.setDescription("Status changed from PENDING to RUNNING");

        when(timelineEventRepository.save(any(TimelineEvent.class))).thenReturn(testEvent);

        TimelineEventResponse response = timelineService.recordStatusChange(containerId, ContainerStatus.PENDING, ContainerStatus.RUNNING);

        assertEquals(TimelineEventType.STATUS_CHANGED, response.eventType());
        assertEquals(ContainerStatus.PENDING, response.previousStatus());
        assertEquals(ContainerStatus.RUNNING, response.newStatus());
    }

    @Test
    void listEventsWithoutFiltersReturnsAll() {
        when(timelineEventRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(testEvent));

        List<TimelineEventResponse> events = timelineService.listEvents(null, null);

        assertEquals(1, events.size());
    }

    @Test
    void listEventsByContainerId() {
        when(timelineEventRepository.findByContainerIdOrderByCreatedAtDesc(containerId))
            .thenReturn(List.of(testEvent));

        List<TimelineEventResponse> events = timelineService.listEvents(containerId, null);

        assertEquals(1, events.size());
        assertEquals(containerId, events.getFirst().containerId());
    }

    @Test
    void listEventsByEventType() {
        when(timelineEventRepository.findByEventTypeOrderByCreatedAtDesc(TimelineEventType.CREATED))
            .thenReturn(List.of(testEvent));

        List<TimelineEventResponse> events = timelineService.listEvents(null, TimelineEventType.CREATED);

        assertEquals(1, events.size());
        assertEquals(TimelineEventType.CREATED, events.getFirst().eventType());
    }

    @Test
    void listEventsByContainerAndType() {
        when(timelineEventRepository.findByContainerIdAndEventTypeOrderByCreatedAtDesc(containerId, TimelineEventType.CREATED))
            .thenReturn(List.of(testEvent));

        List<TimelineEventResponse> events = timelineService.listEvents(containerId, TimelineEventType.CREATED);

        assertEquals(1, events.size());
    }

    @Test
    void listEventsReturnsEmptyWhenNone() {
        when(timelineEventRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        List<TimelineEventResponse> events = timelineService.listEvents(null, null);

        assertTrue(events.isEmpty());
    }

    @Test
    void getEventReturnsResponseWhenFound() {
        when(timelineEventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));

        TimelineEventResponse response = timelineService.getEvent(eventId);

        assertEquals(eventId, response.id());
        assertEquals(containerId, response.containerId());
        assertEquals(TimelineEventType.CREATED, response.eventType());
    }

    @Test
    void getEventThrowsWhenNotFound() {
        when(timelineEventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> timelineService.getEvent(eventId));
    }
}
