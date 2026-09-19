package meteorytics;

import meteorytics.data.ApiAtmosphericDataSource;
import meteorytics.exception.InvalidLocationException;
import meteorytics.model.CurrentWeather;
import meteorytics.model.Location;
import meteorytics.util.JsonUtils;

/**
 * Suite de pruebas unitarias puras en Java para verificar la lógica del backend.
 * 
 * Ejecuta aserciones sobre modelos, utilidades de JSON y validaciones de dominio.
 * 
 * @author Meteorytics Team
 */
public class TestRunner {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   EJECUTANDO PRUEBAS UNITARIAS - METEORYTICS");
        System.out.println("==================================================");

        testLocationModel();
        testCurrentWeatherModel();
        testJsonUtilsExtraction();
        testInvalidCoordinatesValidation();

        System.out.println("--------------------------------------------------");
        System.out.printf("RESULTADO FINAL: %d Pasadas | %d Fallidas\n", passed, failed);
        System.out.println("==================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    /**
     * Prueba el modelo Location.
     */
    private static void testLocationModel() {
        try {
            Location loc = new Location("Madrid", 40.4168, -3.7038);
            assertEquals("Madrid", loc.getName(), "Location.getName()");
            assertEquals(40.4168, loc.getLatitude(), "Location.getLatitude()");
            assertEquals(-3.7038, loc.getLongitude(), "Location.getLongitude()");
            pass("testLocationModel");
        } catch (Throwable t) {
            fail("testLocationModel", t);
        }
    }

    /**
     * Prueba el modelo CurrentWeather.
     */
    private static void testCurrentWeatherModel() {
        try {
            CurrentWeather cw = new CurrentWeather(22.5, 23.0, 60, 1015.0, 12.5);
            assertEquals(22.5, cw.getTemperature(), "CurrentWeather.getTemperature()");
            assertEquals(23.0, cw.getApparentTemperature(), "CurrentWeather.getApparentTemperature()");
            assertEquals(60, cw.getHumidity(), "CurrentWeather.getHumidity()");
            assertEquals(1015.0, cw.getPressure(), "CurrentWeather.getPressure()");
            assertEquals(12.5, cw.getWindSpeed(), "CurrentWeather.getWindSpeed()");
            pass("testCurrentWeatherModel");
        } catch (Throwable t) {
            fail("testCurrentWeatherModel", t);
        }
    }

    /**
     * Prueba la extracción de datos con JsonUtils.
     */
    private static void testJsonUtilsExtraction() {
        try {
            String mockJson = "{\"temperature_2m\": 18.5, \"humidity\": 75, \"status\": \"ok\"}";
            double temp = JsonUtils.extractDouble(mockJson, "temperature_2m", 0.0);
            int hum = JsonUtils.extractInt(mockJson, "humidity", 0);
            String status = JsonUtils.extractString(mockJson, "status");

            assertEquals(18.5, temp, "JsonUtils.extractDouble()");
            assertEquals(75, hum, "JsonUtils.extractInt()");
            assertEquals("ok", status, "JsonUtils.extractString()");
            pass("testJsonUtilsExtraction");
        } catch (Throwable t) {
            fail("testJsonUtilsExtraction", t);
        }
    }

    /**
     * Prueba la validación de coordenadas inválidas en ApiAtmosphericDataSource.
     */
    private static void testInvalidCoordinatesValidation() {
        try {
            ApiAtmosphericDataSource ds = new ApiAtmosphericDataSource();
            try {
                ds.getCurrentWeather(100.0, 0.0); // Latitud > 90 inválida
                fail("testInvalidCoordinatesValidation", new AssertionError("Debería haber lanzado InvalidLocationException"));
            } catch (InvalidLocationException e) {
                pass("testInvalidCoordinatesValidation (latitud fuera de rango detectada correctamente)");
            }
        } catch (Throwable t) {
            fail("testInvalidCoordinatesValidation", t);
        }
    }

    private static void assertEquals(Object expected, Object actual, String testName) {
        if (!expected.equals(actual)) {
            throw new AssertionError(String.format("%s falló: se esperaba [%s] pero fue [%s]", testName, expected, actual));
        }
    }

    private static void assertEquals(double expected, double actual, String testName) {
        if (Math.abs(expected - actual) > 0.0001) {
            throw new AssertionError(String.format("%s falló: se esperaba [%f] pero fue [%f]", testName, expected, actual));
        }
    }

    private static void pass(String name) {
        passed++;
        System.out.println("  [✔ PASÓ] " + name);
    }

    private static void fail(String name, Throwable t) {
        failed++;
        System.out.println("  [❌ FALLÓ] " + name + " -> " + t.getMessage());
    }
}
