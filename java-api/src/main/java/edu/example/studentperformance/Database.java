package edu.example.studentperformance;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        AppConfig config = AppConfig.getInstance();
        return DriverManager.getConnection(
                config.getDatabaseUrl(),
                config.getDatabaseUser(),
                config.getDatabasePassword()
        );
    }
}
