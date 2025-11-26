package aquadrop_latam.fleet_service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import aquadrop_latam.fleet_service.dtos.AssignmentDTO;
import aquadrop_latam.fleet_service.events.TankerAssignedEvent;
import aquadrop_latam.fleet_service.events.AssignmentFailedEvent;
import aquadrop_latam.fleet_service.models.Assignment;
import aquadrop_latam.fleet_service.models.AssignmentStatus;
import aquadrop_latam.fleet_service.models.Driver;
import aquadrop_latam.fleet_service.models.Tanker;
import aquadrop_latam.fleet_service.models.TankerStatus;
import aquadrop_latam.fleet_service.repository.AssignmentRepository;
import aquadrop_latam.fleet_service.repository.DriverRepository;
import aquadrop_latam.fleet_service.repository.TankerRepository;

@Service
public class AssignmentService {
    
    @Autowired
    private AssignmentRepository assignmentRepository;
    
    @Autowired
    private TankerRepository tankerRepository;
    
    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final Logger logger = LoggerFactory.getLogger(AssignmentService.class);
    
    public AssignmentDTO createAssignment(AssignmentDTO dto) {
        Optional<Tanker> tanker = tankerRepository.findById(dto.getTankerId());
        Optional<Driver> driver = driverRepository.findById(dto.getDriverId());
        
        if (!tanker.isPresent() || !driver.isPresent()) {
            throw new IllegalArgumentException("Tanker o Driver no encontrados");
        }
        
        Assignment assignment = new Assignment();
        assignment.setTanker(tanker.get());
        assignment.setDriver(driver.get());
        assignment.setEta(dto.getEndDate());
        assignment.setStatus(AssignmentStatus.PENDING);
        assignment.setCreatedAt(LocalDateTime.now());
        assignment.setUpdatedAt(LocalDateTime.now());
        
        Assignment saved = assignmentRepository.save(assignment);
        return convertToDTO(saved);
    }
    
    public AssignmentDTO getAssignmentById(String id) {
        return assignmentRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }
    
    public List<AssignmentDTO> getAssignmentsByStatus(AssignmentStatus status) {
        return assignmentRepository.findByStatus(status)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<AssignmentDTO> getAssignmentsByTanker(String tankerId) {
        return assignmentRepository.findByTankerId(tankerId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<AssignmentDTO> getAssignmentsByDriver(String driverId) {
        return assignmentRepository.findByDriverId(driverId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public AssignmentDTO updateAssignmentStatus(String id, AssignmentStatus status) {
        Optional<Assignment> assignment = assignmentRepository.findById(id);
        if (assignment.isPresent()) {
            Assignment a = assignment.get();
            a.setStatus(status);
            return convertToDTO(assignmentRepository.save(a));
        }
        return null;
    }
    
    public boolean deleteAssignment(String id) {
        if (assignmentRepository.existsById(id)) {
            assignmentRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    private AssignmentDTO convertToDTO(Assignment assignment) {
        AssignmentDTO dto = new AssignmentDTO();
        dto.setId(assignment.getId());
        dto.setTankerId(assignment.getTanker().getId());
        dto.setDriverId(assignment.getDriver().getId());
        dto.setStatus(assignment.getStatus());
        dto.setCreatedAt(assignment.getCreatedAt());
        return dto;
    }

    /**
     * Asigna un tanquero y chofer disponibles para un booking
     * Emite TankerAssignedEvent en caso exitoso o AssignmentFailedEvent si no hay disponibilidad
     */
    public TankerAssignedEvent assignTankerAndDriver(Integer bookingId) {
        logger.info("🔍 Buscando tanquero y chofer disponibles para booking: {}", bookingId);
        
        try {
            // Buscar un tanquero disponible (con estado AVAILABLE)
            List<Tanker> availableTankers = tankerRepository.findAll()
                    .stream()
                    .filter(t -> t.getStatus() == TankerStatus.AVAILABLE)
                    .collect(Collectors.toList());
            
            if (availableTankers.isEmpty()) {
                logger.warn("⚠️ No hay tanqueros disponibles para booking: {}", bookingId);
                
                // Emitir evento de fallo de asignación
                AssignmentFailedEvent failureEvent = new AssignmentFailedEvent(
                        bookingId,
                        "No hay tanqueros disponibles",
                        "FAILED"
                );
                logger.info("📤 Publicando AssignmentFailedEvent para booking: {} a fleet.events.queue", bookingId);
                rabbitTemplate.convertAndSend("fleet.events.queue", failureEvent);
                logger.info("❌ AssignmentFailedEvent publicado - Sin tanqueros disponibles");
                return null;
            }
            
            Tanker selectedTanker = availableTankers.get(0);
            logger.info("✅ Tanquero seleccionado: Placa {} (ID: {})", selectedTanker.getPlate(), selectedTanker.getId());
            
            // Buscar un chofer disponible (todos los choferes son disponibles por ahora)
            List<Driver> availableDrivers = driverRepository.findAll();
            
            if (availableDrivers.isEmpty()) {
                logger.warn("⚠️ No hay choferes disponibles para booking: {}", bookingId);
                
                // Emitir evento de fallo de asignación
                AssignmentFailedEvent failureEvent = new AssignmentFailedEvent(
                        bookingId,
                        "No hay choferes disponibles",
                        "FAILED"
                );
                logger.info("📤 Publicando AssignmentFailedEvent para booking: {} a fleet.events.queue", bookingId);
                rabbitTemplate.convertAndSend("fleet.events.queue", failureEvent);
                logger.info("❌ AssignmentFailedEvent publicado - Sin choferes disponibles");
                return null;
            }
            
            Driver selectedDriver = availableDrivers.get(0);
            logger.info("✅ Chofer seleccionado: {} (ID: {})", selectedDriver.getName(), selectedDriver.getId());
            
            // Crear la asignación
            Assignment assignment = new Assignment();
            assignment.setBookingId(String.valueOf(bookingId));
            assignment.setTanker(selectedTanker);
            assignment.setDriver(selectedDriver);
            assignment.setStatus(AssignmentStatus.PENDING);
            assignment.setEta(LocalDateTime.now().plusHours(1)); // ETA aproximado
            assignment.setCreatedAt(LocalDateTime.now());
            assignment.setUpdatedAt(LocalDateTime.now());
            
            Assignment savedAssignment = assignmentRepository.save(assignment);
            logger.info("✅ Asignación guardada: {}", savedAssignment.getId());
            
            // Crear el evento TankerAssignedEvent
            TankerAssignedEvent event = new TankerAssignedEvent(
                    bookingId,
                    selectedTanker.getId(),
                    selectedDriver.getId(),
                    savedAssignment.getId(),
                    savedAssignment.getEta(),
                    "ASSIGNED"
            );
            
            // Publicar el evento a RabbitMQ
            logger.info("📤 Publicando TankerAssignedEvent para booking: {} a fleet.events.queue", bookingId);
            rabbitTemplate.convertAndSend("fleet.events.queue", event);
            logger.info("✅ TankerAssignedEvent publicado exitosamente");
            
            return event;
        } catch (Exception e) {
            logger.error("❌ Error al asignar tanquero y chofer: {}", e.getMessage(), e);
            
            // Emitir evento de fallo
            AssignmentFailedEvent failureEvent = new AssignmentFailedEvent(
                    bookingId,
                    "Error durante la asignación: " + e.getMessage(),
                    "FAILED"
            );
            rabbitTemplate.convertAndSend("fleet.events.queue", failureEvent);
            return null;
        }
    }
}
