package aquadrop_latam.fleet_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import aquadrop_latam.fleet_service.dtos.DriverDTO;
import aquadrop_latam.fleet_service.service.DriverService;

@RestController
@RequestMapping("/api/drivers")
@CrossOrigin(origins = "*")
public class DriverController {
    
    @Autowired
    private DriverService driverService;
    
    @PostMapping
    public ResponseEntity<DriverDTO> createDriver(@RequestBody DriverDTO dto) {
        DriverDTO created = driverService.createDriver(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DriverDTO> getDriverById(@PathVariable String id) {
        DriverDTO driver = driverService.getDriverById(id);
        if (driver != null) {
            return ResponseEntity.ok(driver);
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/license-number/{licenseNumber}")
    public ResponseEntity<DriverDTO> getDriverByLicenseNumber(@PathVariable String licenseNumber) {
        DriverDTO driver = driverService.getDriverByLicenseNumber(licenseNumber);
        if (driver != null) {
            return ResponseEntity.ok(driver);
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/phone/{phone}")
    public ResponseEntity<DriverDTO> getDriverByPhone(@PathVariable String phone) {
        DriverDTO driver = driverService.getDriverByPhone(phone);
        if (driver != null) {
            return ResponseEntity.ok(driver);
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/keycloak-sub/{keycloakSub}")
    public ResponseEntity<DriverDTO> getDriverByKeycloakSub(@PathVariable String keycloakSub) {
        DriverDTO driver = driverService.getDriverByKeycloakSub(keycloakSub);
        if (driver != null) {
            return ResponseEntity.ok(driver);
        }
        return ResponseEntity.notFound().build();
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<DriverDTO> updateDriver(@PathVariable String id, @RequestBody DriverDTO dto) {
        DriverDTO updated = driverService.updateDriver(id, dto);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDriver(@PathVariable String id) {
        if (driverService.deleteDriver(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
