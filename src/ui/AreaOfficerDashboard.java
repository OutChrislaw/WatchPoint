package ui;

import manager.LocationManager;
import manager.ReportManager;
import manager.UserManager;
import model.AreaOfficer;
import model.RegistrationRequest;
import model.ProfileChangeRequest;
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
 * Tabs: Reports and Registrations.
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

    private JTable requestTable;
    private DefaultTableModel requestTableModel;
    private JScrollPane requestDetailScroll;
    private List<RegistrationRequest> currentRequests;

    private JPanel reportActions;
    private JPanel requestActions;

    private JTable changeTable;
    private DefaultTableModel changeTableModel;
    private JScrollPane changeDetailScroll;
    private JPanel changeActions;
    private List<ProfileChangeRequest> currentChanges;

    private JTextField profileFullNameField;
    private JTextField profileUsernameField;
    private JTextField profilePasswordField;
    private JTextField profileAreaField;

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

        tabs.addTab("Reports", buildReportsTab());
        tabs.addTab("Registrations", buildRegistrationsTab());
        tabs.addTab("Area Changes", buildAreaChangesTab());
        tabs.addTab("My Profile", buildProfileTab());

        setSize(1180, 780);
        setMinimumSize(new Dimension(980, 660));
        setLocationRelativeTo(null);

        showAreaReports();
        showRegistrations();
        showAreaChanges();
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)));

        JPanel titles = new JPanel();
        titles.setBackground(Color.WHITE);
        titles.setLayout(new javax.swing.BoxLayout(titles, javax.swing.BoxLayout.Y_AXIS));

        JLabel titleLabel = UITheme.label("WatchPoint", UITheme.FONT_TITLE, UITheme.PRIMARY);
        subtitleLabel = UITheme.label("Signed in as", UITheme.FONT_BASE, UITheme.MUTED);
        titles.add(titleLabel);
        titles.add(Box.createVerticalStrut(2));
        titles.add(subtitleLabel);

        JButton logoutButton = UITheme.button("Log Out", UITheme.ACCENT, Color.WHITE);
        logoutButton.addActionListener(e -> logout());

        header.add(titles, BorderLayout.WEST);
        header.add(logoutButton, BorderLayout.EAST);

        tabs = new JTabbedPane();

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
        statusBox = UITheme.options(new String[] { "PENDING", "VERIFIED", "IN_PROGRESS", "RESOLVED" });

        JButton updateStatusButton = UITheme.button("Update Status", UITheme.ACCENT, Color.WHITE);
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

        String statusText = UITheme.text(statusBox);
        ReportStatus status;
        if (statusText.equals("VERIFIED")) {
            status = ReportStatus.VERIFIED;
        } else if (statusText.equals("IN_PROGRESS")) {
            status = ReportStatus.IN_PROGRESS;
        } else if (statusText.equals("RESOLVED")) {
            status = ReportStatus.RESOLVED;
        } else {
            status = ReportStatus.PENDING;
        }

        if (reportManager.updateReportStatusInCity(report.getReportId(), status,
                currentOfficer.getArea())) {
            showMessage("Report " + report.getReportId() + " is now " + status.name() + ".");
            showAreaReports();
        } else {
            showMessage("This report is not inside your area, so its status cannot be changed.");
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
            { "DATE SUBMITTED", report.getDateSubmitted() }
        });

        UITheme.addSection(body, "Location", new String[][] {
            { "CITY / MUNICIPALITY", report.getLocation().getCityMunicipality() },
            { "BARANGAY", report.getLocation().getBarangay() },
            { "STREET", report.getLocation().getStreet() },
            { "SPECIFIC PLACE", report.getLocation().getSpecificPlace() }
        });

        body.add(UITheme.label(UITheme.detailCaption(report.getReportType()),
                UITheme.FONT_SECTION, UITheme.PRIMARY));
        body.add(Box.createVerticalStrut(8));
        body.add(UITheme.valueBox(report.getSpecificDetail()));
        body.add(Box.createVerticalStrut(18));

        body.add(UITheme.label("Description", UITheme.FONT_SECTION, UITheme.PRIMARY));
        body.add(Box.createVerticalStrut(8));
        body.add(UITheme.descriptionBox(report.getDescription()));

        return body;
    }    private JPanel buildRegistrationsTab() {
        JPanel tab = UITheme.page();

        requestTable = UITheme.dataTable(
                new Object[] { "Full Name", "Username", "Barangay", "Contact", "Date Requested" });
        requestTableModel = (DefaultTableModel) requestTable.getModel();
        requestTable.getSelectionModel().addListSelectionListener(e -> showSelectedRequest());

        requestDetailScroll = UITheme.detailScroll();
        requestActions = buildRequestActions();

        JPanel body = new JPanel(new GridLayout(1, 2, 14, 0));
        body.setBackground(UITheme.PAGE);
        body.add(UITheme.borderedScroll(requestTable));
        body.add(UITheme.detailPane(requestDetailScroll, requestActions));
        tab.add(body, BorderLayout.CENTER);

        tab.add(UITheme.hint("Only residents who registered in " + currentOfficer.getArea()
                + " appear here. Accepted residents can log in right away."), BorderLayout.SOUTH);

        return tab;
    }

    private JPanel buildRequestActions() {
        JButton acceptButton = UITheme.button("Accept Registration", UITheme.ACCENT, Color.WHITE);
        acceptButton.addActionListener(e -> acceptRegistration());

        JButton rejectButton = UITheme.button("Reject Registration", UITheme.DANGER, Color.WHITE);
        rejectButton.addActionListener(e -> rejectRegistration());

        return UITheme.actionRow(acceptButton, rejectButton);
    }

    private void showSelectedRequest() {
        RegistrationRequest request = selectedRequest();
        if (request == null) {
            return;
        }
        requestDetailScroll.setViewportView(requestForm(request));
        requestDetailScroll.getVerticalScrollBar().setValue(0);
    }

    private RegistrationRequest selectedRequest() {
        int row = requestTable.getSelectedRow();
        if (row < 0 || currentRequests == null || row >= currentRequests.size()) {
            return null;
        }
        return currentRequests.get(row);
    }

    public void showRegistrations() {
        currentRequests = userManager.getPendingRegistrations(currentOfficer.getArea());

        requestTableModel.setRowCount(0);
        for (int i = 0; i < currentRequests.size(); i++) {
            RegistrationRequest request = currentRequests.get(i);
            requestTableModel.addRow(new Object[] { request.getFullName(), request.getUsername(),
                request.getBarangay(), request.getContactNumber(), request.getDateRequested() });
        }

        if (requestTableModel.getRowCount() > 0) {
            requestTable.setRowSelectionInterval(0, 0);
        } else {
            requestDetailScroll.setViewportView(UITheme.emptyDetail(
                    "No residents are waiting for approval in " + currentOfficer.getArea() + "."));
        }
    }

    public void acceptRegistration() {
        RegistrationRequest request = selectedRequest();
        if (request == null) {
            showMessage("Please select a registration first.");
            return;
        }

        if (userManager.approveRegistration(request)) {
            showMessage("Registration accepted. " + request.getFullName() + " can now log in.");
            showRegistrations();
        } else {
            showMessage("The account could not be created. The username may already be taken.");
        }
    }

    public void rejectRegistration() {
        RegistrationRequest request = selectedRequest();
        if (request == null) {
            showMessage("Please select a registration first.");
            return;
        }

        int answer = JOptionPane.showConfirmDialog(this,
                "Reject the registration of " + request.getFullName() + "?",
                "Reject Registration", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        if (userManager.rejectRegistration(request)) {
            showMessage("Registration rejected.");
            showRegistrations();
        } else {
            showMessage("The registration could not be rejected.");
        }
    }

    public void logout() {
        new LoginFrame(userManager, reportManager, locationManager).setVisible(true);
        this.dispose();
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    private JPanel buildAreaChangesTab() {
        JPanel tab = UITheme.page();

        changeTable = UITheme.dataTable(
                new Object[] { "Resident", "New Municipality", "New Barangay", "Date Requested" });
        changeTableModel = (DefaultTableModel) changeTable.getModel();
        changeTable.getSelectionModel().addListSelectionListener(e -> showSelectedChange());

        changeDetailScroll = UITheme.detailScroll();

        JButton approveButton = UITheme.button("Approve Change", UITheme.ACCENT, Color.WHITE);
        approveButton.addActionListener(e -> approveAreaChange());

        JButton rejectButton = UITheme.button("Reject Change", UITheme.DANGER, Color.WHITE);
        rejectButton.addActionListener(e -> rejectAreaChange());

        changeActions = UITheme.actionRow(approveButton, rejectButton);

        JPanel body = new JPanel(new GridLayout(1, 2, 14, 0));
        body.setBackground(UITheme.PAGE);
        body.add(UITheme.borderedScroll(changeTable));
        body.add(UITheme.detailPane(changeDetailScroll, changeActions));
        tab.add(body, BorderLayout.CENTER);

        tab.add(UITheme.hint("Only residents who moved into " + currentOfficer.getArea()
                + " appear here."), BorderLayout.SOUTH);
        return tab;
    }

    private void showSelectedChange() {
        ProfileChangeRequest request = selectedChange();
        if (request == null) {
            return;
        }
        changeDetailScroll.setViewportView(changeForm(request));
        changeDetailScroll.getVerticalScrollBar().setValue(0);
    }

    private ProfileChangeRequest selectedChange() {
        int row = changeTable.getSelectedRow();
        if (row < 0 || currentChanges == null || row >= currentChanges.size()) {
            return null;
        }
        return currentChanges.get(row);
    }

    public void showAreaChanges() {
        currentChanges = userManager.getAreaChangeRequests(currentOfficer.getArea());

        changeTableModel.setRowCount(0);
        for (int i = 0; i < currentChanges.size(); i++) {
            ProfileChangeRequest request = currentChanges.get(i);
            changeTableModel.addRow(new Object[] { request.getResidentName(),
                request.getMunicipality(), request.getBarangay(), request.getDateRequested() });
        }

        if (changeTableModel.getRowCount() > 0) {
            changeTable.setRowSelectionInterval(0, 0);
        } else {
            changeDetailScroll.setViewportView(UITheme.emptyDetail(
                    "No resident is moving into " + currentOfficer.getArea() + "."));
        }
    }

    public void approveAreaChange() {
        ProfileChangeRequest request = selectedChange();
        if (request == null) {
            showMessage("Please select a request first.");
            return;
        }

        if (userManager.approveAreaChange(request)) {
            showMessage(request.getResidentName() + " is now in " + request.getAddress() + ".");
            showAreaChanges();
        } else {
            showMessage("The change could not be approved.");
        }
    }

    public void rejectAreaChange() {
        ProfileChangeRequest request = selectedChange();
        if (request == null) {
            showMessage("Please select a request first.");
            return;
        }

        int answer = JOptionPane.showConfirmDialog(this,
                "Reject the move of " + request.getResidentName() + "?",
                "Reject Change", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        if (userManager.rejectAreaChange(request)) {
            showMessage("The change was rejected.");
            showAreaChanges();
        } else {
            showMessage("The change could not be rejected.");
        }
    }

    private JPanel changeForm(ProfileChangeRequest request) {
        JPanel body = UITheme.cardBody();

        body.add(UITheme.label(request.getResidentName(), UITheme.FONT_HEADING, UITheme.PRIMARY));
        body.add(Box.createVerticalStrut(6));
        body.add(UITheme.label("Waiting to move into " + currentOfficer.getArea(),
                UITheme.FONT_SMALL, UITheme.statusColor("PENDING")));
        body.add(Box.createVerticalStrut(16));

        UITheme.addSection(body, "Resident", new String[][] {
            { "FULL NAME", request.getResidentName() },
            { "USER ID", request.getResidentId() },
            { "NEW CITY / MUNICIPALITY", request.getMunicipality() },
            { "NEW BARANGAY", request.getBarangay() }
        });

        body.add(UITheme.label("Requested", UITheme.FONT_SECTION, UITheme.PRIMARY));
        body.add(Box.createVerticalStrut(8));
        body.add(UITheme.valueBox(request.getDateRequested()));

        return body;
    }
    private JPanel buildProfileTab() {
        JPanel tab = new JPanel(new GridBagLayout());
        tab.setBackground(UITheme.PAGE);

        JPanel content = UITheme.card();
        content.setLayout(new BorderLayout(0, 14));

        JPanel header = new JPanel();
        header.setBackground(Color.WHITE);
        header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.Y_AXIS));
        header.add(UITheme.label("My Profile", UITheme.FONT_HEADING, UITheme.PRIMARY));
        header.add(Box.createVerticalStrut(2));
        header.add(UITheme.label("Change your own details, then press Save Changes.",
                UITheme.FONT_BASE, UITheme.MUTED));
        content.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);

        int row = 0;

        profileFullNameField = UITheme.hintField("Your full name", 20);
        profileFullNameField.setText(currentOfficer.getFullName());
        UITheme.addFormRow(form, row, "Full Name", profileFullNameField);
        row = row + 2;

        profileUsernameField = UITheme.hintField("", 20);
        profileUsernameField.setText(currentOfficer.getUsername());
        profileUsernameField.setEnabled(false);
        UITheme.addFormRow(form, row, "Username (cannot be changed)", profileUsernameField);
        row = row + 2;

        profilePasswordField = UITheme.hintField("Leave blank to keep your password", 20);
        UITheme.addFormRow(form, row, "New Password", profilePasswordField);
        row = row + 2;

        profileAreaField = UITheme.hintField("", 20);
        profileAreaField.setText(currentOfficer.getArea());
        profileAreaField.setEnabled(false);
        UITheme.addFormRow(form, row, "Assigned Area (ask an administrator to change)", profileAreaField);

        content.add(form, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(0, 10));
        south.setBackground(Color.WHITE);
        south.add(UITheme.hint("Your area is set by an administrator."), BorderLayout.NORTH);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonRow.setBackground(Color.WHITE);
        JButton saveButton = UITheme.button("Save Changes", UITheme.ACCENT, Color.WHITE);
        saveButton.addActionListener(e -> saveProfile());
        buttonRow.add(saveButton);
        south.add(buttonRow, BorderLayout.SOUTH);

        content.add(south, BorderLayout.SOUTH);

        Dimension preferred = content.getPreferredSize();
        content.setPreferredSize(new Dimension(660, preferred.height));

        tab.add(content);
        return tab;
    }

    public void saveProfile() {
        String fullName = profileFullNameField.getText().trim();
        String password = profilePasswordField.getText();

        if (fullName.length() == 0) {
            showMessage("Please enter your full name.");
            return;
        }

        if (userManager.saveProfile(currentOfficer, fullName, password, null, null, null)
                == UserManager.PROFILE_FAILED) {
            showMessage("Your profile could not be saved.");
            return;
        }

        showMessage("Your profile was saved.");
        profilePasswordField.setText("");
        subtitleLabel.setText("Signed in as " + currentOfficer.getFullName()
                + "  -  Area Officer (" + currentOfficer.getArea() + ")");
    }
    private JPanel requestForm(RegistrationRequest request) {
        JPanel body = UITheme.cardBody();

        body.add(UITheme.label(request.getFullName(), UITheme.FONT_HEADING, UITheme.PRIMARY));
        body.add(Box.createVerticalStrut(6));
        body.add(UITheme.label("Pending resident registration", UITheme.FONT_SMALL,
                UITheme.statusColor("PENDING")));
        body.add(Box.createVerticalStrut(16));

        UITheme.addSection(body, "Applicant", new String[][] {
            { "FULL NAME", request.getFullName() },
            { "USERNAME", request.getUsername() },
            { "CONTACT NUMBER", request.getContactNumber() },
            { "DATE REQUESTED", request.getDateRequested() }
        });

        UITheme.addSection(body, "Address", new String[][] {
            { "CITY / MUNICIPALITY", request.getMunicipality() },
            { "BARANGAY", request.getBarangay() }
        });

        return body;
    }
}