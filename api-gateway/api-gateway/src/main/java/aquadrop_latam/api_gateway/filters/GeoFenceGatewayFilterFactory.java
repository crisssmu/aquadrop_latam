package aquadrop_latam.api_gateway.filters;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.geo.Point;
import org.springframework.data.geo.Polygon;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Filtro custom para validar que el payload de booking esté dentro de zonas permitidas
 * Valida ubicaciones contra tabla de polígonos de zonas
 */
@Component
@Slf4j
public class GeoFenceGatewayFilterFactory extends AbstractGatewayFilterFactory<GeoFenceGatewayFilterFactory.Config> {

    private final ObjectMapper objectMapper;
    private final Map<String, Polygon> geoFences;

    public GeoFenceGatewayFilterFactory() {
        super(Config.class);
        this.objectMapper = new ObjectMapper();
        this.geoFences = initializeGeoFences();
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

            return extractBookingLocation(exchange)
                .flatMap(location -> {
                    if (location == null) {
                        return chain.filter(exchange);
                    }

                    Object zoneObj = location.get("zone");
                    Object latObj = location.get("latitude");
                    Object lonObj = location.get("longitude");

                    if (zoneObj == null || latObj == null || lonObj == null) {
                        log.warn("Missing location fields in booking request");
                        return chain.filter(exchange);
                    }

                    String zone = zoneObj.toString();
                    Double latitude = convertToDouble(latObj);
                    Double longitude = convertToDouble(lonObj);

                    if (latitude == null || longitude == null) {
                        log.warn("Invalid latitude or longitude values");
                        exchange.getResponse().setStatusCode(
                            org.springframework.http.HttpStatus.BAD_REQUEST
                        );
                        return exchange.getResponse().writeWith(
                            Mono.just(exchange.getResponse()
                                .bufferFactory()
                                .wrap("{\"error\":\"Invalid coordinates\"}".getBytes()))
                        );
                    }

                    if (!isLocationAllowed(zone, latitude, longitude)) {
                        log.warn("Booking location outside geofence: zone={}, lat={}, lon={}", 
                            zone, latitude, longitude);
                        exchange.getResponse().setStatusCode(
                            org.springframework.http.HttpStatus.FORBIDDEN
                        );
                        return exchange.getResponse().writeWith(
                            Mono.just(exchange.getResponse()
                                .bufferFactory()
                                .wrap("{\"error\":\"Location outside service area\"}".getBytes()))
                        );
                    }

                    return chain.filter(exchange);
                })
                .onErrorResume(ex -> {
                    log.error("Error validating geofence", ex);
                    return chain.filter(exchange);
                });
        };
    }

    private Mono<Map<String, Object>> extractBookingLocation(ServerWebExchange exchange) {
        return DataBufferUtils.join(exchange.getRequest().getBody())
            .flatMap(dataBuffer -> {
                try {
                    String body = dataBuffer.toString(java.nio.charset.StandardCharsets.UTF_8);
                    DataBufferUtils.release(dataBuffer);
                    
                    if (body == null || body.isEmpty()) {
                        return Mono.just(null);
                    }

                    JsonNode jsonNode = objectMapper.readTree(body);
                    Map<String, Object> location = new HashMap<>();

                    // Extraer zona
                    if (jsonNode.has("zone")) {
                        location.put("zone", jsonNode.get("zone").asText());
                    }

                    // Extraer latitude
                    if (jsonNode.has("latitude")) {
                        location.put("latitude", jsonNode.get("latitude").asDouble());
                    }

                    // Extraer longitude
                    if (jsonNode.has("longitude")) {
                        location.put("longitude", jsonNode.get("longitude").asDouble());
                    }

                    // También soportar "address" con coordenadas anidadas
                    if (jsonNode.has("address")) {
                        JsonNode addressNode = jsonNode.get("address");
                        if (addressNode.has("zone")) {
                            location.put("zone", addressNode.get("zone").asText());
                        }
                        if (addressNode.has("latitude")) {
                            location.put("latitude", addressNode.get("latitude").asDouble());
                        }
                        if (addressNode.has("longitude")) {
                            location.put("longitude", addressNode.get("longitude").asDouble());
                        }
                    }

                    if (location.isEmpty()) {
                        return Mono.just(null);
                    }

                    return Mono.just(location);
                } catch (IOException | IllegalArgumentException e) {
                    log.error("Error extracting booking location from request body", e);
                    DataBufferUtils.release(dataBuffer);
                    return Mono.just(null);
                }
            })
            .switchIfEmpty(Mono.just(null));
    }

    private Double convertToDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Double d) {
            return d;
        }
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        try {
            return Double.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean isLocationAllowed(String zone, Double latitude, Double longitude) {
        if (zone == null || !geoFences.containsKey(zone)) {
            log.warn("Zone not found: {}", zone);
            return false;
        }

        Polygon polygon = geoFences.get(zone);
        Point point = new Point(longitude, latitude);

        // Verificar si el punto está dentro del polígono
        return pointInPolygon(point, polygon);
    }

    private boolean pointInPolygon(Point point, Polygon polygon) {
        // Implementación del algoritmo Ray Casting
        List<Point> points = polygon.getPoints();
        int n = points.size();
        boolean inside = false;

        for (int i = 0, j = n - 1; i < n; j = i++) {
            Point pi = points.get(i);
            Point pj = points.get(j);

            if ((pi.getY() > point.getY()) != (pj.getY() > point.getY()) &&
                point.getX() < (pj.getX() - pi.getX()) * (point.getY() - pi.getY()) / 
                               (pj.getY() - pi.getY()) + pi.getX()) {
                inside = !inside;
            }
        }

        return inside;
    }

    private Map<String, Polygon> initializeGeoFences() {
        Map<String, Polygon> fences = new HashMap<>();

        // Zona Bogotá (ejemplo)
        fences.put("bogota", new Polygon(List.of(
            new Point(-74.0, 4.5),
            new Point(-74.0, 4.9),
            new Point(-73.6, 4.9),
            new Point(-73.6, 4.5),
            new Point(-74.0, 4.5)
        )));

        // Zona Medellín (ejemplo)
        fences.put("medellin", new Polygon(List.of(
            new Point(-75.6, 6.1),
            new Point(-75.6, 6.3),
            new Point(-75.4, 6.3),
            new Point(-75.4, 6.1),
            new Point(-75.6, 6.1)
        )));

        // Zona Lima (ejemplo)
        fences.put("lima", new Polygon(List.of(
            new Point(-77.0, -12.0),
            new Point(-77.0, -11.9),
            new Point(-76.9, -11.9),
            new Point(-76.9, -12.0),
            new Point(-77.0, -12.0)
        )));

        return fences;
    }

    public static class Config {
        // Configuración vacía
    }
}
