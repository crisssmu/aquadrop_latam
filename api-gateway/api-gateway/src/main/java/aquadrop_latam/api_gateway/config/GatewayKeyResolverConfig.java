package aquadrop_latam.api_gateway.config;

import java.net.InetSocketAddress;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Configuration
public class GatewayKeyResolverConfig {

    /**
     * Key resolver para rate limiting basado en user ID (OAuth2 'sub' claim)
     * Fallback: IP address del cliente
     */
    @Bean("userIdKeyResolver")
    @org.springframework.context.annotation.Primary
    public KeyResolver userIdKeyResolver() {
        return exchange -> ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Authentication::getName)
            .onErrorResume(e -> Mono.just(getRemoteAddress(exchange)))
            .switchIfEmpty(Mono.just(getRemoteAddress(exchange)));
    }

    /**
     * Key resolver para rate limiting específico de DISPATCHER
     * Solo aplica a usuarios con ROLE_DISPATCHER, otros usan "standard-user"
     */
    @Bean("dispatcherKeyResolver")
    public KeyResolver dispatcherKeyResolver() {
        return exchange -> ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .flatMap(auth -> {
                boolean isDispatcher = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_DISPATCHER"));
                
                if (isDispatcher) {
                    return Mono.just(auth.getName());
                } else {
                    return Mono.just("standard-user");
                }
            })
            .onErrorResume(e -> Mono.just("standard-user"))
            .switchIfEmpty(Mono.just("standard-user"));
    }

    /**
     * Key resolver basado en zona (para geo-fencing)
     * Extrae zona del query parameter 'zone'
     */
    @Bean("zoneKeyResolver")
    public KeyResolver zoneKeyResolver() {
        return exchange -> {
            String zone = exchange.getRequest().getQueryParams().getFirst("zone");
            if (zone != null && !zone.isEmpty()) {
                return Mono.just(zone);
            }
            return Mono.just("default-zone");
        };
    }

    /**
     * Método auxiliar para extraer IP remota
     */
    private String getRemoteAddress(ServerWebExchange exchange) {
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        if (remoteAddress != null) {
            return remoteAddress.getAddress().getHostAddress();
        }
        return "unknown";
    }
}
