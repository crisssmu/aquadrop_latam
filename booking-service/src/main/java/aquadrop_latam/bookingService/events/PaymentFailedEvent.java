package aquadrop_latam.bookingService.events;

public record PaymentFailedEvent(int bookingId, String reason, String errorCode) {

}
