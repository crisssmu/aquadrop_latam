package aquadrop_latam.bookingService.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test-rabbit")
public class TestRabbitController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostMapping("/send")
    public String sendTestMessage(@RequestParam String message) {
        // Cambia "booking.events.queue" por el nombre de la cola que quieras probar
        rabbitTemplate.convertAndSend("booking.events.queue", message);
        return "Mensaje enviado: " + message;
    }
}

