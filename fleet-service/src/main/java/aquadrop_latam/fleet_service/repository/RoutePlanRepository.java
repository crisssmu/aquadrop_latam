package aquadrop_latam.fleet_service.repository;

import aquadrop_latam.fleet_service.models.RoutePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoutePlanRepository extends JpaRepository<RoutePlan, String> {
    List<RoutePlan> findByAssignmentId(String assignmentId);
    
    @Query("SELECT r FROM RoutePlan r WHERE r.assignmentId = :assignmentId ORDER BY r.sequenceOrder ASC")
    List<RoutePlan> findByAssignmentIdOrderBySequenceOrder(@Param("assignmentId") String assignmentId);
}
