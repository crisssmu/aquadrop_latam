package aquadrop_latam.payment_service.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "subsidies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subsidy {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String zone;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SubsidyRule rule;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private Boolean isPercentage;
    
    @Column(nullable = false)
    private Boolean active;
    
    @Column
    private String description;
    
    @Column
    private Integer maxUses;
    
    @Column
    private Integer usesCount;
}
