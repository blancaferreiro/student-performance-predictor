package edu.example.studentperformance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReportService {
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final ReportGenerator reportGenerator;

    public ReportService(
            StudentRepository studentRepository,
            SubjectRepository subjectRepository,
            ReportGenerator reportGenerator) {
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.reportGenerator = reportGenerator;
    }

    public ReportResult generateStudentReport(int studentId, ReportFormat format)
            throws IOException, InterruptedException {

        Student student = studentRepository.findById(studentId);

        if (student == null) {
            return null;
        }

        List<ReportRecord> records = new ArrayList<>();
        String academicYear = "";

        for (Enrollment enrollment : student.getEnrollments()) {
            Subject subject = subjectRepository.findById(enrollment.getSubjectId());
            String subjectName = subject != null
                    ? subject.getName()
                    : "Subject " + enrollment.getSubjectId();

            if (academicYear.isBlank()
                    && enrollment.getAcademicYear() != null
                    && !enrollment.getAcademicYear().isBlank()) {
                academicYear = enrollment.getAcademicYear();
            }

            records.add(new ReportRecord(
                    subjectName,
                    enrollment.getCourse(),
                    enrollment.getAcademicYear(),
                    enrollment.getGrade()));
        }

        AcademicReport report = new AcademicReport(
                student.getId(),
                student.getName(),
                academicYear,
                records);

        return reportGenerator.generate(report, format);
    }
}
