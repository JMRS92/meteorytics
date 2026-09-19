package meteorytics;

import meteorytics.data.ApiAtmosphericDataSource;
import meteorytics.exception.InvalidLocationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas unitarias con JUnit 5 para las validaciones del origen de datos.
 * 
 * @author Meteorytics Team
 */
public class AtmosphericDataSourceTest {

    @Test
    @DisplayName("Debe lanzar InvalidLocationException si la latitud está fuera de rango")
    public void testInvalidLatitudeThrowsException() {
        ApiAtmosphericDataSource dataSource = new ApiAtmosphericDataSource();

        assertThrows(InvalidLocationException.class, () -> {
            dataSource.getCurrentWeather(120.0, 0.0);
        });
    }

    @Test
    @DisplayName("Debe lanzar InvalidLocationException si la longitud está fuera de rango")
    public void testInvalidLongitudeThrowsException() {
        ApiAtmosphericDataSource dataSource = new ApiAtmosphericDataSource();

        assertThrows(InvalidLocationException.class, () -> {
            dataSource.getCurrentWeather(0.0, -200.0);
        });
    }
}
