package aquadrop_latam.fleet_service.dtos;

import aquadrop_latam.fleet_service.models.TankerStatus;
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
public class TankerDTO {
    
    @NotBlank(message = "El ID del tanque no puede estar vacío")
    private String id;
    
    @NotBlank(message = "La placa del tanque no puede estar vacía")
    @Pattern(regexp = "^[A-Z]{2,3}-\\d{3,4}$", message = "La placa debe tener formato válido (ej: ABC-1234)")
    private String plate;
    
    @NotNull(message = "La capacidad del tanque no puede ser nula")
    @Min(value = 100, message = "La capacidad mínima debe ser 100 litros")
    @Max(value = 50000, message = "La capacidad máxima debe ser 50000 litros")
    private Long capacityLiters;
    
    @NotNull(message = "El estado del tanque no puede ser nulo")
    private TankerStatus status;
    
    @NotNull(message = "La latitud no puede ser nula")
    @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90")
    private Double currentLatitude;
    
    @NotNull(message = "La longitud no puede ser nula")
    @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180")
    private Double currentLongitude;
    
    private LocalDateTime lastLocationUpdate;
}
