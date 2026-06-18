package com.grim.contextos.container;

import com.grim.contextos.auth.security.CustomUserDetailsService;
import com.grim.contextos.auth.security.JwtTokenProvider;
import com.grim.contextos.container.controller.ContainerController;
import com.grim.contextos.container.dto.request.CreateContainerRequest;
import com.grim.contextos.container.dto.response.ContainerListResponse;
import com.grim.contextos.container.dto.response.ContainerResponse;
import com.grim.contextos.container.model.ContainerStatus;
import com.grim.contextos.container.model.ContainerType;
import com.grim.contextos.container.service.ContainerService;
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

@WebMvcTest(ContainerController.class)
class ContainerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContainerService containerService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private final UUID containerId = UUID.randomUUID();
    private final ContainerResponse response = new ContainerResponse(
        containerId, "test-ctr", "desc", ContainerType.BOOK, null, ContainerStatus.PENDING,
        null, null, null, null,
        null, null, LocalDateTime.now(), LocalDateTime.now(),
        false, null
    );

    @Test
    void createContainerReturns201() throws Exception {
        when(containerService.createContainer(any(CreateContainerRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/containers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"test-ctr","description":"desc","type":"BOOK"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(containerId.toString()))
            .andExpect(jsonPath("$.data.name").value("test-ctr"))
            .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void createContainerReturns400WhenNameMissing() throws Exception {
        mockMvc.perform(post("/api/v1/containers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"description":"desc","type":"worker"}
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createContainerReturns400WhenTypeMissing() throws Exception {
        mockMvc.perform(post("/api/v1/containers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"test-ctr","description":"desc"}
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createContainerReturns400WhenTypeInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/containers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"test-ctr","description":"desc","type":"INVALID"}
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void listContainersReturns200() throws Exception {
        var listResponse = ContainerListResponse.of(List.of(response), 1, 0, 0, 0);
        when(containerService.listContainers()).thenReturn(listResponse);

        mockMvc.perform(get("/api/v1/containers")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.containers[0].name").value("test-ctr"));
    }

    @Test
    void getContainerReturns200() throws Exception {
        when(containerService.getContainer(containerId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/containers/{id}", containerId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(containerId.toString()));
    }

    @Test
    void deleteContainerReturns200() throws Exception {
        mockMvc.perform(delete("/api/v1/containers/{id}", containerId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateStatusReturns200() throws Exception {
        var runningResponse = new ContainerResponse(
            containerId, "test-ctr", "desc", ContainerType.BOOK, null, ContainerStatus.RUNNING,
            null, null, null, null,
            LocalDateTime.now(), null, LocalDateTime.now(), LocalDateTime.now(),
            false, null
        );
        when(containerService.transitionStatus(containerId, ContainerStatus.RUNNING)).thenReturn(runningResponse);

        mockMvc.perform(patch("/api/v1/containers/{id}/status", containerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status":"RUNNING"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("RUNNING"));
    }

    @Test
    void listPinnedReturns200() throws Exception {
        when(containerService.listPinnedContainers()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/containers/pinned")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].pinned").value(false));
    }

    @Test
    void pinContainerReturns200() throws Exception {
        var pinnedResponse = new ContainerResponse(
            containerId, "test-ctr", "desc", ContainerType.BOOK, null, ContainerStatus.PENDING,
            null, null, null, null,
            null, null, LocalDateTime.now(), LocalDateTime.now(),
            true, LocalDateTime.now());
        when(containerService.pinContainer(containerId)).thenReturn(pinnedResponse);

        mockMvc.perform(post("/api/v1/containers/{id}/pin", containerId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.pinned").value(true));
    }

    @Test
    void unpinContainerReturns200() throws Exception {
        when(containerService.unpinContainer(containerId)).thenReturn(response);

        mockMvc.perform(delete("/api/v1/containers/{id}/pin", containerId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.pinned").value(false));
    }
}
