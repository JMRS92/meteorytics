package meteorytics.model;

import java.util.List;

/**
 * Representa el conjunto completo de predicciones meteorológicas para una ubicación.
 * 
 * @author Meteorytics Team
 */
public class Forecast {

    /** Ubicación asociada al pronóstico */
    private final Location location;

    /** Datos atmosféricos actuales */
    private final CurrentWeather current;

    /** Lista de evoluciones por horas */
    private final List<HourlyWeather> hourly;

    /**
     * Construye la predicción meteorológica completa.
     *
     * @param location Ubicación consultada
     * @param current Condiciones actuales
     * @param hourly Lista de predicciones horarias
     */
    public Forecast(Location location, CurrentWeather current, List<HourlyWeather> hourly) {
        this.location = location;
        this.current = current;
        this.hourly = hourly;
    }

    /**
     * Obtiene la ubicación del pronóstico.
     *
     * @return Objeto {@link Location}
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Obtiene el clima actual.
     *
     * @return Objeto {@link CurrentWeather}
     */
    public CurrentWeather getCurrent() {
        return current;
    }

    /**
     * Obtiene la lista de evoluciones horarias.
     *
     * @return Lista no nula de {@link HourlyWeather}
     */
    public List<HourlyWeather> getHourly() {
        return hourly;
    }
}
