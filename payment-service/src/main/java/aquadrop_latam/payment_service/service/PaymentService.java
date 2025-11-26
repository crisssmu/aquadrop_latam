package aquadrop_latam.payment_service.service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import aquadrop_latam.payment_service.commands.RequestBookingCommand;
import aquadrop_latam.payment_service.dtos.ChargeDto;
import aquadrop_latam.payment_service.dtos.PaymentIntentDto;
import aquadrop_latam.payment_service.events.BookingRequestedEvent;
import aquadrop_latam.payment_service.events.PaymentAuthorizedEvent;
import aquadrop_latam.payment_service.events.RefundIssuedEvent;
import aquadrop_latam.payment_service.models.Charge;
import aquadrop_latam.payment_service.models.PaymentIntent;
import aquadrop_latam.payment_service.models.PaymentProvider;
import aquadrop_latam.payment_service.models.PaymentStatus;
import aquadrop_latam.payment_service.repository.ChargeRepository;
import aquadrop_latam.payment_service.repository.PaymentIntentRepository;
import aquadrop_latam.payment_service.repository.SubsidyRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    private final PaymentIntentRepository paymentIntentRepository;
    private final ChargeRepository chargeRepository;
    private final SubsidyRepository subsidyRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public PaymentIntentDto createPaymentIntent(String bookingId, BigDecimal amount, 
                                                String zone, String customerId, String description) {
        PaymentIntent intent = PaymentIntent.builder()
            .id(UUID.randomUUID().toString())
            .bookingId(bookingId)
            .customerId(customerId)
            .amount(amount)
            .currency("USD")
            .zone(zone)
            .description(description)
            .status(PaymentStatus.PENDING)
            .build();
        
        PaymentIntent saved = paymentIntentRepository.save(intent);
        return mapToPaymentIntentDto(saved);
    }

    @Transactional
    public PaymentIntentDto createPaymentIntentFromBookingEvent(BookingRequestedEvent event) {
        logger.info("💳 Creando PaymentIntent desde BookingRequestedEvent para bookingId: {}", event.bookingId());
        
        PaymentIntent intent = PaymentIntent.builder()
            .id(UUID.randomUUID().toString())
            .bookingId(String.valueOf(event.bookingId()))
            .customerId(String.valueOf(event.userSub()))
            .amount(BigDecimal.valueOf(event.fare()))
            .currency("USD")
            .zone(event.zone())
            .description("Pago por entrega de " + event.volumeLiters() + "L en zona " + event.zone())
            .status(PaymentStatus.PENDING)
            .build();
        
        PaymentIntent saved = paymentIntentRepository.save(intent);
        logger.info("✅ PaymentIntent creado: id={}, amount={}", saved.getId(), saved.getAmount());
        
        return mapToPaymentIntentDto(saved);
    }

    @Transactional
    public PaymentIntentDto createPaymentIntentFromCommand(RequestBookingCommand command) {
        logger.info("💳 Creando PaymentIntent desde RequestBookingCommand para bookingId: {}", command.bookingId());
        
        PaymentIntent intent = PaymentIntent.builder()
            .id(UUID.randomUUID().toString())
            .bookingId(String.valueOf(command.bookingId()))
            .customerId(String.valueOf(command.userSub()))
            .amount(BigDecimal.valueOf(command.fare() != null ? command.fare() : 0f))
            .currency("USD")
            .zone(command.zone())
            .description("Pago por entrega de " + command.volumeLiters() + "L en zona " + command.zone())
            .status(PaymentStatus.PENDING)
            .build();
        
        PaymentIntent saved = paymentIntentRepository.save(intent);
        logger.info("✅ PaymentIntent creado desde comando: id={}, amount={}", saved.getId(), saved.getAmount());
        
        return mapToPaymentIntentDto(saved);
    }

    @Transactional
    public PaymentAuthorizedEvent authorizePaymentFromIntent(String paymentIntentId, PaymentProvider provider) {
        logger.info("🔐 Autorizando pago para PaymentIntent: {}", paymentIntentId);
        
        Optional<PaymentIntent> intentOpt = paymentIntentRepository.findById(paymentIntentId);
        if (intentOpt.isEmpty()) {
            throw new IllegalArgumentException("Payment intent not found: " + paymentIntentId);
        }
        
        PaymentIntent intent = intentOpt.get();
        Charge charge = Charge.builder()
            .id(UUID.randomUUID().toString())
            .paymentIntent(intent)
            .provider(provider)
            .status(PaymentStatus.AUTHORIZED)
            .authorizedAmount(intent.getAmount())
            .providerReference(UUID.randomUUID().toString())
            .build();
        
        Charge saved = chargeRepository.save(charge);
        intent.setCharge(saved);
        intent.setStatus(PaymentStatus.AUTHORIZED);
        paymentIntentRepository.save(intent);
        
        logger.info("✅ Pago autorizado: chargeId={}, bookingId={}", saved.getId(), intent.getBookingId());
        
        // Crear evento de pago autorizado
        PaymentAuthorizedEvent event = new PaymentAuthorizedEvent(
            saved.getId(),
            Integer.parseInt(intent.getBookingId()),
            intent.getId(),
            "AUTHORIZED"
        );
        
        // Publicar evento al broker
        rabbitTemplate.convertAndSend("payment.events.queue", event);
        logger.info("📤 Evento PaymentAuthorized publicado al broker");
        
        return event;
    }

    @Transactional
    public ChargeDto authorizePayment(String paymentIntentId, PaymentProvider provider) {
        Optional<PaymentIntent> intentOpt = paymentIntentRepository.findById(paymentIntentId);
        if (intentOpt.isEmpty()) {
            throw new IllegalArgumentException("Payment intent not found: " + paymentIntentId);
        }
        
        PaymentIntent intent = intentOpt.get();
        Charge charge = Charge.builder()
            .id(UUID.randomUUID().toString())
            .paymentIntent(intent)
            .provider(provider)
            .status(PaymentStatus.AUTHORIZED)
            .authorizedAmount(intent.getAmount())
            .providerReference(UUID.randomUUID().toString())
            .build();
        
        Charge saved = chargeRepository.save(charge);
        intent.setCharge(saved);
        intent.setStatus(PaymentStatus.AUTHORIZED);
        paymentIntentRepository.save(intent);
        
        return mapToChargeDto(saved);
    }

    @Transactional
    public ChargeDto capturePayment(String chargeId, BigDecimal capturedAmount) {
        Optional<Charge> chargeOpt = chargeRepository.findById(chargeId);
        if (chargeOpt.isEmpty()) {
            throw new IllegalArgumentException("Charge not found: " + chargeId);
        }
        
        Charge charge = chargeOpt.get();
        charge.setCapturedAmount(capturedAmount);
        charge.setStatus(PaymentStatus.CAPTURED);
        
        Charge saved = chargeRepository.save(charge);
        PaymentIntent intent = saved.getPaymentIntent();
        intent.setStatus(PaymentStatus.CAPTURED);
        paymentIntentRepository.save(intent);
        
        return mapToChargeDto(saved);
    }

    public PaymentIntentDto queryPaymentStatus(String paymentIntentId) {
        Optional<PaymentIntent> intentOpt = paymentIntentRepository.findById(paymentIntentId);
        return intentOpt.map(this::mapToPaymentIntentDto)
            .orElseThrow(() -> new IllegalArgumentException("Payment intent not found: " + paymentIntentId));
    }

    private PaymentIntentDto mapToPaymentIntentDto(PaymentIntent intent) {
        return PaymentIntentDto.builder()
            .id(intent.getId())
            .bookingId(intent.getBookingId())
            .customerId(intent.getCustomerId())
            .amount(intent.getAmount())
            .currency(intent.getCurrency())
            .zone(intent.getZone())
            .status(intent.getStatus().toString())
            .description(intent.getDescription())
            .build();
    }

    private ChargeDto mapToChargeDto(Charge charge) {
        return ChargeDto.builder()
            .id(charge.getId())
            .paymentIntentId(charge.getPaymentIntent().getId())
            .provider(charge.getProvider().toString())
            .providerReference(charge.getProviderReference())
            .authorizedAmount(charge.getAuthorizedAmount())
            .capturedAmount(charge.getCapturedAmount())
            .status(charge.getStatus().toString())
            .build();
    }

    /**
     * Procesa compensación: emite RefundIssuedEvent cuando la asignación de tanquero falla
     * Esto es parte del patrón Saga de compensación
     */
    @Transactional
    public void processRefundForFailedAssignment(Integer bookingId) {
        logger.warn("🔄 Procesando compensación: Refund para booking fallido: {}", bookingId);
        
        try {
            // Buscar el PaymentIntent asociado al booking usando findAll y filtrado manual
            Optional<PaymentIntent> intentOpt = null;
            for (PaymentIntent pi : paymentIntentRepository.findAll()) {
                if (pi.getBookingId().equals(String.valueOf(bookingId))) {
                    intentOpt = Optional.of(pi);
                    break;
                }
            }
            
            if (intentOpt == null || intentOpt.isEmpty()) {
                logger.error("❌ No se encontró PaymentIntent para booking: {}", bookingId);
                return;
            }
            
            PaymentIntent intent = intentOpt.get();
            
            // Cambiar estado a REFUNDED
            intent.setStatus(PaymentStatus.REFUNDED);
            paymentIntentRepository.save(intent);
            logger.info("✅ PaymentIntent marcado como REFUNDED para booking: {}", bookingId);
            
            // Crear evento RefundIssuedEvent
            RefundIssuedEvent refundEvent = new RefundIssuedEvent(
                    bookingId,
                    Integer.parseInt(intent.getId()),
                    intent.getAmount().floatValue(),
                    "Asignación de tanquero falló - Compensación iniciada",
                    "REFUND_ISSUED"
            );
            
            // Publicar evento al broker para que BookingService lo procese
            logger.info("📤 Publicando RefundIssuedEvent para booking: {} - Monto: {}", 
                       bookingId, intent.getAmount());
            rabbitTemplate.convertAndSend("payment.events.queue", refundEvent);
            logger.info("✅ RefundIssuedEvent publicado exitosamente - Compensación iniciada");
            
        } catch (Exception e) {
            logger.error("❌ Error al procesar refund para booking {}: {}", bookingId, e.getMessage(), e);
        }
    }
}
