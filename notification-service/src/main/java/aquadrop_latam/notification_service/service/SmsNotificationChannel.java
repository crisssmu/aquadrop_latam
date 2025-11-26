package aquadrop_latam.notification_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@Service
@Lazy
public class SmsNotificationChannel implements NotificationChannel {
    
    private static final Logger logger = LoggerFactory.getLogger(SmsNotificationChannel.class);
    
    @Value("${twilio.account-sid:null}")
    private String accountSid;
    
    @Value("${twilio.auth-token:null}")
    private String authToken;
    
    @Value("${twilio.phone-number:null}")
    private String twilioPhoneNumber;
    
    @Override
    public void send(String recipient, String subject, String body) throws Exception {
        if (accountSid == null || accountSid.isEmpty() || accountSid.equals("null") || 
            authToken == null || authToken.isEmpty() || authToken.equals("null")) {
            logger.warn("Twilio credentials not configured");
            return;
        }
        
        try {
            Twilio.init(accountSid, authToken);
            Message message = Message.creator(
                    new PhoneNumber(recipient),
                    new PhoneNumber(twilioPhoneNumber),
                    body
            ).create();
            
            logger.info("SMS sent successfully to {} with SID: {}", recipient, message.getSid());
        } catch (Exception e) {
            logger.error("Error sending SMS to {}: {}", recipient, e.getMessage());
            throw new Exception("Failed to send SMS: " + e.getMessage());
        }
    }
    
    @Override
    public String getChannelName() {
        return "SMS";
    }
}
