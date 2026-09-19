package meteorytics.util;

/**
 * Utilidad liviana para extracción de valores JSON sin dependencias externas.
 * 
 * Permite obtener valores numéricos y cadenas de texto directamente de respuestas JSON.
 * 
 * @author Meteorytics Team
 */
public class JsonUtils {

    /**
     * Extrae un valor de punto flotante (double) dado una clave dentro de una cadena JSON.
     *
     * @param json Cadena de texto en formato JSON
     * @param key Nombre de la clave a buscar
     * @param defaultValue Valor por defecto si no se encuentra la clave
     * @return Valor numérico encontrado o defaultValue
     */
    public static double extractDouble(String json, String key, double defaultValue) {
        try {
            String pattern = "\"" + key + "\":";
            int index = json.indexOf(pattern);
            if (index == -1) return defaultValue;
            
            int start = index + pattern.length();
            while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == ':')) {
                start++;
            }
            
            int end = start;
            while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.' || json.charAt(end) == '-')) {
                end++;
            }
            
            String valStr = json.substring(start, end);
            return Double.parseDouble(valStr);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Extrae un valor entero (int) dado una clave dentro de una cadena JSON.
     *
     * @param json Cadena de texto en formato JSON
     * @param key Nombre de la clave a buscar
     * @param defaultValue Valor por defecto si no se encuentra la clave
     * @return Valor entero encontrado o defaultValue
     */
    public static int extractInt(String json, String key, int defaultValue) {
        return (int) extractDouble(json, key, defaultValue);
    }

    /**
     * Extrae una cadena de texto (String) dada una clave dentro de una cadena JSON.
     *
     * @param json Cadena de texto en formato JSON
     * @param key Nombre de la clave a buscar
     * @return Cadena de texto encontrada o null
     */
    public static String extractString(String json, String key) {
        try {
            String pattern = "\"" + key + "\":";
            int index = json.indexOf(pattern);
            if (index == -1) return null;
            
            int startQuote = json.indexOf("\"", index + pattern.length());
            if (startQuote == -1) return null;
            
            int endQuote = json.indexOf("\"", startQuote + 1);
            if (endQuote == -1) return null;
            
            return json.substring(startQuote + 1, endQuote);
        } catch (Exception e) {
            return null;
        }
    }
}
