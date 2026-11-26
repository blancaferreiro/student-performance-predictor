package edu.example.studentperformance;

import java.util.List;

public class ReportFactory {

    public static AcademicReport createReport(
            String format,
            int studentId,
            String studentName,
            String academicYear,
            List<ReportRecord> records) {
        if (format == null) {
            throw new IllegalArgumentException("El formato del informe no puede ser nulo.");
        }

        switch (format.trim().toLowerCase()) {
            case "csv":
                return new CsvReport(studentId, studentName, academicYear, records);
            case "json":
                return new JsonReport(studentId, studentName, academicYear, records);
            default:
                throw new IllegalArgumentException("Formato de informe no admitido: " + format);
        }
    }
}