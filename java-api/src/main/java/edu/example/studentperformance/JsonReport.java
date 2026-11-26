package edu.example.studentperformance;

import java.util.List;

public class JsonReport extends AcademicReport {

    public JsonReport(
            int studentId,
            String studentName,
            String academicYear,
            List<ReportRecord> records) {
        super(studentId, studentName, academicYear, records);
    }

    @Override
    public String getFormat() {
        return "json";
    }
}