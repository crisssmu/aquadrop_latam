package aquadrop_latam.bookingService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import aquadrop_latam.bookingService.models.Slot;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Integer> {
    public boolean AvailableSlotPerZone(int id, int availableQuota); 
    public Slot findByZone(String zone);
}
