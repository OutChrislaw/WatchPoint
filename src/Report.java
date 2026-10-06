/**
 * Report.java
 * Abstract parent class of all hazard reports in WatchPoint.
 */
public abstract class Report {

    private String reportId;
    private String reporterId;
    private Location location;
    private String description;
    private ReportStatus status;
    private String dateSubmitted;

    protected Report(String reportId, String reporterId, Location location,
                     String description, ReportStatus status, String dateSubmitted) {
        this.reportId = reportId;
        this.reporterId = reporterId;
        this.location = location;
        this.description = description;
        this.status = status;
        this.dateSubmitted = dateSubmitted;
    }

    public String getReportId() {
        return reportId;
    }

    public String getReporterId() {
        return reporterId;
    }

    public Location getLocation() {
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

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public abstract String getReportType();

    public abstract String getSpecificDetail();

    public String toFileString() {
        return getReportType() + "|" + reportId + "|" + reporterId + "|"
                + location.getCityMunicipality() + "|" + location.getBarangay() + "|"
                + location.getStreet() + "|" + location.getSpecificPlace() + "|"
                + description + "|" + getSpecificDetail() + "|"
                + status.name() + "|" + dateSubmitted;
    }
}
