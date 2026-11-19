package edu.example.studentperformance;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gestiona la conexión a MySQL y crea las tablas al arrancar.
 * Los datos de conexión se leen de variables de entorno; los valores
 * por defecto permiten ejecutar con "mvn compile exec:java" en local.
 */
public final class Database {
    private static final AppConfig CONFIG = AppConfig.getInstance();

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            CONFIG.getDatabaseUrl(),
            CONFIG.getDatabaseUser(),
            CONFIG.getDatabasePassword()
        );
    }

    /**
     * Crea las tablas si no existen. Reintenta porque MySQL puede tardar
     * unos segundos en aceptar conexiones aunque su contenedor ya esté levantado.
     */
    public static void init() {
        SQLException lastError = null;
        for (int attempt = 1; attempt <= 30; attempt++) {
            try (Connection connection = getConnection();
                 Statement statement = connection.createStatement()) {

                statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS students ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "name VARCHAR(255) NOT NULL, "
                    + "study_hours DOUBLE NOT NULL)");

                statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS subjects ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "name VARCHAR(255) NOT NULL, "
                    + "difficulty ENUM('EASY','MEDIUM','HARD') NOT NULL)");

                statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS enrollments ("
                    + "student_id INT NOT NULL, "
                    + "subject_id INT NOT NULL, "
                    + "study_hours DOUBLE NOT NULL CHECK (study_hours >= 0), "
                    + "PRIMARY KEY (student_id, subject_id), "
                    + "FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE, "
                    + "FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE)");

                System.out.println("Database ready.");
                return;
            } catch (SQLException exception) {
                lastError = exception;
                System.out.println("Waiting for MySQL (attempt " + attempt + "/30)...");
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

}