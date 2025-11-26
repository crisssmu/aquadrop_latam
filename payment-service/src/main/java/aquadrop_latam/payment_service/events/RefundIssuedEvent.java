package aquadrop_latam.payment_service.events;

import java.io.Serializable;

public record RefundIssuedEvent(
    Integer bookingId,
    Integer paymentId,
    Float refundAmount,
    String reason,
    String status
) implements Serializable {
    
    private static final long serialVersionUID = 1L;
}
