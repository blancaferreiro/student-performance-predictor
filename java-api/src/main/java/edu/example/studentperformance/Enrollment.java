
package edu.example.studentperformance;

/**
 * Representa la matrícula de un estudiante en una asignatura.
 */
public class Enrollment {
    private Long subjectId;
    private double studyHours;
    private String course;
    private String academicYear;
    private Double grade;

    public Enrollment() {
    }

    public Enrollment(Long subjectId, double studyHours) {
        this.subjectId = subjectId;
        this.studyHours = studyHours;
    }

    public Enrollment(
            Long subjectId,
            double studyHours,
            String course,
            String academicYear,
            Double grade) {
        this.subjectId = subjectId;
        this.studyHours = studyHours;
        this.course = course;
        this.academicYear = academicYear;
        this.grade = grade;
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

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public Double getGrade() {
        return grade;
    }

    public void setGrade(Double grade) {
        this.grade = grade;
    }
}
