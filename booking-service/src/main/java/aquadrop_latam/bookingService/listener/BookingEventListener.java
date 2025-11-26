package aquadrop_latam.bookingService.listener;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import aquadrop_latam.bookingService.events.BookingCancelledEvent;
import aquadrop_latam.bookingService.events.PaymentAuthorizedEvent;
import aquadrop_latam.bookingService.events.PaymentFailedEvent;
import aquadrop_latam.bookingService.events.RefundIssuedEvent;
import aquadrop_latam.bookingService.events.TankerAssignedEvent;
import aquadrop_latam.bookingService.models.Booking;
import aquadrop_latam.bookingService.service.BookingService;

@Component
public class BookingEventListener {

    private static final Logger logger = LoggerFactory.getLogger(BookingEventListener.class);

    @Autowired
    private BookingService bookingService;

    @Autowired
    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = "payment.events.queue")
    public void handlePaymentEvents(@Payload Map<String, Object> eventData) {
        handleBookingEvent(eventData);
    }
    
    @RabbitListener(queues = "fleet.events.queue") 
    public void handleFleetEvents(@Payload Map<String, Object> eventData) {
        handleBookingEvent(eventData);
    }

    public void handleBookingEvent(@Payload Map<String, Object> eventData) {
        try {
            String eventType = (String) eventData.get("eventType");
            switch (eventType) {
                case "PaymentAuthorizedEvent" -> {
                    PaymentAuthorizedEvent paymentEvent = objectMapper.convertValue(eventData, PaymentAuthorizedEvent.class);
                    handlePaymentAuthorized(paymentEvent);
                }
                case "PaymentFailedEvent" -> {
                    PaymentFailedEvent failedEvent = objectMapper.convertValue(eventData, PaymentFailedEvent.class);
                    handlePaymentFailed(failedEvent);
                }
                case "TankerAssignedEvent" -> {
                    TankerAssignedEvent tankerEvent = objectMapper.convertValue(eventData, TankerAssignedEvent.class);
                    handleTankerAssigned(tankerEvent);
                }
                case "RefundIssuedEvent" -> {
                    RefundIssuedEvent refundEvent = objectMapper.convertValue(eventData, RefundIssuedEvent.class);
                    handleRefundIssued(refundEvent);
                }
                default -> logger.info("Evento no reconocido: {}", eventType);
            }
        } catch (Exception e) {
            logger.error("Error procesando evento: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Maneja evento PaymentAuthorized - confirma la reserva según diagrama
     * BookingConfirmedEvent hace énfasis a paymentAuthorized
     */
    public void handlePaymentAuthorized(PaymentAuthorizedEvent event) {
        int bookingId = event.bookingId();
        String paymentId = event.paymentId();
        
        logger.info("💳 Pago autorizado para booking: {}, paymentId: {}", bookingId, paymentId);
        
        // Confirmar booking ahora que el pago fue autorizado
        bookingService.confirmBooking(bookingId, paymentId);
    }

    /**
     * Maneja evento PaymentFailed - cancela la reserva
     * BookingCancelledEvent hace énfasis a paymentFailed según diagrama
     */
    public void handlePaymentFailed(PaymentFailedEvent event) {
        int bookingId = event.bookingId();
        String reason = event.reason();
        
        logger.warn("❌ Pago falló para booking: {}, razón: {}", bookingId, reason);
        
        // Cancelar booking por fallo en el pago
        bookingService.cancelBooking(bookingId, reason, BookingCancelledEvent.PAYMENT_FAILED);
    }

    /**
     * Maneja evento TankerAssigned - actualiza estado cuando se asigna tanker
     */
    public void handleTankerAssigned(TankerAssignedEvent event) {
        int bookingId = event.bookingId();
        logger.info("🚚 Tanker asignado para booking: {} - Tanker: {}, Driver: {}", 
                   bookingId, event.tankerId(), event.driverId());
        
        // El booking ya está confirmado, solo actualizamos logs o estado interno si es necesario
        Booking booking = bookingService.getBookingById(bookingId);
        logger.info("✅ Booking {} tiene tanker asignado. Estado actual: {}", bookingId, booking.getStatus());
    }

    /**
     * Maneja evento RefundIssued - inicia compensación (Saga compensation pattern)
     * Se dispara cuando la asignación de tanquero falla post-pago
     */
    public void handleRefundIssued(RefundIssuedEvent event) {
        int bookingId = event.bookingId();
        Float refundAmount = event.refundAmount();
        String reason = event.reason();
        
        logger.warn("🔄 COMPENSACIÓN: Refund emitido para booking: {} - Monto: {}, Razón: {}", 
                   bookingId, refundAmount, reason);
        
        // Cancelar booking con estado de reembolso
        bookingService.cancelBooking(bookingId, 
            "Cancelado con reembolso: " + reason, 
            BookingCancelledEvent.REFUND_ISSUED);
        
        logger.info("✅ Booking cancelado con compensación - Reembolso de ${} procesado", refundAmount);
    }
}

