package com.grim.contextos.container;

import com.grim.contextos.common.exception.ResourceNotFoundException;
import com.grim.contextos.container.dto.request.CreateContainerRequest;
import com.grim.contextos.container.dto.response.ContainerListResponse;
import com.grim.contextos.container.dto.response.ContainerResponse;
import com.grim.contextos.container.model.Container;
import com.grim.contextos.container.model.ContainerStatus;
import com.grim.contextos.container.model.ContainerType;
import com.grim.contextos.container.repository.ContainerRepository;
import com.grim.contextos.container.service.ContainerService;
import com.grim.contextos.timeline.model.TimelineEventType;
import com.grim.contextos.timeline.service.TimelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContainerServiceTest {

    @Mock
    private ContainerRepository containerRepository;

    @Mock
    private TimelineService timelineService;

    private ContainerService containerService;
    private final UUID containerId = UUID.randomUUID();
    private Container testContainer;

    @BeforeEach
    void setUp() {
        containerService = new ContainerService(containerRepository, timelineService);

        testContainer = new Container("test-container", "A test container", ContainerType.BOOK);
        testContainer.setId(containerId);
        testContainer.setStatus(ContainerStatus.PENDING);
    }

    @Test
    void createContainerReturnsResponse() {
        var request = new CreateContainerRequest("my-container", "desc", ContainerType.BOOK, null, null, null, null);

        when(containerRepository.save(any(Container.class))).thenReturn(testContainer);

        ContainerResponse response = containerService.createContainer(request);

        assertEquals(containerId, response.id());
        assertEquals("test-container", response.name());
        assertEquals(ContainerType.BOOK, response.type());
        assertEquals(ContainerStatus.PENDING, response.status());
    }

    @Test
    void createContainerWithAllFields() {
        var request = new CreateContainerRequest("full-container", "full desc", ContainerType.COURSE,
            null, "{\"KEY\":\"val\"}", "{\"cpu\":\"2\"}", "{\"env\":\"prod\"}");

        when(containerRepository.save(any(Container.class))).thenReturn(testContainer);

        ContainerResponse response = containerService.createContainer(request);

        assertNotNull(response);
        verify(containerRepository).save(any(Container.class));
    }

    @Test
    void getContainerReturnsResponseWhenFound() {
        when(containerRepository.findById(containerId)).thenReturn(Optional.of(testContainer));

        ContainerResponse response = containerService.getContainer(containerId);

        assertEquals(containerId, response.id());
        assertEquals("test-container", response.name());
    }

    @Test
    void getContainerThrowsWhenNotFound() {
        when(containerRepository.findById(containerId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> containerService.getContainer(containerId));
    }

    @Test
    void listContainersReturnsSummaries() {
        Container c2 = new Container("container-2", "desc", ContainerType.MOVIE);
        c2.setId(UUID.randomUUID());
        c2.setStatus(ContainerStatus.RUNNING);
        c2.setStartedAt(LocalDateTime.now());

        when(containerRepository.findAll()).thenReturn(List.of(testContainer, c2));
        when(containerRepository.countByStatus(ContainerStatus.RUNNING)).thenReturn(1L);
        when(containerRepository.countByStatus(ContainerStatus.STOPPED)).thenReturn(0L);
        when(containerRepository.countByStatus(ContainerStatus.FAILED)).thenReturn(0L);

        ContainerListResponse response = containerService.listContainers();

        assertEquals(2, response.total());
        assertEquals(1, response.running());
        assertEquals(0, response.stopped());
        assertEquals(0, response.failed());
        assertEquals(2, response.containers().size());
    }

    @Test
    void listContainersWithMixedStatuses() {
        Container running = new Container("running", "running", ContainerType.MOVIE);
        running.setId(UUID.randomUUID());
        running.setStatus(ContainerStatus.RUNNING);
        Container stopped = new Container("stopped", "stopped", ContainerType.MOVIE);
        stopped.setId(UUID.randomUUID());
        stopped.setStatus(ContainerStatus.STOPPED);

        when(containerRepository.findAll()).thenReturn(List.of(testContainer, running, stopped));
        when(containerRepository.countByStatus(ContainerStatus.RUNNING)).thenReturn(1L);
        when(containerRepository.countByStatus(ContainerStatus.STOPPED)).thenReturn(1L);
        when(containerRepository.countByStatus(ContainerStatus.FAILED)).thenReturn(0L);

        ContainerListResponse response = containerService.listContainers();

        assertEquals(3, response.total());
        assertEquals(1, response.running());
        assertEquals(1, response.stopped());
    }

    @Test
    void deleteContainerDeletesWhenExists() {
        when(containerRepository.findById(containerId)).thenReturn(Optional.of(testContainer));

        containerService.deleteContainer(containerId);

        verify(containerRepository).deleteById(containerId);
    }

    @Test
    void deleteContainerThrowsWhenNotFound() {
        when(containerRepository.findById(containerId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> containerService.deleteContainer(containerId));
        verify(containerRepository, never()).deleteById(any());
    }

    @Test
    void transitionStatusMovesForward() {
        when(containerRepository.findById(containerId)).thenReturn(Optional.of(testContainer));
        when(containerRepository.save(any(Container.class))).thenReturn(testContainer);

        ContainerResponse response = containerService.transitionStatus(containerId, ContainerStatus.RUNNING);

        assertEquals(ContainerStatus.RUNNING, response.status());
        assertNotNull(response.startedAt());
    }

    @Test
    void transitionToDestroyedSetsStoppedAt() {
        testContainer.setStatus(ContainerStatus.RUNNING);
        when(containerRepository.findById(containerId)).thenReturn(Optional.of(testContainer));
        when(containerRepository.save(any(Container.class))).thenReturn(testContainer);

        ContainerResponse response = containerService.transitionStatus(containerId, ContainerStatus.DESTROYED);

        assertEquals(ContainerStatus.DESTROYED, response.status());
        assertNotNull(response.stoppedAt());
    }

    @Test
    void transitionFromDestroyedThrows() {
        testContainer.setStatus(ContainerStatus.DESTROYED);
        when(containerRepository.findById(containerId)).thenReturn(Optional.of(testContainer));

        assertThrows(IllegalStateException.class,
            () -> containerService.transitionStatus(containerId, ContainerStatus.PENDING));
    }

    @Test
    void transitionToSameStatusIsIdempotent() {
        testContainer.setStatus(ContainerStatus.PENDING);
        when(containerRepository.findById(containerId)).thenReturn(Optional.of(testContainer));
        when(containerRepository.save(any(Container.class))).thenReturn(testContainer);

        ContainerResponse response = containerService.transitionStatus(containerId, ContainerStatus.PENDING);

        assertEquals(ContainerStatus.PENDING, response.status());
        verify(containerRepository).save(testContainer);
    }

    @Test
    void transitionThrowsWhenContainerNotFound() {
        when(containerRepository.findById(containerId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> containerService.transitionStatus(containerId, ContainerStatus.RUNNING));
    }

    @Test
    void pinContainerSetsPinned() {
        when(containerRepository.findById(containerId)).thenReturn(Optional.of(testContainer));
        when(containerRepository.save(any(Container.class))).thenReturn(testContainer);

        ContainerResponse response = containerService.pinContainer(containerId);

        assertTrue(response.pinned());
        assertNotNull(response.pinnedAt());
        verify(timelineService).recordEvent(eq(containerId), eq(TimelineEventType.PINNED), any());
    }

    @Test
    void unpinContainerClearsPinned() {
        testContainer.setPinned(true);
        testContainer.setPinnedAt(LocalDateTime.now());
        when(containerRepository.findById(containerId)).thenReturn(Optional.of(testContainer));
        when(containerRepository.save(any(Container.class))).thenReturn(testContainer);

        ContainerResponse response = containerService.unpinContainer(containerId);

        assertFalse(response.pinned());
        assertNull(response.pinnedAt());
        verify(timelineService).recordEvent(eq(containerId), eq(TimelineEventType.UNPINNED), any());
    }

    @Test
    void listPinnedContainersReturnsOnlyPinned() {
        Container pinned1 = new Container("pinned1", "desc", ContainerType.BOOK);
        pinned1.setId(UUID.randomUUID());
        pinned1.setPinned(true);
        pinned1.setPinnedAt(LocalDateTime.now());

        when(containerRepository.findByPinnedTrueOrderByPinnedAtDesc()).thenReturn(List.of(pinned1));

        List<ContainerResponse> results = containerService.listPinnedContainers();

        assertEquals(1, results.size());
        assertTrue(results.getFirst().pinned());
    }
}
