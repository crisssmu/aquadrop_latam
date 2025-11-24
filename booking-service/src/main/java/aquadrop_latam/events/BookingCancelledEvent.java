package aquadrop_latam.events;

/**
 * Evento emitido cuando una reserva es cancelada
 * Hace énfasis a RefundEvent y paymentFailed según los diagramas
 */
public record BookingCancelledEvent(int bookingId, String reason, String cancellationType, Float refundAmount) {

    // Tipos de cancelación
    public static final String PAYMENT_FAILED = "PAYMENT_FAILED";
    public static final String REFUND_REQUESTED = "REFUND_REQUESTED";
    public static final String TANKER_REJECTED = "TANKER_REJECTED";
}
