package aquadrop_latam.payment_service.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import aquadrop_latam.payment_service.commands.RequestBookingCommand;
import aquadrop_latam.payment_service.dtos.PaymentIntentDto;
import aquadrop_latam.payment_service.models.PaymentProvider;
import aquadrop_latam.payment_service.service.PaymentService;

/**
 * Listener para comandos de booking entrantes.
 * Procesa RequestBookingCommand enviados desde BookingService vía payment.queue.commands
 */
@Component
public class BookingCommandListener {

    private static final Logger logger = LoggerFactory.getLogger(BookingCommandListener.class);

    @Autowired
    private PaymentService paymentService;

    @RabbitListener(queues = "payment.queue.commands", containerFactory = "rabbitListenerContainerFactory")
    public void handleRequestBookingCommand(RequestBookingCommand command) {
        logger.info("📨 Recibido comando RequestBookingCommand: bookingId={}, volume={}, zone={}, fare={}", 
            command.bookingId(), command.volumeLiters(), command.zone(), command.fare());
        
        try {
            // 1. Crear PaymentIntent basado en el comando
            PaymentIntentDto paymentIntent = paymentService.createPaymentIntentFromCommand(command);
            logger.info("✅ PaymentIntent creado desde comando: id={}", paymentIntent.getId());
            
            // 2. Autorizar automáticamente el pago (simula autorización exitosa)
            paymentService.authorizePaymentFromIntent(paymentIntent.getId(), PaymentProvider.STRIPE);
            logger.info("✅ Pago autorizado y evento PaymentAuthorized publicado para bookingId: {}", command.bookingId());
            
        } catch (Exception e) {
            logger.error("❌ Error al procesar RequestBookingCommand: {}", e.getMessage(), e);
            // TODO: Publicar PaymentFailedEvent para compensación
        }
    }
}
