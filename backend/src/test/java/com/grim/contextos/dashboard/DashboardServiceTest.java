package com.grim.contextos.dashboard;

import com.grim.contextos.container.model.ContainerStatus;
import com.grim.contextos.container.repository.ContainerRepository;
import com.grim.contextos.dashboard.dto.DashboardSummaryResponse;
import com.grim.contextos.dashboard.service.DashboardService;
import com.grim.contextos.tag.repository.TagRepository;
import com.grim.contextos.timeline.model.TimelineEvent;
import com.grim.contextos.timeline.model.TimelineEventType;
import com.grim.contextos.timeline.repository.TimelineEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private ContainerRepository containerRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TimelineEventRepository timelineEventRepository;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(containerRepository, tagRepository, timelineEventRepository);
    }

    @Test
    void getSummaryReturnsAggregatedData() {
        when(containerRepository.count()).thenReturn(5L);
        for (ContainerStatus status : ContainerStatus.values()) {
            when(containerRepository.countByStatus(status)).thenReturn(1L);
        }

        when(tagRepository.count()).thenReturn(3L);

        TimelineEvent event = new TimelineEvent();
        event.setId(UUID.randomUUID());
        event.setContainerId(UUID.randomUUID());
        event.setEventType(TimelineEventType.CREATED);
        when(timelineEventRepository.findAllByOrderByCreatedAtDesc())
            .thenReturn(List.of(event));

        DashboardSummaryResponse summary = dashboardService.getSummary();

        assertEquals(5L, summary.totalContainers());
        assertEquals(ContainerStatus.values().length, summary.containersByStatus().size());
        assertEquals(1L, summary.containersByStatus().get(ContainerStatus.PENDING));
        assertEquals(3L, summary.totalTags());
        assertEquals(1, summary.recentActivity().size());
        assertEquals(TimelineEventType.CREATED, summary.recentActivity().getFirst().eventType());
    }

    @Test
    void getSummaryHandlesEmptyData() {
        when(containerRepository.count()).thenReturn(0L);
        for (ContainerStatus status : ContainerStatus.values()) {
            when(containerRepository.countByStatus(status)).thenReturn(0L);
        }
        when(tagRepository.count()).thenReturn(0L);
        when(timelineEventRepository.findAllByOrderByCreatedAtDesc())
            .thenReturn(List.of());

        DashboardSummaryResponse summary = dashboardService.getSummary();

        assertEquals(0L, summary.totalContainers());
        assertEquals(0L, summary.totalTags());
        assertTrue(summary.recentActivity().isEmpty());
        summary.containersByStatus().values().forEach(v -> assertEquals(0L, v));
    }
}
