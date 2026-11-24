package edu.example.studentperformance;

import java.util.List;

/**
 * Implementación concreta para informes en formato JSON (HU-04).
 */
public class JsonReport extends AcademicReport {

    public JsonReport(Student student, List<Enrollment> enrollments) {
        super(student, enrollments);
    }

    @Override
    public String getFormat() {
        return "json";
    }
}