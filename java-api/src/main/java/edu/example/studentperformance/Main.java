package edu.example.studentperformance;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args)
            throws IOException, InterruptedException {

        StudentRepository repository = new StudentRepository();
        SubjectRepository subjectRepository = new SubjectRepository();
        CourseRepository courseRepository = new CourseRepository();

        AppConfig config = AppConfig.getInstance();
        ReportClient reportClient = new ReportClient(config.getReportServiceUrl());
        ReportGenerator reportGenerator = new PythonReportAdapter(reportClient);
        AcademicReportFacade reportFacade = new AcademicReportFacade(
                repository,
                subjectRepository,
                courseRepository,
                reportGenerator);

        StudentService service = new StudentService(
                repository,
                subjectRepository,
                courseRepository
        );

        StudentController controller = new StudentController(service, reportFacade);

        SubjectService subjectService = new SubjectService(subjectRepository);
        SubjectController subjectController = new SubjectController(subjectService);

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