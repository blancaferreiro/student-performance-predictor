package edu.example.studentperformance;

import java.io.IOException;

public interface ReportGenerator {
    ReportResult generate(AcademicReport report, ReportFormat format)
            throws IOException, InterruptedException;
}
