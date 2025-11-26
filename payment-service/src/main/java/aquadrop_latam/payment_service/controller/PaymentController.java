package aquadrop_latam.payment_service.controller;

import aquadrop_latam.payment_service.dtos.PaymentIntentDto;
import aquadrop_latam.payment_service.dtos.ChargeDto;
import aquadrop_latam.payment_service.models.PaymentProvider;
import aquadrop_latam.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/intents")
    public ResponseEntity<PaymentIntentDto> createPaymentIntent(
            @RequestParam String bookingId,
            @RequestParam BigDecimal amount,
            @RequestParam String zone,
            @RequestParam String customerId,
            @RequestParam(required = false, defaultValue = "") String description) {
        PaymentIntentDto dto = paymentService.createPaymentIntent(bookingId, amount, zone, customerId, description);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/intents/{id}")
    public ResponseEntity<PaymentIntentDto> getPaymentIntent(@PathVariable String id) {
        PaymentIntentDto dto = paymentService.queryPaymentStatus(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/intents/{id}/authorize")
    public ResponseEntity<ChargeDto> authorizePayment(
            @PathVariable String id,
            @RequestParam PaymentProvider provider) {
        ChargeDto dto = paymentService.authorizePayment(id, provider);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/intents/{id}/capture")
    public ResponseEntity<ChargeDto> capturePayment(
            @PathVariable String id,
            @RequestParam BigDecimal capturedAmount) {
        ChargeDto dto = paymentService.capturePayment(id, capturedAmount);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Payment Service is healthy");
    }
}
