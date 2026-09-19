package meteorytics.exception;

/**
 * Excepción lanzada cuando ocurre un error al procesar o parsear los datos recibidos.
 * 
 * @author Meteorytics Team
 */
public class DataProcessingException extends Exception {

    /**
     * Construye la excepción con un mensaje descriptivo.
     *
     * @param message Mensaje explicativo del error de procesamiento
     */
    public DataProcessingException(String message) {
        super(message);
    }
}
