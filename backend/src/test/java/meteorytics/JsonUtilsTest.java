package meteorytics;

import meteorytics.util.JsonUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas unitarias con JUnit 5 para la clase utilitaria JsonUtils.
 * 
 * @author Meteorytics Team
 */
public class JsonUtilsTest {

    @Test
    @DisplayName("Debe extraer valores numéricos y texto de una cadena JSON")
    public void testJsonExtraction() {
        String json = "{\"temperature\": 19.5, \"humidity\": 65, \"city\": \"Madrid\"}";

        assertEquals(19.5, JsonUtils.extractDouble(json, "temperature", 0.0));
        assertEquals(65, JsonUtils.extractInt(json, "humidity", 0));
        assertEquals("Madrid", JsonUtils.extractString(json, "city"));
    }
}
