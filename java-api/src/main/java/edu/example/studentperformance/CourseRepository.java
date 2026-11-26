package edu.example.studentperformance;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona la persistencia de cursos académicos en la tabla "courses" de MySQL (HU-01).
 */
public class CourseRepository {

    public List<Course> findAll() {
        String sql = "SELECT id, name, start_date, end_date FROM courses ORDER BY start_date DESC, id DESC";
        List<Course> courses = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                courses.add(mapRow(rows));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not read courses", exception);
        }
        return courses;
    }

    public Course findById(int id) {
        String sql = "SELECT id, name, start_date, end_date FROM courses WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                if (rows.next()) {
                    return mapRow(rows);
                }
                return null;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not read course " + id, exception);
        }
    }

    public boolean existsByName(String name) {
        String sql = "SELECT 1 FROM courses WHERE LOWER(name) = LOWER(?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not check course name", exception);
        }
    }

    public boolean existsByNameAndIdNot(String name, int id) {
        String sql = "SELECT 1 FROM courses WHERE LOWER(name) = LOWER(?) AND id <> ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setInt(2, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not check duplicate course name", exception);
        }
    }

    public Course create(Course course) {
        String sql = "INSERT INTO courses (name, start_date, end_date) VALUES (?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, course.getName());
            statement.setDate(2, Date.valueOf(course.getStartDate()));
            statement.setDate(3, Date.valueOf(course.getEndDate()));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    course.setId(keys.getInt(1));
                }
            }
            return course;
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not create course", exception);
        }
    }

    public Course update(Course course) {
        String sql = "UPDATE courses SET name = ?, start_date = ?, end_date = ? WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, course.getName());
            statement.setDate(2, Date.valueOf(course.getStartDate()));
            statement.setDate(3, Date.valueOf(course.getEndDate()));
            statement.setInt(4, course.getId());
            int updated = statement.executeUpdate();
            if (updated == 0) {
                return null;
            }
            return course;
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not update course " + course.getId(), exception);
        }
    }

    public boolean hasEnrollments(int courseId) {
        String checkColumnSql =
                "SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'enrollments' AND COLUMN_NAME = 'course_id'";
        try (Connection connection = Database.getConnection();
             PreparedStatement checkStmt = connection.prepareStatement(checkColumnSql);
             ResultSet colRs = checkStmt.executeQuery()) {
            if (!colRs.next()) {
                return false;
            }

            String countSql = "SELECT 1 FROM enrollments WHERE course_id = ? LIMIT 1";
            try (PreparedStatement countStmt = connection.prepareStatement(countSql)) {
                countStmt.setInt(1, courseId);
                try (ResultSet rows = countStmt.executeQuery()) {
                    return rows.next();
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not verify course enrollments", exception);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM courses WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not delete course " + id, exception);
        }
    }

    private Course mapRow(ResultSet rows) throws SQLException {
        return new Course(
                rows.getInt("id"),
                rows.getString("name"),
                rows.getDate("start_date").toString(),
                rows.getDate("end_date").toString()
        );
    }
}