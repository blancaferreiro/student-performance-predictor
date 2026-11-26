package edu.example.studentperformance;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;

public class StudentController implements HttpHandler {

    private final StudentService service;
    private final AcademicReportFacade reportFacade;
    private final Gson gson;

    public StudentController(StudentService service, AcademicReportFacade reportFacade) {
        this.service = service;
        this.reportFacade = reportFacade;
        gson = new Gson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            routeRequest(exchange);

        } catch (JsonParseException exception) {
            sendError(exchange, 400, "Invalid JSON input");

        } catch (NumberFormatException exception) {
            sendError(exchange, 400, "Invalid student identifier");

        } catch (IllegalStateException exception) {
            exception.printStackTrace();
            sendError(exchange, 500, "Database error");
        }
    }

    private void routeRequest(HttpExchange exchange) throws IOException {

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");

        // GET /students
        // POST /students
        if (parts.length == 2) {
            handleCollection(exchange, method);
            return;
        }

        // GET /students/{id}
        // PUT /students/{id}
        // DELETE /students/{id}
        if (parts.length == 3) {
            int id = Integer.parseInt(parts[2]);
            handleStudent(exchange, method, id);
            return;
        }

        // GET /students/{id}/prediction
        // GET /students/{id}/report
        if (parts.length == 4) {

            if ("prediction".equals(parts[3])) {
                int id = Integer.parseInt(parts[2]);
                handlePrediction(exchange, method, id);
                return;
            }

            if ("report".equals(parts[3])) {
                int id = Integer.parseInt(parts[2]);
                handleReport(exchange, method, id);
                return;
            }
        }

        // GET /students/{id}/subjects/{subjectId}/courses/{courseId}/prediction
        if (parts.length == 8
                && "subjects".equals(parts[3])
                && "courses".equals(parts[5])
                && "prediction".equals(parts[7])) {

            int studentId = Integer.parseInt(parts[2]);
            long subjectId = Long.parseLong(parts[4]);
            int courseId = Integer.parseInt(parts[6]);

            handleSubjectPrediction(
                    exchange, method, studentId, subjectId, courseId
            );
            return;
        }

        sendError(exchange, 404, "Route not found");
    }

    private void handleCollection(
            HttpExchange exchange,
            String method) throws IOException {

        if ("GET".equals(method)) {
            sendJson(exchange, 200, gson.toJson(service.getAllStudents()));
            return;
        }

        if ("POST".equals(method)) {
            Student input = readStudent(exchange);
            Student createdStudent = service.createStudent(input);

            if (createdStudent == null) {
                sendError(exchange, 400,
                        "Invalid student: name is required, hours cannot be negative, and enrollments must be valid");
                return;
            }

            sendJson(exchange, 201, gson.toJson(createdStudent));
            return;
        }

        sendError(exchange, 405, "Method not allowed");
    }

    private void handleStudent(
            HttpExchange exchange,
            String method,
            int id) throws IOException {

        if ("GET".equals(method)) {
            Student student = service.getStudent(id);

            if (student == null) {
                sendError(exchange, 404, "Student not found");
                return;
            }

            sendJson(exchange, 200, gson.toJson(student));
            return;
        }

        if ("PUT".equals(method)) {
            Student existingStudent = service.getStudent(id);

            if (existingStudent == null) {
                sendError(exchange, 404, "Student not found");
                return;
            }

            Student updatedStudent = service.updateStudent(
                    id, readStudent(exchange)
            );

            if (updatedStudent == null) {
                sendError(exchange, 400,
                        "Invalid student: name is required, hours cannot be negative, and enrollments must be valid");
                return;
            }

            sendJson(exchange, 200, gson.toJson(updatedStudent));
            return;
        }

        if ("DELETE".equals(method)) {
            if (!service.deleteStudent(id)) {
                sendError(exchange, 404, "Student not found");
                return;
            }

            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        sendError(exchange, 405, "Method not allowed");
    }

    private void handlePrediction(
            HttpExchange exchange,
            String method,
            int id) throws IOException {

        if (!"GET".equals(method)) {
            sendError(exchange, 405, "Method not allowed");
            return;
        }

        Student student = service.getStudent(id);

        if (student == null) {
            sendError(exchange, 404, "Student not found");
            return;
        }

        try {
            double predictedGrade = service.predictGrade(id);

            JsonObject response = new JsonObject();
            response.addProperty("id", student.getId());
            response.addProperty("name", student.getName());
            response.addProperty("studyHours", student.getStudyHours());
            response.addProperty("predictedGrade", predictedGrade);

            sendJson(exchange, 200, gson.toJson(response));

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            sendError(exchange, 503, "Prediction service is unavailable");

        } catch (IOException exception) {
            sendError(exchange, 503, "Prediction service is unavailable");
        }
    }

    private void handleSubjectPrediction(
            HttpExchange exchange,
            String method,
            int studentId,
            long subjectId,
            int courseId) throws IOException {

        if (!"GET".equals(method)) {
            sendError(exchange, 405, "Method not allowed");
            return;
        }

        Student student = service.getStudent(studentId);

        if (student == null) {
            sendError(exchange, 404, "Student not found");
            return;
        }

        try {
            double predictedGrade = service.predictGrade(
                    studentId, subjectId, courseId
            );

            if (predictedGrade < 0) {
                sendError(exchange, 404, "Enrollment or subject not found");
                return;
            }

            JsonObject response = new JsonObject();
            response.addProperty("studentId", studentId);
            response.addProperty("studentName", student.getName());
            response.addProperty("subjectId", subjectId);
            response.addProperty("courseId", courseId);
            response.addProperty("predictedGrade", predictedGrade);

            sendJson(exchange, 200, gson.toJson(response));

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            sendError(exchange, 503, "Prediction service is unavailable");

        } catch (IOException exception) {
            sendError(exchange, 503, "Prediction service is unavailable");
        }
    }

    private void handleReport(
            HttpExchange exchange,
            String method,
            int id) throws IOException {

        if (!"GET".equals(method)) {
            sendError(exchange, 405, "Method not allowed");
            return;
        }

        String formatParam = queryParameter(
                exchange.getRequestURI().getQuery(), "format");

        try {
            ReportResult result = reportFacade.generateReport(id, formatParam);
            sendReport(exchange, result);

        } catch (IllegalArgumentException exception) {
            sendError(exchange, 400, exception.getMessage());

        } catch (NoSuchElementException exception) {
            sendError(exchange, 404, exception.getMessage());

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            sendError(exchange, 503, "Report service is unavailable");

        } catch (IOException exception) {
            sendError(exchange, 503, "Report service is unavailable");
        }
    }

    private String queryParameter(String query, String name) {
        if (query == null) {
            return null;
        }

        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);

            if (parts.length == 2 && name.equals(parts[0])) {
                return URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
            }
        }

        return null;
    }

    private void sendReport(HttpExchange exchange, ReportResult result)
            throws IOException {

        byte[] body = result.getContent().getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", result.getMediaType());
        exchange.getResponseHeaders().set(
                "Content-Disposition",
                "attachment; filename=\"" + result.getFilename() + "\"");

        exchange.sendResponseHeaders(200, body.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(body);
        }
    }

    private Student readStudent(HttpExchange exchange) {
        InputStreamReader reader = new InputStreamReader(
                exchange.getRequestBody(),
                StandardCharsets.UTF_8
        );

        return gson.fromJson(reader, Student.class);
    }

    private void sendError(
            HttpExchange exchange,
            int status,
            String message) throws IOException {

        JsonObject error = new JsonObject();
        error.addProperty("error", message);
        sendJson(exchange, status, gson.toJson(error));
    }

    private void sendJson(
            HttpExchange exchange,
            int status,
            String json) throws IOException {

        byte[] body = json.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=utf-8"
        );

        exchange.sendResponseHeaders(status, body.length);

        OutputStream output = exchange.getResponseBody();
        output.write(body);
        output.close();
    }
}