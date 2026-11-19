package edu.example.studentperformance;

public enum ReportFormat {
    CSV("csv", "text/csv; charset=utf-8"),
    JSON("json", "application/json; charset=utf-8");

    private final String extension;
    private final String mediaType;

    ReportFormat(String extension, String mediaType) {
        this.extension = extension;
        this.mediaType = mediaType;
    }

    public String getExtension() {
        return extension;
    }

    public String getMediaType() {
        return mediaType;
    }

    public static ReportFormat from(String value) {
        if (value == null || value.isBlank()) {
            return JSON;
        }

        for (ReportFormat format : values()) {
            if (format.extension.equalsIgnoreCase(value)
                    || format.name().equalsIgnoreCase(value)) {
                return format;
            }
        }

        throw new IllegalArgumentException("Format must be csv or json");
    }
}
