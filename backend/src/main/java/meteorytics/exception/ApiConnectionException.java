package meteorytics.exception;

/**
 * Excepción lanzada cuando falla la conexión HTTP con el proveedor externo de
 * atos meteorológicos.
 * 
 * @author Meteorytics Team
 */
public class ApiConnectionException extends Exception {

    /**
     * Construye una excepción de conexión a API con un mensaje de detalle.
     *
     * @param message Mensaje descriptivo del fallo de conexión
     */
    public ApiConnectionException(String message) {
        super(message);
    }
}
