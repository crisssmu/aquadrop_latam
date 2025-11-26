package aquadrop_latam.fleet_service.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutePlanDTO {
    
    @NotBlank(message = "El ID del plan de ruta no puede estar vacío")
    private String id;
    
    @NotBlank(message = "El nombre de la ruta no puede estar vacío")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String name;
    
    @NotNull(message = "La latitud de origen no puede ser nula")
    @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90")
    private Double originLatitude;
    
    @NotNull(message = "La longitud de origen no puede ser nula")
    @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180")
    private Double originLongitude;
    
    @NotNull(message = "La latitud de destino no puede ser nula")
    @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90")
    private Double destinationLatitude;
    
    @NotNull(message = "La longitud de destino no puede ser nula")
    @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180")
    private Double destinationLongitude;
    
    @NotNull(message = "La duración estimada no puede ser nula")
    @Min(value = 1, message = "La duración debe ser mayor a 0")
    private Long estimatedDurationMinutes;
    
    @NotNull(message = "La distancia no puede ser nula")
    @DecimalMin(value = "0.1", message = "La distancia debe ser mayor a 0")
    private Double distanceKm;
    
    private String polylineRoute;
}
