package com.grim.contextos.timeline.controller;

import com.grim.contextos.common.response.ApiResponse;
import com.grim.contextos.timeline.dto.response.TimelineEventResponse;
import com.grim.contextos.timeline.model.TimelineEventType;
import com.grim.contextos.timeline.service.TimelineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/timeline")
public class TimelineController {

    private final TimelineService timelineService;

    public TimelineController(TimelineService timelineService) {
        this.timelineService = timelineService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TimelineEventResponse>>> listEvents(
            @RequestParam(required = false) UUID containerId,
            @RequestParam(required = false) TimelineEventType eventType) {
        List<TimelineEventResponse> events = timelineService.listEvents(containerId, eventType);
        return ResponseEntity.ok(ApiResponse.ok(events));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TimelineEventResponse>> getEvent(@PathVariable UUID id) {
        TimelineEventResponse event = timelineService.getEvent(id);
        return ResponseEntity.ok(ApiResponse.ok(event));
    }
}
