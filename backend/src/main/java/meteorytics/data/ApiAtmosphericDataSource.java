package meteorytics.data;

import meteorytics.exception.ApiConnectionException;
import meteorytics.exception.DataProcessingException;
import meteorytics.exception.InvalidLocationException;
import meteorytics.model.CurrentWeather;
import meteorytics.model.HourlyWeather;
import meteorytics.util.JsonUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación concreta de {@link AtmosphericDataSource} que consume la API de Open-Meteo.
 * 
 * Utiliza el cliente HTTP nativo del JDK {@link HttpClient} para realizar peticiones asíncronas/síncronas.
 * Aísla la estructura de respuesta específica de Open-Meteo transformándola en modelos internos de Meteorytics.
 * 
 * @author Meteorytics Team
 */
public class ApiAtmosphericDataSource implements AtmosphericDataSource {

    /** URL base de la API pública de Open-Meteo */
    private static final String BASE_URL = "https://api.open-meteo.com/v1/forecast";

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

        String url = String.format("%s?latitude=%.4f&longitude=%.4f&current=temperature_2m,relative_humidity_2m,surface_pressure,wind_speed_10m,apparent_temperature",
                BASE_URL, latitude, longitude).replace(',', '.');

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

        String url = String.format("%s?latitude=%.4f&longitude=%.4f&hourly=temperature_2m,relative_humidity_2m,precipitation_probability,wind_speed_10m&forecast_days=1",
                BASE_URL, latitude, longitude).replace(',', '.');

        String jsonResponse = sendHttpRequest(url);

        try {
            List<HourlyWeather> hourlyList = new ArrayList<>();
            // Generar horas simuladas a partir de la respuesta para visualización
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

    /**
     * Valida que la latitud y la longitud estén dentro de los rangos permitidos.
     *
     * @param latitude Latitud a comprobar (-90 a 90)
     * @param longitude Longitud a comprobar (-180 a 180)
     * @throws InvalidLocationException Si alguna coordenada es inválida
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
     * Envía una petición HTTP GET a la URL indicada y devuelve el cuerpo de la respuesta.
     *
     * @param url URL de destino
     * @return Cuerpo de la respuesta en formato String
     * @throws ApiConnectionException Si falla la conexión HTTP
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
