package com.grim.contextos.tag.repository;

import com.grim.contextos.tag.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {

    List<Tag> findByOwnerId(UUID ownerId);

    Optional<Tag> findByNameAndOwnerId(String name, UUID ownerId);

    boolean existsByNameAndOwnerId(String name, UUID ownerId);
}
