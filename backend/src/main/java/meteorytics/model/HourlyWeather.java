package meteorytics.model;

/**
 * Representa la previsión de condiciones atmosféricas para una hora específica.
 * 
 * @author Meteorytics Team
 */
public class HourlyWeather {

    /** Hora en formato ISO-8601 oHH:mm */
    private final String time;

    /** Temperatura prevista en °C */
    private final double temperature;

    /** Humedad relativa prevista en % */
    private final int humidity;

    /** Probabilidad de precipitación en % */
    private final int precipitationProbability;

    /** Velocidad del viento en km/h */
    private final double windSpeed;

    /**
     * Construye un registro de tiempo horario.
     *
     * @param time Hora registrada
     * @param temperature Temperatura en °C
     * @param humidity Humedad relativa %
     * @param precipitationProbability Probabilidad de lluvia %
     * @param windSpeed Velocidad del viento en km/h
     */
    public HourlyWeather(String time, double temperature, int humidity, int precipitationProbability, double windSpeed) {
        this.time = time;
        this.temperature = temperature;
        this.humidity = humidity;
        this.precipitationProbability = precipitationProbability;
        this.windSpeed = windSpeed;
    }

    /**
     * Obtiene la hora registrada.
     *
     * @return Marca temporal de la hora
     */
    public String getTime() {
        return time;
    }

    /**
     * Obtiene la temperatura.
     *
     * @return Temperatura en °C
     */
    public double getTemperature() {
        return temperature;
    }

    /**
     * Obtiene la humedad relativa.
     *
     * @return Porcentaje de humedad
     */
    public int getHumidity() {
        return humidity;
    }

    /**
     * Obtiene la probabilidad de lluvia.
     *
     * @return Porcentaje de probabilidad de lluvia
     */
    public int getPrecipitationProbability() {
        return precipitationProbability;
    }

    /**
     * Obtiene la velocidad del viento.
     *
     * @return Velocidad del viento en km/h
     */
    public double getWindSpeed() {
        return windSpeed;
    }
}
