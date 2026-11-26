package edu.example.studentperformance;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {

    private static final String URL = env("DB_URL", "jdbc:mysql://localhost:3306/studentdb?allowPublicKeyRetrieval=true&useSSL=false");
    private static final String USER = env("DB_USER", "app");
    private static final String PASSWORD = env("DB_PASSWORD", "app");

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void init() {
        SQLException lastError = null;

        for (int attempt = 1; attempt <= 30; attempt++) {
            try (Connection connection = getConnection();
                 Statement statement = connection.createStatement()) {

                statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS students (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "study_hours DOUBLE NOT NULL)"
                );

                statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS subjects (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "difficulty ENUM('EASY','MEDIUM','HARD') NOT NULL)"
                );

                statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS courses (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL UNIQUE, " +
                    "start_date DATE NOT NULL, " +
                    "end_date DATE NOT NULL)"
                );

                statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS enrollments (" +
                    "student_id INT NOT NULL, " +
                    "subject_id INT NOT NULL, " +
                    "course_id INT NOT NULL, " +
                    "study_hours DOUBLE NOT NULL CHECK (study_hours >= 0), " +
                    "grade DOUBLE NULL, " +
                    "PRIMARY KEY (student_id, subject_id, course_id), " +
                    "FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE RESTRICT)"
                );

                System.out.println("Database ready.");
                return;

            } catch (SQLException exception) {
                lastError = exception;
                System.out.println("Waiting for MySQL (attempt " + attempt + "/30): " + exception.getMessage());

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        throw new IllegalStateException("Could not connect to MySQL", lastError);
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value;
    }
}