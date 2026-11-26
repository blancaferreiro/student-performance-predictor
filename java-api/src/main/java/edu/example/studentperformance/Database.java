package edu.example.studentperformance;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {

    private static final String URL = env("DB_URL", "jdbc:mysql://localhost:3306/studentdb?allowPublicKeyRetrieval=true&useSSL=false");
    private static final String USER = env("DB_USER", "app");
    private static final String PASSWORD = env("DB_PASSWORD", "app");

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value;
    }
}