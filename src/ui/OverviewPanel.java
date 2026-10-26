package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import manager.LocationManager;
import manager.ReportManager;
import model.Report;
import model.ReportStatus;
import validation.ReportRules;

/**
 * OverviewPanel.java
 * The summary tab of the dashboards: how many reports there are per status, per
 * hazard type and per barangay, plus the report that has been waiting longest.
 *
 * Three filters narrow every number - city / municipality (administrator only),
 * barangay and hazard type - so the same panel answers questions like "how many
 * Flood reports are still PENDING in Cogon?". An Area Officer is tied to its own
 * municipality, so that panel has no city filter. Every number is counted from
 * the reports already loaded, so nothing is stored twice.
 */
public class OverviewPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int BARS = 6;

    private static final String ALL_CITIES = "All municipalities";
    private static final String ALL_BARANGAYS = "All barangays";
    private static final String ALL_TYPES = "All types";

    private final ReportManager reportManager;
    private final LocationManager locationManager;
    private final String fixedCity;

    private final JComboBox<String> cityBox;
    private final JComboBox<String> barangayBox;
    private final JComboBox<String> typeBox;
    private final JLabel filterLabel;
    private final JPanel content;

    /**
     * Pass the city / municipality of an Area Officer, or null for the
     * administrator, who may filter by any city.
     */
    public OverviewPanel(ReportManager reportManager, LocationManager locationManager,
                         String cityMunicipality) {
        this.reportManager = reportManager;
        this.locationManager = locationManager;
        this.fixedCity = cityMunicipality;

        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.PAGE);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 16, 14, 16));

        cityBox = (fixedCity == null)
                ? UITheme.combo(ALL_CITIES, locationManager.getMunicipalities()) : null;
        barangayBox = UITheme.combo(ALL_BARANGAYS, new ArrayList<String>());
        typeBox = UITheme.combo(ALL_TYPES, hazardTypes());
        filterLabel = UITheme.label("", UITheme.FONT_SMALL, UITheme.MUTED);
        content = UITheme.cardBody();

        add(buildHeader(), BorderLayout.NORTH);
        add(UITheme.borderedScroll(content), BorderLayout.CENTER);

        if (cityBox != null) {
            cityBox.addActionListener(e -> {
                fillBarangays();
                refresh();
            });
        }
        barangayBox.addActionListener(e -> refresh());
        typeBox.addActionListener(e -> refresh());

        fillBarangays();
        refresh();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 10));
        header.setBackground(UITheme.PAGE);

        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setBackground(UITheme.PAGE);
        top.add(UITheme.label(title(), UITheme.FONT_HEADING, UITheme.PRIMARY), BorderLayout.WEST);

        JButton refreshButton = UITheme.button("Refresh", UITheme.ACCENT, UITheme.CARD);
        refreshButton.addActionListener(e -> refresh());
        top.add(refreshButton, BorderLayout.EAST);
        header.add(top, BorderLayout.NORTH);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filters.setBackground(UITheme.PAGE);
        if (cityBox != null) {
            filters.add(UITheme.label("City / Municipality", UITheme.FONT_SMALL, UITheme.MUTED));
            filters.add(cityBox);
        }
        filters.add(UITheme.label("Barangay", UITheme.FONT_SMALL, UITheme.MUTED));
        filters.add(barangayBox);
        filters.add(UITheme.label("Hazard type", UITheme.FONT_SMALL, UITheme.MUTED));
        filters.add(typeBox);
        header.add(filters, BorderLayout.CENTER);

        header.add(filterLabel, BorderLayout.SOUTH);
        return header;
    }
    /** Reloads the barangay list for the chosen city, or for the whole province. */
    private void fillBarangays() {
        String keep = UITheme.text(barangayBox);
        barangayBox.removeAllItems();
        barangayBox.addItem(ALL_BARANGAYS);

        String city = cityFilter();
        List<String> names = (city == null)
                ? locationManager.getAllBarangays() : locationManager.getBarangays(city);
        for (int i = 0; i < names.size(); i++) {
            barangayBox.addItem(names.get(i));
        }

        UITheme.selectItem(barangayBox, keep);
        if (barangayBox.getSelectedIndex() < 0) {
            barangayBox.setSelectedIndex(0);
        }
    }

    /** Rebuilds every number on the panel with the filters that are chosen. */
    public void refresh() {
        filterLabel.setText(describeFilters());

        content.removeAll();
        addStatusSection(content);
        addTypeSection(content);
        addBarangaySection(content);
        addOldestSection(content);
        content.add(UITheme.hint("Every number is counted from reports.txt with the filters above. "
                + "Press Refresh after a change."));

        content.revalidate();
        content.repaint();
    }

    private void addStatusSection(JPanel body) {
        UITheme.addSection(body, "Reports by status", new String[][] {
            { "PENDING", count(ReportStatus.PENDING) },
            { "VERIFIED", count(ReportStatus.VERIFIED) },
            { "IN PROGRESS", count(ReportStatus.IN_PROGRESS) },
            { "RESOLVED", count(ReportStatus.RESOLVED) },
            { "REJECTED", count(ReportStatus.REJECTED) },
            { "TOTAL", count(null) }
        });
    }

    private void addTypeSection(JPanel body) {
        List<String[]> totals = reportManager.countByHazardType(cityFilter(), barangayFilter());
        if (totals.isEmpty()) {
            UITheme.addSection(body, "Reports by hazard type", new String[][] {
                { "REPORTS", "0" }
            });
            return;
        }
        UITheme.addSection(body, "Reports by hazard type", asFields(totals));
    }

    private void addBarangaySection(JPanel body) {
        List<String[]> totals = reportManager.countByBarangay(cityFilter(), barangayFilter(),
                typeFilter());
        if (totals.isEmpty()) {
            UITheme.addSection(body, "Busiest barangays", new String[][] {
                { "REPORTS", "0" }
            });
            return;
        }

        List<String[]> busiest = new ArrayList<String[]>();
        for (int i = 0; i < totals.size() && i < BARS; i++) {
            busiest.add(totals.get(i));
        }
        UITheme.addSection(body, "Busiest barangays", asFields(busiest));
    }

    private void addOldestSection(JPanel body) {
        Report oldest = reportManager.oldestPending(cityFilter(), barangayFilter(), typeFilter());
        if (oldest == null) {
            body.add(UITheme.label("Oldest pending report", UITheme.FONT_SECTION, UITheme.PRIMARY));
            body.add(Box.createVerticalStrut(8));
            body.add(UITheme.label("No report is waiting for action with these filters.",
                    UITheme.FONT_BASE, UITheme.MUTED));
            body.add(Box.createVerticalStrut(18));
            return;
        }

        int days = ReportManager.daysWaiting(oldest.getDateSubmitted());
        String waiting;
        if (days < 0) {
            waiting = "-";
        } else if (days == 0) {
            waiting = "Since today";
        } else if (days == 1) {
            waiting = "1 day";
        } else {
            waiting = days + " days";
        }

        UITheme.addSection(body, "Oldest pending report", new String[][] {
            { "REPORT ID", oldest.getReportId() },
            { "TYPE", oldest.getReportType() },
            { "PLACE", oldest.getLocation().getBarangay() + ", "
                + oldest.getLocation().getCityMunicipality() },
            { "DATE SUBMITTED", oldest.getDateSubmitted() },
            { "WAITING", waiting }
        });
    }
    /**
     * Turns the name/count pairs into the String[][] the section helper expects.
     * When the count is odd a total row is added, so the last box is not left
     * without a partner.
     */
    private String[][] asFields(List<String[]> totals) {
        int extra = (totals.size() % 2 == 0) ? 0 : 1;
        String[][] fields = new String[totals.size() + extra][];
        for (int i = 0; i < totals.size(); i++) {
            fields[i] = new String[] { totals.get(i)[0].toUpperCase(), totals.get(i)[1] };
        }
        if (extra == 1) {
            fields[totals.size()] = new String[] { "ALL REPORTS", count(null) };
        }
        return fields;
    }

    private String count(ReportStatus status) {
        return String.valueOf(reportManager.countReports(cityFilter(), barangayFilter(),
                typeFilter(), status));
    }

    /** null means "every city", so the administrator sees the whole province. */
    private String cityFilter() {
        return fixedCity != null ? fixedCity : valueOrNull(cityBox, ALL_CITIES);
    }

    private String barangayFilter() {
        return valueOrNull(barangayBox, ALL_BARANGAYS);
    }

    private String typeFilter() {
        return valueOrNull(typeBox, ALL_TYPES);
    }

    private static String valueOrNull(JComboBox<String> box, String allValue) {
        String value = UITheme.text(box);
        if (value.length() == 0 || value.equals(allValue)) {
            return null;
        }
        return value;
    }

    private String describeFilters() {
        String city = (fixedCity != null) ? fixedCity : textOrAll(cityBox, ALL_CITIES);
        return "Showing: " + city + "   -   " + textOrAll(barangayBox, ALL_BARANGAYS)
                + "   -   " + textOrAll(typeBox, ALL_TYPES);
    }

    private static String textOrAll(JComboBox<String> box, String allValue) {
        String value = UITheme.text(box);
        return value.length() == 0 ? allValue : value;
    }

    private static List<String> hazardTypes() {
        List<String> types = new ArrayList<String>();
        for (int i = 0; i < ReportRules.HAZARD_TYPES.length; i++) {
            types.add(ReportRules.HAZARD_TYPES[i]);
        }
        return types;
    }

    private String title() {
        if (fixedCity == null) {
            return "Overview - whole province";
        }
        return "Overview - " + fixedCity;
    }
}