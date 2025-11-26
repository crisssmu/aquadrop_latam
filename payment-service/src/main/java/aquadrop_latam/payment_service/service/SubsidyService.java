package aquadrop_latam.payment_service.service;

import aquadrop_latam.payment_service.models.Subsidy;
import aquadrop_latam.payment_service.repository.SubsidyRepository;
import aquadrop_latam.payment_service.dtos.SubsidyDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubsidyService {
    private final SubsidyRepository subsidyRepository;

    public SubsidyDto applySubsidy(String zone, BigDecimal baseAmount) {
        Optional<Subsidy> subsidyOpt = subsidyRepository.findByZone(zone);
        if (subsidyOpt.isEmpty()) {
            throw new IllegalArgumentException("No subsidy found for zone: " + zone);
        }
        
        Subsidy subsidy = subsidyOpt.get();
        if (!subsidy.getActive()) {
            throw new IllegalStateException("Subsidy is not active for zone: " + zone);
        }
        
        if (subsidy.getMaxUses() != null && subsidy.getUsesCount() >= subsidy.getMaxUses()) {
            throw new IllegalStateException("Subsidy usage limit exceeded for zone: " + zone);
        }
        
        return mapToSubsidyDto(subsidy);
    }

    public List<SubsidyDto> getActiveSubsidies() {
        List<Subsidy> subsidies = subsidyRepository.findByActive(true);
        return subsidies.stream().map(this::mapToSubsidyDto).toList();
    }

    public SubsidyDto getSubsidyByZone(String zone) {
        Optional<Subsidy> subsidyOpt = subsidyRepository.findByZone(zone);
        return subsidyOpt.map(this::mapToSubsidyDto)
            .orElseThrow(() -> new IllegalArgumentException("Subsidy not found for zone: " + zone));
    }

    public BigDecimal calculateSubsidyAmount(String zone, BigDecimal baseAmount) {
        Subsidy subsidy = subsidyRepository.findByZone(zone)
            .orElseThrow(() -> new IllegalArgumentException("Subsidy not found for zone: " + zone));
        
        if (subsidy.getIsPercentage()) {
            return baseAmount.multiply(subsidy.getAmount().divide(BigDecimal.valueOf(100)));
        } else {
            return subsidy.getAmount();
        }
    }

    private SubsidyDto mapToSubsidyDto(Subsidy subsidy) {
        return SubsidyDto.builder()
            .id(subsidy.getId())
            .zone(subsidy.getZone())
            .rule(subsidy.getRule().toString())
            .amount(subsidy.getAmount())
            .isPercentage(subsidy.getIsPercentage())
            .active(subsidy.getActive())
            .maxUses(subsidy.getMaxUses())
            .usesCount(subsidy.getUsesCount())
            .build();
    }
}
