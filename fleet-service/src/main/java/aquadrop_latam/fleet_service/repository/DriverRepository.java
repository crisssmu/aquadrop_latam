package aquadrop_latam.fleet_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import aquadrop_latam.fleet_service.models.Driver;

@Repository
public interface DriverRepository extends JpaRepository<Driver, String> {
    Optional<Driver> findByLicenseNumber(String licenseNumber);
    
    Optional<Driver> findByPhone(String phone);
    
    Optional<Driver> findByKeycloakSub(String keycloakSub);
}
