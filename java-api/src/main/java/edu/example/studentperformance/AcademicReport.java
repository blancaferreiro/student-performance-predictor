package edu.example.studentperformance;

import java.util.List;

/**
 * Clase abstracta que representa la estructura base de un informe académico.
 * Contiene los atributos y comportamientos comunes (HU-04).
 */
public abstract class AcademicReport {

    protected Student student;
    protected List<Enrollment> enrollments;

    public AcademicReport(Student student, List<Enrollment> enrollments) {
        this.student = student;
        this.enrollments = enrollments;
    }

    /**
     * Método abstracto para obtener el formato específico del informe.
     */
    public abstract String getFormat();

    public Student getStudent() {
        return student;
    }

    public List<Enrollment> getEnrollments() {
        return enrollments;
    }
}