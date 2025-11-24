package aquadrop_latam.bookingService.service;

import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import aquadrop_latam.bookingService.events.TankerAssignedEvent;
import aquadrop_latam.bookingService.models.Slot;
import aquadrop_latam.bookingService.models.dto.SlotDto;
import aquadrop_latam.bookingService.repository.SlotRepository;

@Service
public class SlotService {

    @Autowired
    private SlotRepository slotRepository;

    public Slot getSlotById(int id) {
        return slotRepository.findById(id).orElse(null);
    }

    public Slot AvailableSlotPerZone(String zone, int availableQuota) {
        
        Slot slot = slotRepository.findByZone(zone);

        slot.setAvailableQuota(availableQuota);

        return slotRepository.save(slot);

    }

    public Slot assignedSlot(SlotDto slotDto) {

        Slot slot = new Slot();

        slot.setDate(slotDto.getDate());
        slot.setWindow_start(slotDto.getWindow_start());
        slot.setWindow_end(slotDto.getWindow_end());
        slot.setCapacity_liters(slotDto.getCapacity_liters() - slotDto.getVolumen_liters());
        slot.setAvailableQuota(slotDto.getAvailable_quota() - slotDto.getQuantity());
        slot.setTanker_id(slotDto.getTanker_id());
        
        return slotRepository.save(slot);
    }

    public List<Slot> getAllSlots() {
        return slotRepository.findAll();
    }

    @RabbitListener(queues = "fleet.events.queue")
    public void handleFleetEvents(TankerAssignedEvent eventData, SlotDto slotDto) {
        if(eventData.success()){
            assignedSlot(slotDto);
        } 
    }

    public boolean cancelBookingSlotPerZone(String zone) {
        Slot slot = slotRepository.findByZone(zone);

        if(slot.getAvailableQuota() > 0){
            slot.setAvailableQuota(slot.getAvailableQuota() - 1);
            return true;
        }
        return false;
    }
    
    
}
