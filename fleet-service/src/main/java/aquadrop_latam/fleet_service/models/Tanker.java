package aquadrop_latam.fleet_service.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tankers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tanker {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false, unique = true, length = 20)
    private String plate;
    
    @Column(nullable = false)
    private Long capacityLiters;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TankerStatus status;
    
    @Column(nullable = false)
    private Double currentLatitude;
    
    @Column(nullable = false)
    private Double currentLongitude;
    
    @Column
    private LocalDateTime lastLocationUpdate;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = TankerStatus.AVAILABLE;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
