package aquadrop_latam.fleet_service.controller;

import aquadrop_latam.fleet_service.service.AssignmentService;
import aquadrop_latam.fleet_service.models.AssignmentStatus;
import aquadrop_latam.fleet_service.dtos.AssignmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@CrossOrigin(origins = "*")
public class AssignmentController {
    
    @Autowired
    private AssignmentService assignmentService;
    
    @PostMapping
    public ResponseEntity<AssignmentDTO> createAssignment(@RequestBody AssignmentDTO dto) {
        AssignmentDTO created = assignmentService.createAssignment(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AssignmentDTO> getAssignmentById(@PathVariable String id) {
        AssignmentDTO assignment = assignmentService.getAssignmentById(id);
        if (assignment != null) {
            return ResponseEntity.ok(assignment);
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<AssignmentDTO>> getAssignmentsByStatus(@PathVariable AssignmentStatus status) {
        List<AssignmentDTO> assignments = assignmentService.getAssignmentsByStatus(status);
        return ResponseEntity.ok(assignments);
    }
    
    @GetMapping("/tanker/{tankerId}")
    public ResponseEntity<List<AssignmentDTO>> getAssignmentsByTanker(@PathVariable String tankerId) {
        List<AssignmentDTO> assignments = assignmentService.getAssignmentsByTanker(tankerId);
        return ResponseEntity.ok(assignments);
    }
    
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<AssignmentDTO>> getAssignmentsByDriver(@PathVariable String driverId) {
        List<AssignmentDTO> assignments = assignmentService.getAssignmentsByDriver(driverId);
        return ResponseEntity.ok(assignments);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<AssignmentDTO> updateAssignmentStatus(@PathVariable String id, @RequestParam AssignmentStatus status) {
        AssignmentDTO updated = assignmentService.updateAssignmentStatus(id, status);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable String id) {
        if (assignmentService.deleteAssignment(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
