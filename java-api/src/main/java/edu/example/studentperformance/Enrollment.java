
package edu.example.studentperformance;

/**
 * Representa la matrícula de un estudiante en una asignatura
 * durante un curso académico.
 */
public class Enrollment {

    private Long subjectId;
    private Integer courseId;
    private double studyHours;
    private Double grade;

    /**
     * Constructor vacío necesario para Gson.
     */
    public Enrollment() {
    }

    /**
     * Constructor completo de una matrícula.
     */
    public Enrollment(Long subjectId, Integer courseId,
                      double studyHours, Double grade) {
        this.subjectId = subjectId;
        this.courseId = courseId;
        this.studyHours = studyHours;
        this.grade = grade;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public double getStudyHours() {
        return studyHours;
    }

    public void setStudyHours(double studyHours) {
        this.studyHours = studyHours;
    }

    public Double getGrade() {
        return grade;
    }

    public void setGrade(Double grade) {
        this.grade = grade;
    }
}
