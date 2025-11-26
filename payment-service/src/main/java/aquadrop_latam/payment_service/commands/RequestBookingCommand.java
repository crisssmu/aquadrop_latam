package aquadrop_latam.payment_service.commands;

public record RequestBookingCommand(
    int bookingId,
    int userSub,
    Float volumeLiters,
    String zone,
    Float fare,
    String status
) {}
