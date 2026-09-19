package meteorytics.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilidad liviana para extracción de valores JSON sin dependencias externas.
 * 
 * Permite obtener valores numéricos, cadenas de texto y arrays de objetos directamente de respuestas JSON.
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

    /**
     * Extrae los bloques JSON de un array denominado arrayKey (ej. "results": [{...}, {...}]).
     *
     * @param json Cadena de texto en formato JSON
     * @param arrayKey Clave del array JSON
     * @return Lista de bloques de objetos JSON encontrados
     */
    public static List<String> extractJsonArrayObjects(String json, String arrayKey) {
        List<String> objects = new ArrayList<>();
        try {
            String pattern = "\"" + arrayKey + "\":[";
            int index = json.indexOf(pattern);
            if (index == -1) return objects;

            int start = index + pattern.length();
            int depth = 0;
            int objStart = -1;

            for (int i = start; i < json.length(); i++) {
                char c = json.charAt(i);
                if (c == '{') {
                    if (depth == 0) objStart = i;
                    depth++;
                } else if (c == '}') {
                    depth--;
                    if (depth == 0 && objStart != -1) {
                        objects.add(json.substring(objStart, i + 1));
                        objStart = -1;
                    }
                } else if (c == ']' && depth == 0) {
                    break;
                }
            }
        } catch (Exception e) {
            // Retorna lo extraído hasta el momento si ocurre algún fallo de parsing
        }
        return objects;
    }
}
