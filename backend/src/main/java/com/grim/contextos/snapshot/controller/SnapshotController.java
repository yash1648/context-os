package com.grim.contextos.snapshot.controller;

import com.grim.contextos.common.response.ApiResponse;
import com.grim.contextos.snapshot.dto.request.CreateSnapshotRequest;
import com.grim.contextos.snapshot.dto.response.SnapshotResponse;
import com.grim.contextos.snapshot.service.SnapshotService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class SnapshotController {

    private final SnapshotService snapshotService;

    public SnapshotController(SnapshotService snapshotService) {
        this.snapshotService = snapshotService;
    }

    @PostMapping("/api/v1/containers/{containerId}/snapshots")
    public ResponseEntity<ApiResponse<SnapshotResponse>> createSnapshot(
            @PathVariable UUID containerId,
            @Valid @RequestBody CreateSnapshotRequest request) {
        SnapshotResponse response = snapshotService.createSnapshot(containerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/api/v1/containers/{containerId}/snapshots")
    public ResponseEntity<ApiResponse<List<SnapshotResponse>>> listSnapshots(
            @PathVariable UUID containerId) {
        List<SnapshotResponse> snapshots = snapshotService.listSnapshots(containerId);
        return ResponseEntity.ok(ApiResponse.ok(snapshots));
    }

    @GetMapping("/api/v1/snapshots/{id}")
    public ResponseEntity<ApiResponse<SnapshotResponse>> getSnapshot(@PathVariable UUID id) {
        SnapshotResponse response = snapshotService.getSnapshot(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/api/v1/snapshots/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSnapshot(@PathVariable UUID id) {
        snapshotService.deleteSnapshot(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/api/v1/snapshots/{id}/restore")
    public ResponseEntity<ApiResponse<SnapshotResponse>> restoreSnapshot(@PathVariable UUID id) {
        SnapshotResponse response = snapshotService.restoreSnapshot(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
