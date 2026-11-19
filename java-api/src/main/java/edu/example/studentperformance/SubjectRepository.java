package edu.example.studentperformance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda las asignaturas en la tabla "subjects" de MySQL.
 */
public class SubjectRepository {

    public List<Subject> findAll() {
        String sql = "SELECT id, name, difficulty FROM subjects ORDER BY id";
        List<Subject> subjects = new ArrayList<Subject>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                subjects.add(new Subject(
                        rows.getInt("id"),
                        rows.getString("name"),
                        Difficulty.valueOf(rows.getString("difficulty"))));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not read subjects", exception);
        }
        return subjects;
    }

    public boolean existsByName(String name) {
        // LOWER() para que "Algoritmos" y "algoritmos" cuenten como la misma asignatura.
        String sql = "SELECT 1 FROM subjects WHERE LOWER(name) = LOWER(?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not check subject name", exception);
        }
    }


    public boolean existsById(long id) {
        String sql = "SELECT 1 FROM subjects WHERE id = ?";

        try (Connection connection = Database.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet rows = statement.executeQuery()) {
                return rows.next();
            }

        } catch (SQLException exception) {
            throw new IllegalStateException("Could not check subject id", exception);
        }
    }


    public Subject findById(long id) {
        String sql = "SELECT id, name, difficulty FROM subjects WHERE id = ?";

        try (Connection connection = Database.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet rows = statement.executeQuery()) {
                if (rows.next()) {
                    return new Subject(
                            rows.getInt("id"),
                            rows.getString("name"),
                            Difficulty.valueOf(rows.getString("difficulty"))
                    );
                }
                return null;
            }

        } catch (SQLException exception) {
            throw new IllegalStateException("Could not read subject " + id, exception);
        }
    }



    public Subject create(Subject subject) {
        String sql = "INSERT INTO subjects (name, difficulty) VALUES (?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                 connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, subject.getName());
            statement.setString(2, subject.getDifficulty().name());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    subject.setId(keys.getInt(1));
                }
            }
            return subject;
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not create subject", exception);
        }
    }
}
