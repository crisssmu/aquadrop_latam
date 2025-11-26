package aquadrop_latam.bookingService.events;

public record BookingRequestedEvent(
    int bookingId,
    int userSub,
    Float volumeLiters,
    String zone,
    Float fare,
    String status
) {}
