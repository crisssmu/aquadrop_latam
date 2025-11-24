package aquadrop_latam.bookingService.listener;

import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import aquadrop_latam.bookingService.events.BookingCancelledEvent;
import aquadrop_latam.bookingService.events.PaymentAuthorizedEvent;
import aquadrop_latam.bookingService.events.PaymentFailedEvent;
import aquadrop_latam.bookingService.events.TankerAssignedEvent;
import aquadrop_latam.bookingService.models.Booking;
import aquadrop_latam.bookingService.service.BookingService;

@Component
public class BookingEventListener {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = "payment.events.queue")
    public void handlePaymentEvents(@Payload Map<String, Object> eventData) {
        handleBookigEvent(eventData);
    }
    
    @RabbitListener(queues = "fleet.events.queue") 
    public void handleFleetEvents(@Payload Map<String, Object> eventData) {
        handleBookigEvent(eventData);
    }

    public void handleBookigEvent(@Payload Map<String, Object> eventData) {
        try{
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
                    if (tankerEvent.success()) {
                        handleTankerAssigned(tankerEvent);
                    } else if (tankerEvent.success() == false) {
                        handleTankerRejected(tankerEvent);
                    }
                }
                default -> System.out.println("Evento no reconocido: " + eventType);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Maneja evento PaymentAuthorized - confirma la reserva según diagrama
     * BookingConfirmedEvent hace énfasis a paymentAuthorized
     */
    public void handlePaymentAuthorized(PaymentAuthorizedEvent event) {
        int bookingId = event.bookingId();
        String paymentId = event.paymentId();
        
        System.out.println("Pago autorizado para booking: " + bookingId + ", paymentId: " + paymentId);
        
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
        
        System.out.println("Pago falló para booking: " + bookingId + ", razón: " + reason);
        
        // Cancelar booking por fallo en el pago
        bookingService.cancelBooking(bookingId, reason, BookingCancelledEvent.PAYMENT_FAILED);
    }

    /**
     * Maneja evento TankerAssigned - actualiza estado cuando se asigna tanker
     */
    public void handleTankerAssigned(TankerAssignedEvent event) {
        int bookingId = event.bookingId();
        System.out.println("Tanker asignado para booking: " + bookingId);
        
        // El booking ya está confirmado, solo actualizamos logs o estado interno si es necesario
        Booking booking = bookingService.getBookingById(bookingId);
        System.out.println("Booking " + bookingId + " tiene tanker asignado. Estado actual: " + booking.getStatus());
    }

    /**
     * Maneja evento TankerRejected - cancela la reserva por rechazo de fleet
     */
    public void handleTankerRejected(TankerAssignedEvent event) {
        int bookingId = event.bookingId();
        System.out.println("Tanker rechazado para booking: " + bookingId);
        
        // Cancelar booking por rechazo del fleet service
        bookingService.cancelBooking(bookingId, "Fleet service rechazó la asignación", BookingCancelledEvent.TANKER_REJECTED);
    }
}
