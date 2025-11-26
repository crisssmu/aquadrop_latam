package aquadrop_latam.api_gateway.filters;

import java.time.Duration;
import java.time.LocalDate;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Filtro custom para validar cuotas diarias por usuario/zona en POST /api/bookings
 * Responde 429 si se excede la cuota
 */
@Component
@Slf4j
public class QuotaPolicyGatewayFilterFactory extends AbstractGatewayFilterFactory<QuotaPolicyGatewayFilterFactory.Config> {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    // Cuota diaria: 5 bookings por usuario/zona
    private static final int DAILY_QUOTA = 5;
    private static final Duration QUOTA_TTL = Duration.ofHours(24);

    public QuotaPolicyGatewayFilterFactory(ReactiveRedisTemplate<String, String> redisTemplate) {
        super(Config.class);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            HttpMethod method = exchange.getRequest().getMethod();
            String path = exchange.getRequest().getPath().value();

            // Solo validar POST en /api/bookings/**
            if (method != HttpMethod.POST || !path.startsWith("/api/bookings/")) {
                return chain.filter(exchange);
            }

            return getUserIdAndZone(exchange)
                .flatMap(userZone -> validateQuota(userZone[0], userZone[1])
                    .flatMap(isAllowed -> {
                        if (!isAllowed) {
                            log.warn("Quota exceeded for user: {} in zone: {}", userZone[0], userZone[1]);
                            exchange.getResponse().setStatusCode(
                                org.springframework.http.HttpStatus.TOO_MANY_REQUESTS
                            );
                            return exchange.getResponse().writeWith(
                                Mono.just(exchange.getResponse()
                                    .bufferFactory()
                                    .wrap("{\"error\":\"Daily quota exceeded for this zone\"}".getBytes()))
                            );
                        }
                        return chain.filter(exchange);
                    })
                )
                .onErrorResume(ex -> {
                    log.error("Error validating quota policy", ex);
                    return chain.filter(exchange);
                });
        };
    }

    private Mono<String[]> getUserIdAndZone(ServerWebExchange exchange) {
        String userIdHeader = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        
        Mono<String> userIdMono;
        if (userIdHeader != null && !userIdHeader.isEmpty()) {
            userIdMono = Mono.just(userIdHeader);
        } else {
            // Obtener del principal (OAuth2)
            userIdMono = exchange.getPrincipal()
                .map(p -> p.getName())
                .defaultIfEmpty("anonymous");
        }

        String zone = exchange.getRequest().getQueryParams().getFirst("zone");
        if (zone == null || zone.isEmpty()) {
            zone = "default";
        }

        final String finalZone = zone;
        return userIdMono.map(userId -> new String[]{userId, finalZone});
    }

    private Mono<Boolean> validateQuota(String userId, String zone) {
        String key = "quota:" + userId + ":" + zone + ":" + LocalDate.now();

        return redisTemplate.opsForValue().get(key)
            .switchIfEmpty(Mono.just("0"))
            .flatMap(countStr -> {
                try {
                    int count = Integer.parseInt(countStr);
                    
                    if (count >= DAILY_QUOTA) {
                        log.debug("Quota limit reached for user: {}, zone: {}, count: {}", userId, zone, count);
                        return Mono.just(false);
                    }

                    // Incrementar contador
                    return redisTemplate.opsForValue()
                        .increment(key)
                        .flatMap(newCount -> {
                            if (newCount == 1) {
                                // Primera vez, establecer TTL
                                return redisTemplate.expire(key, QUOTA_TTL)
                                    .then(Mono.just(true));
                            }
                            return Mono.just(true);
                        });
                } catch (NumberFormatException e) {
                    log.error("Error parsing quota count for key: {}", key, e);
                    return Mono.just(true); // Permitir si hay error
                }
            });
    }

    public static class Config {
        // Configuración vacía
    }
}
