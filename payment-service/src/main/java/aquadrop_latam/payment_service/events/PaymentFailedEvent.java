package aquadrop_latam.payment_service.events;

public record PaymentFailedEvent(
    int paymentId,
    int bookingId,
    String reason,
    String status
) {}
