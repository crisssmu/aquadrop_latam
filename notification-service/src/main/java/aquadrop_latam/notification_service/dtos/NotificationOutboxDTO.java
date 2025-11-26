package aquadrop_latam.notification_service.dtos;

import aquadrop_latam.notification_service.models.Channel;
import aquadrop_latam.notification_service.models.NotificationStatus;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationOutboxDTO {
    
    @NotBlank(message = "El ID de la notificación no puede estar vacío")
    private String id;
    
    @NotBlank(message = "El tipo de evento no puede estar vacío")
    private String eventType;
    
    @NotNull(message = "El payload no puede ser nulo")
    private JsonNode payload;
    
    @NotNull(message = "El estado no puede ser nulo")
    private NotificationStatus status;
    
    @NotBlank(message = "El destinatario no puede estar vacío")
    @Email(message = "El destinatario debe ser un email válido o número de teléfono")
    private String recipient;
    
    @NotNull(message = "El canal no puede ser nulo")
    private Channel channel;
    
    @Min(value = 0, message = "Los reintentos no pueden ser negativos")
    @Max(value = 10, message = "Los reintentos no pueden exceder 10")
    private Integer retries;
    
    @Min(value = 1, message = "Max retries debe ser al menos 1")
    @Max(value = 10, message = "Max retries no puede exceder 10")
    private Integer maxRetries;
    
    private String errorMessage;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastRetryAt;
}
