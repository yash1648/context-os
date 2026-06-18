package com.grim.contextos.snapshot;

import com.grim.contextos.common.exception.ResourceNotFoundException;
import com.grim.contextos.container.model.Container;
import com.grim.contextos.container.model.ContainerStatus;
import com.grim.contextos.container.model.ContainerType;
import com.grim.contextos.container.repository.ContainerRepository;
import com.grim.contextos.snapshot.dto.request.CreateSnapshotRequest;
import com.grim.contextos.snapshot.dto.response.SnapshotResponse;
import com.grim.contextos.snapshot.model.Snapshot;
import com.grim.contextos.snapshot.repository.SnapshotRepository;
import com.grim.contextos.snapshot.service.SnapshotService;
import com.grim.contextos.timeline.service.TimelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SnapshotServiceTest {

    @Mock
    private SnapshotRepository snapshotRepository;

    @Mock
    private ContainerRepository containerRepository;

    @Mock
    private TimelineService timelineService;

    private SnapshotService snapshotService;
    private final UUID containerId = UUID.randomUUID();
    private final UUID snapshotId = UUID.randomUUID();
    private Container testContainer;
    private Snapshot testSnapshot;

    @BeforeEach
    void setUp() {
        snapshotService = new SnapshotService(snapshotRepository, containerRepository, timelineService);

        testContainer = new Container("my-container", "A test container", ContainerType.BOOK);
        testContainer.setId(containerId);
        testContainer.setStatus(ContainerStatus.RUNNING);
        testContainer.setMetadata("{\"author\":\"Test\"}");
        testContainer.setEnvVars("{\"KEY\":\"val\"}");

        testSnapshot = new Snapshot(containerId, "v1", "First snapshot");
        testSnapshot.setId(snapshotId);
        testSnapshot.setCapturedName(testContainer.getName());
        testSnapshot.setCapturedDescription(testContainer.getDescription());
        testSnapshot.setCapturedType(testContainer.getType());
        testSnapshot.setCapturedStatus(testContainer.getStatus());
        testSnapshot.setCapturedMetadata(testContainer.getMetadata());
        testSnapshot.setCapturedEnvVars(testContainer.getEnvVars());
    }

    @Test
    void createSnapshotCapturesContainerState() {
        var request = new CreateSnapshotRequest("v1", "First snapshot");
        when(containerRepository.findById(containerId)).thenReturn(Optional.of(testContainer));
        when(snapshotRepository.save(any(Snapshot.class))).thenReturn(testSnapshot);

        SnapshotResponse response = snapshotService.createSnapshot(containerId, request);

        assertEquals(snapshotId, response.id());
        assertEquals("v1", response.name());
        assertEquals("my-container", response.capturedName());
        assertEquals(ContainerType.BOOK, response.capturedType());
        assertEquals(ContainerStatus.RUNNING, response.capturedStatus());
        assertEquals("{\"author\":\"Test\"}", response.capturedMetadata());
        verify(timelineService).recordEvent(any(), any(), any());
    }

    @Test
    void createSnapshotThrowsWhenContainerNotFound() {
        var request = new CreateSnapshotRequest("v1", "desc");
        when(containerRepository.findById(containerId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> snapshotService.createSnapshot(containerId, request));
    }

    @Test
    void listSnapshotsReturnsAllForContainer() {
        when(snapshotRepository.findByContainerIdOrderByCreatedAtDesc(containerId))
            .thenReturn(List.of(testSnapshot));

        List<SnapshotResponse> snapshots = snapshotService.listSnapshots(containerId);

        assertEquals(1, snapshots.size());
        assertEquals("v1", snapshots.getFirst().name());
    }

    @Test
    void listSnapshotsReturnsEmptyWhenNone() {
        when(snapshotRepository.findByContainerIdOrderByCreatedAtDesc(containerId))
            .thenReturn(List.of());

        List<SnapshotResponse> snapshots = snapshotService.listSnapshots(containerId);

        assertTrue(snapshots.isEmpty());
    }

    @Test
    void getSnapshotReturnsResponseWhenFound() {
        when(snapshotRepository.findById(snapshotId)).thenReturn(Optional.of(testSnapshot));

        SnapshotResponse response = snapshotService.getSnapshot(snapshotId);

        assertEquals(snapshotId, response.id());
        assertEquals("v1", response.name());
    }

    @Test
    void getSnapshotThrowsWhenNotFound() {
        when(snapshotRepository.findById(snapshotId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> snapshotService.getSnapshot(snapshotId));
    }

    @Test
    void deleteSnapshotDeletesAndRecordsEvent() {
        when(snapshotRepository.findById(snapshotId)).thenReturn(Optional.of(testSnapshot));

        snapshotService.deleteSnapshot(snapshotId);

        verify(snapshotRepository).deleteById(snapshotId);
        verify(timelineService).recordEvent(any(), any(), any());
    }

    @Test
    void deleteSnapshotThrowsWhenNotFound() {
        when(snapshotRepository.findById(snapshotId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> snapshotService.deleteSnapshot(snapshotId));
        verify(snapshotRepository, never()).deleteById(any());
    }

    @Test
    void restoreSnapshotUpdatesContainerFromSnapshot() {
        when(snapshotRepository.findById(snapshotId)).thenReturn(Optional.of(testSnapshot));
        when(containerRepository.findById(containerId)).thenReturn(Optional.of(testContainer));
        when(containerRepository.save(any(Container.class))).thenReturn(testContainer);

        SnapshotResponse response = snapshotService.restoreSnapshot(snapshotId);

        assertEquals("my-container", testContainer.getName());
        assertEquals(ContainerType.BOOK, testContainer.getType());
        assertEquals(ContainerStatus.RUNNING, testContainer.getStatus());
        assertEquals("{\"author\":\"Test\"}", testContainer.getMetadata());
        assertEquals("v1", response.name());
        verify(containerRepository).save(testContainer);
        verify(timelineService).recordEvent(any(), any(), any());
    }

    @Test
    void restoreSnapshotThrowsWhenSnapshotNotFound() {
        when(snapshotRepository.findById(snapshotId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> snapshotService.restoreSnapshot(snapshotId));
    }

    @Test
    void restoreSnapshotThrowsWhenContainerNotFound() {
        when(snapshotRepository.findById(snapshotId)).thenReturn(Optional.of(testSnapshot));
        when(containerRepository.findById(containerId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> snapshotService.restoreSnapshot(snapshotId));
    }
}
