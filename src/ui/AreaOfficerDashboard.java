package ui;

import manager.LocationManager;
import manager.ReportManager;
import manager.UserManager;
import model.AreaOfficer;


import model.Report;
import model.ReportStatus;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * AreaOfficerDashboard.java
 * The window an Area Officer sees after logging in. The account is tied to one
 * city/municipality, so the officer only ever sees the reports filed in that
 * area, and only decides on the residents who registered there.
 * Tabs: Reports.
 */
public class AreaOfficerDashboard extends javax.swing.JFrame {

    private static final String ALL_BARANGAYS = "All Barangays";
    private static final String[] SORT_OPTIONS = {
        "Report ID (Oldest first)", "Report ID (Newest first)",
        "Date and Time (Newest first)", "Date and Time (Oldest first)",
        "Status", "Hazard Type"
    };

    private AreaOfficer currentOfficer;
    private ReportManager reportManager;
    private UserManager userManager;
    private LocationManager locationManager;

    private JLabel subtitleLabel;
    private JTabbedPane tabs;

    private JTable reportTable;
    private DefaultTableModel reportTableModel;
    private JScrollPane reportDetailScroll;
    private JComboBox<String> reportBarangayBox;
    private JComboBox<String> reportSortBox;
    private JComboBox<String> statusBox;
    private List<Report> currentReports;

    private JPanel reportActions;
    private OverviewPanel overview;




    public AreaOfficerDashboard() {
        initComponents();
    }

