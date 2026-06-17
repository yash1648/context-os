package com.grim.contextos.container.repository;

import com.grim.contextos.container.model.Container;
import com.grim.contextos.container.model.ContainerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ContainerRepository extends JpaRepository<Container, UUID> {

    List<Container> findByStatus(ContainerStatus status);

    List<Container> findByNameContainingIgnoreCase(String name);

    long countByStatus(ContainerStatus status);
}
