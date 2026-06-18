package com.grim.contextos.container.repository;

import com.grim.contextos.container.model.Container;
import com.grim.contextos.container.model.ContainerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface ContainerRepository extends JpaRepository<Container, UUID>, JpaSpecificationExecutor<Container> {

    List<Container> findByStatus(ContainerStatus status);

    List<Container> findByNameContainingIgnoreCase(String name);

    List<Container> findByTagsId(UUID tagId);

    long countByStatus(ContainerStatus status);

    List<Container> findByPinnedTrueOrderByPinnedAtDesc();
}
