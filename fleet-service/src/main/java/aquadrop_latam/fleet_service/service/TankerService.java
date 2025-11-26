package aquadrop_latam.fleet_service.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import aquadrop_latam.fleet_service.dtos.TankerDTO;
import aquadrop_latam.fleet_service.models.Tanker;
import aquadrop_latam.fleet_service.models.TankerStatus;
import aquadrop_latam.fleet_service.repository.TankerRepository;

@Service
public class TankerService {
    
    @Autowired
    private TankerRepository tankerRepository;
    
    public TankerDTO createTanker(TankerDTO dto) {
        Tanker tanker = new Tanker();
        tanker.setId(dto.getId());
        tanker.setPlate(dto.getPlate());
        tanker.setCapacityLiters(dto.getCapacityLiters());
        tanker.setCurrentLatitude(dto.getCurrentLatitude());
        tanker.setCurrentLongitude(dto.getCurrentLongitude());
        tanker.setStatus(TankerStatus.AVAILABLE);
        
        Tanker saved = tankerRepository.save(tanker);
        return convertToDTO(saved);
    }
    
    public TankerDTO getTankerById(String id) {
        return tankerRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }
    
    public TankerDTO getTankerByPlate(String plate) {
        return tankerRepository.findByPlate(plate)
                .map(this::convertToDTO)
                .orElse(null);
    }
    
    public List<TankerDTO> getAvailableTankers() {
        return tankerRepository.findByStatus(TankerStatus.AVAILABLE)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<TankerDTO> getTankersByCapacity(Long minCapacity) {
        return tankerRepository.findByCapacityLitersGreaterThanEqual(minCapacity)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public TankerDTO updateTankerStatus(String id, TankerStatus status) {
        Optional<Tanker> tanker = tankerRepository.findById(id);
        if (tanker.isPresent()) {
            Tanker t = tanker.get();
            t.setStatus(status);
            return convertToDTO(tankerRepository.save(t));
        }
        return null;
    }
    
    public boolean deleteTanker(String id) {
        if (tankerRepository.existsById(id)) {
            tankerRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    private TankerDTO convertToDTO(Tanker tanker) {
        TankerDTO dto = new TankerDTO();
        dto.setId(tanker.getId());
        dto.setPlate(tanker.getPlate());
        dto.setCapacityLiters(tanker.getCapacityLiters());
        dto.setCurrentLatitude(tanker.getCurrentLatitude());
        dto.setCurrentLongitude(tanker.getCurrentLongitude());
        dto.setStatus(tanker.getStatus());
        dto.setLastLocationUpdate(tanker.getLastLocationUpdate());
        return dto;
    }
}
