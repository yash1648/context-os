package com.grim.contextos.health;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/v1/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "contextos-api",
            "version", "1.0.0"
        ));
    }

    @GetMapping("/api/v1/info")
    public ResponseEntity<Map<String, String>> info() {
        return ResponseEntity.ok(Map.of(
            "service", "ContextOS",
            "version", "1.0.0",
            "description", "AI-powered Personal Operating System"
        ));
    }
}
