package com.grim.contextos.container;

import com.grim.contextos.container.dto.search.ContainerSearchCriteria;
import com.grim.contextos.container.dto.response.ContainerResponse;
import com.grim.contextos.container.model.Container;
import com.grim.contextos.container.model.ContainerStatus;
import com.grim.contextos.container.model.ContainerType;
import com.grim.contextos.container.repository.ContainerRepository;
import com.grim.contextos.container.service.ContainerService;
import com.grim.contextos.container.validation.ContainerValidationService;
import com.grim.contextos.timeline.service.TimelineService;
import com.grim.contextos.websocket.event.DomainEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContainerSearchServiceTest {

    @Mock
    private ContainerRepository containerRepository;

    @Mock
    private TimelineService timelineService;

    @Mock
    private ContainerValidationService validationService;

    @Mock
    private DomainEventPublisher eventPublisher;

    private ContainerService containerService;
    private Container testContainer;

    @BeforeEach
    void setUp() {
        containerService = new ContainerService(containerRepository, timelineService, validationService, eventPublisher);

        testContainer = new Container("my-app", "Production app container", ContainerType.SOFTWARE_PROJECT);
        testContainer.setId(UUID.randomUUID());
        testContainer.setStatus(ContainerStatus.RUNNING);
    }

    @Test
    void searchByTextMatchesName() {
        when(containerRepository.findAll(any(Specification.class))).thenReturn(List.of(testContainer));

        var criteria = new ContainerSearchCriteria("my-app", null, null, null);
        List<ContainerResponse> results = containerService.searchContainers(criteria);

        assertEquals(1, results.size());
        assertEquals("my-app", results.getFirst().name());
    }

    @Test
    void searchByTextMatchesDescription() {
        when(containerRepository.findAll(any(Specification.class))).thenReturn(List.of(testContainer));

        var criteria = new ContainerSearchCriteria("production", null, null, null);
        List<ContainerResponse> results = containerService.searchContainers(criteria);

        assertEquals(1, results.size());
    }

    @Test
    void searchByStatus() {
        when(containerRepository.findAll(any(Specification.class))).thenReturn(List.of(testContainer));

        var criteria = new ContainerSearchCriteria(null, ContainerStatus.RUNNING, null, null);
        List<ContainerResponse> results = containerService.searchContainers(criteria);

        assertEquals(1, results.size());
        assertEquals(ContainerStatus.RUNNING, results.getFirst().status());
    }

    @Test
    void searchByType() {
        when(containerRepository.findAll(any(Specification.class))).thenReturn(List.of(testContainer));

        var criteria = new ContainerSearchCriteria(null, null, ContainerType.SOFTWARE_PROJECT, null);
        List<ContainerResponse> results = containerService.searchContainers(criteria);

        assertEquals(1, results.size());
        assertEquals(ContainerType.SOFTWARE_PROJECT, results.getFirst().type());
    }

    @Test
    void searchWithMultipleFilters() {
        when(containerRepository.findAll(any(Specification.class))).thenReturn(List.of(testContainer));

        var criteria = new ContainerSearchCriteria("app", ContainerStatus.RUNNING, ContainerType.SOFTWARE_PROJECT, null);
        List<ContainerResponse> results = containerService.searchContainers(criteria);

        assertEquals(1, results.size());
    }

    @Test
    void searchReturnsEmptyWhenNoMatch() {
        when(containerRepository.findAll(any(Specification.class))).thenReturn(List.of());

        var criteria = new ContainerSearchCriteria("nonexistent", null, null, null);
        List<ContainerResponse> results = containerService.searchContainers(criteria);

        assertTrue(results.isEmpty());
    }

    @Test
    void searchWithNoFiltersReturnsAll() {
        Container c2 = new Container("other", "other", ContainerType.MOVIE);
        c2.setId(UUID.randomUUID());
        c2.setStatus(ContainerStatus.PENDING);

        when(containerRepository.findAll(any(Specification.class))).thenReturn(List.of(testContainer, c2));

        var criteria = new ContainerSearchCriteria(null, null, null, null);
        List<ContainerResponse> results = containerService.searchContainers(criteria);

        assertEquals(2, results.size());
    }
}
