package aquadrop_latam.payment_service.dtos;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundDto {
    private String id;
    private String chargeId;
    private BigDecimal amount;
    private String status;
    private String reason;
    private String providerReference;
}
