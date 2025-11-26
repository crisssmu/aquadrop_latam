package aquadrop_latam.fleet_service.controller;

import aquadrop_latam.fleet_service.service.RoutePlanService;
import aquadrop_latam.fleet_service.dtos.RoutePlanDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/routeplans")
@CrossOrigin(origins = "*")
public class RoutePlanController {
    
    @Autowired
    private RoutePlanService routePlanService;
    
    @PostMapping
    public ResponseEntity<RoutePlanDTO> createRoutePlan(@RequestBody RoutePlanDTO dto) {
        RoutePlanDTO created = routePlanService.createRoutePlan(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RoutePlanDTO> getRoutePlanById(@PathVariable String id) {
        RoutePlanDTO routePlan = routePlanService.getRoutePlanById(id);
        if (routePlan != null) {
            return ResponseEntity.ok(routePlan);
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<List<RoutePlanDTO>> getRoutePlansByAssignment(@PathVariable String assignmentId) {
        List<RoutePlanDTO> routePlans = routePlanService.getRoutePlansByAssignment(assignmentId);
        return ResponseEntity.ok(routePlans);
    }
    
    @PutMapping
    public ResponseEntity<RoutePlanDTO> updateRoutePlan(@RequestBody RoutePlanDTO dto) {
        RoutePlanDTO updated = routePlanService.updateRoutePlan(dto);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoutePlan(@PathVariable String id) {
        if (routePlanService.deleteRoutePlan(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
