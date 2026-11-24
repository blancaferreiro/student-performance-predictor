package edu.example.studentperformance;

import java.util.List;

/**
 * Implementación concreta para informes en formato CSV (HU-04).
 */
public class CsvReport extends AcademicReport {

    public CsvReport(Student student, List<Enrollment> enrollments) {
        super(student, enrollments);
    }

    @Override
    public String getFormat() {
        return "csv";
    }
}