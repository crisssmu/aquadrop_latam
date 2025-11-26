package aquadrop_latam.fleet_service.events;

import java.io.Serializable;

public record PaymentAuthorizedEvent(
    Integer paymentId,
    Integer bookingId,
    Integer intentId,
    String status
) implements Serializable {
    
    private static final long serialVersionUID = 1L;
}
