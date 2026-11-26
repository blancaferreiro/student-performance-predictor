package edu.example.studentperformance;

import java.util.List;

public abstract class AcademicReport {

    protected final int studentId;
    protected final String studentName;
    protected final String academicYear;
    protected final List<ReportRecord> records;

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

    public abstract String getFormat();

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