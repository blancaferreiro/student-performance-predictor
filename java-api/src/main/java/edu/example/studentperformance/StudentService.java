package edu.example.studentperformance;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StudentService {

    private final StudentRepository repository;
    private final SubjectRepository subjectRepository;
    private final CourseRepository courseRepository;

    public StudentService(
            StudentRepository repository,
            SubjectRepository subjectRepository,
            CourseRepository courseRepository) {

        this.repository = repository;
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