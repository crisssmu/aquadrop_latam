package aquadrop_latam.events;

public record PaymentFailedEvent(int bookingId, String reason, String errorCode) {

}
