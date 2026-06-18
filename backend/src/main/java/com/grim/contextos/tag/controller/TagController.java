package com.grim.contextos.tag.controller;

import com.grim.contextos.auth.model.UserPrincipal;
import com.grim.contextos.common.response.ApiResponse;
import com.grim.contextos.tag.dto.request.CreateTagRequest;
import com.grim.contextos.tag.dto.request.MergeTagsRequest;
import com.grim.contextos.tag.dto.request.UpdateTagRequest;
import com.grim.contextos.tag.dto.response.TagResponse;
import com.grim.contextos.tag.service.TagService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TagResponse>> createTag(
            @Valid @RequestBody CreateTagRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        TagResponse response = tagService.createTag(request, principal.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TagResponse>>> listTags(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<TagResponse> response = tagService.listTags(principal.id());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<TagResponse>>> searchTags(
            @RequestParam String q,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<TagResponse> results = tagService.searchTags(q, principal.id());
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<ApiResponse<List<TagResponse>>> autocompleteTags(
            @RequestParam String q,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<TagResponse> results = tagService.autocompleteTags(q, principal.id());
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TagResponse>> getTag(@PathVariable UUID id) {
        TagResponse response = tagService.getTag(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TagResponse>> updateTag(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTagRequest request) {
        TagResponse response = tagService.updateTag(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTag(@PathVariable UUID id) {
        tagService.deleteTag(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/merge")
    public ResponseEntity<ApiResponse<TagResponse>> mergeTags(
            @Valid @RequestBody MergeTagsRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        TagResponse response = tagService.mergeTags(
            request.sourceTagId(), request.targetTagId(), principal.id());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<Void>> assignTagsToContainer(
            @RequestParam UUID containerId,
            @RequestBody Set<UUID> tagIds) {
        tagService.assignTagsToContainer(containerId, tagIds);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/{tagId}/containers/{containerId}")
    public ResponseEntity<ApiResponse<Void>> removeTagFromContainer(
            @PathVariable UUID tagId,
            @PathVariable UUID containerId) {
        tagService.removeTagFromContainer(containerId, tagId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
