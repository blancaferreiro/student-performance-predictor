package edu.example.studentperformance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Contiene las reglas de negocio y coordina el repositorio con el servicio de predicción.
 */
public class StudentService {
    private final StudentRepository repository;
    private final PredictionClient predictionClient;
    private final SubjectRepository subjectRepository;

    public StudentService(StudentRepository repository, PredictionClient predictionClient, SubjectRepository subjectRepository) {
        this.repository = repository;
        this.predictionClient = predictionClient;
        this.subjectRepository = subjectRepository;
    }

    public List<Student> getAllStudents() {
        return repository.findAll();
    }

    public Student getStudent(int id) {
        return repository.findById(id);
    }

    public Student createStudent(Student student) {
        // Si el JSON no trae "enrollments", el estudiante se crea sin matrículas.
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
        // Si el JSON no trae "enrollments", se conservan las matrículas actuales.
        // Para borrarlas hay que enviar explícitamente "enrollments": [].
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

    public double predictGrade(int id) throws IOException, InterruptedException {
        Student student = repository.findById(id);
        if (student == null) {
            return -1;
        }

        return predictionClient.requestPrediction(student.getStudyHours());
    }


    public double predictGrade(int studentId, long subjectId)
            throws IOException, InterruptedException {

        // Buscamos al estudiante.
        Student student = repository.findById(studentId);

        if (student == null) {
            return -1;
        }

        // Buscamos la matrícula de esa asignatura.
        Enrollment selectedEnrollment = null;

        for (Enrollment enrollment : student.getEnrollments()) {
            if (enrollment.getSubjectId() == subjectId) {
                selectedEnrollment = enrollment;
                break;
            }
        }

        if (selectedEnrollment == null) {
            return -1;
        }

        // Consultamos la asignatura para obtener su dificultad.
        Subject subject = subjectRepository.findById(subjectId);

        if (subject == null || subject.getDifficulty() == null) {
            return -1;
        }

        // Enviamos a Python las horas y la dificultad de esa matrícula.
        return predictionClient.requestPrediction(
                selectedEnrollment.getStudyHours(),
                subject.getDifficulty().name()
        );
    }


    /**
     * Aplica las reglas de validación de estudiantes y matrículas.
     */
    private boolean isValid(Student student) {
        if (student == null) {
            return false;
        }

        if (student.getName() == null || student.getName().trim().isEmpty()) {
            return false;
        }

        if (!Double.isFinite(student.getStudyHours()) || student.getStudyHours() < 0) {
            return false;
        }

        List<Enrollment> enrollments = student.getEnrollments();
        if (enrollments == null) {
            return false;
        }

        Set<Long> subjectIds = new HashSet<>();

        for (Enrollment enrollment : enrollments) {
            if (enrollment == null) {
                return false;
            }

            Long subjectId = enrollment.getSubjectId();

            if (subjectId == null || subjectId <= 0) {
                return false;
            }

            if (!subjectRepository.existsById(subjectId)) {
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

            if (!subjectIds.add(subjectId)) {
                return false;
            }
        }

        return true;
    }
}
