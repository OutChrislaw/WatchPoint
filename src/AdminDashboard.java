import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * AdminDashboard.java
 * The window an administrator sees after logging in.
 * Tabs: Reports and Users.
 */
public class AdminDashboard extends javax.swing.JFrame {

    private static final Color PRIMARY = new Color(0, 51, 102);
    private static final Color MUTED = new Color(102, 102, 102);
    private static final Color TEXT = new Color(51, 51, 51);
    private static final Color ACCENT = new Color(0, 153, 255);
    private static final Color BORDER = new Color(204, 204, 204);
    private static final Color DANGER = new Color(198, 40, 40);
    private static final Color PAGE = new Color(238, 238, 238);
    private static final Color ROW_LINE = new Color(226, 226, 226);
    private static final Color SELECTION = new Color(204, 229, 255);

    private static final Font FONT_TITLE = new Font("Segoe UI", 1, 24);
    private static final Font FONT_HEADING = new Font("Segoe UI", 1, 18);
    private static final Font FONT_SECTION = new Font("Segoe UI", 1, 15);
    private static final Font FONT_BOLD = new Font("Segoe UI", 1, 12);
    private static final Font FONT_BASE = new Font("Segoe UI", 0, 12);
    private static final Font FONT_SMALL = new Font("Segoe UI", 1, 11);
    private static final Font FONT_VALUE = new Font("Segoe UI", 0, 13);

    private static final String ALL_MUNICIPALITIES = "All Municipalities";
    private static final String ALL_BARANGAYS = "All Barangays";

    private Administrator currentUser;
    private ReportManager reportManager;
    private UserManager userManager;
    private LocationManager locationManager;

    private JTable reportTable;
    private DefaultTableModel reportTableModel;
    private JScrollPane reportDetailScroll;
    private JComboBox<String> reportFilterCityBox;
    private JComboBox<String> reportFilterBarangayBox;
    private JComboBox<String> reportFilterSortBox;
    private List<Report> allReports;
    private List<Report> currentReports;
    private JComboBox<String> statusBox;

    private JTable userTable;
    private DefaultTableModel userTableModel;
    private JScrollPane userDetailScroll;
    private List<User> currentUsers;

    public AdminDashboard() {
        initComponents();
    }

    public AdminDashboard(Administrator currentUser, ReportManager reportManager, UserManager userManager,
                          LocationManager locationManager) {
        this.currentUser = currentUser;
        this.reportManager = reportManager;
        this.userManager = userManager;
        this.locationManager = locationManager;

        initComponents();

        setTitle("WatchPoint - Administrator: " + currentUser.getFullName());
        subtitleLabel.setText("Signed in as " + currentUser.getFullName() + "  -  Administrator");

        tabs.addTab("Reports", buildReportsTab());
        tabs.addTab("Users", buildUsersTab());

        setSize(1180, 780);
        setMinimumSize(new Dimension(980, 660));
        setLocationRelativeTo(null);

        showAllReports();
        manageUsers();
    }
    private static final String[] SORT_OPTIONS = {
        "Report ID (Oldest first)", "Report ID (Newest first)",
        "Date and Time (Newest first)", "Date and Time (Oldest first)",
        "Status", "Hazard Type"
    };

    private JPanel buildReportsTab() {
        JPanel tab = page();
        tab.add(buildFilterRow(), BorderLayout.NORTH);

        reportTable = dataTable(new Object[] { "Report ID", "Type", "Reporter", "Status", "Date & Time" });
        reportTableModel = (DefaultTableModel) reportTable.getModel();
        reportTable.getColumnModel().getColumn(3).setCellRenderer(new StatusRenderer());
        reportTable.getSelectionModel().addListSelectionListener(e -> showSelectedReport());

        reportDetailScroll = detailScroll();

        JPanel body = new JPanel(new GridLayout(1, 2, 14, 0));
        body.setBackground(PAGE);
        body.add(borderedScroll(reportTable));
        body.add(reportDetailScroll);
        tab.add(body, BorderLayout.CENTER);

        tab.add(buildReportControls(), BorderLayout.SOUTH);
        return tab;
    }

    private JPanel buildFilterRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setBackground(PAGE);

        row.add(label("City / Municipality:", FONT_BASE, TEXT));
        reportFilterCityBox = combo(ALL_MUNICIPALITIES, locationManager.getMunicipalities());
        reportFilterCityBox.addActionListener(e -> {
            updateFilterBarangays();
            refreshReports();
        });
        row.add(reportFilterCityBox);

