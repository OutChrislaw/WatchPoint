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
        boolean knownType = false;
        for (int i = 0; i < HAZARD_TYPES.length; i++) {
            if (HAZARD_TYPES[i].equals(hazardType)) {
                knownType = true;
            }
        }
        if (!knownType) {
            return "Please choose one of the listed hazard types.";
        }
        if (location == null) {
            return "Please select a City / Municipality and a Barangay.";
        }

        String city = oneLine(location.getCityMunicipality());
        if (city.length() == 0) {
            return "Please select a City / Municipality.";
        }
        String barangay = oneLine(location.getBarangay());
        if (barangay.length() == 0) {
            return "Please select a Barangay.";
        }

        String street = oneLine(location.getStreet());
        String place = oneLine(location.getSpecificPlace());
        String detail = oneLine(specificDetail);
        String text = oneLine(description);

        String forbidden = forbiddenCharacterProblem(street, place, detail, text);
        if (forbidden != null) {
            return forbidden;
        }

        if (place.length() < 2) {
            return "Please enter the specific place, like Near Water Refiller.";
        }
        if (place.length() > 60) {
            return "The specific place must be 60 characters or fewer.";
        }
        if (street.length() > 60) {
            return "The street must be 60 characters or fewer.";
        }
        if (detail.length() < 2) {
            return "Please enter the specific detail, like the pole number or depth.";
        }
        if (detail.length() > 80) {
            return "The specific detail must be 80 characters or fewer.";
        }
        if (text.length() < 10) {
            return "Please describe the hazard in at least 10 characters.";
        }
        if (text.length() > 500) {
            return "The description must be 500 characters or fewer.";
        }
        return null;
    }

    private static String forbiddenCharacterProblem(String street, String place, String detail,
                                                    String description) {
        String[] values = { street, place, detail, description };
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            if (value == null) {
                continue;
            }
            for (int j = 0; j < value.length(); j++) {
                if (value.charAt(j) == '|') {
                    return "The | character cannot be used in a report.";
                }
            }
        }
        return null;
    }

    /**
     * True when this resident already has a PENDING report with the same hazard
     * type, barangay and specific detail.
     */
    public boolean hasPendingDuplicate(String reporterId, String hazardType, String barangay,
                                       String specificDetail) {
        for (int i = 0; i < reports.size(); i++) {
            Report report = reports.get(i);
            if (report.getStatus() != ReportStatus.PENDING) {
                continue;
            }
            if (!report.getReporterId().equals(reporterId)) {
                continue;
            }
            if (!report.getReportType().equals(hazardType)) {
                continue;
            }
            if (!report.getLocation().getBarangay().equalsIgnoreCase(barangay.trim())) {
                continue;
            }
            if (report.getSpecificDetail().equalsIgnoreCase(specificDetail.trim())) {
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
        return updateReportStatusInCity(reportId, status, null, remark);
    }

    public boolean updateReportStatusInCity(String reportId, ReportStatus status,
                                            String cityMunicipality) {
        return updateReportStatusInCity(reportId, status, cityMunicipality, null);
    }

    public boolean updateReportStatusInCity(String reportId, ReportStatus status,
                                            String cityMunicipality, String remark) {
        Report report = getReportById(reportId);
        if (report == null) {
            return false;
        }
        if (cityMunicipality != null
                && !report.getLocation().getCityMunicipality().equals(cityMunicipality)) {
            return false;
        }
        if (status == ReportStatus.REJECTED && report.getStatus() == ReportStatus.RESOLVED) {
            return false;
        }
        if (status == ReportStatus.REJECTED && remarkProblem(remark) != null) {
            return false;
        }

        report.setStatus(status);
        report.setRemark(status == ReportStatus.REJECTED ? oneLine(remark) : "");
        saveReports();
        return true;
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
        if (text.length() > 200) {
            return "The reason must be 200 characters or fewer.";
        }
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '|') {
                return "The | character cannot be used in the reason.";
            }
        }
        return null;
    }

    /** Every status name, in display order, for the status picker. */
    public static String[] statusNames() {
        ReportStatus[] values = ReportStatus.values();
        String[] names = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            names[i] = values[i].name();
        }
        return names;
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
