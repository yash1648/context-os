package com.grim.contextos.snapshot;

import com.grim.contextos.auth.security.CustomUserDetailsService;
import com.grim.contextos.auth.security.JwtTokenProvider;
import com.grim.contextos.container.model.ContainerStatus;
import com.grim.contextos.container.model.ContainerType;
import com.grim.contextos.snapshot.controller.SnapshotController;
import com.grim.contextos.snapshot.dto.response.SnapshotResponse;
import com.grim.contextos.snapshot.service.SnapshotService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SnapshotController.class)
class SnapshotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SnapshotService snapshotService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private final UUID snapshotId = UUID.randomUUID();
    private final UUID containerId = UUID.randomUUID();
    private final SnapshotResponse snapshotResponse = new SnapshotResponse(
        snapshotId, containerId, "v1", "First snapshot",
        "my-container", "desc", ContainerType.BOOK, ContainerStatus.RUNNING,
        null, null, null, null, LocalDateTime.now());

    @Test
    void createSnapshotReturns201() throws Exception {
        when(snapshotService.createSnapshot(any(), any())).thenReturn(snapshotResponse);

        mockMvc.perform(post("/api/v1/containers/{containerId}/snapshots", containerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"v1","description":"First snapshot"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("v1"))
            .andExpect(jsonPath("$.data.capturedType").value("BOOK"));
    }

    @Test
    void createSnapshotReturns400WhenNameMissing() throws Exception {
        mockMvc.perform(post("/api/v1/containers/{containerId}/snapshots", containerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"description":"First snapshot"}
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void listSnapshotsReturns200() throws Exception {
        when(snapshotService.listSnapshots(containerId)).thenReturn(List.of(snapshotResponse));

        mockMvc.perform(get("/api/v1/containers/{containerId}/snapshots", containerId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].name").value("v1"));
    }

    @Test
    void listSnapshotsReturnsEmptyList() throws Exception {
        when(snapshotService.listSnapshots(containerId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/containers/{containerId}/snapshots", containerId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void getSnapshotReturns200() throws Exception {
        when(snapshotService.getSnapshot(snapshotId)).thenReturn(snapshotResponse);

        mockMvc.perform(get("/api/v1/snapshots/{id}", snapshotId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(snapshotId.toString()))
            .andExpect(jsonPath("$.data.name").value("v1"));
    }

    @Test
    void deleteSnapshotReturns200() throws Exception {
        mockMvc.perform(delete("/api/v1/snapshots/{id}", snapshotId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void restoreSnapshotReturns200() throws Exception {
        when(snapshotService.restoreSnapshot(snapshotId)).thenReturn(snapshotResponse);

        mockMvc.perform(post("/api/v1/snapshots/{id}/restore", snapshotId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("v1"));
    }
}
