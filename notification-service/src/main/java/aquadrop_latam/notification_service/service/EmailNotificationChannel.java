package aquadrop_latam.notification_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailNotificationChannel implements NotificationChannel {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationChannel.class);
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Override
    public void send(String recipient, String subject, String body) throws Exception {
        if (mailSender == null) {
            logger.warn("Email sender not configured");
            return;
        }
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom("noreply@aquadrop-latam.com");
        
        try {
            mailSender.send(message);
            logger.info("Email sent successfully to {}", recipient);
        } catch (Exception e) {
            logger.error("Error sending email to {}: {}", recipient, e.getMessage());
            throw new Exception("Failed to send email: " + e.getMessage());
        }
    }
    
    @Override
    public String getChannelName() {
        return "EMAIL";
    }
}
