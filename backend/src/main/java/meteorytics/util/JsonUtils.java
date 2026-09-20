package meteorytics.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilidad liviana para extracción de valores JSON sin dependencias externas.
 * 
 * Permite obtener objetos, valores numéricos, cadenas de texto y arrays directamente de respuestas JSON.
 * 
 * @author Meteorytics Team
 */
public class JsonUtils {

    /**
     * Extrae el bloque de objeto JSON (ej. "current": { ... }) dada su clave.
     *
     * @param json Cadena JSON completa
     * @param key Clave del objeto a extraer
     * @return Subcadena con el objeto JSON { ... } o el json completo si no se encuentra
     */
    public static String extractJsonObject(String json, String key) {
        try {
            String pattern = "\"" + key + "\":{";
            int index = json.indexOf(pattern);
            if (index == -1) {
                // Probar variante con espacio tras dos puntos
                pattern = "\"" + key + "\": {";
                index = json.indexOf(pattern);
                if (index == -1) return json;
            }

            int start = json.indexOf("{", index + key.length());
            if (start == -1) return json;

            int depth = 0;
            for (int i = start; i < json.length(); i++) {
                char c = json.charAt(i);
                if (c == '{') depth++;
                else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        return json.substring(start, i + 1);
                    }
                }
            }
        } catch (Exception ignored) {}
        return json;
    }

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
            
            // Si el valor es una cadena de texto (ej. unidades "°C"), se omite
            if (start < json.length() && json.charAt(start) == '"') {
                // Buscar siguiente ocurrencia si la primera era una unidad
                int nextIndex = json.indexOf(pattern, start);
                if (nextIndex != -1) {
                    start = nextIndex + pattern.length();
                    while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == ':')) {
                        start++;
                    }
                }
                if (start < json.length() && json.charAt(start) == '"') {
                    return defaultValue;
                }
            }

            int end = start;
            while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.' || json.charAt(end) == '-')) {
                end++;
            }
            
            if (start == end) return defaultValue;

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
     * Extrae un array de valores de punto flotante (double) de un JSON.
     *
     * @param json Cadena de texto JSON
     * @param key Clave del array (ej. "temperature_2m")
     * @return Lista de valores numéricos extraídos
     */
    public static List<Double> extractDoubleArray(String json, String key) {
        List<Double> list = new ArrayList<>();
        try {
            String pattern = "\"" + key + "\":[";
            int index = json.indexOf(pattern);
            if (index == -1) {
                pattern = "\"" + key + "\": [";
                index = json.indexOf(pattern);
                if (index == -1) return list;
            }

            int start = index + pattern.length();
            int end = json.indexOf("]", start);
            if (end == -1) return list;

            String arrayContent = json.substring(start, end);
            String[] tokens = arrayContent.split(",");
            for (String token : tokens) {
                try {
                    list.add(Double.parseDouble(token.trim()));
                } catch (NumberFormatException ignored) {}
            }
        } catch (Exception ignored) {}
        return list;
    }

    /**
     * Extrae un array de cadenas de texto (String) de un JSON.
     *
     * @param json Cadena de texto JSON
     * @param key Clave del array (ej. "time")
     * @return Lista de cadenas de texto extraídas
     */
    public static List<String> extractStringArray(String json, String key) {
        List<String> list = new ArrayList<>();
        try {
            String pattern = "\"" + key + "\":[";
            int index = json.indexOf(pattern);
            if (index == -1) {
                pattern = "\"" + key + "\": [";
                index = json.indexOf(pattern);
                if (index == -1) return list;
            }

            int start = index + pattern.length();
            int end = json.indexOf("]", start);
            if (end == -1) return list;

            String arrayContent = json.substring(start, end);
            String[] tokens = arrayContent.split(",");
            for (String token : tokens) {
                String clean = token.trim().replace("\"", "");
                if (!clean.isEmpty()) {
                    list.add(clean);
                }
            }
        } catch (Exception ignored) {}
        return list;
    }

    /**
     * Extrae los bloques JSON de un array de objetos denominado arrayKey.
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
            if (index == -1) {
                pattern = "\"" + arrayKey + "\": [";
                index = json.indexOf(pattern);
                if (index == -1) return objects;
            }

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
