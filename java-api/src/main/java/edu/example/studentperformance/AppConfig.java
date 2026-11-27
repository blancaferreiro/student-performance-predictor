
package edu.example.studentperformance;

/**
 * Configuración centralizada de la aplicación.
 * Implementa el patrón Singleton.
 */
public final class AppConfig {

    private static final AppConfig INSTANCE = new AppConfig();

    private final String databaseUrl;
    private final String databaseUser;
    private final String databasePassword;
    private final String reportServiceUrl;

    private AppConfig() {
        databaseUrl = getEnvironmentVariable(
                "DB_URL",
                "jdbc:mysql://localhost:3306/studentdb"
        );

        databaseUser = getEnvironmentVariable("DB_USER", "app");
        databasePassword = getEnvironmentVariable("DB_PASSWORD", "app");

        reportServiceUrl = getEnvironmentVariable(
                "REPORT_SERVICE_URL",
                "http://localhost:8001"
        );
    }

    public static AppConfig getInstance() {
        return INSTANCE;
    }

    private String getEnvironmentVariable(String name, String defaultValue) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public String getDatabaseUser() {
        return databaseUser;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public String getReportServiceUrl() {
        return reportServiceUrl;
    }
}
