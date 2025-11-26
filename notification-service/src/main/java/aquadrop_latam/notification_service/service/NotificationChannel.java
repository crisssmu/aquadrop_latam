package aquadrop_latam.notification_service.service;

public interface NotificationChannel {
    void send(String recipient, String subject, String body) throws Exception;
    String getChannelName();
}
