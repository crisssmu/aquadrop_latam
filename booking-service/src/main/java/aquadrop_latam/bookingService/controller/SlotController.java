package aquadrop_latam.bookingService.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import aquadrop_latam.bookingService.models.Slot;
import aquadrop_latam.bookingService.models.dto.SlotDto;
import aquadrop_latam.bookingService.service.SlotService;


@Controller
@RequestMapping("/slot")
public class SlotController {

    @Autowired
    private SlotService slotService;

    @PostMapping("/assigned")
    public ResponseEntity<Slot> assignedSlot(@RequestBody SlotDto dto) {
        boolean result = slotService.cancelBookingSlotPerZone(dto.getZone());

        if(!result){
            return ResponseEntity.badRequest().build();
        }
        
        Slot slot = slotService.assignedSlot(dto);

        if (slot == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/")
    public List<Slot> getAllSlots() {
        return slotService.getAllSlots();
    }

    @PostMapping("/assignedPerZone/{zone}")
    public ResponseEntity<Slot> assignedSlotPerZone(@RequestBody int availableQuota, @PathVariable String zone) {

        Slot slot = slotService.AvailableSlotPerZone(zone, availableQuota);

        if (slot == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }
    
}
