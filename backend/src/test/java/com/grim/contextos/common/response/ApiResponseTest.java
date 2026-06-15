package com.grim.contextos.common.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void okCreatesSuccessResponse() {
        var response = ApiResponse.ok("hello");
        assertTrue(response.success());
        assertEquals("hello", response.data());
        assertNotNull(response.timestamp());
    }

    @Test
    void okWithNullData() {
        var response = ApiResponse.ok(null);
        assertTrue(response.success());
        assertNull(response.data());
    }

    @Test
    void okWithNumericData() {
        var response = ApiResponse.ok(42);
        assertTrue(response.success());
        assertEquals(42, response.data());
    }

    @Test
    void timestampIsIsoFormat() {
        var response = ApiResponse.ok("data");
        assertTrue(response.timestamp().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*"));
    }

    @Test
    void okCreatesNewInstanceEachCall() {
        var r1 = ApiResponse.ok("a");
        var r2 = ApiResponse.ok("b");
        assertNotSame(r1, r2);
    }
}
