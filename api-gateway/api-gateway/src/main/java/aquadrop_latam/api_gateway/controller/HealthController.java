package aquadrop_latam.api_gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/actuator/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "api-gateway"
        ));
    }

    @GetMapping("/actuator/info")
    public ResponseEntity<Map<String, Object>> info() {
        return ResponseEntity.ok(Map.of(
            "app", "AquaDrop LATAM API Gateway",
            "version", "1.0.0-SNAPSHOT",
            "description", "Central gateway for microservices routing with Eureka discovery"
        ));
    }
}
