package aquadrop_latam.payment_service.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import aquadrop_latam.payment_service.events.AssignmentFailedEvent;
import aquadrop_latam.payment_service.service.PaymentService;

@Component
public class FleetEventListener {

    private static final Logger logger = LoggerFactory.getLogger(FleetEventListener.class);

    @Autowired
    private PaymentService paymentService;

    /**
     * Escucha eventos de AssignmentFailedEvent desde FleetService
     * Inicia compensación: genera refund del pago
     */
    @RabbitListener(queues = "fleet.events.queue")
    public void handleAssignmentFailed(AssignmentFailedEvent event) {
        logger.info("📨 Recibido AssignmentFailedEvent para booking: {} - Razón: {}", 
            event.bookingId(), event.reason());
        
        try {
            // Iniciar compensación: procesar refund
            paymentService.processRefundForFailedAssignment(event.bookingId());
            
            logger.info("✅ Refund procesado exitosamente para booking: {}", event.bookingId());
        } catch (Exception e) {
            logger.error("❌ Error al procesar AssignmentFailedEvent para booking {}: {}", 
                        event.bookingId(), e.getMessage(), e);
        }
    }
}
