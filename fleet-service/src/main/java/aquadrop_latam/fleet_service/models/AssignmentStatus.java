package aquadrop_latam.fleet_service.models;

public enum AssignmentStatus {
    PENDING,        // Esperando confirmación del conductor
    ACCEPTED,       // Aceptado por el conductor
    IN_PROGRESS,    // En progreso
    COMPLETED,      // Completado
    FAILED,         // Falló
    CANCELLED       // Cancelado
}
