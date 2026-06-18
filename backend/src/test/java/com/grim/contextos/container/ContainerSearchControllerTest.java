package com.grim.contextos.container;

import com.grim.contextos.auth.security.CustomUserDetailsService;
import com.grim.contextos.auth.security.JwtTokenProvider;
import com.grim.contextos.container.controller.ContainerController;
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
class ContainerSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContainerService containerService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private final ContainerResponse containerResponse = new ContainerResponse(
        UUID.randomUUID(), "my-app", "desc", ContainerType.SOFTWARE_PROJECT,
        null, ContainerStatus.RUNNING, null, null, null, null,
        null, null, LocalDateTime.now(), LocalDateTime.now(),
        false, null, null);

    @Test
    void searchByTextReturns200() throws Exception {
        when(containerService.searchContainers(any())).thenReturn(List.of(containerResponse));

        mockMvc.perform(get("/api/v1/containers/search")
                .param("q", "my-app")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].name").value("my-app"));
    }

    @Test
    void searchByStatusReturns200() throws Exception {
        when(containerService.searchContainers(any())).thenReturn(List.of(containerResponse));

        mockMvc.perform(get("/api/v1/containers/search")
                .param("status", "RUNNING")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].status").value("RUNNING"));
    }

    @Test
    void searchByTypeReturns200() throws Exception {
        when(containerService.searchContainers(any())).thenReturn(List.of(containerResponse));

        mockMvc.perform(get("/api/v1/containers/search")
                .param("type", "SOFTWARE_PROJECT")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].type").value("SOFTWARE_PROJECT"));
    }

    @Test
    void searchByTagIdReturns200() throws Exception {
        when(containerService.searchContainers(any())).thenReturn(List.of(containerResponse));

        mockMvc.perform(get("/api/v1/containers/search")
                .param("tagId", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].name").value("my-app"));
    }

    @Test
    void searchWithAllFiltersReturns200() throws Exception {
        when(containerService.searchContainers(any())).thenReturn(List.of(containerResponse));

        mockMvc.perform(get("/api/v1/containers/search")
                .param("q", "app")
                .param("status", "RUNNING")
                .param("type", "SOFTWARE_PROJECT")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].name").value("my-app"));
    }

    @Test
    void searchReturnsEmptyList() throws Exception {
        when(containerService.searchContainers(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/containers/search")
                .param("q", "nonexistent")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void searchWithoutParamsReturnsAll() throws Exception {
        ContainerResponse c2 = new ContainerResponse(
            UUID.randomUUID(), "other", null, ContainerType.MOVIE,
            null, ContainerStatus.PENDING, null, null, null, null,
            null, null, LocalDateTime.now(), LocalDateTime.now(),
            false, null, null);

        when(containerService.searchContainers(any())).thenReturn(List.of(containerResponse, c2));

        mockMvc.perform(get("/api/v1/containers/search")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(2));
    }
}
