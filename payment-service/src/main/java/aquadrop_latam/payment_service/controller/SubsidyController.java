package aquadrop_latam.payment_service.controller;

import aquadrop_latam.payment_service.dtos.SubsidyDto;
import aquadrop_latam.payment_service.service.SubsidyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/subsidies")
@RequiredArgsConstructor
public class SubsidyController {
    private final SubsidyService subsidyService;

    @GetMapping
    public ResponseEntity<List<SubsidyDto>> getActiveSubsidies() {
        List<SubsidyDto> subsidies = subsidyService.getActiveSubsidies();
        return ResponseEntity.ok(subsidies);
    }

    @GetMapping("/zone/{zone}")
    public ResponseEntity<SubsidyDto> getSubsidyByZone(@PathVariable String zone) {
        SubsidyDto subsidy = subsidyService.getSubsidyByZone(zone);
        return ResponseEntity.ok(subsidy);
    }

    @GetMapping("/calculate")
    public ResponseEntity<BigDecimal> calculateSubsidyAmount(
            @RequestParam String zone,
            @RequestParam BigDecimal baseAmount) {
        BigDecimal subsidyAmount = subsidyService.calculateSubsidyAmount(zone, baseAmount);
        return ResponseEntity.ok(subsidyAmount);
    }

    @PostMapping("/apply")
    public ResponseEntity<SubsidyDto> applySubsidy(
            @RequestParam String zone,
            @RequestParam BigDecimal baseAmount) {
        SubsidyDto subsidy = subsidyService.applySubsidy(zone, baseAmount);
        return ResponseEntity.ok(subsidy);
    }
}
