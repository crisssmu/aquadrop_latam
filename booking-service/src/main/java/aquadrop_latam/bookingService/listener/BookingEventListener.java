package aquadrop_latam.bookingService.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import aquadrop_latam.bookingService.events.PaymentAuthorizedEvent;
import aquadrop_latam.bookingService.service.BookingService;

/**
 * Listener para eventos del PaymentService
 * Escucha payment.events.queue para PaymentAuthorized
 */
@Component
public class BookingEventListener {

    private static final Logger logger = LoggerFactory.getLogger(BookingEventListener.class);

    @Autowired
    private BookingService bookingService;

    /**
     * Maneja evento PaymentAuthorized del PaymentService
     * Según diagrama: después de autorización exitosa, se envía comando al FleetService
     */
    @RabbitListener(queues = "payment.events.queue", id = "paymentAuthorizedListener", containerFactory = "rabbitListenerContainerFactory")
    public void handlePaymentAuthorized(PaymentAuthorizedEvent event) {
        logger.info("💳 Recibido PaymentAuthorized - bookingId: {}, paymentId: {}, status: {}", 
                   event.bookingId(), event.paymentId(), event.status());
        
        try {
            // El pago fue autorizado, ahora enviamos comando al FleetService para asignar tanker
            bookingService.onPaymentAuthorized(event.bookingId(), event.paymentId());
            logger.info("✅ Comando enviado a FleetService para asignar tanker - bookingId: {}", event.bookingId());
        } catch (Exception e) {
            logger.error("❌ Error procesando PaymentAuthorized para booking {}: {}", 
                        event.bookingId(), e.getMessage(), e);
        }
    }
}

