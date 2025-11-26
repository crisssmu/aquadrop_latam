package aquadrop_latam.payment_service.models;

public enum RefundStatus {
    PENDING,      // Pendiente de procesamiento
    PROCESSING,   // En proceso
    COMPLETED,    // Completado
    FAILED,       // Falló
    CANCELLED     // Cancelado
}
