package aquadrop_latam.notification_service.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PushNotificationChannel implements NotificationChannel {
    
    private static final Logger logger = LoggerFactory.getLogger(PushNotificationChannel.class);
    
    @Override
    public void send(String recipient, String subject, String body) throws Exception {
        try {
            Message message = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(subject)
                            .setBody(body)
                            .build())
                    .setToken(recipient)
                    .build();
            
            String response = FirebaseMessaging.getInstance().send(message);
            logger.info("Push notification sent successfully with message ID: {}", response);
        } catch (Exception e) {
            logger.error("Error sending push notification to {}: {}", recipient, e.getMessage());
            throw new Exception("Failed to send push notification: " + e.getMessage());
        }
    }
    
    @Override
    public String getChannelName() {
        return "PUSH";
    }
}
