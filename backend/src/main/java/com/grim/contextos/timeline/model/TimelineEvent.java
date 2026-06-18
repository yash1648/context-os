package com.grim.contextos.timeline.model;

import com.grim.contextos.common.audit.BaseEntity;
import com.grim.contextos.container.model.ContainerStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "timeline_events")
public class TimelineEvent extends BaseEntity {

    @Column(nullable = false)
    private UUID containerId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TimelineEventType eventType;

    @Enumerated(EnumType.STRING)
    private ContainerStatus previousStatus;

    @Enumerated(EnumType.STRING)
    private ContainerStatus newStatus;

    @Column(length = 500)
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private String metadata;

    public TimelineEvent() {}

    public TimelineEvent(UUID containerId, TimelineEventType eventType, String description) {
        this.containerId = containerId;
        this.eventType = eventType;
        this.description = description;
    }

    public UUID getContainerId() { return containerId; }
    public void setContainerId(UUID containerId) { this.containerId = containerId; }

    public TimelineEventType getEventType() { return eventType; }
    public void setEventType(TimelineEventType eventType) { this.eventType = eventType; }

    public ContainerStatus getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(ContainerStatus previousStatus) { this.previousStatus = previousStatus; }

    public ContainerStatus getNewStatus() { return newStatus; }
    public void setNewStatus(ContainerStatus newStatus) { this.newStatus = newStatus; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
}
