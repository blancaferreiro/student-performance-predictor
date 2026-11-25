package edu.example.studentperformance;

import java.util.List;

public class CsvReport extends AcademicReport {

    public CsvReport(
            int studentId,
            String studentName,
            String academicYear,
            List<ReportRecord> records) {
        super(studentId, studentName, academicYear, records);
    }

    @Override
    public String getFormat() {
        return "csv";
    }
}