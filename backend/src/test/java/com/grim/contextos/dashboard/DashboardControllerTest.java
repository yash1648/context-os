package com.grim.contextos.dashboard;

import com.grim.contextos.auth.security.CustomUserDetailsService;
import com.grim.contextos.auth.security.JwtTokenProvider;
import com.grim.contextos.container.model.ContainerStatus;
import com.grim.contextos.dashboard.controller.DashboardController;
import com.grim.contextos.dashboard.dto.DashboardSummaryResponse;
import com.grim.contextos.dashboard.service.DashboardService;
import com.grim.contextos.timeline.dto.response.TimelineEventResponse;
import com.grim.contextos.timeline.model.TimelineEventType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.grim.contextos.auth.model.Role;
import com.grim.contextos.auth.model.UserPrincipal;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private final UUID ownerId = UUID.randomUUID();
    private final UserPrincipal principal = new UserPrincipal(ownerId, "test@test.com", "hash", Role.USER);

    @Test
    void getSummaryReturns200() throws Exception {
        Map<ContainerStatus, Long> byStatus = new LinkedHashMap<>();
        for (ContainerStatus s : ContainerStatus.values()) {
            byStatus.put(s, 1L);
        }

        var activity = List.of(new TimelineEventResponse(
            UUID.randomUUID(), UUID.randomUUID(), TimelineEventType.CREATED,
            null, null, "Container created", null, LocalDateTime.now()
        ));

        var summary = new DashboardSummaryResponse(5L, byStatus, 3L, activity);
        when(dashboardService.getSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/v1/dashboard/summary")
                .with(user(principal))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalContainers").value(5))
            .andExpect(jsonPath("$.data.totalTags").value(3))
            .andExpect(jsonPath("$.data.recentActivity[0].eventType").value("CREATED"));
    }
}
