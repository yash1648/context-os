package com.grim.contextos.snapshot.repository;

import com.grim.contextos.snapshot.model.Snapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SnapshotRepository extends JpaRepository<Snapshot, UUID> {

    List<Snapshot> findByContainerIdOrderByCreatedAtDesc(UUID containerId);

    long countByContainerId(UUID containerId);
}
