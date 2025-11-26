package aquadrop_latam.fleet_service.events;

import java.io.Serializable;
import java.time.LocalDateTime;

public record TankerAssignedEvent(
    Integer bookingId,
    String tankerId,
    String driverId,
    String assignmentId,
    LocalDateTime eta,
    String status
) implements Serializable {
    
    private static final long serialVersionUID = 1L;
}
