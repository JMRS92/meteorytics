package meteorytics.model;

/**
 * Representa las condiciones atmosféricas actuales de una ubicación.
 * 
 * @author Meteorytics Team
 */
public class CurrentWeather {

    /** Temperatura actual en grados Celsius */
    private final double temperature;

    /** Sensación térmica en grados Celsius */
    private final double apparentTemperature;

    /** Humedad relativa del aire en porcentaje (0-100%) */
    private final int humidity;

    /** Presión atmosférica en hectopascales (hPa) */
    private final double pressure;

    /** Velocidad del viento en km/h */
    private final double windSpeed;

    /**
     * Construye las condiciones meteorológicas actuales.
     *
     * @param temperature Temperatura en °C
     * @param apparentTemperature Sensación térmica en °C
     * @param humidity Humedad relativa %
     * @param pressure Presión en hPa
     * @param windSpeed Velocidad del viento en km/h
     */
    public CurrentWeather(double temperature, double apparentTemperature, int humidity, double pressure, double windSpeed) {
        this.temperature = temperature;
        this.apparentTemperature = apparentTemperature;
        this.humidity = humidity;
        this.pressure = pressure;
        this.windSpeed = windSpeed;
    }

    /**
     * Obtiene la temperatura actual.
     *
     * @return Temperatura en °C
     */
    public double getTemperature() {
        return temperature;
    }

    /**
     * Obtiene la sensación térmica.
     *
     * @return Sensación térmica en °C
     */
    public double getApparentTemperature() {
        return apparentTemperature;
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
     * Obtiene la presión atmosférica.
     *
     * @return Presión en hPa
     */
    public double getPressure() {
        return pressure;
    }

    /**
     * Obtiene la velocidad del viento.
     *
     * @return Velocidad en km/h
     */
    public double getWindSpeed() {
        return windSpeed;
    }
}
