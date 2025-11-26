package aquadrop_latam.payment_service.events;

public record PaymentAuthorizedEvent(
    int paymentId,
    int bookingId,
    int intentId,
    String status
) {}
