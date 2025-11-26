package aquadrop_latam.fleet_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import aquadrop_latam.fleet_service.models.Tanker;
import aquadrop_latam.fleet_service.models.TankerStatus;

@Repository
public interface TankerRepository extends JpaRepository<Tanker, String> {
    Optional<Tanker> findByPlate(String plate);
    
    List<Tanker> findByStatus(TankerStatus status);
    
    List<Tanker> findByCapacityLitersGreaterThanEqual(Long capacity);
    
    @Query("SELECT t FROM Tanker t WHERE t.status = :status AND t.capacityLiters >= :capacity")
    List<Tanker> findAvailableTankersWithCapacity(@Param("status") TankerStatus status, @Param("capacity") Long capacity);
}
