package aquadrop_latam.payment_service.events;

public record BookingRequestedEvent(
    int bookingId,
    int userSub,
    Float volumeLiters,
    String zone,
    Float fare,
    String status
) {}
