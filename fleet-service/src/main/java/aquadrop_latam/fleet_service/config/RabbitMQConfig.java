package aquadrop_latam.fleet_service.config;

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

import aquadrop_latam.fleet_service.commands.ConfirmeBookingCommand;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    public static final String FLEET_COMMANDS_QUEUE = "fleet.queue.commands";
    public static final String FLEET_EVENTS_QUEUE = "fleet.events.queue";

    /**
     * Queue para recibir comandos del BookingService
     */
    @Bean
    public Queue fleetCommandsQueue() {
        return new Queue(FLEET_COMMANDS_QUEUE, true);
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
     * Con mapeo de tipos para recibir comandos del BookingService
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        
        // Configurar mapeo de tipos para aceptar clases de otros paquetes
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("*"); // Confiar en todos los paquetes
        
        // Mapear la clase del booking-service a la clase local
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put("aquadrop_latam.bookingService.commands.ConfirmeBookingCommand", 
                          ConfirmeBookingCommand.class);
        typeMapper.setIdClassMapping(idClassMapping);
        
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }
    
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
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
