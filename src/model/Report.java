package model;

public abstract class Report {

    private String reportId;
    private String reporterId;
    private Location location;
    private String description;
    private ReportStatus status;
    private String dateSubmitted;
    private String remark;
    private String reporterName;
    private String reporterAddress;
    private String reporterContact;

    protected Report(String reportId, String reporterId, Location location,
                     String description, ReportStatus status, String dateSubmitted) {
        this.reportId = reportId;
        this.reporterId = reporterId;
        this.location = location;
        this.description = description;
        this.status = status;
        this.dateSubmitted = dateSubmitted;
        this.remark = "";
        this.reporterName = "";
        this.reporterAddress = "";
        this.reporterContact = "";
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

    /** True when the workflow allows this report to move to the given status. */
    public boolean canChangeStatusTo(ReportStatus target) {
        return status != null && status.canMoveTo(target);
    }

    public String getRemark() {
        if (remark == null) {
            return "";
        }
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    /** The reporter's full name at the time the report was filed. */
    public String getReporterName() {
        return reporterName == null ? "" : reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    /** The reporter's address at the time the report was filed. */
    public String getReporterAddress() {
        return reporterAddress == null ? "" : reporterAddress;
    }

    public void setReporterAddress(String reporterAddress) {
        this.reporterAddress = reporterAddress;
    }

    /** The reporter's contact number at the time the report was filed. */
    public String getReporterContact() {
        return reporterContact == null ? "" : reporterContact;
    }

    public void setReporterContact(String reporterContact) {
        this.reporterContact = reporterContact;
    }

    public abstract String getReportType();

    public abstract String getSpecificDetail();

    public abstract void setSpecificDetail(String specificDetail);

    public String toFileString() {
        return getReportType() + "|" + reportId + "|" + reporterId + "|"
                + location.getCityMunicipality() + "|" + location.getBarangay() + "|"
                + location.getStreet() + "|" + location.getSpecificPlace() + "|"
                + description + "|" + getSpecificDetail() + "|"
                + status.name() + "|" + dateSubmitted + "|" + getRemark() + "|"
                + getReporterName() + "|" + getReporterAddress() + "|" + getReporterContact();
    }
}
