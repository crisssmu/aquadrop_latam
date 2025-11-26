package aquadrop_latam.fleet_service.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import aquadrop_latam.fleet_service.models.Assignment;
import aquadrop_latam.fleet_service.models.AssignmentStatus;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, String> {
    List<Assignment> findByStatus(AssignmentStatus status);
    
    @Query("SELECT a FROM Assignment a WHERE a.tanker.id = :tankerId")
    List<Assignment> findByTankerId(@Param("tankerId") String tankerId);
    
    @Query("SELECT a FROM Assignment a WHERE a.driver.id = :driverId")
    List<Assignment> findByDriverId(@Param("driverId") String driverId);
    
    List<Assignment> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT a FROM Assignment a WHERE a.status = :status ORDER BY a.createdAt DESC")
    List<Assignment> findByStatusOrderByCreatedAtDesc(@Param("status") AssignmentStatus status);
    
    @Query("SELECT a FROM Assignment a WHERE a.tanker.id = :tankerId AND a.status = 'ACTIVE'")
    List<Assignment> findActiveAssignmentsByTanker(@Param("tankerId") String tankerId);
}
