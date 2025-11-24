package aquadrop_latam.bookingService.events;

/**
 * Evento emitido cuando una reserva es confirmada después de autorización de pago
 * Este evento hace énfasis a paymentAuthorized según los diagramas
 */
public record BookingConfirmedEvent(int bookingId, Float amount, String paymentId, String status) {

}
