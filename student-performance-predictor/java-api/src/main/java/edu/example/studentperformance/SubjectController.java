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

/**
 * API REST de asignaturas.
 *
 * GET  /api/subjects -> lista las asignaturas
 * POST /api/subjects -> registra una asignatura {"name": "...", "difficulty": "EASY|MEDIUM|HARD"}
 */
public class SubjectController implements HttpHandler {
    private final SubjectService service;
    private final Gson gson = new Gson();

    public SubjectController(SubjectService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            if (!"/api/subjects".equals(path) && !"/api/subjects/".equals(path)) {
                sendError(exchange, 404, "Route not found");
                return;
            }

            String method = exchange.getRequestMethod();
            if ("GET".equals(method)) {
                sendJson(exchange, 200, gson.toJson(service.getAllSubjects()));
            } else if ("POST".equals(method)) {
                Subject input = gson.fromJson(
                        new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8),
                        Subject.class);
                sendJson(exchange, 201, gson.toJson(service.createSubject(input)));
            } else {
                sendError(exchange, 405, "Method not allowed");
            }
        } catch (JsonParseException exception) {
            sendError(exchange, 400, "Invalid JSON input");
        } catch (IllegalArgumentException exception) {
            sendError(exchange, 400, exception.getMessage());
        } catch (IllegalStateException exception) {
            // Error de base de datos: se registra en consola y el cliente recibe un 500 limpio.
            exception.printStackTrace();
            sendError(exchange, 500, "Database error");
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
