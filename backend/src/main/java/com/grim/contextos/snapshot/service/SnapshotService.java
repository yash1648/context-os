package com.grim.contextos.snapshot.service;

import com.grim.contextos.common.exception.ResourceNotFoundException;
import com.grim.contextos.container.model.Container;
import com.grim.contextos.container.model.ContainerStatus;
import com.grim.contextos.container.repository.ContainerRepository;
import com.grim.contextos.snapshot.dto.request.CreateSnapshotRequest;
import com.grim.contextos.snapshot.dto.response.SnapshotResponse;
import com.grim.contextos.snapshot.model.Snapshot;
import com.grim.contextos.snapshot.repository.SnapshotRepository;
import com.grim.contextos.timeline.model.TimelineEventType;
import com.grim.contextos.timeline.service.TimelineService;
import com.grim.contextos.websocket.event.DomainEvent;
import com.grim.contextos.websocket.event.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SnapshotService {

    private final SnapshotRepository snapshotRepository;
    private final ContainerRepository containerRepository;
    private final TimelineService timelineService;
    private final DomainEventPublisher eventPublisher;

    public SnapshotService(SnapshotRepository snapshotRepository,
                           ContainerRepository containerRepository,
                           TimelineService timelineService,
                           DomainEventPublisher eventPublisher) {
        this.snapshotRepository = snapshotRepository;
        this.containerRepository = containerRepository;
        this.timelineService = timelineService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public SnapshotResponse createSnapshot(UUID containerId, CreateSnapshotRequest request) {
        Container container = containerRepository.findById(containerId)
            .orElseThrow(() -> new ResourceNotFoundException("Container", containerId));

        Snapshot snapshot = new Snapshot(containerId, request.name(), request.description());
        snapshot.setCapturedName(container.getName());
        snapshot.setCapturedDescription(container.getDescription());
        snapshot.setCapturedType(container.getType());
        snapshot.setCapturedStatus(container.getStatus());
        snapshot.setCapturedMetadata(container.getMetadata());
        snapshot.setCapturedEnvVars(container.getEnvVars());
        snapshot.setCapturedResourceLimits(container.getResourceLimits());
        snapshot.setCapturedLabels(container.getLabels());

        snapshot = snapshotRepository.save(snapshot);

        timelineService.recordEvent(containerId, TimelineEventType.CREATED,
            "Snapshot '" + request.name() + "' created for container '" + container.getName() + "'");
        eventPublisher.publish(new DomainEvent("SNAPSHOT", "CREATED", snapshot.getId(),
            "Snapshot '" + request.name() + "' created", null));

        return SnapshotResponse.from(snapshot);
    }

    @Transactional(readOnly = true)
    public List<SnapshotResponse> listSnapshots(UUID containerId) {
        return snapshotRepository.findByContainerIdOrderByCreatedAtDesc(containerId).stream()
            .map(SnapshotResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public SnapshotResponse getSnapshot(UUID id) {
        Snapshot snapshot = snapshotRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Snapshot", id));
        return SnapshotResponse.from(snapshot);
    }

    @Transactional
    public void deleteSnapshot(UUID id) {
        Snapshot snapshot = snapshotRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Snapshot", id));
        snapshotRepository.deleteById(id);

        timelineService.recordEvent(snapshot.getContainerId(), TimelineEventType.DELETED,
            "Snapshot '" + snapshot.getName() + "' deleted");
        eventPublisher.publish(new DomainEvent("SNAPSHOT", "DELETED", id,
            "Snapshot '" + snapshot.getName() + "' deleted", null));
    }

    @Transactional
    public SnapshotResponse restoreSnapshot(UUID id) {
        Snapshot snapshot = snapshotRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Snapshot", id));

        Container container = containerRepository.findById(snapshot.getContainerId())
            .orElseThrow(() -> new ResourceNotFoundException("Container", snapshot.getContainerId()));

        container.setName(snapshot.getCapturedName());
        container.setDescription(snapshot.getCapturedDescription());
        container.setType(snapshot.getCapturedType());
        container.setStatus(snapshot.getCapturedStatus());
        container.setMetadata(snapshot.getCapturedMetadata());
        container.setEnvVars(snapshot.getCapturedEnvVars());
        container.setResourceLimits(snapshot.getCapturedResourceLimits());
        container.setLabels(snapshot.getCapturedLabels());

        containerRepository.save(container);

        timelineService.recordEvent(container.getId(), TimelineEventType.UPDATED,
            "Container restored from snapshot '" + snapshot.getName() + "'");
        eventPublisher.publish(new DomainEvent("SNAPSHOT", "RESTORED", id,
            "Container restored from snapshot '" + snapshot.getName() + "'", null));

        return SnapshotResponse.from(snapshot);
    }
}
