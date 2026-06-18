package com.grim.contextos.timeline;

import com.grim.contextos.auth.security.CustomUserDetailsService;
import com.grim.contextos.auth.security.JwtTokenProvider;
import com.grim.contextos.timeline.controller.TimelineController;
import com.grim.contextos.timeline.dto.response.TimelineEventResponse;
import com.grim.contextos.timeline.model.TimelineEventType;
import com.grim.contextos.timeline.service.TimelineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TimelineController.class)
class TimelineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TimelineService timelineService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private final UUID eventId = UUID.randomUUID();
    private final UUID containerId = UUID.randomUUID();
    private final TimelineEventResponse eventResponse = new TimelineEventResponse(
        eventId, containerId, TimelineEventType.CREATED, null, null,
        "Container created", null, LocalDateTime.now());

    @Test
    void listEventsReturns200() throws Exception {
        when(timelineService.listEvents(null, null)).thenReturn(List.of(eventResponse));

        mockMvc.perform(get("/api/v1/timeline")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].id").value(eventId.toString()))
            .andExpect(jsonPath("$.data[0].eventType").value("CREATED"));
    }

    @Test
    void listEventsWithContainerFilter() throws Exception {
        when(timelineService.listEvents(eq(containerId), eq(null)))
            .thenReturn(List.of(eventResponse));

        mockMvc.perform(get("/api/v1/timeline")
                .param("containerId", containerId.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].containerId").value(containerId.toString()));
    }

    @Test
    void listEventsWithTypeFilter() throws Exception {
        when(timelineService.listEvents(eq(null), eq(TimelineEventType.CREATED)))
            .thenReturn(List.of(eventResponse));

        mockMvc.perform(get("/api/v1/timeline")
                .param("eventType", "CREATED")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].eventType").value("CREATED"));
    }

    @Test
    void listEventsReturnsEmptyList() throws Exception {
        when(timelineService.listEvents(null, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/timeline")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void getEventReturns200() throws Exception {
        when(timelineService.getEvent(eventId)).thenReturn(eventResponse);

        mockMvc.perform(get("/api/v1/timeline/{id}", eventId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(eventId.toString()))
            .andExpect(jsonPath("$.data.eventType").value("CREATED"))
            .andExpect(jsonPath("$.data.description").value("Container created"));
    }
}
