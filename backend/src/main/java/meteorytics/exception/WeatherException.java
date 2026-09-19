package meteorytics.exception;

/**
 * Excepción lanzada cuando ocurre un error genérico en el servicio
 * meteorológico.
 * 
 * @author Meteorytics Team
 */
public class WeatherException extends Exception {

    /**
     * Construye la excepción con un mensaje descriptivo.
     *
     * @param message Mensaje explicativo del error
     */
    public WeatherException(String message) {
        super(message);
    }
}
