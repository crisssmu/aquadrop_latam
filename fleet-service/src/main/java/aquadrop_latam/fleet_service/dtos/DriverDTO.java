package aquadrop_latam.fleet_service.dtos;

import jakarta.validation.constraints.*;

public class DriverDTO {
    
    @NotBlank(message = "El ID del conductor no puede estar vacío")
    private String id;
    
    @NotBlank(message = "El nombre del conductor no puede estar vacío")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String name;
    
    @NotBlank(message = "El número de teléfono no puede estar vacío")
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "El teléfono debe ser un número válido")
    private String phone;
    
    @NotBlank(message = "El keycloak sub no puede estar vacío")
    private String keycloakSub;
    
    @NotBlank(message = "El número de licencia no puede estar vacío")
    @Pattern(regexp = "^[A-Z0-9]{6,15}$", message = "El número de licencia debe tener entre 6 y 15 caracteres alfanuméricos")
    private String licenseNumber;
    
    // Constructores
    public DriverDTO() {}
    
    public DriverDTO(String id, String name, String phone, String keycloakSub, String licenseNumber) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.keycloakSub = keycloakSub;
        this.licenseNumber = licenseNumber;
    }
    
    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getKeycloakSub() { return keycloakSub; }
    public void setKeycloakSub(String keycloakSub) { this.keycloakSub = keycloakSub; }
    
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
}


