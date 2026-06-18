package com.grim.contextos.snapshot.model;

import com.grim.contextos.common.audit.BaseEntity;
import com.grim.contextos.container.model.ContainerStatus;
import com.grim.contextos.container.model.ContainerType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "snapshots")
public class Snapshot extends BaseEntity {

    @Column(nullable = false)
    private UUID containerId;

    @Column(length = 255)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, length = 255)
    private String capturedName;

    @Column(columnDefinition = "TEXT")
    private String capturedDescription;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ContainerType capturedType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ContainerStatus capturedStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private String capturedMetadata;

    @Column(columnDefinition = "TEXT")
    private String capturedEnvVars;

    @Column(columnDefinition = "TEXT")
    private String capturedResourceLimits;

    @Column(columnDefinition = "TEXT")
    private String capturedLabels;

    public Snapshot() {}

    public Snapshot(UUID containerId, String name, String description) {
        this.containerId = containerId;
        this.name = name;
        this.description = description;
    }

    public UUID getContainerId() { return containerId; }
    public void setContainerId(UUID containerId) { this.containerId = containerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCapturedName() { return capturedName; }
    public void setCapturedName(String capturedName) { this.capturedName = capturedName; }

    public String getCapturedDescription() { return capturedDescription; }
    public void setCapturedDescription(String capturedDescription) { this.capturedDescription = capturedDescription; }

    public ContainerType getCapturedType() { return capturedType; }
    public void setCapturedType(ContainerType capturedType) { this.capturedType = capturedType; }

    public ContainerStatus getCapturedStatus() { return capturedStatus; }
    public void setCapturedStatus(ContainerStatus capturedStatus) { this.capturedStatus = capturedStatus; }

    public String getCapturedMetadata() { return capturedMetadata; }
    public void setCapturedMetadata(String capturedMetadata) { this.capturedMetadata = capturedMetadata; }

    public String getCapturedEnvVars() { return capturedEnvVars; }
    public void setCapturedEnvVars(String capturedEnvVars) { this.capturedEnvVars = capturedEnvVars; }

    public String getCapturedResourceLimits() { return capturedResourceLimits; }
    public void setCapturedResourceLimits(String capturedResourceLimits) { this.capturedResourceLimits = capturedResourceLimits; }

    public String getCapturedLabels() { return capturedLabels; }
    public void setCapturedLabels(String capturedLabels) { this.capturedLabels = capturedLabels; }
}
