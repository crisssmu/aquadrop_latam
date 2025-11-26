package aquadrop_latam.fleet_service.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String bookingId;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "tanker_id", nullable = false)
    private Tanker tanker;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;
    
    @ManyToOne(optional = true)
    @JoinColumn(name = "route_plan_id", nullable = true)
    private RoutePlan routePlan;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AssignmentStatus status;
    
    @Column(nullable = false)
    private LocalDateTime eta;
    
    @Column
    private LocalDateTime acceptedAt;
    
    @Column
    private LocalDateTime startedAt;
    
    @Column
    private LocalDateTime completedAt;
    
    @Column(length = 500)
    private String failureReason;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = AssignmentStatus.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
