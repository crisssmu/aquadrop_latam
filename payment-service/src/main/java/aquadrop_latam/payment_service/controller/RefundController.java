package aquadrop_latam.payment_service.controller;

import aquadrop_latam.payment_service.dtos.RefundDto;
import aquadrop_latam.payment_service.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
public class RefundController {
    private final RefundService refundService;

    @PostMapping
    public ResponseEntity<RefundDto> issueRefund(
            @RequestParam String chargeId,
            @RequestParam BigDecimal amount,
            @RequestParam String reason) {
        RefundDto dto = refundService.issueRefund(chargeId, amount, reason);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/{id}/process")
    public ResponseEntity<RefundDto> processRefund(@PathVariable String id) {
        RefundDto dto = refundService.processRefund(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RefundDto> getRefund(@PathVariable String id) {
        RefundDto dto = refundService.queryRefundStatus(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/charge/{chargeId}")
    public ResponseEntity<List<RefundDto>> getRefundsByCharge(@PathVariable String chargeId) {
        List<RefundDto> refunds = refundService.getRefundsByChargeId(chargeId);
        return ResponseEntity.ok(refunds);
    }
}
