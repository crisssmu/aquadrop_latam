package aquadrop_latam.fleet_service.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PAYMENT_EVENTS_QUEUE = "payment.events.queue";
    public static final String FLEET_EVENTS_QUEUE = "fleet.events.queue";

    /**
     * Queue para recibir eventos de PaymentAuthorizedEvent del PaymentService
     */
    @Bean
    public Queue paymentEventsQueue() {
        return new Queue(PAYMENT_EVENTS_QUEUE, true);
    }

    /**
     * Queue para enviar eventos de TankerAssignedEvent
     */
    @Bean
    public Queue fleetEventsQueue() {
        return new Queue(FLEET_EVENTS_QUEUE, true);
    }

    /**
     * Configurar Jackson como convertidor de mensajes JSON
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
