package manager;

import model.Report;
import model.RoadHazardReport;
import model.FloodReport;
import model.StreetlightReport;
import model.OtherHazardReport;
import model.Location;
import model.ReportStatus;
import storage.FileManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ReportManager {

    private List<Report> reports;
    private FileManager fileManager;

    public ReportManager(FileManager fileManager) {
        this.fileManager = fileManager;
        this.reports = new ArrayList<Report>();
        loadReports();
    }

    public Report submitReport(String reporterId, String hazardType, Location location,
                               String description, String specificDetail) {
        if (reportProblem(hazardType, location, description, specificDetail) != null) {
            return null;
        }

        description = oneLine(description);
        specificDetail = specificDetail.trim();
        location = new Location(location.getCityMunicipality().trim(),
                location.getBarangay().trim(), location.getStreet().trim(),
                location.getSpecificPlace().trim());

        String reportId = generateReportId();
        String dateSubmitted = currentTimestamp();
        Report report;

        if (hazardType.equals("Road Hazard")) {
            report = new RoadHazardReport(reportId, reporterId, location, description,
                    ReportStatus.PENDING, dateSubmitted, specificDetail);
        } else if (hazardType.equals("Flood")) {
            report = new FloodReport(reportId, reporterId, location, description,
                    ReportStatus.PENDING, dateSubmitted, specificDetail);
        } else if (hazardType.equals("Streetlight")) {
            report = new StreetlightReport(reportId, reporterId, location, description,
                    ReportStatus.PENDING, dateSubmitted, specificDetail);
        } else {
            report = new OtherHazardReport(reportId, reporterId, location, description,
                    ReportStatus.PENDING, dateSubmitted, specificDetail);
        }

        reports.add(report);
        saveReports();
        return report;
    }

    public Report getReportById(String reportId) {
        for (int i = 0; i < reports.size(); i++) {
            if (reports.get(i).getReportId().equals(reportId)) {
                return reports.get(i);
            }
        }
        return null;
    }

    public List<Report> getReportsByReporter(String reporterId) {
        List<Report> result = new ArrayList<Report>();
        for (int i = 0; i < reports.size(); i++) {
            if (reports.get(i).getReporterId().equals(reporterId)) {
                result.add(reports.get(i));
            }
        }
        return result;
    }

    public List<Report> getAllReports() {
        return new ArrayList<Report>(reports);
    }

    /**
     * The hazard types the system accepts, matching the report classes.
     */
    public static final String[] HAZARD_TYPES = { "Road Hazard", "Flood", "Streetlight", "Other" };

    /**
     * Collapses every run of spaces, tabs and line breaks into one space, so a
     * description typed on several lines is still saved as a single line.
     */
    public static String oneLine(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("\\s+", " ").trim();
    }

    /**
     * The rules for a new report. Returns the problem to show, or null when the
     * report is acceptable.
     */
    public static String reportProblem(String hazardType, Location location, String description,
                                       String specificDetail) {
        if (!isHazardType(hazardType)) {
            return "Please choose one of the listed hazard types.";
        }
        if (location == null) {
            return "Please select a City / Municipality and a Barangay.";
        }

        String city = oneLine(location.getCityMunicipality());
        String barangay = oneLine(location.getBarangay());
        String street = oneLine(location.getStreet());
        String place = oneLine(location.getSpecificPlace());
        String detail = oneLine(specificDetail);
        String text = oneLine(description);

        return firstProblem(
                city.length() == 0 ? "Please select a City / Municipality." : null,
                barangay.length() == 0 ? "Please select a Barangay." : null,
                pipeProblem(street, place, detail, text),
                place.length() < 2
                        ? "Please enter the specific place, like Near Water Refiller." : null,
                maxProblem(place, "specific place", 60),
                maxProblem(street, "street", 60),
                detail.length() < 2
                        ? "Please enter the specific detail, like the pole number or depth." : null,
                maxProblem(detail, "specific detail", 80),
                text.length() < 10 ? "Please describe the hazard in at least 10 characters." : null,
                maxProblem(text, "description", 500));
    }

    // ------------------------------------------------------------------- helpers

    /** True when the text is one of the listed hazard types. */
    private static boolean isHazardType(String hazardType) {
        for (int i = 0; i < HAZARD_TYPES.length; i++) {
            if (HAZARD_TYPES[i].equals(hazardType)) {
                return true;
            }
        }
        return false;
    }

    /** True when the text holds the pipe character, which the data file cannot store. */
    private static boolean hasPipe(String text) {
        return text != null && text.indexOf('|') >= 0;
    }

    /** The first message in the list, or null when every value is null. */
    private static String firstProblem(String... problems) {
        for (int i = 0; i < problems.length; i++) {
            if (problems[i] != null) {
                return problems[i];
            }
        }
        return null;
    }

    /** The pipe rule for every text of a report, or null. */
    private static String pipeProblem(String... values) {
        for (int i = 0; i < values.length; i++) {
            if (hasPipe(values[i])) {
                return "The | character cannot be used in a report.";
            }
        }
        return null;
    }

    /** The "not more than" rule for a text value, or null. */
    private static String maxProblem(String text, String caption, int max) {
        return text.length() > max
                ? "The " + caption + " must be " + max + " characters or fewer." : null;
    }

    /**
     * True when this resident already has a PENDING report with the same hazard
     * type, barangay and specific detail.
     */
    public boolean hasPendingDuplicate(String reporterId, String hazardType, String barangay,
                                       String specificDetail) {
        String area = barangay == null ? "" : barangay.trim();
        String detail = specificDetail == null ? "" : specificDetail.trim();
        for (int i = 0; i < reports.size(); i++) {
            Report report = reports.get(i);
            boolean same = report.getStatus() == ReportStatus.PENDING
                    && report.getReporterId().equals(reporterId)
                    && report.getReportType().equals(hazardType)
                    && report.getLocation().getBarangay().equalsIgnoreCase(area)
                    && report.getSpecificDetail().equalsIgnoreCase(detail);
            if (same) {
                return true;
            }
        }
        return false;
    }

    public boolean updateResidentReport(Report report, String residentId) {
        if (report == null) {
            return false;
        }
        if (!report.getReporterId().equals(residentId)) {
            return false;
        }
        if (report.getStatus() != ReportStatus.PENDING) {
            return false;
        }

        saveReports();
        return true;
    }

    public boolean deleteResidentReport(String reportId, String residentId) {
        Report report = getReportById(reportId);
        if (report == null) {
            return false;
        }
        if (!report.getReporterId().equals(residentId)) {
            return false;
        }
        if (report.getStatus() != ReportStatus.PENDING) {
            return false;
        }

        reports.remove(report);
        saveReports();
        return true;
    }

    public boolean updateReportStatus(String reportId, ReportStatus status) {
        return updateReportStatus(reportId, status, null);
    }

    public boolean updateReportStatus(String reportId, ReportStatus status, String remark) {
        return changeStatus(getReportById(reportId), status, remark) == null;
    }

    public boolean updateReportStatusInCity(String reportId, ReportStatus status,
                                            String cityMunicipality) {
        return updateReportStatusInCity(reportId, status, cityMunicipality, null);
    }

    public boolean updateReportStatusInCity(String reportId, ReportStatus status,
                                            String cityMunicipality, String remark) {
        return changeStatusInCity(getReportById(reportId), status, remark, cityMunicipality) == null;
    }

    /**
     * The workflow rule for one status change. Returns the message to show, or
     * null when the change is allowed.
     */
    public static String statusProblem(Report report, ReportStatus target, String reason) {
        if (report == null) {
            return "That report is no longer in the records.";
        }
        if (target == null) {
            return "Please choose the new status.";
        }

        ReportStatus current = report.getStatus();
        if (current == null) {
            return "This report has no status yet.";
        }
        if (target == current) {
            return "Report " + report.getReportId() + " is already " + current.displayName() + ".";
        }
        if (current == ReportStatus.REJECTED) {
            return "A rejected report cannot be changed. Only an administrator can reopen it.";
        }
        if (current == ReportStatus.RESOLVED) {
            return "This report is already RESOLVED and can no longer be changed.";
        }
        if (!current.canMoveTo(target)) {
            return "A " + current.displayName() + " report can only move to "
                    + nextStatusesText(current) + ".";
        }
        return target == ReportStatus.REJECTED ? remarkProblem(reason) : null;
    }

    private static String nextStatusesText(ReportStatus status) {
        List<ReportStatus> next = status.nextStatuses();
        String text = "";
        for (int i = 0; i < next.size(); i++) {
            if (i > 0) {
                text = text + (i == next.size() - 1 ? " or " : ", ");
            }
            text = text + next.get(i).displayName();
        }
        return text;
    }

    /**
     * Validates, applies and saves a status change. Returns null on success, or
     * the message explaining why the change was denied.
     */
    public String changeStatus(Report report, ReportStatus target, String reason) {
        return changeStatusInCity(report, target, reason, null);
    }

    public String changeStatusInCity(Report report, ReportStatus target, String reason,
                                     String cityMunicipality) {
        if (report == null || getReportById(report.getReportId()) == null) {
            return "That report is no longer in the records.";
        }
        if (cityMunicipality != null
                && !report.getLocation().getCityMunicipality().equals(cityMunicipality)) {
            return "This report is not inside your area, so its status cannot be changed.";
        }

        String problem = statusProblem(report, target, reason);
        if (problem != null) {
            return problem;
        }

        report.setStatus(target);
        report.setRemark(target == ReportStatus.REJECTED ? oneLine(reason) : "");
        saveReports();
        return null;
    }

    /**
     * Reopens a rejected report: it goes back to PENDING and the rejection
     * reason is cleared. Only an administrator may do this.
     */
    public String reopenReport(Report report, boolean administrator) {
        if (!administrator) {
            return "Only an administrator can reopen a rejected report.";
        }
        if (report == null || getReportById(report.getReportId()) == null) {
            return "That report is no longer in the records.";
        }
        if (report.getStatus() != ReportStatus.REJECTED) {
            return "Only a rejected report can be reopened.";
        }

        report.setStatus(ReportStatus.PENDING);
        report.setRemark("");
        saveReports();
        return null;
    }

    public boolean deleteInvalidReport(String reportId) {
        Report report = getReportById(reportId);
        if (report == null) {
            return false;
        }

        reports.remove(report);
        saveReports();
        return true;
    }

    public List<Report> getReports(String cityMunicipality, String barangay, String sortMode) {
        List<Report> result = new ArrayList<Report>();
        for (int i = 0; i < reports.size(); i++) {
            Report report = reports.get(i);
            if (cityMunicipality != null && !report.getLocation().getCityMunicipality().equals(cityMunicipality)) {
                continue;
            }
            if (barangay != null && !report.getLocation().getBarangay().equals(barangay)) {
                continue;
            }
            result.add(report);
        }

        sort(result, sortMode);
        return result;
    }

    public List<Report> getReportsInCity(String cityMunicipality) {
        return getReports(cityMunicipality, null, null);
    }

    /**
     * Number of reports in one area (null = the whole province) with the given
     * status (null = every status).
     */
    public int countReports(String cityMunicipality, ReportStatus status) {
        int total = 0;
        for (int i = 0; i < reports.size(); i++) {
            Report report = reports.get(i);
            if (cityMunicipality != null
                    && !report.getLocation().getCityMunicipality().equals(cityMunicipality)) {
                continue;
            }
            if (status != null && report.getStatus() != status) {
                continue;
            }
            total++;
        }
        return total;
    }

    /** Reports per barangay, busiest first, as { barangay, count } pairs. */
    public List<String[]> countByBarangay(String cityMunicipality) {
        return countBy(cityMunicipality, true);
    }

    /** Reports per hazard type, most common first, as { type, count } pairs. */
    public List<String[]> countByHazardType(String cityMunicipality) {
        return countBy(cityMunicipality, false);
    }

    private List<String[]> countBy(String cityMunicipality, boolean byBarangay) {
        List<String> names = new ArrayList<String>();
        List<Integer> totals = new ArrayList<Integer>();

        for (int i = 0; i < reports.size(); i++) {
            Report report = reports.get(i);
            if (cityMunicipality != null
                    && !report.getLocation().getCityMunicipality().equals(cityMunicipality)) {
                continue;
            }
            if (byBarangay) {
                addTotal(names, totals, report.getLocation().getBarangay());
            } else {
                addTotal(names, totals, report.getReportType());
            }
        }

        for (int i = 0; i < totals.size(); i++) {
            for (int j = i + 1; j < totals.size(); j++) {
                if (totals.get(j).intValue() > totals.get(i).intValue()) {
                    String name = names.get(i);
                    names.set(i, names.get(j));
                    names.set(j, name);

                    Integer total = totals.get(i);
                    totals.set(i, totals.get(j));
                    totals.set(j, total);
                }
            }
        }

        List<String[]> result = new ArrayList<String[]>();
        for (int i = 0; i < names.size(); i++) {
            result.add(new String[] { names.get(i), String.valueOf(totals.get(i)) });
        }
        return result;
    }

    private static void addTotal(List<String> names, List<Integer> totals, String name) {
        for (int i = 0; i < names.size(); i++) {
            if (names.get(i).equals(name)) {
                totals.set(i, Integer.valueOf(totals.get(i).intValue() + 1));
                return;
            }
        }
        names.add(name);
        totals.add(Integer.valueOf(1));
    }

    /** The PENDING report that has been waiting the longest in one area. */
    public Report oldestPending(String cityMunicipality) {
        Report oldest = null;
        for (int i = 0; i < reports.size(); i++) {
            Report report = reports.get(i);
            if (cityMunicipality != null
                    && !report.getLocation().getCityMunicipality().equals(cityMunicipality)) {
                continue;
            }
            if (report.getStatus() != ReportStatus.PENDING) {
                continue;
            }
            if (oldest == null
                    || report.getDateSubmitted().compareTo(oldest.getDateSubmitted()) < 0) {
                oldest = report;
            }
        }
        return oldest;
    }

    /**
     * Whole days between the submitted timestamp and now, or -1 when the
     * timestamp cannot be read.
     */
    public static int daysWaiting(String dateSubmitted) {
        if (dateSubmitted == null || dateSubmitted.trim().length() < 10) {
            return -1;
        }
        try {
            String value = dateSubmitted.trim();
            java.time.LocalDateTime submitted;
            if (value.length() <= 10) {
                submitted = java.time.LocalDate.parse(value).atStartOfDay();
            } else {
                submitted = java.time.LocalDateTime.parse(value,
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            }
            long days = java.time.Duration.between(submitted, java.time.LocalDateTime.now()).toDays();
            if (days < 0) {
                return 0;
            }
            return (int) days;
        } catch (Exception error) {
            return -1;
        }
    }

    /** Rules for the reason written when a report is rejected. */
    public static String remarkProblem(String remark) {
        if (remark == null || remark.trim().length() < 5) {
            return "Please write at least 5 characters to explain why the report is rejected.";
        }

        String text = oneLine(remark);
        return firstProblem(
                maxProblem(text, "reason", 200),
                hasPipe(text) ? "The | character cannot be used in the reason." : null);
    }

    /** Reads a status name, falling back to PENDING for anything unknown. */
    public static ReportStatus parseStatus(String text) {
        if (text == null) {
            return ReportStatus.PENDING;
        }
        String value = text.trim().toUpperCase();
        if (value.equals("VERIFIED")) {
            return ReportStatus.VERIFIED;
        }
        if (value.equals("IN_PROGRESS")) {
            return ReportStatus.IN_PROGRESS;
        }
        if (value.equals("RESOLVED")) {
            return ReportStatus.RESOLVED;
        }
        if (value.equals("REJECTED")) {
            return ReportStatus.REJECTED;
        }
        return ReportStatus.PENDING;
    }

    private static void sort(List<Report> list, String sortMode) {
        if (sortMode == null) {
            return;
        }

        final String mode = sortMode;
        Collections.sort(list, new Comparator<Report>() {
            @Override
            public int compare(Report first, Report second) {
                if (mode.equals("Report ID (Newest first)")) {
                    return second.getReportId().compareTo(first.getReportId());
                }
                if (mode.equals("Date and Time (Newest first)")) {
                    return second.getDateSubmitted().compareTo(first.getDateSubmitted());
                }
                if (mode.equals("Date and Time (Oldest first)")) {
                    return first.getDateSubmitted().compareTo(second.getDateSubmitted());
                }
                if (mode.equals("Status")) {
                    return first.getStatus().name().compareTo(second.getStatus().name());
                }
                if (mode.equals("Hazard Type")) {
                    return first.getReportType().compareTo(second.getReportType());
                }
                return first.getReportId().compareTo(second.getReportId());
            }
        });
    }

    private void loadReports() {
        List<String> lines = fileManager.readLines("reports.txt");
        for (int i = 0; i < lines.size(); i++) {
            Report report = parseReport(lines.get(i));
            if (report != null) {
                reports.add(report);
            }
        }
    }

    private void saveReports() {
        List<String> lines = new ArrayList<String>();
        for (int i = 0; i < reports.size(); i++) {
            lines.add(reports.get(i).toFileString());
        }
        fileManager.writeLines("reports.txt", lines);
    }

    private Report parseReport(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 11) {
            return null;
        }

        String type = parts[0];
        String reportId = parts[1];
        String reporterId = parts[2];
        Location location = new Location(parts[3], parts[4], parts[5], parts[6]);
        String description = parts[7];
        String specificDetail = parts[8];

        ReportStatus status = parseStatus(parts[9]);
        String dateSubmitted = parts[10];
        String remark = parts.length > 11 ? parts[11] : "";

        Report report;
        if (type.equals("Road Hazard")) {
            report = new RoadHazardReport(reportId, reporterId, location, description,
                    status, dateSubmitted, specificDetail);
        } else if (type.equals("Flood")) {
            report = new FloodReport(reportId, reporterId, location, description,
                    status, dateSubmitted, specificDetail);
        } else if (type.equals("Streetlight")) {
            report = new StreetlightReport(reportId, reporterId, location, description,
                    status, dateSubmitted, specificDetail);
        } else {
            report = new OtherHazardReport(reportId, reporterId, location, description,
                    status, dateSubmitted, specificDetail);
        }
        report.setRemark(remark);
        return report;
    }

    private String currentTimestamp() {
        String now = LocalDateTime.now().toString().replace('T', ' ');
        if (now.length() > 16) {
            now = now.substring(0, 16);
        }
        return now;
    }

    private String generateReportId() {
        int highest = 0;
        for (int i = 0; i < reports.size(); i++) {
            String reportId = reports.get(i).getReportId();
            if (reportId != null && reportId.length() > 1) {
                try {
                    int number = Integer.parseInt(reportId.substring(1));
                    if (number > highest) {
                        highest = number;
                    }
                } catch (Exception e) {
                    // ids that are not in the R<number> format are ignored
                }
            }
        }

        String number = "" + (highest + 1);
        while (number.length() < 3) {
            number = "0" + number;
        }
        return "R" + number;
    }
}
