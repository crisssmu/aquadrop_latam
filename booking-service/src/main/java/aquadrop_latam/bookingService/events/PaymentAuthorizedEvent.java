package aquadrop_latam.bookingService.events;

public record PaymentAuthorizedEvent(int bookingId, Float amount, String paymentId) {

}
