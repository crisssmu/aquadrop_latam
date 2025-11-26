package aquadrop_latam.fleet_service.controller;

import aquadrop_latam.fleet_service.service.TankerService;
import aquadrop_latam.fleet_service.models.TankerStatus;
import aquadrop_latam.fleet_service.dtos.TankerDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tankers")
@CrossOrigin(origins = "*")
public class TankerController {
    
    @Autowired
    private TankerService tankerService;
    
    @PostMapping
    public ResponseEntity<TankerDTO> createTanker(@RequestBody TankerDTO dto) {
        TankerDTO created = tankerService.createTanker(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TankerDTO> getTankerById(@PathVariable String id) {
        TankerDTO tanker = tankerService.getTankerById(id);
        if (tanker != null) {
            return ResponseEntity.ok(tanker);
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/plate/{plate}")
    public ResponseEntity<TankerDTO> getTankerByPlate(@PathVariable String plate) {
        TankerDTO tanker = tankerService.getTankerByPlate(plate);
        if (tanker != null) {
            return ResponseEntity.ok(tanker);
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<TankerDTO>> getAvailableTankers() {
        List<TankerDTO> tankers = tankerService.getAvailableTankers();
        return ResponseEntity.ok(tankers);
    }
    
    @GetMapping("/capacity/{minCapacity}")
    public ResponseEntity<List<TankerDTO>> getTankersByCapacity(@PathVariable Long minCapacity) {
        List<TankerDTO> tankers = tankerService.getTankersByCapacity(minCapacity);
        return ResponseEntity.ok(tankers);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<TankerDTO> updateTankerStatus(@PathVariable String id, @RequestParam TankerStatus status) {
        TankerDTO updated = tankerService.updateTankerStatus(id, status);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTanker(@PathVariable String id) {
        if (tankerService.deleteTanker(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
