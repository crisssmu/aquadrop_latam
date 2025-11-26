package aquadrop_latam.notification_service.controller;

import aquadrop_latam.notification_service.service.NotificationService;
import aquadrop_latam.notification_service.models.NotificationStatus;
import aquadrop_latam.notification_service.models.NotificationOutbox;
import aquadrop_latam.notification_service.dtos.NotificationOutboxDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<NotificationOutbox>> getNotificationsByStatus(@PathVariable NotificationStatus status) {
        List<NotificationOutbox> notifications = notificationService.getNotificationsByStatus(status);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/dead-letter")
    public ResponseEntity<List<NotificationOutbox>> getDeadLetterNotifications() {
        List<NotificationOutbox> notifications = notificationService.getDeadLetterNotifications();
        return ResponseEntity.ok(notifications);
    }
    
    @PostMapping("/{id}/send")
    public ResponseEntity<String> sendNotification(@PathVariable String id) {
        try {
            notificationService.sendNotification(id);
            return ResponseEntity.ok("Notification sent successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error sending notification: " + e.getMessage());
        }
    }
    
    @PostMapping("/retry-failed")
    public ResponseEntity<String> retryFailedNotifications() {
        try {
            notificationService.processFailedNotifications();
            return ResponseEntity.ok("Failed notifications retry process completed");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing failed notifications: " + e.getMessage());
        }
    }
}
