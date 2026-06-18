package com.grim.contextos.container.controller;

import com.grim.contextos.common.response.ApiResponse;
import com.grim.contextos.container.dto.request.CreateContainerRequest;
import com.grim.contextos.container.dto.request.UpdateContainerRequest;
import com.grim.contextos.container.dto.response.ContainerListResponse;
import com.grim.contextos.container.dto.response.ContainerResponse;
import com.grim.contextos.container.dto.search.ContainerSearchCriteria;
import com.grim.contextos.container.model.ContainerStatus;
import com.grim.contextos.container.model.ContainerType;
import com.grim.contextos.container.service.ContainerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ContainerResponse>> updateContainer(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateContainerRequest request) {
        ContainerResponse response = containerService.updateContainer(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ContainerListResponse>> listContainers() {
        ContainerListResponse response = containerService.listContainers();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ContainerResponse>>> searchContainers(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) ContainerStatus status,
            @RequestParam(required = false) ContainerType type,
            @RequestParam(required = false) UUID tagId) {
        var criteria = new ContainerSearchCriteria(q, status, type, tagId);
        List<ContainerResponse> results = containerService.searchContainers(criteria);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    @GetMapping("/pinned")
    public ResponseEntity<ApiResponse<List<ContainerResponse>>> listPinned() {
        List<ContainerResponse> pinned = containerService.listPinnedContainers();
        return ResponseEntity.ok(ApiResponse.ok(pinned));
    }

    @PostMapping("/{id}/pin")
    public ResponseEntity<ApiResponse<ContainerResponse>> pinContainer(@PathVariable UUID id) {
        ContainerResponse response = containerService.pinContainer(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}/pin")
    public ResponseEntity<ApiResponse<ContainerResponse>> unpinContainer(@PathVariable UUID id) {
        ContainerResponse response = containerService.unpinContainer(id);
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

    @PostMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<ContainerResponse>> restoreContainer(@PathVariable UUID id) {
        ContainerResponse response = containerService.restoreContainer(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<ApiResponse<Void>> hardDeleteContainer(@PathVariable UUID id) {
        containerService.hardDeleteContainer(id);
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
