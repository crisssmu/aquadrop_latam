package aquadrop_latam.api_gateway.filters;

import java.time.Duration;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Filtro custom para exigir Idempotency-Key y de-duplicar POST de reservas
 * Evita requests duplicados en 10 minutos
 */
@Component
@Slf4j
public class IdempotencyKeyGatewayFilterFactory extends AbstractGatewayFilterFactory<IdempotencyKeyGatewayFilterFactory.Config> {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    private static final Duration IDEMPOTENCY_TTL = Duration.ofMinutes(10);

    public IdempotencyKeyGatewayFilterFactory(ReactiveRedisTemplate<String, String> redisTemplate) {
        super(Config.class);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            HttpMethod method = exchange.getRequest().getMethod();
            String path = exchange.getRequest().getPath().value();

            // Solo aplicar a POST
            if (method != HttpMethod.POST) {
                return chain.filter(exchange);
            }

            String idempotencyKey = exchange.getRequest().getHeaders().getFirst("Idempotency-Key");
            
            // Exigir Idempotency-Key para booking y payment
            if ((path.startsWith("/api/bookings/") || path.startsWith("/api/payments/")) 
                && (idempotencyKey == null || idempotencyKey.isEmpty())) {
                log.warn("Missing Idempotency-Key header for POST {}", path);
                exchange.getResponse().setStatusCode(
                    org.springframework.http.HttpStatus.BAD_REQUEST
                );
                return exchange.getResponse().writeWith(
                    Mono.just(exchange.getResponse()
                        .bufferFactory()
                        .wrap("{\"error\":\"Idempotency-Key header is required\"}".getBytes()))
                );
            }

            if (idempotencyKey == null || idempotencyKey.isEmpty()) {
                return chain.filter(exchange);
            }

            // Validar idempotencia
            return validateIdempotency(idempotencyKey, exchange)
                .flatMap(isDuplicate -> {
                    if (isDuplicate) {
                        log.info("Idempotent request detected with key: {}", idempotencyKey);
                        exchange.getResponse().setStatusCode(
                            org.springframework.http.HttpStatus.OK
                        );
                        return exchange.getResponse().setComplete();
                    }
                    return chain.filter(exchange);
                });
        };
    }

    private Mono<Boolean> validateIdempotency(String idempotencyKey, ServerWebExchange exchange) {
        return exchange.getPrincipal()
            .map(p -> p.getName())
            .defaultIfEmpty("anonymous")
            .flatMap(userId -> {
                String key = "idempotency:" + userId + ":" + idempotencyKey;

                return redisTemplate.opsForValue().get(key)
                    .flatMap(existingValue -> {
                        // Ya existe en Redis, es un request duplicado
                        log.debug("Idempotency key already processed: {}", key);
                        return Mono.just(true);
                    })
                    .switchIfEmpty(
                        // Primera vez, guardar en Redis y retornar false (no es duplicado)
                        redisTemplate.opsForValue()
                            .set(key, "processed", IDEMPOTENCY_TTL)
                            .then(Mono.just(false))
                    );
            })
            .onErrorReturn(false);
    }

    public static class Config {
        // Configuración vacía
    }
}
