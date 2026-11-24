
package edu.example.studentperformance;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Punto de entrada que crea los objetos de la aplicación
 * y arranca el servidor HTTP.
 */
public class Main {

    public static void main(String[] args)
            throws IOException, InterruptedException {

        Database.init();

        // Repositorios
        StudentRepository repository = new StudentRepository();
        SubjectRepository subjectRepository = new SubjectRepository();
        CourseRepository courseRepository = new CourseRepository();

        // Dirección del microservicio de predicciones
        String predictionServiceUrl = System.getenv("PYTHON_SERVICE_URL");

        if (predictionServiceUrl == null
                || predictionServiceUrl.isBlank()) {
            predictionServiceUrl = "http://localhost:8000";
        }

        PredictionClient predictionClient =
                new PredictionClient(predictionServiceUrl);

        // Servicio de estudiantes
        StudentService service = new StudentService(
                repository,
                predictionClient,
                subjectRepository,
                courseRepository
        );

        StudentController controller =
                new StudentController(service);

        // Servicio de asignaturas
        SubjectService subjectService =
                new SubjectService(subjectRepository);

        SubjectController subjectController =
                new SubjectController(subjectService);

        // Servicio de cursos académicos
        CourseService courseService =
                new CourseService(courseRepository);

        CourseController courseController =
                new CourseController(courseService);

        // Servidor HTTP
        HttpServer server = HttpServer.create(
                new InetSocketAddress(8080), 0
        );

        CourseService courseService = new CourseService(courseRepository);
        CourseController courseController = new CourseController(courseService);

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/students", controller);
        server.createContext("/api/subjects", subjectController);
        server.createContext("/api/courses", courseController);
        server.createContext("/subjects", new StaticFileHandler());
        server.createContext("/courses", new StaticFileHandler());
        server.createContext("/", new StaticFileHandler());

        server.start();

        System.out.println(
                "Student Performance Predictor is running at http://localhost:8080"
        );
    }
}