package edu.example.studentperformance;

public class ReportResult {
    private final String filename;
    private final String mediaType;
    private final String content;

    public ReportResult(String filename, String mediaType, String content) {
        this.filename = filename;
        this.mediaType = mediaType;
        this.content = content;
    }

    public String getFilename() {
        return filename;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getContent() {
        return content;
    }
}
