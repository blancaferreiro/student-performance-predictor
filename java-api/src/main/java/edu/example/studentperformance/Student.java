package edu.example.studentperformance;

import java.util.ArrayList;
import java.util.List;


/**
 * Representa los datos de un estudiante, incluidas sus matrículas.
 */
public class Student {
    private int id;
    private String name;
    private double studyHours;
    private List<Enrollment> enrollments;

    public Student() {
        // enrollments queda a null a propósito: así se distingue "no enviado" de "lista vacía".
    }

    public Student(int id, String name, double studyHours) {
        this.id = id;
        this.name = name;
        this.studyHours = studyHours;
        this.enrollments = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getStudyHours() {
        return studyHours;
    }

    public void setStudyHours(double studyHours) {
        this.studyHours = studyHours;
    }

    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    public void setEnrollments(List<Enrollment> enrollments) {
        this.enrollments = enrollments != null
                ? enrollments
                : new ArrayList<>();
    }
}
