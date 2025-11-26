package aquadrop_latam.bookingService.events;

import java.io.Serializable;

public record PaymentAuthorizedEvent(
    String paymentId,
    int bookingId,
    String intentId,
    String status
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
