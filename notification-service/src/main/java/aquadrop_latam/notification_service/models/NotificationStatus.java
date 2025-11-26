package aquadrop_latam.notification_service.models;

public enum NotificationStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    SENT("Sent"),
    FAILED("Failed"),
    DEAD_LETTER("Dead Letter");
    
    private final String displayName;
    
    NotificationStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
