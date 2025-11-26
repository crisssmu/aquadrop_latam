package aquadrop_latam.api_gateway.filters;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Filtro custom para enrutamiento canary
 * Si X-Canary: true, enruta a booking-service-v2 usando predicate Weight
 */
@Component
@Slf4j
public class CanaryRouterGatewayFilterFactory extends AbstractGatewayFilterFactory<CanaryRouterGatewayFilterFactory.Config> {

    public CanaryRouterGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String canaryHeader = exchange.getRequest().getHeaders().getFirst("X-Canary");

            if ("true".equalsIgnoreCase(canaryHeader)) {
                log.info("Canary routing enabled, forwarding to v2 service");
                exchange.getAttributes().put("canary_enabled", true);
                exchange.getResponse().getHeaders().add("X-Routed-To", "v2");
            } else {
                exchange.getAttributes().put("canary_enabled", false);
                exchange.getResponse().getHeaders().add("X-Routed-To", "v1");
            }

            return chain.filter(exchange);
        };
    }

    public static class Config {
        // Configuración vacía
    }
}
