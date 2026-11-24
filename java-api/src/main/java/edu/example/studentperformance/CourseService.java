package edu.example.studentperformance;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Reglas de negocio de los cursos académicos (HU-01).
 */
public class CourseService {
    private final CourseRepository repository;

    public CourseService(CourseRepository repository) {
        this.repository = repository;
    }

    public List<Course> getAllCourses() {
        return repository.findAll();
    }

    public Course getCourseById(int id) {
        Course course = repository.findById(id);
        if (course == null) {
            throw new NoSuchElementException("Course not found");
        }
        return course;
    }

    public Course createCourse(Course input) {
        Course validated = validateAndNormalize(input, null);
        if (repository.existsByName(validated.getName())) {
            throw new IllegalArgumentException("An academic course with that name already exists");
        }
        return repository.create(validated);
    }

    public Course updateCourse(int id, Course input) {
        if (repository.findById(id) == null) {
            throw new NoSuchElementException("Course not found");
        }
        Course validated = validateAndNormalize(input, id);
        if (repository.existsByNameAndIdNot(validated.getName(), id)) {
            throw new IllegalArgumentException("Another academic course with that name already exists");
        }
        return repository.update(validated);
    }

    public void deleteCourse(int id) {
        if (repository.findById(id) == null) {
            throw new NoSuchElementException("Course not found");
        }
        if (repository.hasEnrollments(id)) {
            throw new IllegalArgumentException("Cannot delete a course that has associated enrollments");
        }
        repository.delete(id);
    }

    private Course validateAndNormalize(Course input, Integer id) {
        if (input == null || input.getName() == null || input.getName().isBlank()) {
            throw new IllegalArgumentException("Course name is required");
        }
        if (input.getStartDate() == null || input.getStartDate().isBlank()) {
            throw new IllegalArgumentException("Start date is required");
        }
        if (input.getEndDate() == null || input.getEndDate().isBlank()) {
            throw new IllegalArgumentException("End date is required");
        }

        LocalDate start;
        LocalDate end;
        try {
            start = LocalDate.parse(input.getStartDate().trim());
            end = LocalDate.parse(input.getEndDate().trim());
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Dates must follow YYYY-MM-DD format");
        }

        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        int courseId = (id != null) ? id : 0;
        return new Course(courseId, input.getName().trim(), start.toString(), end.toString());
    }
}