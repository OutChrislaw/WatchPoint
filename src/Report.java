// Report.java
// ABSTRACT parent class of all hazard reports.
// It holds the common attributes every report must have.

public abstract class Report {

    // Private attributes (Encapsulation).
    private String reportId;
    private String reporterId;
    private String location;     // the full address text of the hazard
    private String description;
    private ReportStatus status;
    private String dateSubmitted;

    // Constructor used by all subclasses through super(...).
    public Report(String reportId, String reporterId, String location,
                  String description, ReportStatus status, String dateSubmitted) {
        this.reportId = reportId;
        this.reporterId = reporterId;
        this.location = location;
        this.description = description;
        this.status = status;
        this.dateSubmitted = dateSubmitted;
    }

    // Getters.
    public String getReportId() {
        return reportId;
    }

    public String getReporterId() {
        return reporterId;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public String getDateSubmitted() {
        return dateSubmitted;
    }

    // Setter that controls how the status can change.
    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    // Abstract methods. Each subclass returns its own hazard type and detail.
    public abstract String getReportType();

    public abstract String getSpecificDetail();

    // toFileString() uses the abstract methods above, so it works for every
    // subclass automatically. Format:
    // type|reportId|reporterId|location|description|status|dateSubmitted|specificDetail
    public String toFileString() {
        return getReportType() + "|" + reportId + "|" + reporterId + "|" + location + "|"
                + description + "|" + status.name() + "|" + dateSubmitted + "|"
                + getSpecificDetail();
    }

    // Overriding toString() from Object to show a readable report line.
    @Override
    public String toString() {
        return getReportType() + " #" + reportId + " at " + location
                + " [" + status.getLabel() + "]";
    }
}
