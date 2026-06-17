package com.grim.contextos.container.service;

import com.grim.contextos.common.exception.ResourceNotFoundException;
import com.grim.contextos.container.dto.request.CreateContainerRequest;
import com.grim.contextos.container.dto.response.ContainerListResponse;
import com.grim.contextos.container.dto.response.ContainerResponse;
import com.grim.contextos.container.model.Container;
import com.grim.contextos.container.model.ContainerStatus;
import com.grim.contextos.container.repository.ContainerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ContainerService {

    private final ContainerRepository containerRepository;

    public ContainerService(ContainerRepository containerRepository) {
        this.containerRepository = containerRepository;
    }

    @Transactional
    public ContainerResponse createContainer(CreateContainerRequest request) {
        Container container = new Container(request.name(), request.description(), request.type());
        container.setEnvVars(request.envVars());
        container.setResourceLimits(request.resourceLimits());
        container.setLabels(request.labels());
        container = containerRepository.save(container);
        return ContainerResponse.from(container);
    }

    public ContainerResponse getContainer(UUID id) {
        Container container = containerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Container", id));
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
        if (!containerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Container", id);
        }
        containerRepository.deleteById(id);
    }

    @Transactional
    public ContainerResponse transitionStatus(UUID id, ContainerStatus newStatus) {
        Container container = containerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Container", id));

        validateTransition(container.getStatus(), newStatus);
        container.setStatus(newStatus);

        if (newStatus == ContainerStatus.RUNNING) {
            container.setStartedAt(java.time.LocalDateTime.now());
        } else if (newStatus == ContainerStatus.STOPPED || newStatus == ContainerStatus.FAILED || newStatus == ContainerStatus.DESTROYED) {
            container.setStoppedAt(java.time.LocalDateTime.now());
        }

        container = containerRepository.save(container);
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
