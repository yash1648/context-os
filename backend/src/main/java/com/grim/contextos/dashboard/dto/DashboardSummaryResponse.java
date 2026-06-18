package com.grim.contextos.dashboard.dto;

import com.grim.contextos.container.model.ContainerStatus;
import com.grim.contextos.timeline.dto.response.TimelineEventResponse;

import java.util.List;
import java.util.Map;

public record DashboardSummaryResponse(
    long totalContainers,
    Map<ContainerStatus, Long> containersByStatus,
    long totalTags,
    List<TimelineEventResponse> recentActivity
) {}
