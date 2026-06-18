package com.grim.contextos.dashboard.service;

import com.grim.contextos.container.model.ContainerStatus;
import com.grim.contextos.container.repository.ContainerRepository;
import com.grim.contextos.dashboard.dto.DashboardSummaryResponse;
import com.grim.contextos.tag.repository.TagRepository;
import com.grim.contextos.timeline.dto.response.TimelineEventResponse;
import com.grim.contextos.timeline.repository.TimelineEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final ContainerRepository containerRepository;
    private final TagRepository tagRepository;
    private final TimelineEventRepository timelineEventRepository;

    public DashboardService(ContainerRepository containerRepository,
                            TagRepository tagRepository,
                            TimelineEventRepository timelineEventRepository) {
        this.containerRepository = containerRepository;
        this.tagRepository = tagRepository;
        this.timelineEventRepository = timelineEventRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        long totalContainers = containerRepository.count();

        Map<ContainerStatus, Long> containersByStatus = Arrays.stream(ContainerStatus.values())
            .collect(Collectors.toMap(
                status -> status,
                status -> containerRepository.countByStatus(status),
                (a, b) -> a,
                LinkedHashMap::new
            ));

        long totalTags = tagRepository.count();

        var recentActivity = timelineEventRepository.findAllByOrderByCreatedAtDesc()
            .stream()
            .limit(10)
            .map(TimelineEventResponse::from)
            .toList();

        return new DashboardSummaryResponse(
            totalContainers,
            containersByStatus,
            totalTags,
            recentActivity
        );
    }
}
