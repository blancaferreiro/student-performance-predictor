
package edu.example.studentperformance;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Punto de entrada que crea los objetos de la aplicación y arranca el servidor HTTP.
 */
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {

        Database.init();

        AppConfig config = AppConfig.getInstance();
        

        // Creamos los repositorios
        StudentRepository repository = new StudentRepository();
        SubjectRepository subjectRepository = new SubjectRepository();

        PredictionClient predictionClient = new PredictionClient(
            config.getPredictionServiceUrl()
        );

        StudentService service = new StudentService(
            repository,
            predictionClient,
            subjectRepository
        );

        StudentController controller = new StudentController(service);

        SubjectService subjectService = new SubjectService(subjectRepository);
        SubjectController subjectController = new SubjectController(subjectService);

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/students", controller);
        server.createContext("/api/subjects", subjectController);
        server.createContext("/subjects", new StaticFileHandler());
        server.createContext("/", new StaticFileHandler());
        server.start();

        System.out.println("Student Performance Predictor is running at http://localhost:8080");
    }
}
