package com.grim.contextos.container.model;

import com.grim.contextos.common.audit.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "containers")
public class Container extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 100)
    private String type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ContainerStatus status = ContainerStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String envVars;

    @Column(columnDefinition = "TEXT")
    private String resourceLimits;

    @Column(columnDefinition = "TEXT")
    private String labels;

    @Column(length = 1000)
    private String errorMessage;

    private LocalDateTime startedAt;
    private LocalDateTime stoppedAt;

    public Container() {}

    public Container(String name, String description, String type) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.status = ContainerStatus.PENDING;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public ContainerStatus getStatus() { return status; }
    public void setStatus(ContainerStatus status) { this.status = status; }

    public String getEnvVars() { return envVars; }
    public void setEnvVars(String envVars) { this.envVars = envVars; }

    public String getResourceLimits() { return resourceLimits; }
    public void setResourceLimits(String resourceLimits) { this.resourceLimits = resourceLimits; }

    public String getLabels() { return labels; }
    public void setLabels(String labels) { this.labels = labels; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getStoppedAt() { return stoppedAt; }
    public void setStoppedAt(LocalDateTime stoppedAt) { this.stoppedAt = stoppedAt; }
}
