package aquadrop_latam.fleet_service.dtos;

import aquadrop_latam.fleet_service.models.AssignmentStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentDTO {
    
    @NotBlank(message = "El ID de la asignación no puede estar vacío")
    private String id;
    
    @NotBlank(message = "El ID del tanque no puede estar vacío")
    private String tankerId;
    
    @NotBlank(message = "El ID del conductor no puede estar vacío")
    private String driverId;
    
    @NotNull(message = "La fecha de inicio no puede ser nula")
    @FutureOrPresent(message = "La fecha de inicio debe ser futura o presente")
    private LocalDateTime startDate;
    
    @NotNull(message = "La fecha de fin no puede ser nula")
    @Future(message = "La fecha de fin debe ser futura")
    private LocalDateTime endDate;
    
    @NotNull(message = "El estado de la asignación no puede ser nulo")
    private AssignmentStatus status;
    
    private LocalDateTime createdAt;
    
}
