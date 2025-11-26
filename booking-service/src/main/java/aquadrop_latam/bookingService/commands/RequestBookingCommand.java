package aquadrop_latam.bookingService.commands;

public record RequestBookingCommand(
    int bookingId,
    int userSub,
    Float volumeLiters,
    String zone,
    Float fare,
    String status
) {}
