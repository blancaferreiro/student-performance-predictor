package edu.example.studentperformance;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * API REST de cursos académicos (HU-01).
 *
 * GET    /api/courses      -> lista todos los cursos
 * POST   /api/courses      -> crea un curso {"name": "2025/2026", "startDate": "2025-09-01", "endDate": "2026-06-30"}
 * GET    /api/courses/{id} -> obtiene un curso concreto
 * PUT    /api/courses/{id} -> actualiza un curso existente
 * DELETE /api/courses/{id} -> elimina un curso si no tiene matrículas asociadas
 */
public class CourseController implements HttpHandler {
    private final CourseService service;
    private final Gson gson = new Gson();

    public CourseController(CourseService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            if ("/api/courses".equals(path) || "/api/courses/".equals(path)) {
                handleCollection(exchange, method);
                return;
            }

            if (path.startsWith("/api/courses/")) {
                String idSegment = path.substring("/api/courses/".length());
                if (idSegment.endsWith("/")) {
                    idSegment = idSegment.substring(0, idSegment.length() - 1);
                }
                int id = parseId(idSegment);
                handleItem(exchange, method, id);
                return;
            }

            sendError(exchange, 404, "Route not found");
        } catch (JsonParseException exception) {
            sendError(exchange, 400, "Invalid JSON input");
        } catch (IllegalArgumentException exception) {
            sendError(exchange, 400, exception.getMessage());
        } catch (NoSuchElementException exception) {
            sendError(exchange, 404, exception.getMessage());
        } catch (IllegalStateException exception) {
            exception.printStackTrace();
            sendError(exchange, 500, "Database error");
        }
    }

    private void handleCollection(HttpExchange exchange, String method) throws IOException {
        if ("GET".equals(method)) {
            sendJson(exchange, 200, gson.toJson(service.getAllCourses()));
        } else if ("POST".equals(method)) {
            Course input = gson.fromJson(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8),
                    Course.class);
            sendJson(exchange, 201, gson.toJson(service.createCourse(input)));
        } else {
            sendError(exchange, 405, "Method not allowed");
        }
    }

    private void handleItem(HttpExchange exchange, String method, int id) throws IOException {
        if ("GET".equals(method)) {
            sendJson(exchange, 200, gson.toJson(service.getCourseById(id)));
        } else if ("PUT".equals(method)) {
            Course input = gson.fromJson(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8),
                    Course.class);
            sendJson(exchange, 200, gson.toJson(service.updateCourse(id, input)));
        } else if ("DELETE".equals(method)) {
            service.deleteCourse(id);
            sendJson(exchange, 200, gson.toJson(Map.of("deleted", true)));
        } else {
            sendError(exchange, 405, "Method not allowed");
        }
    }

    private int parseId(String rawId) {
        try {
            int id = Integer.parseInt(rawId);
            if (id <= 0) {
                throw new IllegalArgumentException("Invalid course id");
            }
            return id;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid course id");
        }
    }

    private void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(body);
        }
    }

    private void sendError(HttpExchange exchange, int status, String message) throws IOException {
        sendJson(exchange, status, gson.toJson(Map.of("error", message)));
    }
}