package edu.example.studentperformance;

/**
 * Asignatura registrada en la aplicación.
 * Gson convierte esta clase a JSON y viceversa usando los nombres de los campos.
 */
public class Subject {
    private int id;
    private String name;
    private Difficulty difficulty;

    public Subject() {
    }

    public Subject(int id, String name, Difficulty difficulty) {
        this.id = id;
        this.name = name;
        this.difficulty = difficulty;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }
}
