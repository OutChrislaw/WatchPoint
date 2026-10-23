package manager;

import model.Report;
import model.RoadHazardReport;
import model.FloodReport;
import model.StreetlightReport;
import model.OtherHazardReport;
import model.Location;
import model.ReportStatus;
import model.Resident;
import model.User;
import storage.FileManager;
import validation.ReportRules;
import validation.TextRules;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ReportManager {

    private List<Report> reports;
    private FileManager fileManager;
    private UserManager userManager;

    /** Every report carries its own reporter details, so no account is needed. */
    public ReportManager(FileManager fileManager) {
        this(fileManager, null);
    }

    /** The accounts are only used to fill reports saved before the details were stored. */
    public ReportManager(FileManager fileManager, UserManager userManager) {
        this.fileManager = fileManager;
        this.userManager = userManager;
        this.reports = new ArrayList<Report>();
        loadReports();
        fileManager.reserveNumber("R", highestReportNumber());
        fileManager.reserveNumber("U", highestReporterNumber());
    }

    public Report submitReport(Resident reporter, String hazardType, Location location,
                               String description, String specificDetail) {
        if (reporter == null) {
            return null;
        }
        if (ReportRules.reportProblem(hazardType, location, description, specificDetail) != null) {
            return null;
        }

        String reporterId = reporter.getUserId();

        description = TextRules.oneLine(description);
        specificDetail = specificDetail.trim();
        location = new Location(location.getCityMunicipality().trim(),
                location.getBarangay().trim(), location.getStreet().trim(),
                location.getSpecificPlace().trim());

        String reportId = generateReportId();
        if (reportId == null) {
            return null;
        }
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

        report.setReporterName(stored(reporter.getFullName()));
        report.setReporterAddress(stored(reporter.getAddress()));
        report.setReporterContact(stored(reporter.getContactNumber()));

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

    /** How many reports one account filed. */
    public int countReportsByReporter(String reporterId) {
        return getReportsByReporter(reporterId).size();
    }

    /** The reporter's full name saved with the report, or the account when the line is old. */
    public String reporterName(Report report) {
        if (report == null) {
            return "";
        }
        if (report.getReporterName().length() > 0) {
            return report.getReporterName();
        }
        Resident account = reporterAccount(report);
        return account == null ? "" : account.getFullName();
    }

    /** The reporter's address saved with the report, or the account when the line is old. */
    public String reporterAddress(Report report) {
        if (report == null) {
            return "";
        }
        if (report.getReporterAddress().length() > 0) {
            return report.getReporterAddress();
        }
        Resident account = reporterAccount(report);
        return account == null ? "" : account.getAddress();
    }

    /** The reporter's contact number saved with the report, or the account when the line is old. */
    public String reporterContact(Report report) {
        if (report == null) {
            return "";
        }
        if (report.getReporterContact().length() > 0) {
            return report.getReporterContact();
        }
        Resident account = reporterAccount(report);
        return account == null ? "" : account.getContactNumber();
    }

    /** The account behind a report, or null when it was deleted or no accounts are loaded. */
    private Resident reporterAccount(Report report) {
        if (userManager == null || report == null) {
            return null;
        }
        User user = userManager.getUserById(report.getReporterId());
        if (user instanceof Resident) {
            return (Resident) user;
        }
        return null;
    }

    /** Keeps a saved reporter value on one line and free of the field separator. */
    private static String stored(String value) {
        return TextRules.oneLine(value).replace('|', '-');
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

        String problem = ReportRules.statusProblem(report, target, reason);
        if (problem != null) {
            return problem;
        }

        report.setStatus(target);
        report.setRemark(target == ReportStatus.REJECTED ? TextRules.oneLine(reason) : "");
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
        String[] parts = line.split("\\|", -1);
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
        if (parts.length > 12) {
            report.setReporterName(parts[12]);
        }
        if (parts.length > 13) {
            report.setReporterAddress(parts[13]);
        }
        if (parts.length > 14) {
            report.setReporterContact(parts[14]);
        }
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
        int number = fileManager.nextNumber("R");
        if (number < 0) {
            return null;
        }

        String text = "" + number;
        while (text.length() < 3) {
            text = "0" + text;
        }
        return "R" + text;
    }

    /** The highest report number stored, so a deleted id is never handed out again. */
    private int highestReportNumber() {
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
        return highest;
    }

    /** Reports keep the id of a deleted account, so that number stays reserved as well. */
    private int highestReporterNumber() {
        int highest = 0;
        for (int i = 0; i < reports.size(); i++) {
            String reporterId = reports.get(i).getReporterId();
            if (reporterId != null && reporterId.startsWith("U") && reporterId.length() > 1) {
                try {
                    int number = Integer.parseInt(reporterId.substring(1));
                    if (number > highest) {
                        highest = number;
                    }
                } catch (Exception e) {
                    // ids that are not in the U<number> format are ignored
                }
            }
        }
        return highest;
    }
}
