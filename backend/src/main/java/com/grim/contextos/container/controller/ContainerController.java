package com.grim.contextos.container.controller;

import com.grim.contextos.common.response.ApiResponse;
import com.grim.contextos.container.dto.request.CreateContainerRequest;
import com.grim.contextos.container.dto.response.ContainerListResponse;
import com.grim.contextos.container.dto.response.ContainerResponse;
import com.grim.contextos.container.model.ContainerStatus;
import com.grim.contextos.container.service.ContainerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/containers")
public class ContainerController {

    private final ContainerService containerService;

    public ContainerController(ContainerService containerService) {
        this.containerService = containerService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ContainerResponse>> createContainer(@Valid @RequestBody CreateContainerRequest request) {
        ContainerResponse response = containerService.createContainer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ContainerListResponse>> listContainers() {
        ContainerListResponse response = containerService.listContainers();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContainerResponse>> getContainer(@PathVariable UUID id) {
        ContainerResponse response = containerService.getContainer(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteContainer(@PathVariable UUID id) {
        containerService.deleteContainer(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ContainerResponse>> updateStatus(
            @PathVariable UUID id,
            @RequestBody StatusUpdateRequest request) {
        ContainerResponse response = containerService.transitionStatus(id, request.status());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    public record StatusUpdateRequest(ContainerStatus status) {}
}
