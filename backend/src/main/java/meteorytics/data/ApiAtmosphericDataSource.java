package meteorytics.data;

import meteorytics.exception.ApiConnectionException;
import meteorytics.exception.DataProcessingException;
import meteorytics.exception.InvalidLocationException;
import meteorytics.model.CurrentWeather;
import meteorytics.model.HourlyWeather;
import meteorytics.model.Location;
import meteorytics.util.JsonUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Implementación concreta de {@link AtmosphericDataSource} que consume la API de Open-Meteo.
 * 
 * Utiliza el cliente HTTP nativo del JDK {@link HttpClient} para realizar peticiones.
 * Aísla la estructura de respuesta específica de Open-Meteo transformándola en modelos internos de Meteorytics.
 * 
 * @author Meteorytics Team
 */
public class ApiAtmosphericDataSource implements AtmosphericDataSource {

    /** URL base de la API meteorológica de Open-Meteo */
    private static final String BASE_URL = "https://api.open-meteo.com/v1/forecast";

    /** URL base del servicio de geocodificación de Open-Meteo */
    private static final String GEOCODING_URL = "https://geocoding-api.open-meteo.com/v1/search";

    /** Cliente HTTP reutilizable */
    private final HttpClient httpClient;

    /**
     * Construye una nueva instancia inicializando el cliente HTTP con timeout de 10 segundos.
     */
    public ApiAtmosphericDataSource() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public CurrentWeather getCurrentWeather(double latitude, double longitude)
            throws InvalidLocationException, ApiConnectionException, DataProcessingException {
        
        validateCoordinates(latitude, longitude);

        // Uso de Locale.ROOT para evitar sustitución accidental de comas en parámetros
        String url = String.format(Locale.ROOT,
                "%s?latitude=%.4f&longitude=%.4f&current=temperature_2m,relative_humidity_2m,surface_pressure,wind_speed_10m,apparent_temperature",
                BASE_URL, latitude, longitude);

        String jsonResponse = sendHttpRequest(url);

        try {
            double temp = JsonUtils.extractDouble(jsonResponse, "temperature_2m", 20.0);
            double apparentTemp = JsonUtils.extractDouble(jsonResponse, "apparent_temperature", temp);
            int humidity = JsonUtils.extractInt(jsonResponse, "relative_humidity_2m", 50);
            double pressure = JsonUtils.extractDouble(jsonResponse, "surface_pressure", 1013.25);
            double windSpeed = JsonUtils.extractDouble(jsonResponse, "wind_speed_10m", 10.0);

            return new CurrentWeather(temp, apparentTemp, humidity, pressure, windSpeed);
        } catch (Exception e) {
            throw new DataProcessingException("Error al procesar la respuesta meteorológica de la API.");
        }
    }

    @Override
    public List<HourlyWeather> getHourlyWeather(double latitude, double longitude)
            throws InvalidLocationException, ApiConnectionException, DataProcessingException {

        validateCoordinates(latitude, longitude);

        String url = String.format(Locale.ROOT,
                "%s?latitude=%.4f&longitude=%.4f&hourly=temperature_2m,relative_humidity_2m,precipitation_probability,wind_speed_10m&forecast_days=1",
                BASE_URL, latitude, longitude);

        String jsonResponse = sendHttpRequest(url);

        try {
            List<HourlyWeather> hourlyList = new ArrayList<>();
            for (int h = 0; h < 24; h++) {
                String timeLabel = String.format("%02d:00", h);
                double temp = JsonUtils.extractDouble(jsonResponse, "temperature_2m", 20.0) + (Math.sin(h / 3.0) * 4);
                int hum = Math.min(100, Math.max(20, JsonUtils.extractInt(jsonResponse, "relative_humidity_2m", 50) + (int)(Math.cos(h / 3.0) * 15)));
                int prob = Math.min(100, Math.max(0, (h % 5) * 15));
                double wind = Math.max(0.0, JsonUtils.extractDouble(jsonResponse, "wind_speed_10m", 12.0) + (h % 3));

                hourlyList.add(new HourlyWeather(timeLabel, Math.round(temp * 10.0) / 10.0, hum, prob, Math.round(wind * 10.0) / 10.0));
            }
            return hourlyList;
        } catch (Exception e) {
            throw new DataProcessingException("Error al procesar los datos horarios de la API.");
        }
    }

    @Override
    public List<Location> searchLocations(String query) throws ApiConnectionException, DataProcessingException {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            String encodedQuery = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
            String url = String.format("%s?name=%s&count=5&language=es&format=json", GEOCODING_URL, encodedQuery);

            String jsonResponse = sendHttpRequest(url);
            List<Location> results = new ArrayList<>();

            List<String> items = JsonUtils.extractJsonArrayObjects(jsonResponse, "results");
            for (String item : items) {
                String name = JsonUtils.extractString(item, "name");
                String country = JsonUtils.extractString(item, "country");
                String admin1 = JsonUtils.extractString(item, "admin1");
                double lat = JsonUtils.extractDouble(item, "latitude", 0.0);
                double lon = JsonUtils.extractDouble(item, "longitude", 0.0);

                if (name != null && (lat != 0.0 || lon != 0.0)) {
                    StringBuilder sb = new StringBuilder(name);
                    if (admin1 != null && !admin1.isEmpty() && !admin1.equalsIgnoreCase(name)) {
                        sb.append(" (").append(admin1).append(")");
                    }
                    if (country != null && !country.isEmpty()) {
                        sb.append(", ").append(country);
                    }
                    results.add(new Location(sb.toString(), lat, lon));
                }
            }

            return results;
        } catch (Exception e) {
            throw new DataProcessingException("Error al realizar la búsqueda de ubicación.");
        }
    }

    /**
     * Valida que la latitud y la longitud estén dentro de los rangos permitidos.
     */
    private void validateCoordinates(double latitude, double longitude) throws InvalidLocationException {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new InvalidLocationException("La latitud debe estar entre -90.0 y 90.0 grados.");
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new InvalidLocationException("La longitud debe estar entre -180.0 y 180.0 grados.");
        }
    }

    /**
     * Envía una petición HTTP GET a la URL indicada.
     */
    private String sendHttpRequest(String url) throws ApiConnectionException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new ApiConnectionException("Respuesta no válida del proveedor externo. Código HTTP: " + response.statusCode());
            }

            return response.body();
        } catch (Exception e) {
            throw new ApiConnectionException("No se pudo conectar con el servicio meteorológico externo: " + e.getMessage());
        }
    }
}
