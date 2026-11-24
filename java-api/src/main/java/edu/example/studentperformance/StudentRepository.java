
package edu.example.studentperformance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
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

    /**
     * Recupera todas las matrículas históricas de un estudiante.
     */
    private void loadEnrollments(Connection connection, Student student)
            throws SQLException {

        String sql = "SELECT subject_id, course_id, study_hours, grade "
                + "FROM enrollments WHERE student_id = ? "
                + "ORDER BY course_id, subject_id";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, student.getId());

            try (ResultSet rows = statement.executeQuery()) {
                List<Enrollment> enrollments = new ArrayList<>();

                while (rows.next()) {
                    double gradeValue = rows.getDouble("grade");
                    Double grade = rows.wasNull() ? null : gradeValue;

                    enrollments.add(new Enrollment(
                            rows.getLong("subject_id"),
                            rows.getInt("course_id"),
                            rows.getDouble("study_hours"),
                            grade
                    ));
                }

                student.setEnrollments(enrollments);
            }
        }
    }

    /**
     * Inserta las matrículas nuevas o actualiza las existentes.
     * No elimina las matrículas históricas.
     * Si la nota recibida es null, conserva la nota anterior.
     */
    private void saveEnrollments(Connection connection, Student student)
            throws SQLException {

        String sql = "INSERT INTO enrollments "
                + "(student_id, subject_id, course_id, study_hours, grade) "
                + "VALUES (?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "study_hours = VALUES(study_hours), "
                + "grade = COALESCE(VALUES(grade), grade)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            for (Enrollment enrollment : student.getEnrollments()) {

                if (enrollment.getCourseId() == null) {
                    throw new SQLException(
                            "Every enrollment must have an academic course"
                    );
                }

                statement.setInt(1, student.getId());
                statement.setLong(2, enrollment.getSubjectId());
                statement.setInt(3, enrollment.getCourseId());
                statement.setDouble(4, enrollment.getStudyHours());

                if (enrollment.getGrade() == null) {
                    statement.setNull(5, Types.DOUBLE);
                } else {
                    statement.setDouble(5, enrollment.getGrade());
                }

                statement.addBatch();
            }

            statement.executeBatch();
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
