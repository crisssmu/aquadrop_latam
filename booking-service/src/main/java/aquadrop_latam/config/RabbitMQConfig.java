package aquadrop_latam.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
// import com.rabbitmq.client.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {
    
    // Colas del BookingService
    public static final String BOOKING_QUEUE_STATUS= "booking.status.queue"; 
    public static final String BOOKING_QUEUE_EVENTS = "booking.events.queue";
    
    // Colas para recibir eventos de otros servicios
    public static final String PAYMENT_EVENTS_QUEUE = "payment.events.queue";
    public static final String FLEET_EVENTS_QUEUE = "fleet.events.queue";

    @Bean
    public Queue bookingQueueEvents() {
        return new Queue(BOOKING_QUEUE_EVENTS, true);
    }

    @Bean
    public Queue bookingQueueStatus() {
        return new Queue(BOOKING_QUEUE_STATUS, true);
    }
    
    @Bean
    public Queue paymentEventsQueue() {
        return new Queue(PAYMENT_EVENTS_QUEUE, true);
    }
    
    @Bean
    public Queue fleetEventsQueue() {
        return new Queue(FLEET_EVENTS_QUEUE, true);
    }
    
    // Colas para comandos hacia otros servicios
    @Bean
    public Queue paymentCommandsQueue() {
        return new Queue("payment.queue.commands", true);
    }
    
    @Bean
    public Queue paymentRefundQueue() {
        return new Queue("payment.queue.refund", true);
    }
    
    @Bean 
    public Queue fleetCommandsQueue() {
        return new Queue("fleet.queue.commands", true);
    }

    @Bean
public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
    RabbitAdmin admin = new RabbitAdmin(connectionFactory);
    admin.setAutoStartup(true);
    return admin;
}


    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setCreateMessageIds(true);
        return converter;
    }
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}   