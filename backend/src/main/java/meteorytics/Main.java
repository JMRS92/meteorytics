package meteorytics;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import meteorytics.controller.WeatherController;
import meteorytics.data.ApiAtmosphericDataSource;
import meteorytics.data.AtmosphericDataSource;
import meteorytics.service.WeatherService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;

/**
 * Clase principal y punto de entrada para la aplicación Meteorytics Backend.
 * <p>
 * Inicializa el servidor HTTP embebido del JDK {@link HttpServer} en el puerto 8080,
 * configura los servicios de negocio y registra los controladores REST y el servidor de archivos estáticos.
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
        
        // Inicializar capas de arquitectura
        AtmosphericDataSource dataSource = new ApiAtmosphericDataSource();
        WeatherService weatherService = new WeatherService(dataSource);
        WeatherController weatherController = new WeatherController(weatherService);

        // Endpoint de salud
        server.createContext("/api/health", exchange -> {
            String response = "{\"status\":\"UP\",\"service\":\"Meteorytics API\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        // Endpoint meteorológico
        server.createContext("/api/weather", weatherController);

        // Servidor de archivos estáticos del frontend
        server.createContext("/", new StaticFileHandler("frontend"));

        server.setExecutor(null);
        server.start();
        System.out.println("Servidor Meteorytics ejecutándose en http://localhost:" + PORT);
    }

    /**
     * Handler HTTP para servir recursos estáticos (HTML, CSS, JS) del frontend.
     */
    private static class StaticFileHandler implements HttpHandler {
        private final String baseDir;

        public StaticFileHandler(String baseDir) {
            this.baseDir = baseDir;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }

            File file = new File(baseDir + path);
            if (!file.exists() || file.isDirectory()) {
                file = new File(baseDir + "/index.html");
            }

            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                if (path.endsWith(".css")) contentType = "text/css";
                else if (path.endsWith(".js")) contentType = "text/javascript";
                else contentType = "text/html";
            }

            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, file.length());
            try (OutputStream os = exchange.getResponseBody(); FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int count;
                while ((count = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, count);
                }
            }
        }
    }
}
