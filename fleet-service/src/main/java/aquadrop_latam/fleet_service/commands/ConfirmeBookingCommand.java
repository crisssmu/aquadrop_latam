package aquadrop_latam.fleet_service.commands;

import java.io.Serializable;

public record ConfirmeBookingCommand(int bookingId) implements Serializable {
    private static final long serialVersionUID = 1L;
}
