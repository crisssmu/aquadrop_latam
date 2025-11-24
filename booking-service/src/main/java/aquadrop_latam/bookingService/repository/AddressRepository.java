package aquadrop_latam.bookingService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import aquadrop_latam.bookingService.models.Address;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {
    
}
