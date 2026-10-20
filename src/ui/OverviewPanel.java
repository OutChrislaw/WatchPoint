package ui;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import manager.ReportManager;
import model.Report;
import model.ReportStatus;

/**
 * OverviewPanel.java
 * The summary tab of the dashboards: how many reports there are per status, per
 * hazard type and per barangay, plus the report that has been waiting longest.
 * Pass a city/municipality for one area, or null for the whole province. Every
 * number comes from the reports already loaded, so nothing is stored twice.
 */
public class OverviewPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int BARS = 6;

    private final ReportManager reportManager;
    private final String cityMunicipality;

    public OverviewPanel(ReportManager reportManager, String cityMunicipality) {
        this.reportManager = reportManager;
        this.cityMunicipality = cityMunicipality;

        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.PAGE);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 16, 14, 16));

        refresh();
    }

    /** Rebuilds every number on the panel. */
    public void refresh() {
        removeAll();

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(UITheme.PAGE);
        header.add(UITheme.label(title(), UITheme.FONT_HEADING, UITheme.PRIMARY), BorderLayout.WEST);

        JButton refreshButton = UITheme.button("Refresh", UITheme.ACCENT, UITheme.CARD);
        refreshButton.addActionListener(e -> refresh());
        header.add(refreshButton, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel body = UITheme.cardBody();
        addStatusSection(body);
        addTypeSection(body);
        addBarangaySection(body);
        addOldestSection(body);
        body.add(UITheme.hint("Every number is counted from reports.txt. Press Refresh after a change."));

        JScrollPane scroll = UITheme.borderedScroll(body);
        add(scroll, BorderLayout.CENTER);

        revalidate();
        repaint();
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
        List<String[]> totals = reportManager.countByHazardType(cityMunicipality);
        if (totals.isEmpty()) {
            UITheme.addSection(body, "Reports by hazard type", new String[][] {
                { "REPORTS", "0" }
            });
            return;
        }
        UITheme.addSection(body, "Reports by hazard type", asFields(totals));
    }

    private void addBarangaySection(JPanel body) {
        List<String[]> totals = reportManager.countByBarangay(cityMunicipality);
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
        Report oldest = reportManager.oldestPending(cityMunicipality);
        if (oldest == null) {
            body.add(UITheme.label("Oldest pending report", UITheme.FONT_SECTION, UITheme.PRIMARY));
            body.add(Box.createVerticalStrut(8));
            body.add(UITheme.label("No report is waiting for action right now.", UITheme.FONT_BASE,
                    UITheme.MUTED));
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
        return String.valueOf(reportManager.countReports(cityMunicipality, status));
    }

    private String title() {
        if (cityMunicipality == null) {
            return "Overview - whole province";
        }
        return "Overview - " + cityMunicipality;
    }
}
