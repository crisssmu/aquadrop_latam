package aquadrop_latam.fleet_service.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import aquadrop_latam.fleet_service.events.PaymentAuthorizedEvent;
import aquadrop_latam.fleet_service.events.TankerAssignedEvent;
import aquadrop_latam.fleet_service.service.AssignmentService;

@Service
public class PaymentEventListener {

    private static final Logger logger = LoggerFactory.getLogger(PaymentEventListener.class);
    
    private final AssignmentService assignmentService;

    public PaymentEventListener(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    /**
     * Escucha eventos de PaymentAuthorizedEvent desde PaymentService
     * Asigna un tanquero y chofer disponibles, y emite TankerAssignedEvent
     */
    @RabbitListener(queues = "payment.events.queue")
    public void handlePaymentAuthorized(PaymentAuthorizedEvent event) {
        logger.info("📨 Recibiendo PaymentAuthorizedEvent para booking: {} - Payment ID: {}", 
                   event.bookingId(), event.paymentId());
        
        try {
            // Asignar tanquero y chofer disponibles
            TankerAssignedEvent tankerAssignedEvent = assignmentService.assignTankerAndDriver(event.bookingId());
            
            if (tankerAssignedEvent != null) {
                logger.info("✅ Tanquero asignado exitosamente - Booking: {}, Tanker: {}, Driver: {}", 
                           event.bookingId(), tankerAssignedEvent.tankerId(), tankerAssignedEvent.driverId());
                
                // El TankerAssignedEvent será publicado por AssignmentService
            } else {
                logger.error("❌ No se pudo asignar tanquero disponible para booking: {}", event.bookingId());
            }
        } catch (Exception e) {
            logger.error("❌ Error al procesar PaymentAuthorizedEvent para booking {}: {}", 
                        event.bookingId(), e.getMessage(), e);
        }
    }
}
