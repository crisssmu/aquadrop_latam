package aquadrop_latam.bookingService.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import aquadrop_latam.bookingService.commands.CancelBookingCommand;
import aquadrop_latam.bookingService.commands.ConfirmeBookingCommand;
import aquadrop_latam.bookingService.commands.RequestBookingCommand;
import aquadrop_latam.bookingService.config.RabbitMQConfig;

@Service
public class CommandService {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Envía comando al PaymentService para autorizar pago
     * Según diagrama: BookingService -> PaymentService
     */
    public void sendCreateBooking(RequestBookingCommand command) {
        rabbitTemplate.convertAndSend("payment.queue.commands", command);
        System.out.println("Enviado RequestBookingCommand a PaymentService para booking: " + command.bookingId());
    }

    /**
     * Envía comando de cancelación/reembolso al PaymentService
     * Según diagrama: BookingService -> PaymentService (para reembolso)
     */
    public void sendCancelBooking(CancelBookingCommand command) {
        rabbitTemplate.convertAndSend("payment.queue.refund", command);
        System.out.println("Enviado CancelBookingCommand a PaymentService para reembolso: " + command.bookingId());
    }

    /**
     * Envía comando al FleetService para asignar tanker
     * Según diagrama: BookingService -> FleetService (después de pago autorizado)
     */
    public void sendConfirmeBooking(ConfirmeBookingCommand command) {
        rabbitTemplate.convertAndSend("fleet.queue.commands", command);
        System.out.println("Enviado ConfirmeBookingCommand a FleetService para asignar tanker: " + command.bookingId());
    }
    
    /**
     * Publica eventos del BookingService
     */
    public void publishBookingEvent(Object event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.BOOKING_QUEUE_EVENTS, event);
        System.out.println("Publicado evento: " + event.getClass().getSimpleName());
    }
}
