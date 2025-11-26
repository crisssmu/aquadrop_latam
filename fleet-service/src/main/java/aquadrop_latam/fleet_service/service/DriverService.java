package aquadrop_latam.fleet_service.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import aquadrop_latam.fleet_service.dtos.DriverDTO;
import aquadrop_latam.fleet_service.models.Driver;
import aquadrop_latam.fleet_service.repository.DriverRepository;

@Service
public class DriverService {
    
    @Autowired
    private DriverRepository driverRepository;
    
    public DriverDTO createDriver(DriverDTO dto) {
        Driver driver = new Driver();
        driver.setName(dto.getName());
        driver.setPhone(dto.getPhone());
        driver.setKeycloakSub(dto.getKeycloakSub());
        driver.setLicenseNumber(dto.getLicenseNumber());
        
        Driver saved = driverRepository.save(driver);
        return convertToDTO(saved);
    }
    
    public DriverDTO getDriverById(String id) {
        return driverRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }
    
    public DriverDTO getDriverByLicenseNumber(String licenseNumber) {
        return driverRepository.findByLicenseNumber(licenseNumber)
                .map(this::convertToDTO)
                .orElse(null);
    }
    
    public DriverDTO getDriverByPhone(String phone) {
        return driverRepository.findByPhone(phone)
                .map(this::convertToDTO)
                .orElse(null);
    }
    
    public DriverDTO getDriverByKeycloakSub(String keycloakSub) {
        return driverRepository.findByKeycloakSub(keycloakSub)
                .map(this::convertToDTO)
                .orElse(null);
    }
    
    public DriverDTO updateDriver(String id, DriverDTO dto) {
        Optional<Driver> driver = driverRepository.findById(id);
        if (driver.isPresent()) {
            Driver d = driver.get();
            d.setName(dto.getName());
            d.setPhone(dto.getPhone());
            d.setKeycloakSub(dto.getKeycloakSub());
            d.setLicenseNumber(dto.getLicenseNumber());
            return convertToDTO(driverRepository.save(d));
        }
        return null;
    }
    
    public boolean deleteDriver(String id) {
        if (driverRepository.existsById(id)) {
            driverRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    private DriverDTO convertToDTO(Driver driver) {
        DriverDTO dto = new DriverDTO();
        dto.setId(driver.getId());
        dto.setName(driver.getName());
        dto.setPhone(driver.getPhone());
        dto.setKeycloakSub(driver.getKeycloakSub());
        dto.setLicenseNumber(driver.getLicenseNumber());
        return dto;
    }
}
