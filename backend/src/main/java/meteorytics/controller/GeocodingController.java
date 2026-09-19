package meteorytics.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import meteorytics.model.Location;
import meteorytics.service.WeatherService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Controlador HTTP para el servicio de búsqueda de ubicaciones (Geocodificación).
 * 
 * @author Meteorytics Team
 */
public class GeocodingController implements HttpHandler {

    /** Servicio de lógica de negocio */
    private final WeatherService weatherService;

    /**
     * Construye el controlador de geocodificación.
     *
     * @param weatherService Instancia de {@link WeatherService}
     */
    public GeocodingController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String queryStr = exchange.getRequestURI().getQuery();
        String query = "";
        if (queryStr != null && queryStr.contains("q=")) {
            for (String param : queryStr.split("&")) {
                if (param.startsWith("q=")) {
                    query = URLDecoder.decode(param.substring(2), StandardCharsets.UTF_8);
                }
            }
        }

        try {
            List<Location> locations = weatherService.searchLocations(query);
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < locations.size(); i++) {
                Location loc = locations.get(i);
                sb.append(String.format(Locale.ROOT,
                        "{\"name\":\"%s\",\"latitude\":%.4f,\"longitude\":%.4f}",
                        loc.getName().replace("\"", "\\\""), loc.getLatitude(), loc.getLongitude()));
                if (i < locations.size() - 1) sb.append(",");
            }
            sb.append("]");

            byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } catch (Exception e) {
            String errJson = "{\"error\":\"Error al buscar ubicaciones.\"}";
            byte[] bytes = errJson.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(500, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}
