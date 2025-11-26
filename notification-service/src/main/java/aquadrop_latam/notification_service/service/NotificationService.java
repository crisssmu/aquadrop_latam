package aquadrop_latam.notification_service.service;

import aquadrop_latam.notification_service.models.NotificationOutbox;
import aquadrop_latam.notification_service.models.NotificationStatus;
import aquadrop_latam.notification_service.models.NotificationTemplate;
import aquadrop_latam.notification_service.models.Channel;
import aquadrop_latam.notification_service.repository.NotificationOutboxRepository;
import aquadrop_latam.notification_service.repository.NotificationTemplateRepository;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    @Autowired
    private NotificationTemplateRepository templateRepository;
    
    @Autowired
    private NotificationOutboxRepository outboxRepository;
    
    @Autowired
    private Map<String, NotificationChannel> notificationChannels;
    
    public NotificationOutbox createNotification(String eventType, String recipient, 
                                                  Channel channel, JsonNode payload) {
        NotificationOutbox notification = NotificationOutbox.builder()
                .id(UUID.randomUUID().toString())
                .eventType(eventType)
                .recipient(recipient)
                .channel(channel)
                .payload(payload)
                .status(NotificationStatus.PENDING)
                .retries(0)
                .maxRetries(3)
                .build();
        
        return outboxRepository.save(notification);
    }
    
    @Retry(name = "notificationRetry")
    public void sendNotification(String notificationId) {
        Optional<NotificationOutbox> notification = outboxRepository.findById(notificationId);
        
        if (notification.isEmpty()) {
            logger.warn("Notification not found: {}", notificationId);
            return;
        }
        
        NotificationOutbox outbox = notification.get();
        outbox.setStatus(NotificationStatus.PROCESSING);
        outboxRepository.save(outbox);
        
        try {
            NotificationChannel channel = getNotificationChannel(outbox.getChannel());
            if (channel == null) {
                throw new IllegalArgumentException("No channel found for: " + outbox.getChannel());
            }
            
            String subject = extractSubject(outbox.getPayload());
            String body = extractBody(outbox.getPayload());
            
            channel.send(outbox.getRecipient(), subject, body);
            
            outbox.setStatus(NotificationStatus.SENT);
            logger.info("Notification sent successfully: {}", notificationId);
        } catch (Exception e) {
            handleNotificationFailure(outbox, e);
        }
        
        outboxRepository.save(outbox);
    }
    
    @Transactional
    public void processFailedNotifications() {
        List<NotificationOutbox> failedNotifications = outboxRepository.findFailedNotificationsForRetry();
        
        for (NotificationOutbox notification : failedNotifications) {
            if (notification.getRetries() < notification.getMaxRetries()) {
                notification.setRetries(notification.getRetries() + 1);
                notification.setLastRetryAt(LocalDateTime.now());
                outboxRepository.save(notification);
                
                try {
                    sendNotification(notification.getId());
                } catch (Exception e) {
                    logger.error("Error retrying notification {}: {}", notification.getId(), e.getMessage());
                }
            } else {
                notification.setStatus(NotificationStatus.DEAD_LETTER);
                outboxRepository.save(notification);
                logger.warn("Notification moved to dead letter: {}", notification.getId());
            }
        }
    }
    
    private void handleNotificationFailure(NotificationOutbox outbox, Exception e) {
        outbox.setStatus(NotificationStatus.FAILED);
        outbox.setErrorMessage(e.getMessage());
        outbox.setRetries(outbox.getRetries() + 1);
        outbox.setLastRetryAt(LocalDateTime.now());
        
        if (outbox.getRetries() >= outbox.getMaxRetries()) {
            outbox.setStatus(NotificationStatus.DEAD_LETTER);
            logger.error("Notification failed and moved to dead letter: {} - {}", outbox.getId(), e.getMessage());
        } else {
            logger.warn("Notification failed, will retry: {} - Attempt {}/{}", 
                       outbox.getId(), outbox.getRetries(), outbox.getMaxRetries());
        }
    }
    
    private NotificationChannel getNotificationChannel(Channel channel) {
        return notificationChannels.get(channel.name());
    }
    
    private String extractSubject(JsonNode payload) {
        if (payload.has("subject")) {
            return payload.get("subject").asText();
        }
        return "AquaDrop Notification";
    }
    
    private String extractBody(JsonNode payload) {
        if (payload.has("body")) {
            return payload.get("body").asText();
        }
        if (payload.has("message")) {
            return payload.get("message").asText();
        }
        return payload.toString();
    }
    
    public List<NotificationOutbox> getNotificationsByStatus(NotificationStatus status) {
        return outboxRepository.findByStatus(status);
    }
    
    public List<NotificationOutbox> getDeadLetterNotifications() {
        return outboxRepository.findByStatus(NotificationStatus.DEAD_LETTER);
    }
}
