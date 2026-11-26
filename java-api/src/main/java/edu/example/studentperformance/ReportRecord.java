package edu.example.studentperformance;

public class ReportRecord {
    private final String subject;
    private final String course;
    private final String academicYear;
    private final Double score;

    public ReportRecord(String subject, String course, String academicYear, Double score) {
        this.subject = subject;
        this.course = course;
        this.academicYear = academicYear;
        this.score = score;
    }

    public String getSubject() {
        return subject;
    }

    public String getCourse() {
        return course;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public Double getScore() {
        return score;
    }
}
