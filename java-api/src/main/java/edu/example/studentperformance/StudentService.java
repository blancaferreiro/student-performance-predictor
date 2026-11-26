package edu.example.studentperformance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StudentService {

    private final StudentRepository repository;
    private final PredictionClient predictionClient;
    private final SubjectRepository subjectRepository;
    private final CourseRepository courseRepository;

    public StudentService(
            StudentRepository repository,
            PredictionClient predictionClient,
            SubjectRepository subjectRepository,
            CourseRepository courseRepository) {

        this.repository = repository;
        this.predictionClient = predictionClient;
        this.subjectRepository = subjectRepository;
        this.courseRepository = courseRepository;
    }

    public List<Student> getAllStudents() {
        return repository.findAll();
    }

    public Student getStudent(int id) {
        return repository.findById(id);
    }

    public Student createStudent(Student student) {

        if (student != null && student.getEnrollments() == null) {
            student.setEnrollments(new ArrayList<>());
        }

        if (!isValid(student)) {
            return null;
        }

        return repository.create(student);
    }

    public Student updateStudent(int id, Student student) {

        Student existing = repository.findById(id);

        if (existing == null) {
            return null;
        }

        if (student != null && student.getEnrollments() == null) {
            student.setEnrollments(existing.getEnrollments());
        }

        if (!isValid(student)) {
            return null;
        }

        return repository.update(id, student);
    }

    public boolean deleteStudent(int id) {
        return repository.delete(id);
    }

    public double predictGrade(int id)
            throws IOException, InterruptedException {

        Student student = repository.findById(id);

        if (student == null) {
            return -1;
        }

        return predictionClient.requestPrediction(student.getStudyHours());
    }

    public double predictGrade(int studentId, long subjectId)
            throws IOException, InterruptedException {

        Student student = repository.findById(studentId);

        if (student == null || student.getEnrollments() == null) {
            return -1;
        }

        Enrollment selectedEnrollment = null;

        for (Enrollment enrollment : student.getEnrollments()) {

            if (enrollment.getSubjectId() != null
                    && enrollment.getSubjectId().longValue() == subjectId) {

                if (selectedEnrollment != null) {
                    return -1;
                }

                selectedEnrollment = enrollment;
            }
        }

        if (selectedEnrollment == null) {
            return -1;
        }

        Subject subject = subjectRepository.findById(subjectId);

        if (subject == null || subject.getDifficulty() == null) {
            return -1;
        }

        return predictionClient.requestPrediction(
                selectedEnrollment.getStudyHours(),
                subject.getDifficulty().name()
        );
    }

    public double predictGrade(
            int studentId,
            long subjectId,
            int courseId)
            throws IOException, InterruptedException {

        Student student = repository.findById(studentId);

        if (student == null || student.getEnrollments() == null) {
            return -1;
        }

        Enrollment selectedEnrollment = null;

        for (Enrollment enrollment : student.getEnrollments()) {

            if (enrollment.getSubjectId() != null
                    && enrollment.getSubjectId().longValue() == subjectId
                    && enrollment.getCourseId() != null
                    && enrollment.getCourseId().intValue() == courseId) {

                selectedEnrollment = enrollment;
                break;
            }
        }

        if (selectedEnrollment == null) {
            return -1;
        }

        Subject subject = subjectRepository.findById(subjectId);

        if (subject == null || subject.getDifficulty() == null) {
            return -1;
        }

        return predictionClient.requestPrediction(
                selectedEnrollment.getStudyHours(),
                subject.getDifficulty().name()
        );
    }

    private boolean isValid(Student student) {

        if (student == null) {
            return false;
        }

        if (student.getName() == null
                || student.getName().trim().isEmpty()) {
            return false;
        }

        if (!Double.isFinite(student.getStudyHours())
                || student.getStudyHours() < 0) {
            return false;
        }

        List<Enrollment> enrollments = student.getEnrollments();

        if (enrollments == null) {
            return false;
        }

        Set<String> enrollmentKeys = new HashSet<>();

        for (Enrollment enrollment : enrollments) {

            if (enrollment == null) {
                return false;
            }

            Long subjectId = enrollment.getSubjectId();
            Integer courseId = enrollment.getCourseId();

            if (subjectId == null || subjectId <= 0) {
                return false;
            }

            if (courseId == null || courseId <= 0) {
                return false;
            }

            if (!subjectRepository.existsById(subjectId)) {
                return false;
            }

            if (courseRepository.findById(courseId) == null) {
                return false;
            }

            if (!Double.isFinite(enrollment.getStudyHours())
                    || enrollment.getStudyHours() < 0) {
                return false;
            }

            Double grade = enrollment.getGrade();
            if (grade != null
                    && (!Double.isFinite(grade) || grade < 0 || grade > 10)) {
                return false;
            }

            String key = subjectId + "-" + courseId;
            if (!enrollmentKeys.add(key)) {
                return false;
            }
        }

        return true;
    }
}