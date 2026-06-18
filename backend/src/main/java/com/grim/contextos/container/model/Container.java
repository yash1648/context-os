package com.grim.contextos.container.model;

import com.grim.contextos.common.audit.BaseEntity;
import com.grim.contextos.tag.model.Tag;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "containers")
public class Container extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ContainerType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ContainerStatus status = ContainerStatus.PENDING;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private String metadata;

    @Column(columnDefinition = "TEXT")
    private String envVars;

    @Column(columnDefinition = "TEXT")
    private String resourceLimits;

    @Column(columnDefinition = "TEXT")
    private String labels;

    @ManyToMany
    @JoinTable(
        name = "container_tags",
        joinColumns = @JoinColumn(name = "container_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @Column(length = 1000)
    private String errorMessage;

    private LocalDateTime startedAt;
    private LocalDateTime stoppedAt;

    @Column(nullable = false)
    private boolean pinned = false;

    private LocalDateTime pinnedAt;

    public Container() {}

    public Container(String name, String description, ContainerType type) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.status = ContainerStatus.PENDING;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ContainerType getType() { return type; }
    public void setType(ContainerType type) { this.type = type; }

    public ContainerStatus getStatus() { return status; }
    public void setStatus(ContainerStatus status) { this.status = status; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public String getEnvVars() { return envVars; }
    public void setEnvVars(String envVars) { this.envVars = envVars; }

    public String getResourceLimits() { return resourceLimits; }
    public void setResourceLimits(String resourceLimits) { this.resourceLimits = resourceLimits; }

    public String getLabels() { return labels; }
    public void setLabels(String labels) { this.labels = labels; }

    public Set<Tag> getTags() { return tags; }
    public void setTags(Set<Tag> tags) { this.tags = tags; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getStoppedAt() { return stoppedAt; }
    public void setStoppedAt(LocalDateTime stoppedAt) { this.stoppedAt = stoppedAt; }

    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }

    public LocalDateTime getPinnedAt() { return pinnedAt; }
    public void setPinnedAt(LocalDateTime pinnedAt) { this.pinnedAt = pinnedAt; }
}
