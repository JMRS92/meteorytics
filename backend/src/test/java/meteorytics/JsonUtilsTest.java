package meteorytics;

import meteorytics.util.JsonUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Pruebas unitarias con JUnit 5 para la clase utilitaria JsonUtils.
 * 
 * @author Meteorytics Team
 */
public class JsonUtilsTest {

    @Test
    @DisplayName("Debe extraer valores numéricos y texto ignorando bloques de unidades")
    public void testJsonExtraction() {
        String json = "{\"current_units\":{\"temperature_2m\":\"°C\"},\"current\":{\"temperature_2m\":29.9,\"relative_humidity_2m\":21},\"city\":\"Madrid\"}";

        String currentBlock = JsonUtils.extractJsonObject(json, "current");
        assertEquals(29.9, JsonUtils.extractDouble(currentBlock, "temperature_2m", 0.0));
        assertEquals(21, JsonUtils.extractInt(currentBlock, "relative_humidity_2m", 0));
        assertEquals("Madrid", JsonUtils.extractString(json, "city"));
    }

    @Test
    @DisplayName("Debe extraer arrays numéricos y de texto del JSON")
    public void testArrayExtraction() {
        String json = "{\"hourly\":{\"time\":[\"00:00\",\"01:00\"],\"temperature_2m\":[20.5,19.8]}}";

        String hourlyBlock = JsonUtils.extractJsonObject(json, "hourly");
        List<String> times = JsonUtils.extractStringArray(hourlyBlock, "time");
        List<Double> temps = JsonUtils.extractDoubleArray(hourlyBlock, "temperature_2m");

        assertNotNull(times);
        assertEquals(2, times.size());
        assertEquals("00:00", times.get(0));

        assertNotNull(temps);
        assertEquals(2, temps.size());
        assertEquals(20.5, temps.get(0));
    }
}
