package aquadrop_latam.api_gateway.filters;

import java.util.UUID;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * Filtro custom que genera y propaga X-Correlation-Id para trazabilidad distribuida
 * Enlaza traceId y spanId de Brave/Micrometer para correlacionar logs
 */
@Component
public class CustomCorrelationGatewayFilterFactory extends AbstractGatewayFilterFactory<CustomCorrelationGatewayFilterFactory.Config> {

    public CustomCorrelationGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Generar o recuperar Correlation ID
            String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-Id");
            if (correlationId == null || correlationId.isEmpty()) {
                correlationId = UUID.randomUUID().toString();
            }

            final String finalCorrelationId = correlationId;
            final String traceId = extractTraceId(finalCorrelationId);

            // Agregar headers de correlación al request
            final ServerWebExchange mutatedExchange = exchange.mutate()
                .request(exchange.getRequest().mutate()
                    .header("X-Correlation-Id", finalCorrelationId)
                    .header("X-B3-TraceId", traceId)
                    .header("X-B3-SpanId", generateSpanId())
                    .build()
                )
                .build();

            // Propagar al response
            return chain.filter(mutatedExchange).then(Mono.fromRunnable(() -> {
                mutatedExchange.getResponse().getHeaders().add("X-Correlation-Id", finalCorrelationId);
                mutatedExchange.getResponse().getHeaders().add("X-B3-TraceId", traceId);
            }));
        };
    }

    private String extractTraceId(String correlationId) {
        // Usar los primeros 16 caracteres del UUID sin guiones para Zipkin
        return correlationId.replace("-", "").substring(0, 16);
    }

    private String generateSpanId() {
        // Generar un span ID de 16 caracteres hex para Zipkin
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    public static class Config {
        // Configuración vacía, filtro siempre activo
    }
}
