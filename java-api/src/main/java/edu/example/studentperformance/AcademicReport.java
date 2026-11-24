package edu.example.studentperformance;

import java.util.List;

public class AcademicReport {
    private final int studentId;
    private final String studentName;
    private final String academicYear;
    private final List<ReportRecord> records;

    public AcademicReport(
            int studentId,
            String studentName,
            String academicYear,
            List<ReportRecord> records) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.academicYear = academicYear;
        this.records = records;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public List<ReportRecord> getRecords() {
        return records;
    }
}