    public AreaOfficerDashboard(AreaOfficer currentOfficer, ReportManager reportManager,
                                UserManager userManager, LocationManager locationManager) {
        this.currentOfficer = currentOfficer;
        this.reportManager = reportManager;
        this.userManager = userManager;
        this.locationManager = locationManager;

        initComponents();

        setTitle("WatchPoint - Area Officer: " + currentOfficer.getFullName()
                + " (" + currentOfficer.getArea() + ")");
        subtitleLabel.setText("Signed in as " + currentOfficer.getFullName()
                + "  -  Area Officer (" + currentOfficer.getArea() + ")");

        overview = new OverviewPanel(reportManager, currentOfficer.getArea());
        tabs.addTab("Overview", overview);
        tabs.addTab("Reports", buildReportsTab());
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 0) {
                overview.refresh();
            }
        });

        setSize(1180, 780);
        setMinimumSize(new Dimension(980, 660));
        setLocationRelativeTo(null);

        showAreaReports();
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)));

        JPanel titles = new JPanel();
        titles.setBackground(UITheme.CARD);
        titles.setLayout(new javax.swing.BoxLayout(titles, javax.swing.BoxLayout.Y_AXIS));

        JLabel titleLabel = UITheme.label("WatchPoint", UITheme.FONT_TITLE, UITheme.PRIMARY);
        subtitleLabel = UITheme.label("Signed in as", UITheme.FONT_BASE, UITheme.MUTED);
        titles.add(titleLabel);
        titles.add(Box.createVerticalStrut(2));
        titles.add(subtitleLabel);

        JButton profileButton = UITheme.button("Settings", UITheme.ACCENT, UITheme.CARD);
        profileButton.addActionListener(e -> openSettings());

        header.add(titles, BorderLayout.WEST);
        header.add(profileButton, BorderLayout.EAST);

        tabs = new JTabbedPane();
        UITheme.styleTabs(tabs);
        getContentPane().setBackground(UITheme.PAGE);

        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }    private JPanel buildReportsTab() {
        JPanel tab = UITheme.page();
        tab.add(buildFilterRow(), BorderLayout.NORTH);

        reportTable = UITheme.dataTable(
                new Object[] { "Report ID", "Type", "Reporter", "Status", "Date & Time" });
        reportTableModel = (DefaultTableModel) reportTable.getModel();
        reportTable.getColumnModel().getColumn(3).setCellRenderer(UITheme.statusRenderer());
        reportTable.getSelectionModel().addListSelectionListener(e -> showSelectedReport());

        reportDetailScroll = UITheme.detailScroll();
        reportActions = buildReportActions();

        JPanel body = new JPanel(new GridLayout(1, 2, 14, 0));
        body.setBackground(UITheme.PAGE);
        body.add(UITheme.borderedScroll(reportTable));
        body.add(UITheme.detailPane(reportDetailScroll, reportActions));
        tab.add(body, BorderLayout.CENTER);

        tab.add(UITheme.hint("You only see the reports filed in " + currentOfficer.getArea() + "."),
                BorderLayout.SOUTH);
        return tab;
    }

    private JPanel buildFilterRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setBackground(UITheme.PAGE);

        row.add(UITheme.label("Area:", UITheme.FONT_BASE, UITheme.TEXT));
        row.add(UITheme.label(currentOfficer.getArea(), UITheme.FONT_BOLD, UITheme.PRIMARY));

        row.add(UITheme.label("Barangay:", UITheme.FONT_BASE, UITheme.TEXT));
        reportBarangayBox = UITheme.combo(ALL_BARANGAYS,
                locationManager.getBarangays(currentOfficer.getArea()));
        reportBarangayBox.addActionListener(e -> refreshReports());
        row.add(reportBarangayBox);

        row.add(UITheme.label("Sort by:", UITheme.FONT_BASE, UITheme.TEXT));
        reportSortBox = UITheme.options(SORT_OPTIONS);
        reportSortBox.addActionListener(e -> refreshReports());
        row.add(reportSortBox);

        return row;
    }

    private JPanel buildReportActions() {
        statusBox = UITheme.options(ReportManager.statusNames());

        JButton updateStatusButton = UITheme.button("Update Status", UITheme.ACCENT, UITheme.CARD);
        updateStatusButton.addActionListener(e -> updateReportStatus());

        return UITheme.actionRow(UITheme.label("Set status to:", UITheme.FONT_BASE, UITheme.TEXT),
                statusBox, updateStatusButton);
    }

    public void showAreaReports() {
        refreshReports();
    }

    private void refreshReports() {
        currentReports = reportManager.getReports(currentOfficer.getArea(),
                filterValue(reportBarangayBox, ALL_BARANGAYS), UITheme.text(reportSortBox));

        reportTableModel.setRowCount(0);
        for (int i = 0; i < currentReports.size(); i++) {
            Report report = currentReports.get(i);
            reportTableModel.addRow(new Object[] { report.getReportId(), report.getReportType(),
                userManager.getFullNameById(report.getReporterId()), report.getStatus().name(),
                report.getDateSubmitted() });
        }

        if (reportTableModel.getRowCount() > 0) {
            reportTable.setRowSelectionInterval(0, 0);
        } else {
            reportDetailScroll.setViewportView(UITheme.emptyDetail(
                    "No reports were filed in " + currentOfficer.getArea() + " yet."));
        }
    }

    private static String filterValue(JComboBox<String> box, String allOption) {
        String value = UITheme.text(box);
        if (value.length() == 0 || value.equals(allOption)) {
            return null;
        }
        return value;
    }    private void showSelectedReport() {
        Report report = selectedReport();
        if (report == null) {
            return;
        }
        reportDetailScroll.setViewportView(reportForm(report));
        reportDetailScroll.getVerticalScrollBar().setValue(0);
    }

    private Report selectedReport() {
        int row = reportTable.getSelectedRow();
        if (row < 0 || currentReports == null || row >= currentReports.size()) {
            return null;
        }
        return currentReports.get(row);
    }

    public void updateReportStatus() {
        Report report = selectedReport();
        if (report == null) {
            showMessage("Please select a report first.");
            return;
        }

        ReportStatus status = ReportManager.parseStatus(UITheme.text(statusBox));

        if (status == report.getStatus()) {
            showMessage("Report " + report.getReportId() + " is already " + status.name() + ".");
            return;
        }
        if (status == ReportStatus.REJECTED) {
            rejectReport(report);
            return;
        }

        if (reportManager.updateReportStatusInCity(report.getReportId(), status,
                currentOfficer.getArea())) {
            showMessage("Report " + report.getReportId() + " is now " + status.name() + ".");
            showAreaReports();
            overview.refresh();
        } else {
            showMessage("This report is not inside your area, so its status cannot be changed.");
        }
    }

    /** Asks for the reason, then marks the report as rejected. */
    private void rejectReport(Report report) {
        if (report.getStatus() == ReportStatus.RESOLVED) {
            showMessage("Report " + report.getReportId()
                    + " is already RESOLVED, so it cannot be rejected.");
            return;
        }

        ReportRejectDialog dialog = new ReportRejectDialog(this, report);
        String reason = dialog.showDialog();
        if (reason == null) {
            return;
        }

        if (reportManager.updateReportStatusInCity(report.getReportId(), ReportStatus.REJECTED,
                currentOfficer.getArea(), reason)) {
            showMessage("Report " + report.getReportId()
                    + " was rejected. The resident sees the reason you wrote.");
            showAreaReports();
            overview.refresh();
        } else {
            showMessage("The report could not be rejected.");
        }
    }

    private JPanel reportForm(Report report) {
        JPanel body = UITheme.cardBody();

        body.add(UITheme.label("Report " + report.getReportId() + "   -   " + report.getReportType(),
                UITheme.FONT_HEADING, UITheme.PRIMARY));
        body.add(Box.createVerticalStrut(6));
        body.add(UITheme.label(report.getStatus().name(), UITheme.FONT_SMALL,
                UITheme.statusColor(report.getStatus().name())));
        body.add(Box.createVerticalStrut(16));

        UITheme.addSection(body, "Report Information", new String[][] {
            { "REPORT ID", report.getReportId() },
            { "REPORT TYPE", report.getReportType() },
            { "REPORTER", userManager.getFullNameById(report.getReporterId()) },
            { "REPORTER CONTACT", userManager.getContactById(report.getReporterId()) },
            { "DATE SUBMITTED", report.getDateSubmitted() }
        });

        UITheme.addSection(body, "Location", new String[][] {
            { "CITY / MUNICIPALITY", report.getLocation().getCityMunicipality() },
            { "BARANGAY", report.getLocation().getBarangay() },
            { "STREET", report.getLocation().getStreet() },
            { "SPECIFIC PLACE", report.getLocation().getSpecificPlace() }
        });

        if (report.getRemark().length() > 0) {
            body.add(UITheme.label("Rejection reason", UITheme.FONT_SECTION, UITheme.PRIMARY));
            body.add(Box.createVerticalStrut(8));
            body.add(UITheme.descriptionBox(report.getRemark()));
            body.add(Box.createVerticalStrut(18));
        }

        body.add(UITheme.label(UITheme.detailCaption(report.getReportType()),
                UITheme.FONT_SECTION, UITheme.PRIMARY));
        body.add(Box.createVerticalStrut(8));
        body.add(UITheme.valueBox(report.getSpecificDetail()));
        body.add(Box.createVerticalStrut(18));

        body.add(UITheme.label("Description", UITheme.FONT_SECTION, UITheme.PRIMARY));
        body.add(Box.createVerticalStrut(8));
        body.add(UITheme.descriptionBox(report.getDescription()));

        return body;
    }

    public void logout() {
        new LoginFrame(userManager, reportManager, locationManager).setVisible(true);
        this.dispose();
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    public void openSettings() {
        ProfileDialog dialog = new ProfileDialog(this, currentOfficer, userManager, locationManager);
        if (dialog.showDialog()) {
            subtitleLabel.setText("Signed in as " + currentOfficer.getFullName()
                    + "  -  Area Officer (" + currentOfficer.getArea() + ")");
        } else if (dialog.isLogoutRequested()) {
            logout();
        }
    }


}