package aquadrop_latam.api_gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@Slf4j
public class FallbackController {

    /**
     * Fallback general para circuit breaker
     */
    @GetMapping("/fallback")
    @PostMapping("/fallback")
    public ResponseEntity<Map<String, Object>> fallback() {
        log.error("Circuit breaker activated - service unavailable");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "error", "Service temporarily unavailable",
                "message", "The requested service is currently experiencing issues. Please try again later.",
                "timestamp", LocalDateTime.now().toString(),
                "status", 503
            ));
    }

    /**
     * Fallback específico para booking service
     */
    @GetMapping("/fallback/bookings")
    @PostMapping("/fallback/bookings")
    public ResponseEntity<Map<String, Object>> bookingsFallback() {
        log.error("Booking service fallback activated");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "error", "Booking service unavailable",
                "message", "We're unable to process booking requests at the moment. Please try again later.",
                "timestamp", LocalDateTime.now().toString(),
                "status", 503
            ));
    }

    /**
     * Fallback específico para fleet service
     */
    @GetMapping("/fallback/fleet")
    @PostMapping("/fallback/fleet")
    public ResponseEntity<Map<String, Object>> fleetFallback() {
        log.error("Fleet service fallback activated");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "error", "Fleet service unavailable",
                "message", "Fleet management is currently unavailable. Please try again later.",
                "timestamp", LocalDateTime.now().toString(),
                "status", 503
            ));
    }

    /**
     * Fallback específico para payment service
     */
    @GetMapping("/fallback/payments")
    @PostMapping("/fallback/payments")
    public ResponseEntity<Map<String, Object>> paymentsFallback() {
        log.error("Payment service fallback activated");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "error", "Payment service unavailable",
                "message", "Payment processing is currently unavailable. Please try again later.",
                "timestamp", LocalDateTime.now().toString(),
                "status", 503
            ));
    }

    /**
     * Fallback específico para notification service
     */
    @GetMapping("/fallback/notifications")
    @PostMapping("/fallback/notifications")
    public ResponseEntity<Map<String, Object>> notificationsFallback() {
        log.error("Notification service fallback activated");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "error", "Notification service unavailable",
                "message", "Notifications are currently unavailable. Please try again later.",
                "timestamp", LocalDateTime.now().toString(),
                "status", 503
            ));
    }
}
