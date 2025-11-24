package aquadrop_latam.events;

public record PaymentAuthorizedEvent(int bookingId, Float amount, String paymentId) {

}
