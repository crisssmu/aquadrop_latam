package aquadrop_latam.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import aquadrop_latam.models.Address;
import aquadrop_latam.models.Booking;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    boolean existsByIdAndAvailable_quotaGreaterThan(int bookingId, Address address); 
}
