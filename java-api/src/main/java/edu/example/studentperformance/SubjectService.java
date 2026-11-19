package edu.example.studentperformance;

import java.util.List;

/**
 * Reglas de negocio de las asignaturas (HU-01).
 */
public class SubjectService {
    private final SubjectRepository repository;

    public SubjectService(SubjectRepository repository) {
        this.repository = repository;
    }

    public List<Subject> getAllSubjects() {
        return repository.findAll();
    }

    /**
     * Valida y guarda una asignatura.
     *
     * @throws IllegalArgumentException si faltan datos o el nombre ya existe;
     *         el mensaje se devuelve tal cual al cliente.
     */
    public Subject createSubject(Subject input) {
        if (input == null || input.getName() == null || input.getName().isBlank()) {
            throw new IllegalArgumentException("Subject name is required");
        }
        // difficulty es null si falta o si el valor no es EASY, MEDIUM o HARD (Gson lo deja a null).
        if (input.getDifficulty() == null) {
            throw new IllegalArgumentException("Difficulty must be EASY, MEDIUM or HARD");
        }

        String name = input.getName().trim();
        if (repository.existsByName(name)) {
            throw new IllegalArgumentException("A subject with that name already exists");
        }

        // Se crea un objeto nuevo para no aceptar un id enviado por el cliente.
        return repository.create(new Subject(0, name, input.getDifficulty()));
    }
}
