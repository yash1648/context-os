package com.grim.contextos.container.service;

import com.grim.contextos.common.exception.ResourceNotFoundException;
import com.grim.contextos.common.exception.ValidationException;
import com.grim.contextos.container.dto.request.CreateContainerRequest;
import com.grim.contextos.container.dto.request.UpdateContainerRequest;
import com.grim.contextos.container.dto.response.ContainerListResponse;
import com.grim.contextos.container.dto.response.ContainerResponse;
import com.grim.contextos.container.dto.search.ContainerSearchCriteria;
import com.grim.contextos.container.model.Container;
import com.grim.contextos.container.model.ContainerStatus;
import com.grim.contextos.container.repository.ContainerRepository;
import com.grim.contextos.container.validation.ContainerValidationService;
import com.grim.contextos.timeline.model.TimelineEventType;
import com.grim.contextos.timeline.service.TimelineService;
import com.grim.contextos.websocket.event.DomainEvent;
import com.grim.contextos.websocket.event.DomainEventPublisher;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ContainerService {

    private final ContainerRepository containerRepository;
    private final TimelineService timelineService;
    private final ContainerValidationService validationService;
    private final DomainEventPublisher eventPublisher;

    public ContainerService(ContainerRepository containerRepository, TimelineService timelineService,
                            ContainerValidationService validationService,
                            DomainEventPublisher eventPublisher) {
        this.containerRepository = containerRepository;
        this.timelineService = timelineService;
        this.validationService = validationService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ContainerResponse createContainer(CreateContainerRequest request) {
        List<String> validationErrors = validationService.validate(request.metadata(), request.type());
        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Container validation failed",
                validationErrors.stream()
                    .map(msg -> new ValidationException.FieldError("metadata", msg))
                    .toList());
        }
        Container container = new Container(request.name(), request.description(), request.type());
        container.setMetadata(request.metadata());
        container.setEnvVars(request.envVars());
        container.setResourceLimits(request.resourceLimits());
        container.setLabels(request.labels());
        container = containerRepository.save(container);
        timelineService.recordEvent(container.getId(), TimelineEventType.CREATED,
            "Container '" + container.getName() + "' created");
        eventPublisher.publish(new DomainEvent("CONTAINER", "CREATED", container.getId(),
            "Container '" + container.getName() + "' created", null));
        return ContainerResponse.from(container);
    }

    public ContainerResponse getContainer(UUID id) {
        Container container = containerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Container", id));
        return ContainerResponse.from(container);
    }

    @Transactional
    public ContainerResponse updateContainer(UUID id, UpdateContainerRequest request) {
        Container container = containerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Container", id));

        if (request.name() != null) {
            container.setName(request.name());
        }
        if (request.description() != null) {
            container.setDescription(request.description());
        }
        if (request.metadata() != null) {
            List<String> validationErrors = validationService.validate(request.metadata(), container.getType());
            if (!validationErrors.isEmpty()) {
                throw new ValidationException("Container validation failed",
                    validationErrors.stream()
                        .map(msg -> new ValidationException.FieldError("metadata", msg))
                        .toList());
            }
            container.setMetadata(request.metadata());
        }
        if (request.envVars() != null) {
            container.setEnvVars(request.envVars());
        }
        if (request.resourceLimits() != null) {
            container.setResourceLimits(request.resourceLimits());
        }
        if (request.labels() != null) {
            container.setLabels(request.labels());
        }

        container = containerRepository.save(container);
        timelineService.recordEvent(id, TimelineEventType.UPDATED,
            "Container '" + container.getName() + "' updated");
        eventPublisher.publish(new DomainEvent("CONTAINER", "UPDATED", id,
            "Container '" + container.getName() + "' updated", null));
        return ContainerResponse.from(container);
    }

    public ContainerListResponse listContainers() {
        List<Container> all = containerRepository.findAll();
        long running = containerRepository.countByStatus(ContainerStatus.RUNNING);
        long stopped = containerRepository.countByStatus(ContainerStatus.STOPPED);
        long failed = containerRepository.countByStatus(ContainerStatus.FAILED);

        List<ContainerResponse> responses = all.stream()
            .map(ContainerResponse::from)
            .toList();

        return ContainerListResponse.of(responses, all.size(), running, stopped, failed);
    }

    @Transactional
    public void deleteContainer(UUID id) {
        Container container = containerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Container", id));
        String name = container.getName();
        container.setDeletedAt(LocalDateTime.now());
        containerRepository.save(container);
        timelineService.recordEvent(id, TimelineEventType.DELETED,
            "Container '" + name + "' deleted");
        eventPublisher.publish(new DomainEvent("CONTAINER", "DELETED", id,
            "Container '" + name + "' deleted", null));
    }

    @Transactional
    public ContainerResponse restoreContainer(UUID id) {
        Container container = containerRepository.findDeletedById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Container", id));
        container.setDeletedAt(null);
        container = containerRepository.save(container);
        timelineService.recordEvent(id, TimelineEventType.UPDATED,
            "Container '" + container.getName() + "' restored");
        eventPublisher.publish(new DomainEvent("CONTAINER", "UPDATED", id,
            "Container '" + container.getName() + "' restored", null));
        return ContainerResponse.from(container);
    }

    @Transactional
    public ContainerResponse updateProgress(UUID id, int progress) {
        Container container = containerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Container", id));
        container.setProgress(progress);
        container = containerRepository.save(container);
        eventPublisher.publish(new DomainEvent("CONTAINER", "PROGRESS_UPDATED", id,
            "Progress updated to " + progress + "%", null));
        return ContainerResponse.from(container);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void hardDeleteContainer(UUID id) {
        Container container = containerRepository.findDeletedById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Container", id));
        String name = container.getName();
        containerRepository.delete(container);
        eventPublisher.publish(new DomainEvent("CONTAINER", "HARD_DELETED", id,
            "Container '" + name + "' permanently deleted", null));
    }

    @Transactional
    public ContainerResponse transitionStatus(UUID id, ContainerStatus newStatus) {
        Container container = containerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Container", id));

        ContainerStatus previous = container.getStatus();
        validateTransition(previous, newStatus);
        container.setStatus(newStatus);

        if (newStatus == ContainerStatus.RUNNING) {
            container.setStartedAt(java.time.LocalDateTime.now());
        } else if (newStatus == ContainerStatus.STOPPED || newStatus == ContainerStatus.FAILED || newStatus == ContainerStatus.DESTROYED) {
            container.setStoppedAt(java.time.LocalDateTime.now());
        }

        container = containerRepository.save(container);
        timelineService.recordStatusChange(id, previous, newStatus);
        eventPublisher.publish(new DomainEvent("CONTAINER", "STATUS_CHANGED", id,
            "Status changed from " + previous + " to " + newStatus, null));
        return ContainerResponse.from(container);
    }

    @Transactional(readOnly = true)
    public List<ContainerResponse> searchContainers(ContainerSearchCriteria criteria) {
        Specification<Container> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.q() != null && !criteria.q().isBlank()) {
                String pattern = "%" + criteria.q().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("labels")), pattern)
                ));
            }
            if (criteria.status() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.status()));
            }
            if (criteria.type() != null) {
                predicates.add(cb.equal(root.get("type"), criteria.type()));
            }
            if (criteria.tagId() != null) {
                predicates.add(cb.equal(root.join("tags").get("id"), criteria.tagId()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return containerRepository.findAll(spec).stream()
            .map(ContainerResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ContainerResponse> listPinnedContainers() {
        return containerRepository.findByPinnedTrueOrderByPinnedAtDesc().stream()
            .map(ContainerResponse::from)
            .toList();
    }

    @Transactional
    public ContainerResponse pinContainer(UUID id) {
        Container container = containerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Container", id));
        container.setPinned(true);
        container.setPinnedAt(java.time.LocalDateTime.now());
        container = containerRepository.save(container);
        timelineService.recordEvent(id, TimelineEventType.PINNED,
            "Container '" + container.getName() + "' pinned");
        eventPublisher.publish(new DomainEvent("CONTAINER", "PINNED", id,
            "Container '" + container.getName() + "' pinned", null));
        return ContainerResponse.from(container);
    }

    @Transactional
    public ContainerResponse unpinContainer(UUID id) {
        Container container = containerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Container", id));
        container.setPinned(false);
        container.setPinnedAt(null);
        container = containerRepository.save(container);
        timelineService.recordEvent(id, TimelineEventType.UNPINNED,
            "Container '" + container.getName() + "' unpinned");
        eventPublisher.publish(new DomainEvent("CONTAINER", "UNPINNED", id,
            "Container '" + container.getName() + "' unpinned", null));
        return ContainerResponse.from(container);
    }

    private void validateTransition(ContainerStatus current, ContainerStatus next) {
        if (current == ContainerStatus.DESTROYED) {
            throw new IllegalStateException("Cannot transition a destroyed container");
        }
        if (current == next) {
            return; // Idempotent
        }
        // Allow any forward transition for now — Stage 2+ will enforce stricter rules
    }
}
