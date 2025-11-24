package aquadrop_latam.bookingService.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import aquadrop_latam.bookingService.models.PriorityTag;

public interface PriorityTagRepository extends JpaRepository<PriorityTag, Integer> {

}
