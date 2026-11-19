package edu.example.studentperformance;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ReportClient {
    private final String serviceUrl;
    private final HttpClient httpClient;
    private final Gson gson;

    public ReportClient(String serviceUrl) {
        this.serviceUrl = serviceUrl;
        httpClient = HttpClient.newHttpClient();
        gson = new Gson();
    }

    public ReportResult requestReport(String requestBody, ReportFormat format)
            throws IOException, InterruptedException {

        URI uri = URI.create(serviceUrl + "/reports/" + format.getExtension());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Report service returned " + response.statusCode());
        }

        JsonObject data = gson.fromJson(response.body(), JsonObject.class);

        if (data == null
                || !data.has("filename")
                || !data.has("media_type")
                || !data.has("content")) {
            throw new IOException("Invalid report response");
        }

        return new ReportResult(
                data.get("filename").getAsString(),
                data.get("media_type").getAsString(),
                data.get("content").getAsString());
    }
}
