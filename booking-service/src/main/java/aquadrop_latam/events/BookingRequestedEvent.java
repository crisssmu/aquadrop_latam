package aquadrop_latam.events;

public record BookingRequestedEvent(int bookingId, int userSub, Float amount, String status) {

}
