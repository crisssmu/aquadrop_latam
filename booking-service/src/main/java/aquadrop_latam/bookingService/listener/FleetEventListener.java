package aquadrop_latam.bookingService.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import aquadrop_latam.bookingService.events.TankerAssignedEvent;
import aquadrop_latam.bookingService.service.BookingService;

@Service
public class FleetEventListener {

    private static final Logger logger = LoggerFactory.getLogger(FleetEventListener.class);
    
    private final BookingService bookingService;

    public FleetEventListener(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Escucha eventos de TankerAssignedEvent desde FleetService
     * Actualiza el estado del booking a CONFIRMED
     */
    @RabbitListener(queues = "fleet.events.queue")
    public void handleTankerAssigned(TankerAssignedEvent event) {
        logger.info("📨 Recibiendo TankerAssignedEvent para booking: {} - Tanker: {}, Driver: {}", 
                   event.bookingId(), event.tankerId(), event.driverId());
        
        try {
            // Confirmar el booking usando la asignación como referencia
            // paymentId se envía como assignmentId ya que es la referencia de la asignación
            bookingService.confirmBooking(event.bookingId(), event.assignmentId());
            
            logger.info("✅ Booking confirmado exitosamente - Booking ID: {}, Assignment ID: {}, ETA: {}", 
                       event.bookingId(), event.assignmentId(), event.eta());
            
        } catch (Exception e) {
            logger.error("❌ Error al procesar TankerAssignedEvent para booking {}: {}", 
                        event.bookingId(), e.getMessage(), e);
        }
    }
}
