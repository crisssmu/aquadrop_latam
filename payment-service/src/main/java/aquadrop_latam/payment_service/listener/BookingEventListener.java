package aquadrop_latam.payment_service.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import aquadrop_latam.payment_service.dtos.PaymentIntentDto;
import aquadrop_latam.payment_service.events.BookingRequestedEvent;
import aquadrop_latam.payment_service.models.PaymentProvider;
import aquadrop_latam.payment_service.service.PaymentService;

@Component
public class BookingEventListener {

    private static final Logger logger = LoggerFactory.getLogger(BookingEventListener.class);

    @Autowired
    private PaymentService paymentService;

    @RabbitListener(queues = "booking.events.queue")
    public void handleBookingRequested(BookingRequestedEvent event) {
        logger.info("📨 Recibido evento BookingRequested: bookingId={}, volume={}, zone={}, fare={}", 
            event.bookingId(), event.volumeLiters(), event.zone(), event.fare());
        
        try {
            // 1. Crear PaymentIntent basado en el evento
            PaymentIntentDto paymentIntent = paymentService.createPaymentIntentFromBookingEvent(event);
            logger.info("✅ PaymentIntent creado: id={}", paymentIntent.getId());
            
            // 2. Autorizar automáticamente el pago (simula autorización exitosa)
            // En producción esto iría a un gateway de pago real
            paymentService.authorizePaymentFromIntent(paymentIntent.getId(), PaymentProvider.STRIPE);
            logger.info("✅ Pago autorizado y evento PaymentAuthorized publicado para bookingId: {}", event.bookingId());
            
        } catch (Exception e) {
            logger.error("❌ Error al procesar BookingRequested: {}", e.getMessage(), e);
            // TODO: Publicar PaymentFailedEvent para compensación
        }
    }
}

