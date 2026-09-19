package meteorytics;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.io.OutputStream;
import java.io.IOException;

/**
 * Clase principal y punto de entrada para la aplicación Meteorytics Backend.
 * <p>
 * Inicializa el servidor HTTP embebido del JDK {@link HttpServer} en el puerto configurado
 * y define los contextos/endpoints base de la aplicación.
 * </p>
 * 
 * @author Meteorytics Team
 * @version 1.0.0
 */
public class Main {
    
    /** Puerto por defecto en el que escucha el servidor HTTP */
    private static final int PORT = 8080;

    /**
     * Punto de entrada principal de la aplicación.
     *
     * @param args Argumentos de la línea de comandos
     * @throws IOException Si ocurre un error al iniciar el servidor HTTP
     */
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        // Endpoint de comprobación de estado de la API
        server.createContext("/api/health", exchange -> {
            String response = "{\"status\":\"UP\",\"service\":\"Meteorytics API\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Servidor Meteorytics ejecutándose en http://localhost:" + PORT);
    }
}
