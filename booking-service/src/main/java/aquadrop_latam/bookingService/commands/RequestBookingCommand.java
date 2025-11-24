package aquadrop_latam.bookingService.commands;

public record RequestBookingCommand(int bookingId, float amount, String status) {

}
