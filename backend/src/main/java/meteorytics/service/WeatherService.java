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
 * Servicio principal de lógica de negocio para la gestión de datos meteorológicos y geocodificación.
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
     */
    public CurrentWeather getCurrentWeather(String name, double latitude, double longitude)
            throws InvalidLocationException, ApiConnectionException, DataProcessingException {
        return dataSource.getCurrentWeather(latitude, longitude);
    }

    /**
     * Obtiene el pronóstico completo (actual + por horas) para una ubicación.
     */
    public Forecast getFullForecast(String name, double latitude, double longitude)
            throws InvalidLocationException, ApiConnectionException, DataProcessingException {
        
        Location loc = new Location(name, latitude, longitude);
        CurrentWeather current = dataSource.getCurrentWeather(latitude, longitude);
        List<HourlyWeather> hourly = dataSource.getHourlyWeather(latitude, longitude);

        return new Forecast(loc, current, hourly);
    }

    /**
     * Busca ubicaciones geográficas coincidentes con el texto ingresado.
     */
    public List<Location> searchLocations(String query)
            throws ApiConnectionException, DataProcessingException {
        return dataSource.searchLocations(query);
    }
}