        row.add(label("Barangay:", FONT_BASE, TEXT));
        reportFilterBarangayBox = new JComboBox<String>();
        reportFilterBarangayBox.setFont(FONT_BASE);
        reportFilterBarangayBox.addItem(ALL_BARANGAYS);
        reportFilterBarangayBox.addActionListener(e -> refreshReports());
        row.add(reportFilterBarangayBox);

        row.add(label("Sort by:", FONT_BASE, TEXT));
        reportFilterSortBox = options(SORT_OPTIONS);
        reportFilterSortBox.addActionListener(e -> refreshReports());
        row.add(reportFilterSortBox);

        return row;
    }

    private void updateFilterBarangays() {
        reportFilterBarangayBox.removeAllItems();
        reportFilterBarangayBox.addItem(ALL_BARANGAYS);

        if (reportFilterCityBox.getSelectedItem() == null) {
            return;
        }

        String municipality = reportFilterCityBox.getSelectedItem().toString();
        if (municipality.equals(ALL_MUNICIPALITIES)) {
            return;
        }

        List<String> barangays = locationManager.getBarangays(municipality);
        for (int i = 0; i < barangays.size(); i++) {
            reportFilterBarangayBox.addItem(barangays.get(i));
        }
    }
    private JPanel buildReportControls() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setBackground(PAGE);

        row.add(label("Set status to:", FONT_BASE, TEXT));
        statusBox = options(new String[] { "PENDING", "VERIFIED", "IN_PROGRESS", "RESOLVED" });
        row.add(statusBox);

        JButton updateStatusButton = button("Update Status", ACCENT, Color.WHITE);
        updateStatusButton.addActionListener(e -> updateReportStatus());
        row.add(updateStatusButton);

        JButton deleteButton = button("Delete Invalid Report", DANGER, Color.WHITE);
        deleteButton.addActionListener(e -> deleteInvalidReport());
        row.add(deleteButton);

        row.add(hint("Select a report on the left to review it."));
        return row;
    }

    private JPanel buildUsersTab() {
        JPanel tab = page();

        userTable = dataTable(new Object[] { "User ID", "Type", "Username", "Full Name" });
        userTableModel = (DefaultTableModel) userTable.getModel();
        userTable.getSelectionModel().addListSelectionListener(e -> showSelectedUser());

        userDetailScroll = detailScroll();

        JPanel body = new JPanel(new GridLayout(1, 2, 14, 0));
        body.setBackground(PAGE);
        body.add(borderedScroll(userTable));
        body.add(userDetailScroll);
        tab.add(body, BorderLayout.CENTER);

        tab.add(buildUserControls(), BorderLayout.SOUTH);
        return tab;
    }

    private JPanel buildUserControls() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setBackground(PAGE);

        JButton createUserButton = button("Create User", ACCENT, Color.WHITE);
        createUserButton.addActionListener(e -> createUser());
        row.add(createUserButton);

        JButton updateUserButton = button("Update User", BORDER, TEXT);
        updateUserButton.addActionListener(e -> updateUser());
        row.add(updateUserButton);

        JButton deleteUserButton = button("Delete User", DANGER, Color.WHITE);
        deleteUserButton.addActionListener(e -> deleteUser());
        row.add(deleteUserButton);

        row.add(hint("Select a user on the left to view the account."));
        return row;
    }

    public void showAllReports() {
        allReports = reportManager.getAllReports();
        refreshReports();
    }

    private void refreshReports() {
        currentReports = selectedReports(allReports, reportFilterCityBox,
                reportFilterBarangayBox, reportFilterSortBox);

        reportTableModel.setRowCount(0);
        for (int i = 0; i < currentReports.size(); i++) {
            Report report = currentReports.get(i);
            reportTableModel.addRow(new Object[] { report.getReportId(), report.getReportType(),
                reporterName(report), report.getStatus().name(), report.getDateSubmitted() });
        }

        if (reportTableModel.getRowCount() > 0) {
            reportTable.setRowSelectionInterval(0, 0);
        } else {
            reportDetailScroll.setViewportView(emptyDetail("No reports match the selected filters."));
        }
    }

    private static List<Report> selectedReports(List<Report> reports, JComboBox<String> cityBox,
                                               JComboBox<String> barangayBox, JComboBox<String> sortBox) {
        String municipality = selected(cityBox, ALL_MUNICIPALITIES);
        String barangay = selected(barangayBox, ALL_BARANGAYS);

        List<Report> result = new ArrayList<Report>();
        if (reports != null) {
            for (int i = 0; i < reports.size(); i++) {
                Report report = reports.get(i);
                if (municipality != null && !report.getLocation().getCityMunicipality().equals(municipality)) {
                    continue;
                }
                if (barangay != null && !report.getLocation().getBarangay().equals(barangay)) {
                    continue;
                }
                result.add(report);
            }
        }

        sortReports(result, selected(sortBox, null));
        return result;
    }

    private static String selected(JComboBox<String> box, String allOption) {
        if (box == null || box.getSelectedItem() == null) {
            return null;
        }
        String value = box.getSelectedItem().toString();
        if (allOption != null && value.equals(allOption)) {
            return null;
        }
        return value;
    }

    private static void sortReports(List<Report> reports, final String sortMode) {
        if (sortMode == null) {
            return;
        }

        Collections.sort(reports, new Comparator<Report>() {
            @Override
            public int compare(Report first, Report second) {
                if (sortMode.equals("Report ID (Newest first)")) {
                    return second.getReportId().compareTo(first.getReportId());
                }
                if (sortMode.equals("Date and Time (Newest first)")) {
                    return second.getDateSubmitted().compareTo(first.getDateSubmitted());
                }
                if (sortMode.equals("Date and Time (Oldest first)")) {
                    return first.getDateSubmitted().compareTo(second.getDateSubmitted());
                }
                if (sortMode.equals("Status")) {
                    return first.getStatus().name().compareTo(second.getStatus().name());
                }
                if (sortMode.equals("Hazard Type")) {
                    return first.getReportType().compareTo(second.getReportType());
                }
                return first.getReportId().compareTo(second.getReportId());
            }
        });
    }
    private void showSelectedReport() {
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

        String statusText = text(statusBox);
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

        if (reportManager.updateReportStatus(report.getReportId(), status)) {
            showMessage("Report " + report.getReportId() + " is now " + status.name() + ".");
            showAllReports();
        } else {
            showMessage("The report status could not be updated.");
        }
    }

    public void deleteInvalidReport() {
        Report report = selectedReport();
        if (report == null) {
            showMessage("Please select a report first.");
            return;
        }

        int answer = JOptionPane.showConfirmDialog(this, "Delete report " + report.getReportId() + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        if (reportManager.deleteInvalidReport(report.getReportId())) {
            showMessage("Report deleted.");
            showAllReports();
        } else {
            showMessage("The report could not be deleted.");
        }
    }

    private void showSelectedUser() {
        User user = selectedUser();
        if (user == null) {
            return;
        }
        userDetailScroll.setViewportView(userForm(user));
        userDetailScroll.getVerticalScrollBar().setValue(0);
    }

    private User selectedUser() {
        int row = userTable.getSelectedRow();
        if (row < 0 || currentUsers == null || row >= currentUsers.size()) {
            return null;
        }
        return currentUsers.get(row);
    }

    public void manageUsers() {
        currentUsers = userManager.getAllUsers();

        userTableModel.setRowCount(0);
        for (int i = 0; i < currentUsers.size(); i++) {
            User user = currentUsers.get(i);
            userTableModel.addRow(new Object[] { user.getUserId(), user.getUserType(),
                user.getUsername(), user.getFullName() });
        }

        if (userTableModel.getRowCount() > 0) {
            userTable.setRowSelectionInterval(0, 0);
        } else {
            userDetailScroll.setViewportView(emptyDetail("There are no accounts to show."));
        }
    }
    public void createUser() {
        String[] types = { "Resident", "Administrator" };
        String type = (String) JOptionPane.showInputDialog(this, "User type:", "Create User",
                JOptionPane.QUESTION_MESSAGE, null, types, types[0]);
        if (type == null) {
            return;
        }

        String fullName = JOptionPane.showInputDialog(this, "Full name:");
        if (fullName == null || fullName.trim().length() == 0) {
            return;
        }

        String username = JOptionPane.showInputDialog(this, "Username:");
        if (username == null || username.trim().length() == 0) {
            return;
        }

        String password = JOptionPane.showInputDialog(this, "Password:");
        if (password == null || password.trim().length() == 0) {
            return;
        }

        List<User> users = userManager.getAllUsers();
        int highest = 0;
        for (int i = 0; i < users.size(); i++) {
            String id = users.get(i).getUserId();
            if (id != null && id.length() > 1) {
                try {
                    int number = Integer.parseInt(id.substring(1));
                    if (number > highest) {
                        highest = number;
                    }
                } catch (Exception e) {
                    // ids that are not in the U<number> format are ignored
                }
            }
        }
        String userId = "U" + (highest + 1);

        User user;
        if (type.equals("Resident")) {
            String address = JOptionPane.showInputDialog(this, "Address:");
            String contactNumber = JOptionPane.showInputDialog(this, "Contact number:");
            user = new Resident(userId, username.trim(), password, fullName.trim(), address, contactNumber);
        } else {
            user = new Administrator(userId, username.trim(), password, fullName.trim());
        }

        if (userManager.addUser(user)) {
            showMessage("User created.");
            manageUsers();
        } else {
            showMessage("The user could not be created. The username may already be taken.");
        }
    }

    public void updateUser() {
        User existing = selectedUser();
        if (existing == null) {
            showMessage("Please select a user first.");
            return;
        }

        String fullName = JOptionPane.showInputDialog(this, "Full name:", existing.getFullName());
        if (fullName == null || fullName.trim().length() == 0) {
            return;
        }

        String password = JOptionPane.showInputDialog(this, "New password:", "");
        if (password == null || password.trim().length() == 0) {
            return;
        }

        User updatedUser;
        if (existing instanceof Resident) {
            Resident resident = (Resident) existing;
            String address = JOptionPane.showInputDialog(this, "Address:", resident.getAddress());
            if (address == null) {
                address = resident.getAddress();
            }
            String contactNumber = JOptionPane.showInputDialog(this, "Contact number:",
                    resident.getContactNumber());
            if (contactNumber == null) {
                contactNumber = resident.getContactNumber();
            }
            updatedUser = new Resident(existing.getUserId(), existing.getUsername(), password,
                    fullName.trim(), address, contactNumber);
        } else {
            updatedUser = new Administrator(existing.getUserId(), existing.getUsername(), password,
                    fullName.trim());
        }

        if (userManager.updateUser(updatedUser)) {
            showMessage("User updated.");
            manageUsers();
        } else {
            showMessage("The user could not be updated.");
        }
    }

    public void deleteUser() {
        User existing = selectedUser();
        if (existing == null) {
            showMessage("Please select a user first.");
            return;
        }

        int answer = JOptionPane.showConfirmDialog(this, "Delete user " + existing.getUserId() + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        if (userManager.deleteUser(existing.getUserId())) {
            showMessage("User deleted.");
            manageUsers();
        } else {
            showMessage("The user could not be deleted.");
        }
    }

    public void logout() {
        new LoginFrame(userManager, reportManager, locationManager).setVisible(true);
        this.dispose();
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
    private static JPanel page() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(PAGE);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        return panel;
    }

    private static JPanel card(int width, int height) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(width, height));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(20, 22, 20, 22)));
        return panel;
    }

    private static JLabel label(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private static JLabel hint(String text) {
        return label(text, FONT_BASE, MUTED);
    }

    private static JButton button(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFont(FONT_BOLD);
        button.setFocusPainted(false);
        return button;
    }

    private static JComboBox<String> combo(String firstItem, List<String> items) {
        JComboBox<String> box = new JComboBox<String>();
        box.setFont(FONT_BASE);
        if (firstItem != null) {
            box.addItem(firstItem);
        }
        for (int i = 0; i < items.size(); i++) {
            box.addItem(items.get(i));
        }
        return box;
    }

    private static JComboBox<String> options(String[] values) {
        JComboBox<String> box = new JComboBox<String>();
        box.setFont(FONT_BASE);
        for (int i = 0; i < values.length; i++) {
            box.addItem(values[i]);
        }
        return box;
    }

    private static String text(JComboBox<String> box) {
        if (box.getSelectedItem() == null) {
            return "";
        }
        return box.getSelectedItem().toString();
    }

    private static JScrollPane borderedScroll(Component view) {
        JScrollPane pane = new JScrollPane(view);
        pane.setBorder(BorderFactory.createLineBorder(BORDER));
        pane.getViewport().setBackground(Color.WHITE);
        pane.getVerticalScrollBar().setUnitIncrement(16);
        return pane;
    }

    private static JScrollPane detailScroll() {
        return borderedScroll(null);
    }

    private static JTable dataTable(Object[] columns) {
        JTable table = new JTable(new DefaultTableModel(columns, 0));
        table.setFont(FONT_BASE);
        table.setForeground(TEXT);
        table.setBackground(Color.WHITE);
        table.setRowHeight(28);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(ROW_LINE);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(SELECTION);
        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);
        table.setDefaultEditor(Object.class, null);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(10, 32));
        header.setDefaultRenderer(new HeaderRenderer());
        return table;
    }

    private static JPanel fieldBox(String caption, String value) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(FONT_SMALL);
        captionLabel.setForeground(MUTED);

        JLabel valueLabel = new JLabel(display(value));
        valueLabel.setFont(FONT_VALUE);
        valueLabel.setForeground(TEXT);

        box.add(captionLabel, BorderLayout.NORTH);
        box.add(valueLabel, BorderLayout.CENTER);
        return box;
    }

    private static JPanel valueBox(String value) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        box.add(label(display(value), FONT_VALUE, TEXT), BorderLayout.CENTER);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        return box;
    }

    private static JPanel descriptionBox(String text) {
        String value = display(text);

        JTextArea area = new JTextArea(value);
        area.setFont(FONT_VALUE);
        area.setForeground(TEXT);
        area.setBackground(Color.WHITE);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setColumns(30);
        area.setBorder(null);

        int rows = value.length() / 70 + 2;
        if (rows < 3) {
            rows = 3;
        }
        if (rows > 14) {
            rows = 14;
        }
        area.setRows(rows);

        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        box.add(area, BorderLayout.CENTER);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        return box;
    }

    private static JPanel twoColumns(JPanel left, JPanel right) {
        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setBackground(Color.WHITE);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(left);
        row.add(right);
        return row;
    }

    private static void addSection(JPanel body, String title, String[][] fields) {
        body.add(label(title, FONT_SECTION, PRIMARY));
        body.add(Box.createVerticalStrut(8));

        for (int i = 0; i < fields.length; i = i + 2) {
            body.add(twoColumns(fieldBox(fields[i][0], fields[i][1]),
                    fieldBox(fields[i + 1][0], fields[i + 1][1])));
            body.add(Box.createVerticalStrut(12));
        }
        body.add(Box.createVerticalStrut(6));
    }

    private static String display(String text) {
        if (text == null || text.trim().length() == 0) {
            return "-";
        }
        return text;
    }

    private static Color statusColor(String status) {
        if ("VERIFIED".equals(status)) {
            return new Color(0, 102, 204);
        }
        if ("IN_PROGRESS".equals(status)) {
            return new Color(123, 31, 162);
        }
        if ("RESOLVED".equals(status)) {
            return new Color(46, 125, 50);
        }
        if ("PENDING".equals(status)) {
            return new Color(230, 126, 0);
        }
        return MUTED;
    }

    private static String detailCaption(String reportType) {
        if (reportType.equals("Road Hazard")) {
            return "Road Hazard Type";
        }
        if (reportType.equals("Flood")) {
            return "Water Level";
        }
        if (reportType.equals("Streetlight")) {
            return "Pole Number";
        }
        return "Hazard Category";
    }

    private String reporterName(Report report) {
        if (userManager != null) {
            User reporter = userManager.getUserById(report.getReporterId());
            if (reporter != null) {
                return reporter.getFullName();
            }
        }
        return report.getReporterId();
    }

    private JPanel reportForm(Report report) {
        BodyPanel body = new BodyPanel();

        body.add(label("Report " + report.getReportId() + "   -   " + report.getReportType(),
                FONT_HEADING, PRIMARY));
        body.add(Box.createVerticalStrut(6));
        body.add(label(report.getStatus().name(), FONT_SMALL, statusColor(report.getStatus().name())));
        body.add(Box.createVerticalStrut(16));

        addSection(body, "Report Information", new String[][] {
            { "REPORT ID", report.getReportId() },
            { "REPORT TYPE", report.getReportType() },
            { "REPORTER", reporterName(report) },
            { "DATE SUBMITTED", report.getDateSubmitted() }
        });

        addSection(body, "Location", new String[][] {
            { "CITY / MUNICIPALITY", report.getLocation().getCityMunicipality() },
            { "BARANGAY", report.getLocation().getBarangay() },
            { "STREET", report.getLocation().getStreet() },
            { "SPECIFIC PLACE", report.getLocation().getSpecificPlace() }
        });

        body.add(label(detailCaption(report.getReportType()), FONT_SECTION, PRIMARY));
        body.add(Box.createVerticalStrut(8));
        body.add(valueBox(report.getSpecificDetail()));
        body.add(Box.createVerticalStrut(18));

        body.add(label("Description", FONT_SECTION, PRIMARY));
        body.add(Box.createVerticalStrut(8));
        body.add(descriptionBox(report.getDescription()));

        return body;
    }

    private static JPanel emptyDetail(String message) {
        BodyPanel body = new BodyPanel();
        body.add(label(message, FONT_BASE, MUTED));
        return body;
    }

    private static final class HeaderRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);
            label.setOpaque(true);
            label.setBackground(PRIMARY);
            label.setForeground(Color.WHITE);
            label.setFont(FONT_BOLD);
            label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            return label;
        }
    }

    private static final class StatusRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);
            label.setFont(FONT_SMALL);
            label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            if (isSelected) {
                label.setForeground(TEXT);
            } else if (value != null) {
                label.setForeground(statusColor(value.toString()));
            }
            return label;
        }
    }

    private static final class BodyPanel extends JPanel implements javax.swing.Scrollable {
        private static final long serialVersionUID = 1L;

        BodyPanel() {
            setBackground(Color.WHITE);
            setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private static JPanel userForm(User user) {
        BodyPanel body = new BodyPanel();

        body.add(label(user.getFullName(), FONT_HEADING, PRIMARY));
        body.add(Box.createVerticalStrut(6));
        body.add(label(user.getUserType(), FONT_SMALL, ACCENT));
        body.add(Box.createVerticalStrut(16));

        addSection(body, "Account Information", new String[][] {
            { "USER ID", user.getUserId() },
            { "ACCOUNT TYPE", user.getUserType() },
            { "USERNAME", user.getUsername() },
            { "FULL NAME", user.getFullName() }
        });

        if (user instanceof Resident) {
            Resident resident = (Resident) user;
            addSection(body, "Resident Details", new String[][] {
                { "ADDRESS", resident.getAddress() },
                { "CONTACT NUMBER", resident.getContactNumber() }
            });
        }

        return body;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLayeredPane1 = new javax.swing.JLayeredPane();
        titleLabel = new javax.swing.JLabel();
        subtitleLabel = new javax.swing.JLabel();
        logoutButton = new javax.swing.JButton();
        tabs = new javax.swing.JTabbedPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jLayeredPane1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        titleLabel.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        titleLabel.setForeground(new java.awt.Color(0, 51, 102));
        titleLabel.setText("WatchPoint");

        subtitleLabel.setForeground(new java.awt.Color(102, 102, 102));
        subtitleLabel.setText("Signed in as");

        logoutButton.setBackground(new java.awt.Color(0, 153, 255));
        logoutButton.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        logoutButton.setForeground(new java.awt.Color(255, 255, 255));
        logoutButton.setText("Log Out");
        logoutButton.addActionListener(this::logoutButtonActionPerformed);

        jLayeredPane1.setLayer(titleLabel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(subtitleLabel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(logoutButton, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(tabs, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jLayeredPane1Layout = new javax.swing.GroupLayout(jLayeredPane1);
        jLayeredPane1.setLayout(jLayeredPane1Layout);
        jLayeredPane1Layout.setHorizontalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane1Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(titleLabel)
                    .addComponent(subtitleLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                .addComponent(logoutButton, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28))
            .addGroup(jLayeredPane1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(tabs, javax.swing.GroupLayout.DEFAULT_SIZE, 1000, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );
        jLayeredPane1Layout.setVerticalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(titleLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(subtitleLabel)
                .addGap(16, 16, 16)
                .addComponent(tabs, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
                .addGap(18, 18, 18))
            .addGroup(jLayeredPane1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(logoutButton, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLayeredPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLayeredPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void logoutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoutButtonActionPerformed
        logout();
    }//GEN-LAST:event_logoutButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton logoutButton;
    private javax.swing.JLabel subtitleLabel;
    private javax.swing.JTabbedPane tabs;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
