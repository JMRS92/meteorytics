package meteorytics.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import meteorytics.exception.ApiConnectionException;
import meteorytics.exception.DataProcessingException;
import meteorytics.exception.InvalidLocationException;
import meteorytics.model.CurrentWeather;
import meteorytics.model.Forecast;
import meteorytics.model.HourlyWeather;
import meteorytics.service.WeatherService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador HTTP que gestiona los endpoints de la API meteorológica.
 * 
 * @author Meteorytics Team
 */
public class WeatherController implements HttpHandler {

    /** Servicio de lógica de negocio meteorológica */
    private final WeatherService weatherService;

    /**
     * Construye el controlador inyectando el servicio meteorológico.
     *
     * @param weatherService Instancia de {@link WeatherService}
     */
    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Permitir peticiones CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        URIQueryParams params = parseQueryParams(exchange.getRequestURI().getQuery());
        double lat = params.getDouble("lat", 40.4168); // Por defecto Madrid
        double lon = params.getDouble("lon", -3.7038);
        String name = params.getString("name", "Ubicación");

        try {
            Forecast forecast = weatherService.getFullForecast(name, lat, lon);
            String jsonResponse = toJson(forecast);
            sendJsonResponse(exchange, 200, jsonResponse);
        } catch (InvalidLocationException e) {
            sendJsonResponse(exchange, 400, String.format("{\"error\":\"%s\"}", e.getMessage()));
        } catch (ApiConnectionException e) {
            sendJsonResponse(exchange, 502, String.format("{\"error\":\"%s\"}", e.getMessage()));
        } catch (DataProcessingException e) {
            sendJsonResponse(exchange, 500, String.format("{\"error\":\"%s\"}", e.getMessage()));
        } catch (Exception e) {
            sendJsonResponse(exchange, 500, "{\"error\":\"Error interno en el servidor meteorológico.\"}");
        }
    }

    /**
     * Convierte el modelo Forecast a una cadena en formato JSON.
     *
     * @param forecast Objeto {@link Forecast} a serializar
     * @return Cadena de texto JSON
     */
    private String toJson(Forecast forecast) {
        CurrentWeather cur = forecast.getCurrent();
        
        // Calcular analíticas atmosféricas avanzadas
        double maxTemp = cur.getTemperature();
        double minTemp = cur.getTemperature();
        int maxRainProb = 0;
        for (HourlyWeather h : forecast.getHourly()) {
            if (h.getTemperature() > maxTemp) maxTemp = h.getTemperature();
            if (h.getTemperature() < minTemp) minTemp = h.getTemperature();
            if (h.getPrecipitationProbability() > maxRainProb) maxRainProb = h.getPrecipitationProbability();
        }

        double dewPoint = cur.getTemperature() - ((100.0 - cur.getHumidity()) / 5.0);
        double tempSpread = maxTemp - minTemp;
        double airDensity = (cur.getPressure() * 100) / (287.058 * (cur.getTemperature() + 273.15));
        int uvEstimate = Math.min(11, Math.max(1, (int)(maxTemp / 3.0)));

        String comfort = (cur.getTemperature() >= 18 && cur.getTemperature() <= 25 && cur.getHumidity() <= 60)
                ? "Optimal (Comfortable)"
                : (cur.getTemperature() > 25 ? "Warm / Humid" : "Cool / Cold");

        String rainRisk = maxRainProb > 70 ? "High (" + maxRainProb + "%)" : (maxRainProb > 30 ? "Moderate (" + maxRainProb + "%)" : "Low (" + maxRainProb + "%)");
        String windStatus = cur.getWindSpeed() > 30 ? "Strong Wind" : (cur.getWindSpeed() > 15 ? "Moderate Breeze" : "Calm");
        String baroStatus = cur.getPressure() >= 1013.25 ? "High Pressure (Stable)" : "Low Pressure (Unstable)";

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(String.format(java.util.Locale.ROOT,
                "\"location\":{\"name\":\"%s\",\"latitude\":%.4f,\"longitude\":%.4f},",
                forecast.getLocation().getName().replace("\"", "\\\""), forecast.getLocation().getLatitude(), forecast.getLocation().getLongitude()));
        
        sb.append(String.format(java.util.Locale.ROOT,
                "\"current\":{\"temperature\":%.1f,\"apparentTemperature\":%.1f,\"humidity\":%d,\"pressure\":%.1f,\"windSpeed\":%.1f},",
                cur.getTemperature(), cur.getApparentTemperature(), cur.getHumidity(), cur.getPressure(), cur.getWindSpeed()));
        
        sb.append(String.format(java.util.Locale.ROOT,
                "\"analytics\":{\"thermalComfort\":\"%s\",\"maxTemperature\":%.1f,\"minTemperature\":%.1f,\"tempSpread\":%.1f,\"dewPoint\":%.1f,\"airDensity\":%.3f,\"uvIndex\":%d,\"baroStatus\":\"%s\",\"rainRisk\":\"%s\",\"windStatus\":\"%s\"},",
                comfort, maxTemp, minTemp, tempSpread, dewPoint, airDensity, uvEstimate, baroStatus, rainRisk, windStatus));

        sb.append("\"hourly\":[");
        for (int i = 0; i < forecast.getHourly().size(); i++) {
            HourlyWeather h = forecast.getHourly().get(i);
            sb.append(String.format(java.util.Locale.ROOT,
                    "{\"time\":\"%s\",\"temperature\":%.1f,\"humidity\":%d,\"precipitationProbability\":%d,\"windSpeed\":%.1f}",
                    h.getTime(), h.getTemperature(), h.getHumidity(), h.getPrecipitationProbability(), h.getWindSpeed()));
            if (i < forecast.getHourly().size() - 1) sb.append(",");
        }
        sb.append("]}");
        return sb.toString();
    }

    /**
     * Envía una respuesta HTTP en formato JSON.
     *
     * @param exchange Objeto {@link HttpExchange}
     * @param statusCode Código de estado HTTP
     * @param jsonBody Cuerpo de la respuesta JSON
     * @throws IOException Si falla la escritura en la respuesta
     */
    private void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonBody) throws IOException {
        byte[] bytes = jsonBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * Parsea los parámetros de la Query String de la URI.
     */
    private URIQueryParams parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query != null && !query.isEmpty()) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length > 1) {
                    map.put(pair[0], pair[1]);
                }
            }
        }
        return new URIQueryParams(map);
    }

    /** Helper para extracción de parámetros */
    private static class URIQueryParams {
        private final Map<String, String> map;

        public URIQueryParams(Map<String, String> map) {
            this.map = map;
        }

        public double getDouble(String key, double defaultValue) {
            try {
                return map.containsKey(key) ? Double.parseDouble(map.get(key)) : defaultValue;
            } catch (Exception e) {
                return defaultValue;
            }
        }

        public String getString(String key, String defaultValue) {
            return map.getOrDefault(key, defaultValue);
        }
    }
}
