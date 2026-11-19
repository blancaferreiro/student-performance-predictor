
package edu.example.studentperformance;

/**
 * Representa la matrícula de un estudiante en una asignatura.
 */
public class Enrollment {
    private Long subjectId;
    private double studyHours;

    public Enrollment() {
    }

    public Enrollment(Long subjectId, double studyHours) {
        this.subjectId = subjectId;
        this.studyHours = studyHours;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public double getStudyHours() {
        return studyHours;
    }

    public void setStudyHours(double studyHours) {
        this.studyHours = studyHours;
    }
}
