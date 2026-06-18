package com.grim.contextos.timeline.repository;

import com.grim.contextos.timeline.model.TimelineEvent;
import com.grim.contextos.timeline.model.TimelineEventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TimelineEventRepository extends JpaRepository<TimelineEvent, UUID> {

    List<TimelineEvent> findByContainerIdOrderByCreatedAtDesc(UUID containerId);

    List<TimelineEvent> findByEventTypeOrderByCreatedAtDesc(TimelineEventType eventType);

    List<TimelineEvent> findAllByOrderByCreatedAtDesc();

    List<TimelineEvent> findByContainerIdAndEventTypeOrderByCreatedAtDesc(UUID containerId, TimelineEventType eventType);
}
