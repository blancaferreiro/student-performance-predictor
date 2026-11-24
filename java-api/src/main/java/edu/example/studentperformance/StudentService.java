
package edu.example.studentperformance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Contiene las reglas de negocio y coordina el repositorio
 * con los servicios de predicción y cursos académicos.
 */
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

        // Si no se envían matrículas, se conservan las existentes.
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

    /**
     * Predice la nota utilizando las horas generales del estudiante.
     */
    public double predictGrade(int id)
            throws IOException, InterruptedException {

        Student student = repository.findById(id);

        if (student == null) {
            return -1;
        }

        return predictionClient.requestPrediction(student.getStudyHours());
    }

    /**
     * Predice la nota de una asignatura.
     *
     * Este método mantiene la compatibilidad con llamadas antiguas.
     * Si el estudiante tiene varias matrículas de esa asignatura
     * en distintos cursos, devuelve -1 porque no se puede saber
     * qué curso se quiere utilizar.
     */
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

                // Si aparece más de una matrícula de esa asignatura,
                // necesitamos que se indique el curso.
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

    /**
     * Predice la nota de una asignatura en un curso académico concreto.
     *
     * Utiliza las horas de estudio de la matrícula correspondiente
     * a ese estudiante, asignatura y curso.
     */
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

    /**
     * Valida los datos del estudiante y sus matrículas.
     */
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

        // Guardamos la combinación asignatura + curso.
        Set<String> enrollmentKeys = new HashSet<>();

        for (Enrollment enrollment : enrollments) {

            if (enrollment == null) {
                return false;
            }

            Long subjectId = enrollment.getSubjectId();
            Integer courseId = enrollment.getCourseId();

            // Comprobar identificadores.
            if (subjectId == null || subjectId <= 0) {
                return false;
            }

            if (courseId == null || courseId <= 0) {
                return false;
            }

            // Comprobar que existe la asignatura.
            if (!subjectRepository.existsById(subjectId)) {
                return false;
            }

            // Comprobar que existe el curso.
            if (courseRepository.findById(courseId) == null) {
                return false;
            }

            // Comprobar las horas de estudio.
            if (!Double.isFinite(enrollment.getStudyHours())
                    || enrollment.getStudyHours() < 0) {
                return false;
            }

            Double grade = enrollment.getGrade();
            if (grade != null
                    && (!Double.isFinite(grade) || grade < 0 || grade > 10)) {
                return false;
            }

            if (!subjectIds.add(subjectId)) {
                return false;
            }
        }

        return true;
    }
}
