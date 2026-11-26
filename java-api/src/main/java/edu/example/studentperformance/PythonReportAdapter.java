package edu.example.studentperformance;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;

public class PythonReportAdapter implements ReportGenerator {
    private final ReportClient client;
    private final Gson gson;

    public PythonReportAdapter(ReportClient client) {
        this.client = client;
        gson = new Gson();
    }

    @Override
    public ReportResult generate(AcademicReport report, ReportFormat format)
            throws IOException, InterruptedException {

        JsonObject payload = new JsonObject();
        payload.addProperty("student", report.getStudentId());
        payload.addProperty("name", report.getStudentName());
        payload.addProperty("academic_year", report.getAcademicYear());

        JsonArray records = new JsonArray();

        for (ReportRecord record : report.getRecords()) {
            JsonObject item = new JsonObject();
            item.addProperty("subject", record.getSubject());
            item.addProperty("course", record.getCourse());
            item.addProperty("academic_year", record.getAcademicYear());
            item.addProperty("score", record.getScore());
            records.add(item);
        }

        payload.add("records", records);

        return client.requestReport(gson.toJson(payload), format);
    }
}
