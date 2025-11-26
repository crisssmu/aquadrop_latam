package aquadrop_latam.fleet_service.events;

import java.io.Serializable;

public record PaymentAuthorizedEvent(
    String paymentId,
    Integer bookingId,
    String intentId,
    String status
) implements Serializable {
    
    private static final long serialVersionUID = 1L;
}
