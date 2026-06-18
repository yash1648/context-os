package com.grim.contextos.container.repository;

import com.grim.contextos.container.model.Container;
import com.grim.contextos.container.model.ContainerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContainerRepository extends JpaRepository<Container, UUID>, JpaSpecificationExecutor<Container> {

    List<Container> findByStatus(ContainerStatus status);

    List<Container> findByNameContainingIgnoreCase(String name);

    List<Container> findByTagsId(UUID tagId);

    long countByStatus(ContainerStatus status);

    List<Container> findByPinnedTrueOrderByPinnedAtDesc();

    // Bypass @SQLRestriction for admin operations
    @Query("SELECT c FROM Container c WHERE c.id = :id AND c.deletedAt IS NOT NULL")
    Optional<Container> findDeletedById(UUID id);

    @Query("SELECT c FROM Container c WHERE c.deletedAt IS NOT NULL")
    List<Container> findAllDeleted();
}
