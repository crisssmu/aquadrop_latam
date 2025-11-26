package aquadrop_latam.payment_service.dtos;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChargeDto {
    private String id;
    private String paymentIntentId;
    private String provider;
    private String providerReference;
    private BigDecimal authorizedAmount;
    private BigDecimal capturedAmount;
    private String status;
}
