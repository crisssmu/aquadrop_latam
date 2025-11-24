package aquadrop_latam.bookingService.events;

import java.util.Date;

public record DeliveryCompletedEvent(int bookingId, int tankerId, Date deliveredAt, Float volumeDelivered) {

}
