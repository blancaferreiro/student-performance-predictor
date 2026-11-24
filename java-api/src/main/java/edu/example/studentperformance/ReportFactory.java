package edu.example.studentperformance;

import java.util.List;

/**
 * Factoría encargada de instanciar el tipo de informe correspondiente
 * según el formato solicitado, evitando instanciaciones directas (HU-04).
 */
public class ReportFactory {

    public static AcademicReport createReport(String format, Student student, List<Enrollment> enrollments) {
        if (format == null) {
            throw new IllegalArgumentException("El formato del informe no puede ser nulo.");
        }

        switch (format.trim().toLowerCase()) {
            case "csv":
                return new CsvReport(student, enrollments);
            case "json":
                return new JsonReport(student, enrollments);
            default:
                throw new IllegalArgumentException("Formato de informe no admitido: " + format);
        }
    }
}