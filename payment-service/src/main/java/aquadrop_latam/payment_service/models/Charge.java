package aquadrop_latam.payment_service.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "charges")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Charge {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @OneToOne(optional = false)
    @JoinColumn(name = "payment_intent_id", nullable = false)
    private PaymentIntent paymentIntent;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentProvider provider;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    
    @Column(nullable = false)
    private String providerReference;
    
    @Column(nullable = false)
    private LocalDateTime authorizedAt;
    
    @Column
    private LocalDateTime capturedAt;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal authorizedAmount;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal capturedAmount;
    
    @Column
    private String failureReason;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
