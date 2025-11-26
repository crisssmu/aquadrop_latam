package aquadrop_latam.fleet_service.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import aquadrop_latam.fleet_service.commands.ConfirmeBookingCommand;
import aquadrop_latam.fleet_service.events.TankerAssignedEvent;
import aquadrop_latam.fleet_service.service.AssignmentService;

/**
 * Listener para comandos del BookingService
 * Escucha fleet.queue.commands para asignar tanqueros
 */
@Service
public class BookingCommandListener {

    private static final Logger logger = LoggerFactory.getLogger(BookingCommandListener.class);
    
    private final AssignmentService assignmentService;

    public BookingCommandListener(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    /**
     * Escucha comandos de asignación de tanquero desde BookingService
     * Según diagrama: BookingService -> FleetService (después de pago autorizado)
     */
    @RabbitListener(queues = "fleet.queue.commands", containerFactory = "rabbitListenerContainerFactory")
    public void handleConfirmeBookingCommand(ConfirmeBookingCommand command) {
        logger.info("📨 Recibiendo comando ConfirmeBookingCommand para booking: {}", command.bookingId());
        
        try {
            // Asignar tanquero y chofer disponibles
            TankerAssignedEvent tankerAssignedEvent = assignmentService.assignTankerAndDriver(command.bookingId());
            
            if (tankerAssignedEvent != null) {
                logger.info("✅ Tanquero asignado exitosamente - Booking: {}, Tanker: {}, Driver: {}", 
                           command.bookingId(), tankerAssignedEvent.tankerId(), tankerAssignedEvent.driverId());
            } else {
                logger.error("❌ No se pudo asignar tanquero disponible para booking: {}", command.bookingId());
                // TODO: Publicar AssignmentFailedEvent para compensación
            }
        } catch (Exception e) {
            logger.error("❌ Error al procesar comando AssignTanker para booking {}: {}", 
                        command.bookingId(), e.getMessage(), e);
            // TODO: Publicar AssignmentFailedEvent para compensación
        }
    }
}
