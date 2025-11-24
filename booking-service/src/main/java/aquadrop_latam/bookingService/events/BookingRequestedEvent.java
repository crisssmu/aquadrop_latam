package aquadrop_latam.bookingService.events;

public record BookingRequestedEvent(int bookingId, int userSub, Float amount, String status) {

}
