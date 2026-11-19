
package edu.example.studentperformance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda estudiantes y sus matrículas en MySQL.
 */
public class StudentRepository {

    public List<Student> findAll() {
        String sql = "SELECT id, name, study_hours FROM students ORDER BY id";
        List<Student> students = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {

            while (rows.next()) {
                students.add(toStudent(rows));
            }

            for (Student student : students) {
                loadEnrollments(connection, student);
            }

        } catch (SQLException exception) {
            throw new IllegalStateException("Could not read students", exception);
        }

        return students;
    }

    public Student findById(int id) {
        String sql = "SELECT id, name, study_hours FROM students WHERE id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            Student student = null;

            try (ResultSet rows = statement.executeQuery()) {
                if (rows.next()) {
                    student = toStudent(rows);
                }
            }

            if (student != null) {
                loadEnrollments(connection, student);
            }

            return student;

        } catch (SQLException exception) {
            throw new IllegalStateException("Could not read student " + id, exception);
        }
    }

    public Student create(Student student) {
        String sql = "INSERT INTO students (name, study_hours) VALUES (?, ?)";

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);

            try {
                try (PreparedStatement statement = connection.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {

                    statement.setString(1, student.getName());
                    statement.setDouble(2, student.getStudyHours());
                    statement.executeUpdate();

                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (keys.next()) {
                            student.setId(keys.getInt(1));
                        }
                    }
                }

                saveEnrollments(connection, student);
                connection.commit();

                return student;

            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }

        } catch (SQLException exception) {
            throw new IllegalStateException("Could not create student", exception);
        }
    }

    public Student update(int id, Student student) {
        String sql = "UPDATE students SET name = ?, study_hours = ? WHERE id = ?";

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);

            try {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, student.getName());
                    statement.setDouble(2, student.getStudyHours());
                    statement.setInt(3, id);
                    statement.executeUpdate();
                }

                student.setId(id);
                saveEnrollments(connection, student);
                connection.commit();

                return student;

            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }

        } catch (SQLException exception) {
            throw new IllegalStateException("Could not update student " + id, exception);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM students WHERE id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;

        } catch (SQLException exception) {
            throw new IllegalStateException("Could not delete student " + id, exception);
        }
    }

    private void loadEnrollments(Connection connection, Student student)
            throws SQLException {

        String sql = "SELECT subject_id, study_hours FROM enrollments WHERE student_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, student.getId());

            try (ResultSet rows = statement.executeQuery()) {
                List<Enrollment> enrollments = new ArrayList<>();

                while (rows.next()) {
                    enrollments.add(new Enrollment(
                            rows.getLong("subject_id"),
                            rows.getDouble("study_hours")
                    ));
                }

                student.setEnrollments(enrollments);
            }
        }
    }

    private void saveEnrollments(Connection connection, Student student)
            throws SQLException {

        String deleteSql = "DELETE FROM enrollments WHERE student_id = ?";
        String insertSql = "INSERT INTO enrollments (student_id, subject_id, study_hours) "
                + "VALUES (?, ?, ?)";

        try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {
            deleteStatement.setInt(1, student.getId());
            deleteStatement.executeUpdate();
        }

        try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
            for (Enrollment enrollment : student.getEnrollments()) {
                insertStatement.setInt(1, student.getId());
                insertStatement.setLong(2, enrollment.getSubjectId());
                insertStatement.setDouble(3, enrollment.getStudyHours());
                insertStatement.addBatch();
            }

            insertStatement.executeBatch();
        }
    }

    private Student toStudent(ResultSet rows) throws SQLException {
        return new Student(
                rows.getInt("id"),
                rows.getString("name"),
                rows.getDouble("study_hours")
        );
    }
}
