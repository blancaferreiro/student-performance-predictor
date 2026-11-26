package edu.example.studentperformance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class AcademicReportFacade {

    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final CourseRepository courseRepository;
    private final ReportGenerator reportGenerator;

    public AcademicReportFacade(
            StudentRepository studentRepository,
            SubjectRepository subjectRepository,
            CourseRepository courseRepository,
            ReportGenerator reportGenerator) {
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.courseRepository = courseRepository;
        this.reportGenerator = reportGenerator;
    }

    public ReportResult generateReport(int studentId, String formatName)
            throws IOException, InterruptedException {

        ReportFormat format = ReportFormat.from(formatName);

        Student student = studentRepository.findById(studentId);
        if (student == null) {
            throw new NoSuchElementException("Student not found");
        }

        List<ReportRecord> records = new ArrayList<>();
        String defaultAcademicYear = "";

        if (student.getEnrollments() != null) {
            for (Enrollment enrollment : student.getEnrollments()) {
                Subject subject = subjectRepository.findById(enrollment.getSubjectId());
                String subjectName = subject != null
                        ? subject.getName()
                        : "Subject " + enrollment.getSubjectId();

                String courseName = enrollment.getCourse();
                if (enrollment.getCourseId() != null) {
                    Course course = courseRepository.findById(enrollment.getCourseId());
                    if (course != null && course.getName() != null) {
                        courseName = course.getName();
                    }
                }

                String academicYear = enrollment.getAcademicYear();
                if ((academicYear == null || academicYear.isBlank()) && courseName != null) {
                    academicYear = courseName;
                }

                if (defaultAcademicYear.isBlank()
                        && academicYear != null
                        && !academicYear.isBlank()) {
                    defaultAcademicYear = academicYear;
                }

                records.add(new ReportRecord(
                        subjectName,
                        courseName,
                        academicYear,
                        enrollment.getGrade()
                ));
            }
        }

        AcademicReport report = ReportFactory.createReport(
                format.getExtension(),
                student.getId(),
                student.getName(),
                defaultAcademicYear,
                records
        );

        return reportGenerator.generate(report, format);
    }
}