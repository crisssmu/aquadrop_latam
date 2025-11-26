package aquadrop_latam.payment_service.service;

import aquadrop_latam.payment_service.models.Charge;
import aquadrop_latam.payment_service.models.Refund;
import aquadrop_latam.payment_service.models.RefundStatus;
import aquadrop_latam.payment_service.repository.ChargeRepository;
import aquadrop_latam.payment_service.repository.RefundRepository;
import aquadrop_latam.payment_service.dtos.RefundDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.Optional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefundService {
    private final RefundRepository refundRepository;
    private final ChargeRepository chargeRepository;

    @Transactional
    public RefundDto issueRefund(String chargeId, BigDecimal amount, String reason) {
        Optional<Charge> chargeOpt = chargeRepository.findById(chargeId);
        if (chargeOpt.isEmpty()) {
            throw new IllegalArgumentException("Charge not found: " + chargeId);
        }
        
        Charge charge = chargeOpt.get();
        Refund refund = Refund.builder()
            .id(UUID.randomUUID().toString())
            .charge(charge)
            .amount(amount)
            .reason(reason)
            .status(RefundStatus.PENDING)
            .build();
        
        Refund saved = refundRepository.save(refund);
        return mapToRefundDto(saved);
    }

    @Transactional
    public RefundDto processRefund(String refundId) {
        Optional<Refund> refundOpt = refundRepository.findById(refundId);
        if (refundOpt.isEmpty()) {
            throw new IllegalArgumentException("Refund not found: " + refundId);
        }
        
        Refund refund = refundOpt.get();
        refund.setStatus(RefundStatus.PROCESSING);
        refundRepository.save(refund);
        
        // Simulate async processing
        completeRefund(refundId);
        
        return mapToRefundDto(refund);
    }

    @Transactional
    private void completeRefund(String refundId) {
        Optional<Refund> refundOpt = refundRepository.findById(refundId);
        if (refundOpt.isPresent()) {
            Refund refund = refundOpt.get();
            refund.setStatus(RefundStatus.COMPLETED);
            refundRepository.save(refund);
        }
    }

    public List<RefundDto> getRefundsByChargeId(String chargeId) {
        List<Refund> refunds = refundRepository.findByChargeId(chargeId);
        return refunds.stream().map(this::mapToRefundDto).toList();
    }

    public RefundDto queryRefundStatus(String refundId) {
        Optional<Refund> refundOpt = refundRepository.findById(refundId);
        return refundOpt.map(this::mapToRefundDto)
            .orElseThrow(() -> new IllegalArgumentException("Refund not found: " + refundId));
    }

    private RefundDto mapToRefundDto(Refund refund) {
        return RefundDto.builder()
            .id(refund.getId())
            .chargeId(refund.getCharge().getId())
            .amount(refund.getAmount())
            .status(refund.getStatus().toString())
            .reason(refund.getReason())
            .providerReference(refund.getProviderReference())
            .build();
    }
}
