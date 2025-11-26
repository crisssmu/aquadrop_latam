package aquadrop_latam.payment_service.dtos;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubsidyDto {
    private String id;
    private String zone;
    private String rule;
    private BigDecimal amount;
    private Boolean isPercentage;
    private Boolean active;
    private Integer maxUses;
    private Integer usesCount;
}
