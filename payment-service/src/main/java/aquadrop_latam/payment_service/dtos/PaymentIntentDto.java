package aquadrop_latam.payment_service.dtos;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentIntentDto {
    private String id;
    private String bookingId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String zone;
    private String description;
    private String customerId;
}
