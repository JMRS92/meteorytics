package meteorytics.data;

import meteorytics.exception.ApiConnectionException;
import meteorytics.exception.DataProcessingException;
import meteorytics.exception.InvalidLocationException;
import meteorytics.model.CurrentWeather;
import meteorytics.model.HourlyWeather;

import java.util.List;

/**
 * Interfaz que abstrae el origen de datos meteorológicos externos.
 * <p>
 * Aísla a la aplicación de proveedores específicos (como Open-Meteo o WeatherAPI),
 * garantizando la regla del 80% de abstracción en el núcleo de Meteorytics.
 * </p>
 * 
 * @author Meteorytics Team
 */
public interface AtmosphericDataSource {

    /**
     * Obtiene el clima actual para las coordenadas geográficas especificadas.
     *
     * @param latitude Latitud en grados decimales (-90.0 a 90.0)
     * @param longitude Longitud en grados decimales (-180.0 a 180.0)
     * @return Las condiciones meteorológicas actuales en objeto {@link CurrentWeather}
     * @throws InvalidLocationException Si las coordenadas están fuera de rango o son inválidas
     * @throws ApiConnectionException Si ocurre un fallo de comunicación con la API externa
     * @throws DataProcessingException Si falla la conversión o lectura de los datos recibidos
     */
    CurrentWeather getCurrentWeather(double latitude, double longitude)
            throws InvalidLocationException, ApiConnectionException, DataProcessingException;

    /**
     * Obtiene la previsión horaria para las coordenadas geográficas especificadas.
     *
     * @param latitude Latitud en grados decimales (-90.0 a 90.0)
     * @param longitude Longitud en grados decimales (-180.0 a 180.0)
     * @return Lista de registros horarios {@link HourlyWeather}
     * @throws InvalidLocationException Si las coordenadas están fuera de rango o son inválidas
     * @throws ApiConnectionException Si ocurre un fallo de comunicación con la API externa
     * @throws DataProcessingException Si falla la conversión o lectura de los datos recibidos
     */
    List<HourlyWeather> getHourlyWeather(double latitude, double longitude)
            throws InvalidLocationException, ApiConnectionException, DataProcessingException;
}
