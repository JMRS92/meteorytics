package meteorytics.model;

/**
 * Representa una ubicación geográfica definida por nombre, latitud y longitud.
 * 
 * @author Meteorytics Team
 */
public class Location {
    
    /** Nombre de la ubicación (ej. Madrid, Barcelona) */
    private final String name;

    /** Latitud en grados decimales (-90.0 a 90.0) */
    private final double latitude;

    /** Longitud en grados decimales (-180.0 a 180.0) */
    private final double longitude;

    /**
     * Construye una nueva ubicación geográfica.
     *
     * @param name Nombre de la ubicación
     * @param latitude Latitud geográfica
     * @param longitude Longitud geográfica
     */
    public Location(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Obtiene el nombre de la ubicación.
     *
     * @return Nombre de la ciudad o zona
     */
    public String getName() {
        return name;
    }

    /**
     * Obtiene la latitud de la ubicación.
     *
     * @return Latitud en grados decimales
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Obtiene la longitud de la ubicación.
     *
     * @return Longitud en grados decimales
     */
    public double getLongitude() {
        return longitude;
    }
}
