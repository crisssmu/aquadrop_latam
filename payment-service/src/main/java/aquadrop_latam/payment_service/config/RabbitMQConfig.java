package aquadrop_latam.payment_service.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import aquadrop_latam.payment_service.commands.RequestBookingCommand;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    // Colas para recibir eventos de BookingService
    public static final String BOOKING_EVENTS_QUEUE = "booking.events.queue";
    
    // Cola para recibir comandos de BookingService
    public static final String PAYMENT_COMMANDS_QUEUE = "payment.queue.commands";
    
    // Colas para enviar eventos de PaymentService
    public static final String PAYMENT_EVENTS_QUEUE = "payment.events.queue";

    @Bean
    public Queue bookingEventsQueue() {
        return new Queue(BOOKING_EVENTS_QUEUE, true);
    }

    @Bean
    public Queue paymentCommandsQueue() {
        return new Queue(PAYMENT_COMMANDS_QUEUE, true);
    }

    @Bean
    public Queue paymentEventsQueue() {
        return new Queue(PAYMENT_EVENTS_QUEUE, true);
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
        
        // Configurar mapeo de tipos para aceptar comandos del BookingService
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("*");
        
        // Mapear la clase del booking-service a la clase local
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put("aquadrop_latam.bookingService.commands.RequestBookingCommand", 
                          RequestBookingCommand.class);
        typeMapper.setIdClassMapping(idClassMapping);
        
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory factory = 
            new org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }
}
