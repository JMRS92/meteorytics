package meteorytics.exception;

/**
 * Excepción lanzada cuando las coordenadas o el nombre de la ubicación no son válidos.
 * 
 * @author Meteorytics Team
 */
public class InvalidLocationException extends Exception {

    /**
     * Construye la excepción con un mensaje descriptivo.
     *
     * @param message Mensaje explicativo de la ubicación no válida
     */
    public InvalidLocationException(String message) {
        super(message);
    }
}
