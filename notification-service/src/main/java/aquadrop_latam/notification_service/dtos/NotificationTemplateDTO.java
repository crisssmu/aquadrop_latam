package aquadrop_latam.notification_service.dtos;

import aquadrop_latam.notification_service.models.Channel;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationTemplateDTO {
    
    @NotBlank(message = "El ID de la plantilla no puede estar vacío")
    private String id;
    
    @NotBlank(message = "El código de la plantilla no puede estar vacío")
    @Pattern(regexp = "^[A-Z_]+$", message = "El código debe estar en mayúsculas y contener solo guiones bajos")
    private String code;
    
    @NotNull(message = "El canal no puede ser nulo")
    private Channel channel;
    
    @NotBlank(message = "El asunto no puede estar vacío")
    @Size(min = 3, max = 200, message = "El asunto debe tener entre 3 y 200 caracteres")
    private String subject;
    
    @NotBlank(message = "El cuerpo no puede estar vacío")
    @Size(min = 10, max = 5000, message = "El cuerpo debe tener entre 10 y 5000 caracteres")
    private String body;
    
    @NotNull(message = "El estado activo no puede ser nulo")
    private boolean active;
    
    private String description;
}
