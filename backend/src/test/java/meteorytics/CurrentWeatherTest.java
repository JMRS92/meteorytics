package meteorytics;

import meteorytics.model.CurrentWeather;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas unitarias con JUnit 5 para el modelo CurrentWeather.
 * 
 * @author Meteorytics Team
 */
public class CurrentWeatherTest {

    @Test
    @DisplayName("Debe instanciar las métricas meteorológicas actuales de forma correcta")
    public void testCurrentWeatherFields() {
        CurrentWeather weather = new CurrentWeather(25.4, 26.1, 55, 1013.2, 14.8);

        assertEquals(25.4, weather.getTemperature());
        assertEquals(26.1, weather.getApparentTemperature());
        assertEquals(55, weather.getHumidity());
        assertEquals(1013.2, weather.getPressure());
        assertEquals(14.8, weather.getWindSpeed());
    }
}
