package aquadrop_latam.payment_service.models;

public enum PaymentStatus {
    PENDING,          // Pendiente de autorización
    AUTHORIZED,       // Autorizado pero no capturado
    CAPTURED,         // Capturado/Pagado
    FAILED,           // Falló
    REFUNDED,         // Reembolsado
    PARTIALLY_REFUNDED, // Parcialmente reembolsado
    EXPIRED,          // Expiró
    CANCELLED         // Cancelado
}
