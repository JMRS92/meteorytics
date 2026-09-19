package meteorytics;

import meteorytics.model.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas unitarias con JUnit 5 para el modelo Location.
 * 
 * @author Meteorytics Team
 */
public class LocationTest {

    @Test
    @DisplayName("Debe instanciar correctamente la ubicación con sus campos")
    public void testLocationFields() {
        Location location = new Location("Barcelona", 41.3879, 2.1699);

        assertEquals("Barcelona", location.getName());
        assertEquals(41.3879, location.getLatitude());
        assertEquals(2.1699, location.getLongitude());
    }
}
