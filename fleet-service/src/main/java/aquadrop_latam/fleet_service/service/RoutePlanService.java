package aquadrop_latam.fleet_service.service;

import aquadrop_latam.fleet_service.models.RoutePlan;
import aquadrop_latam.fleet_service.repository.RoutePlanRepository;
import aquadrop_latam.fleet_service.dtos.RoutePlanDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoutePlanService {
    
    @Autowired
    private RoutePlanRepository routePlanRepository;
    
    public RoutePlanDTO createRoutePlan(RoutePlanDTO dto) {
        RoutePlan routePlan = new RoutePlan();
        routePlan.setId(dto.getId());
        routePlan.setName(dto.getName());
        routePlan.setOriginLatitude(dto.getOriginLatitude());
        routePlan.setOriginLongitude(dto.getOriginLongitude());
        routePlan.setDestinationLatitude(dto.getDestinationLatitude());
        routePlan.setDestinationLongitude(dto.getDestinationLongitude());
        routePlan.setEstimatedDurationMinutes(dto.getEstimatedDurationMinutes());
        routePlan.setDistanceKm(dto.getDistanceKm());
        routePlan.setPolylineRoute(dto.getPolylineRoute());
        
        RoutePlan saved = routePlanRepository.save(routePlan);
        return convertToDTO(saved);
    }
    
    public RoutePlanDTO getRoutePlanById(String id) {
        return routePlanRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }
    
    public List<RoutePlanDTO> getRoutePlansByAssignment(String assignmentId) {
        return routePlanRepository.findByAssignmentId(assignmentId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public RoutePlanDTO updateRoutePlan(RoutePlanDTO dto) {
        RoutePlan routePlan = new RoutePlan();
        routePlan.setId(dto.getId());
        routePlan.setName(dto.getName());
        routePlan.setOriginLatitude(dto.getOriginLatitude());
        routePlan.setOriginLongitude(dto.getOriginLongitude());
        routePlan.setDestinationLatitude(dto.getDestinationLatitude());
        routePlan.setDestinationLongitude(dto.getDestinationLongitude());
        routePlan.setEstimatedDurationMinutes(dto.getEstimatedDurationMinutes());
        routePlan.setDistanceKm(dto.getDistanceKm());
        routePlan.setPolylineRoute(dto.getPolylineRoute());
        
        RoutePlan saved = routePlanRepository.save(routePlan);
        return convertToDTO(saved);
    }
    
    public boolean deleteRoutePlan(String id) {
        if (routePlanRepository.existsById(id)) {
            routePlanRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    private RoutePlanDTO convertToDTO(RoutePlan routePlan) {
        RoutePlanDTO dto = new RoutePlanDTO();
        dto.setId(routePlan.getId());
        dto.setName(routePlan.getName());
        dto.setOriginLatitude(routePlan.getOriginLatitude());
        dto.setOriginLongitude(routePlan.getOriginLongitude());
        dto.setDestinationLatitude(routePlan.getDestinationLatitude());
        dto.setDestinationLongitude(routePlan.getDestinationLongitude());
        dto.setEstimatedDurationMinutes(routePlan.getEstimatedDurationMinutes());
        dto.setDistanceKm(routePlan.getDistanceKm());
        dto.setPolylineRoute(routePlan.getPolylineRoute());
        return dto;
    }
}
