package com.grim.contextos.container;

import com.grim.contextos.container.model.Container;
import com.grim.contextos.container.model.ContainerStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ContainerModelTest {

    @Test
    void containerDefaultStatusIsPending() {
        Container c = new Container("test", "desc", "worker");
        assertEquals(ContainerStatus.PENDING, c.getStatus());
    }

    @Test
    void containerConstructorSetsFields() {
        Container c = new Container("my-container", "My description", "gpu-worker");
        assertEquals("my-container", c.getName());
        assertEquals("My description", c.getDescription());
        assertEquals("gpu-worker", c.getType());
    }

    @Test
    void containerSettersAndGetters() {
        Container c = new Container();
        c.setId(java.util.UUID.randomUUID());
        c.setName("name");
        c.setDescription("desc");
        c.setType("type");
        c.setStatus(ContainerStatus.RUNNING);
        c.setEnvVars("{\"key\":\"val\"}");
        c.setResourceLimits("{\"cpu\":\"2\"}");
        c.setLabels("{\"env\":\"prod\"}");
        c.setErrorMessage("OOM killed");
        c.setStartedAt(LocalDateTime.now());
        c.setStoppedAt(LocalDateTime.now());

        assertEquals("name", c.getName());
        assertEquals("desc", c.getDescription());
        assertEquals("type", c.getType());
        assertEquals(ContainerStatus.RUNNING, c.getStatus());
        assertEquals("{\"key\":\"val\"}", c.getEnvVars());
        assertEquals("{\"cpu\":\"2\"}", c.getResourceLimits());
        assertEquals("{\"env\":\"prod\"}", c.getLabels());
        assertEquals("OOM killed", c.getErrorMessage());
        assertNotNull(c.getStartedAt());
        assertNotNull(c.getStoppedAt());
    }

    @Test
    void containerStatusValues() {
        assertNotNull(ContainerStatus.valueOf("PENDING"));
        assertNotNull(ContainerStatus.valueOf("BUILDING"));
        assertNotNull(ContainerStatus.valueOf("RUNNING"));
        assertNotNull(ContainerStatus.valueOf("STOPPED"));
        assertNotNull(ContainerStatus.valueOf("FAILED"));
        assertNotNull(ContainerStatus.valueOf("DESTROYED"));
    }

    @Test
    void containerHasSixStatuses() {
        assertEquals(6, ContainerStatus.values().length);
    }
}
