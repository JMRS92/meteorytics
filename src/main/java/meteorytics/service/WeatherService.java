package meteorytics.service;

import meteorytics.data.AtmosphericDataSource;
import meteorytics.exception.ApiConnectionException;
import meteorytics.exception.DataProcessingException;
import meteorytics.exception.InvalidLocationException;
import meteorytics.model.CurrentWeather;
import meteorytics.model.Forecast;
import meteorytics.model.HourlyWeather;
import meteorytics.model.Location;

import java.util.List;

/**
 * Servicio principal de lógica de negocio para la gestión de datos meteorológicos.
 * 
 * @author Meteorytics Team
 */
public class WeatherService {

    /** Fuente de datos atmosféricos (abstraída por interfaz) */
    private final AtmosphericDataSource dataSource;

    /**
     * Construye el servicio inyectando la fuente de datos deseada.
     *
     * @param dataSource Implementación de {@link AtmosphericDataSource}
     */
    public WeatherService(AtmosphericDataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Obtiene la información meteorológica actual para las coordenadas dadas.
     *
     * @param name Nombre o etiqueta de la ubicación
     * @param latitude Latitud geográfica
     * @param longitude Longitud geográfica
     * @return Objeto {@link CurrentWeather}
     * @throws InvalidLocationException Si las coordenadas no son válidas
     * @throws ApiConnectionException Si falla la comunicación externa
     * @throws DataProcessingException Si falla el procesamiento de datos
     */
    public CurrentWeather getCurrentWeather(String name, double latitude, double longitude)
            throws InvalidLocationException, ApiConnectionException, DataProcessingException {
        return dataSource.getCurrentWeather(latitude, longitude);
    }

    /**
     * Obtiene el pronóstico completo (actual + por horas) para una ubicación.
     *
     * @param name Nombre de la ubicación
     * @param latitude Latitud geográfica
     * @param longitude Longitud geográfica
     * @return Objeto {@link Forecast} con toda la información
     * @throws InvalidLocationException Si las coordenadas son inválidas
     * @throws ApiConnectionException Si falla la conexión externa
     * @throws DataProcessingException Si falla el procesamiento de datos
     */
    public Forecast getFullForecast(String name, double latitude, double longitude)
            throws InvalidLocationException, ApiConnectionException, DataProcessingException {
        
        Location loc = new Location(name, latitude, longitude);
        CurrentWeather current = dataSource.getCurrentWeather(latitude, longitude);
        List<HourlyWeather> hourly = dataSource.getHourlyWeather(latitude, longitude);

        return new Forecast(loc, current, hourly);
    }
}
