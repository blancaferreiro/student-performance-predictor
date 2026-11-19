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

        // Las dependencias se conectan de forma explícita para mostrar la relación entre capas.
        StudentRepository repository = new StudentRepository();
        SubjectRepository subjectRepository = new SubjectRepository();

        // en desarrollo local se utiliza localhost.
        String predictionServiceUrl = System.getenv("PYTHON_SERVICE_URL");
        if (predictionServiceUrl == null) {
            predictionServiceUrl = "http://localhost:8000";
        } else if (predictionServiceUrl.isBlank()) {
            predictionServiceUrl = "http://localhost:8000";
        }
        PredictionClient predictionClient = new PredictionClient(predictionServiceUrl);
        StudentService service = new StudentService(repository, predictionClient, subjectRepository);

        String reportServiceUrl = envOrDefault(
                "REPORT_SERVICE_URL",
                "http://localhost:8001");
        ReportClient reportClient = new ReportClient(reportServiceUrl);
        ReportGenerator reportGenerator = new PythonReportAdapter(reportClient);
        ReportService reportService = new ReportService(
                repository,
                subjectRepository,
                reportGenerator);

        StudentController controller = new StudentController(service, reportService);

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

    private static String envOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value;
    }
}
