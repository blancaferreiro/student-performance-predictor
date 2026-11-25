package edu.example.studentperformance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

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

        String sql = "SELECT e.subject_id, e.course_id, e.study_hours, e.grade, c.name AS course_name "
                + "FROM enrollments e "
                + "LEFT JOIN courses c ON e.course_id = c.id "
                + "WHERE e.student_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, student.getId());

            try (ResultSet rows = statement.executeQuery()) {
                List<Enrollment> enrollments = new ArrayList<>();

                while (rows.next()) {
                    Object gradeValue = rows.getObject("grade");
                    String courseName = rows.getString("course_name");

                    Enrollment enrollment = new Enrollment(
                            rows.getLong("subject_id"),
                            rows.getInt("course_id"),
                            rows.getDouble("study_hours"),
                            gradeValue == null ? null : rows.getDouble("grade")
                    );
                    enrollment.setCourse(courseName);
                    enrollment.setAcademicYear(courseName);
                    enrollments.add(enrollment);
                }

                student.setEnrollments(enrollments);
            }
        }
    }

    private void saveEnrollments(Connection connection, Student student)
            throws SQLException {

        String deleteSql = "DELETE FROM enrollments WHERE student_id = ?";
        String insertSql = "INSERT INTO enrollments "
                + "(student_id, subject_id, course_id, study_hours, grade) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {
            deleteStatement.setInt(1, student.getId());
            deleteStatement.executeUpdate();
        }

        if (student.getEnrollments() == null || student.getEnrollments().isEmpty()) {
            return;
        }

        try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
            for (Enrollment enrollment : student.getEnrollments()) {
                insertStatement.setInt(1, student.getId());
                insertStatement.setLong(2, enrollment.getSubjectId());
                insertStatement.setInt(3, enrollment.getCourseId());
                insertStatement.setDouble(4, enrollment.getStudyHours());

                if (enrollment.getGrade() == null) {
                    insertStatement.setNull(5, Types.DOUBLE);
                } else {
                    insertStatement.setDouble(5, enrollment.getGrade());
                }

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