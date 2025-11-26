package aquadrop_latam.notification_service.repository;

import aquadrop_latam.notification_service.models.NotificationOutbox;
import aquadrop_latam.notification_service.models.NotificationStatus;
import aquadrop_latam.notification_service.models.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, String> {
    
    List<NotificationOutbox> findByStatus(NotificationStatus status);
    
    List<NotificationOutbox> findByEventType(String eventType);
    
    List<NotificationOutbox> findByChannel(Channel channel);
    
    @Query("SELECT n FROM NotificationOutbox n WHERE n.status = :status AND n.retries < n.maxRetries")
    List<NotificationOutbox> findRetryableNotifications(@Param("status") NotificationStatus status);
    
    @Query("SELECT n FROM NotificationOutbox n WHERE n.status = :status AND n.retries >= n.maxRetries")
    List<NotificationOutbox> findDeadLetterNotifications(@Param("status") NotificationStatus status);
    
    @Query("SELECT n FROM NotificationOutbox n WHERE n.status = 'FAILED' AND n.retries < n.maxRetries ORDER BY n.updatedAt ASC")
    List<NotificationOutbox> findFailedNotificationsForRetry();
    
    @Query("SELECT n FROM NotificationOutbox n WHERE n.status = 'PENDING' AND n.createdAt < :beforeTime")
    List<NotificationOutbox> findStaleNotifications(@Param("beforeTime") LocalDateTime beforeTime);
    
    List<NotificationOutbox> findByRecipient(String recipient);
    
    List<NotificationOutbox> findByStatusAndChannelOrderByCreatedAtDesc(NotificationStatus status, Channel channel);
}
