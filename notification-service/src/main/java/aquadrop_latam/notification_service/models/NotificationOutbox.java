package aquadrop_latam.notification_service.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;

@Entity
@Table(name = "notification_outbox", 
       indexes = {
           @Index(name = "idx_status", columnList = "status"),
           @Index(name = "idx_event_type", columnList = "event_type"),
           @Index(name = "idx_created_at", columnList = "created_at")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationOutbox {
    
    @Id
    private String id;
    
    @Column(nullable = false)
    private String eventType;
    
    @Column(columnDefinition = "jsonb", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode payload;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationStatus status;
    
    @Column(nullable = false)
    private Integer retries = 0;
    
    @Column(nullable = false)
    private Integer maxRetries = 3;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    private LocalDateTime lastRetryAt;
    
    private String errorMessage;
    
    private String recipient;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Channel channel;
    
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
