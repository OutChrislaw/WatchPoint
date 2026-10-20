package ui;

import manager.UserManager;
import manager.ReportManager;
import manager.LocationManager;
import model.User;
import model.Resident;
import model.Administrator;
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

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * AdminDashboard.java
 * The window an administrator sees after logging in.
 * Tabs: Reports and Users.
 */
public class AdminDashboard extends javax.swing.JFrame {

    private static final String ALL_MUNICIPALITIES = "All Municipalities";
    private static final String ALL_BARANGAYS = "All Barangays";
    private static final String ALL_TYPES = "All Types";
    private static final String[] SORT_OPTIONS = {
        "Report ID (Oldest first)", "Report ID (Newest first)",
        "Date and Time (Newest first)", "Date and Time (Oldest first)",
        "Status", "Hazard Type"
    };

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
    private List<Report> currentReports;
    private JComboBox<String> statusBox;

    private JTable userTable;
    private DefaultTableModel userTableModel;
    private JScrollPane userDetailScroll;
    private List<User> currentUsers;
    private JTextField userSearchField;
    private JComboBox<String> userTypeBox;
    private JPanel reportActions;
    private JPanel userActions;


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
    private JPanel buildReportsTab() {
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

        tab.add(UITheme.hint("Select a report on the left to review it."), BorderLayout.SOUTH);
        return tab;
    }

    private JPanel buildFilterRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setBackground(UITheme.PAGE);

        row.add(UITheme.label("City / Municipality:", UITheme.FONT_BASE, UITheme.TEXT));
        reportFilterCityBox = UITheme.combo(ALL_MUNICIPALITIES, locationManager.getMunicipalities());
        reportFilterCityBox.addActionListener(e -> {
            updateFilterBarangays();
            refreshReports();
        });
        row.add(reportFilterCityBox);

        row.add(UITheme.label("Barangay:", UITheme.FONT_BASE, UITheme.TEXT));
        reportFilterBarangayBox = UITheme.options(new String[] { ALL_BARANGAYS });
        reportFilterBarangayBox.addActionListener(e -> refreshReports());
        row.add(reportFilterBarangayBox);

        row.add(UITheme.label("Sort by:", UITheme.FONT_BASE, UITheme.TEXT));
        reportFilterSortBox = UITheme.options(SORT_OPTIONS);
        reportFilterSortBox.addActionListener(e -> refreshReports());
        row.add(reportFilterSortBox);

        return row;
    }

    private void updateFilterBarangays() {
        reportFilterBarangayBox.removeAllItems();
        reportFilterBarangayBox.addItem(ALL_BARANGAYS);

        String municipality = UITheme.text(reportFilterCityBox);
        if (municipality.length() == 0 || municipality.equals(ALL_MUNICIPALITIES)) {
            return;
        }

        List<String> barangays = locationManager.getBarangays(municipality);
        for (int i = 0; i < barangays.size(); i++) {
            reportFilterBarangayBox.addItem(barangays.get(i));
        }
    }

    private JPanel buildReportActions() {
        statusBox = UITheme.options(new String[] { "PENDING", "VERIFIED", "IN_PROGRESS", "RESOLVED" });

        JButton updateStatusButton = UITheme.button("Update Status", UITheme.ACCENT, Color.WHITE);
        updateStatusButton.addActionListener(e -> updateReportStatus());

        JButton deleteButton = UITheme.button("Delete Invalid Report", UITheme.DANGER, Color.WHITE);
        deleteButton.addActionListener(e -> deleteInvalidReport());

        return UITheme.actionRow(UITheme.label("Set status to:", UITheme.FONT_BASE, UITheme.TEXT),
                statusBox, updateStatusButton, deleteButton);
    }

    private JPanel buildUserFilterRow() {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(UITheme.PAGE);

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filter.setBackground(UITheme.PAGE);
        filter.add(UITheme.label("Type:", UITheme.FONT_BASE, UITheme.TEXT));
        userTypeBox = UITheme.options(new String[] { ALL_TYPES, "Resident", "Administrator",
            "Area Officer" });
        userTypeBox.addActionListener(e -> manageUsers());
        filter.add(userTypeBox);
        row.add(filter, BorderLayout.WEST);

        JPanel search = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        search.setBackground(UITheme.PAGE);
        search.add(UITheme.label("Search:", UITheme.FONT_BASE, UITheme.TEXT));
        userSearchField = UITheme.textField(26);
        userSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent event) {
                manageUsers();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent event) {
                manageUsers();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent event) {
                manageUsers();
            }
        });
        search.add(userSearchField);
        row.add(search, BorderLayout.EAST);

        return row;
    }

    private JPanel buildUsersTab() {
        JPanel tab = UITheme.page();
        tab.add(buildUserFilterRow(), BorderLayout.NORTH);

        userTable = UITheme.dataTable(new Object[] { "User ID", "Type", "Username", "Full Name" });
        userTableModel = (DefaultTableModel) userTable.getModel();
        userTable.getSelectionModel().addListSelectionListener(e -> showSelectedUser());

        userDetailScroll = UITheme.detailScroll();
        userActions = buildUserActions();

        JPanel body = new JPanel(new GridLayout(1, 2, 14, 0));
        body.setBackground(UITheme.PAGE);
        body.add(UITheme.borderedScroll(userTable));
        body.add(UITheme.detailPane(userDetailScroll, userActions));
        tab.add(body, BorderLayout.CENTER);

        tab.add(buildUserControls(), BorderLayout.SOUTH);
        return tab;
    }

    private JPanel buildUserControls() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setBackground(UITheme.PAGE);

        JButton createUserButton = UITheme.button("Create User", UITheme.ACCENT, Color.WHITE);
        createUserButton.addActionListener(e -> createUser());
        row.add(createUserButton);

        row.add(UITheme.hint("Only an administrator can create area officer and administrator accounts."));
        return row;
    }

    private JPanel buildUserActions() {
        JButton updateUserButton = UITheme.button("Update User", UITheme.BORDER, UITheme.TEXT);
        updateUserButton.addActionListener(e -> updateUser());

        JButton deleteUserButton = UITheme.button("Delete User", UITheme.DANGER, Color.WHITE);
        deleteUserButton.addActionListener(e -> deleteUser());

        return UITheme.actionRow(updateUserButton, deleteUserButton);
    }
    public void showAllReports() {
        refreshReports();
    }

    private void refreshReports() {
        currentReports = reportManager.getReports(filterValue(reportFilterCityBox, ALL_MUNICIPALITIES),
                filterValue(reportFilterBarangayBox, ALL_BARANGAYS),
                UITheme.text(reportFilterSortBox));

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
            reportDetailScroll.setViewportView(UITheme.emptyDetail("No reports match the selected filters."));
        }
    }

    private static String filterValue(JComboBox<String> box, String allOption) {
        String value = UITheme.text(box);
        if (value.length() == 0 || value.equals(allOption)) {
            return null;
        }
        return value;
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

    private static String loginName(User user) {
        if (user instanceof Resident) {
            return ((Resident) user).getUsername();
        }
        return "-";
    }

    private User selectedUser() {
        int row = userTable.getSelectedRow();
        if (row < 0 || currentUsers == null || row >= currentUsers.size()) {
            return null;
        }
        return currentUsers.get(row);
    }

    public void manageUsers() {
        String keyword = "";
        if (userSearchField != null) {
            keyword = userSearchField.getText();
        }
        String type = ALL_TYPES;
        if (userTypeBox != null) {
            type = UITheme.text(userTypeBox);
        }

        currentUsers = userManager.searchUsers(keyword, type);

        userTableModel.setRowCount(0);
        for (int i = 0; i < currentUsers.size(); i++) {
            User user = currentUsers.get(i);
            userTableModel.addRow(new Object[] { user.getUserId(), user.getUserType(),
                loginName(user), user.getFullName() });
        }

        if (userTableModel.getRowCount() > 0) {
            userTable.setRowSelectionInterval(0, 0);
        } else if (keyword.trim().length() > 0 || !ALL_TYPES.equals(type)) {
            userDetailScroll.setViewportView(UITheme.emptyDetail("No accounts match your filter."));
        } else {
            userDetailScroll.setViewportView(UITheme.emptyDetail("There are no accounts to show."));
        }
    }

    public void createUser() {
        UserCreateDialog dialog = new UserCreateDialog(this, userManager, locationManager);
        if (dialog.showDialog()) {
            manageUsers();
        }
    }
    public void updateUser() {
        User existing = selectedUser();
        if (existing == null) {
            showMessage("Please select a user first.");
            return;
        }

        UserUpdateDialog dialog = new UserUpdateDialog(this, existing, userManager, locationManager);
        if (dialog.showDialog()) {
            showMessage("Account updated.");
            manageUsers();
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
    }

    private JPanel userForm(User user) {
        JPanel body = UITheme.cardBody();

        body.add(UITheme.label(user.getFullName(), UITheme.FONT_HEADING, UITheme.PRIMARY));
        body.add(Box.createVerticalStrut(6));
        body.add(UITheme.label(user.getUserType(), UITheme.FONT_SMALL, UITheme.ACCENT));
        body.add(Box.createVerticalStrut(16));

        if (user instanceof Resident) {
            Resident residentUser = (Resident) user;
            UITheme.addSection(body, "Account Information", new String[][] {
                { "USER ID", residentUser.getUserId() },
                { "ACCOUNT TYPE", residentUser.getUserType() },
                { "USERNAME", residentUser.getUsername() },
                { "FULL NAME", residentUser.getFullName() }
            });
        } else {
            UITheme.addSection(body, "Account Information", new String[][] {
                { "USER ID", user.getUserId() },
                { "ACCOUNT TYPE", user.getUserType() },
                { "FULL NAME", user.getFullName() }
            });
        }

        if (user instanceof Resident) {
            Resident resident = (Resident) user;
            UITheme.addSection(body, "Resident Details", new String[][] {
                { "ADDRESS", resident.getAddress() },
                { "CONTACT NUMBER", resident.getContactNumber() }
            });
        }

        if (user instanceof AreaOfficer) {
            AreaOfficer officer = (AreaOfficer) user;
            body.add(UITheme.label("Area Officer Details", UITheme.FONT_SECTION, UITheme.PRIMARY));
            body.add(Box.createVerticalStrut(8));
            body.add(UITheme.fieldBox("ASSIGNED AREA", officer.getArea()));
            body.add(Box.createVerticalStrut(6));
        }

        return body;
    }
    public void openSettings() {
        ProfileDialog dialog = new ProfileDialog(this, currentUser, userManager, locationManager);
        if (dialog.showDialog()) {
            subtitleLabel.setText("Signed in as " + currentUser.getFullName() + "  -  Administrator");
        } else if (dialog.isLogoutRequested()) {
            logout();
        }
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
        profileButton = new javax.swing.JButton();
        tabs = new javax.swing.JTabbedPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jLayeredPane1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        titleLabel.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        titleLabel.setForeground(new java.awt.Color(0, 51, 102));
        titleLabel.setText("WatchPoint");

        subtitleLabel.setForeground(new java.awt.Color(102, 102, 102));
        subtitleLabel.setText("Signed in as");

        profileButton.setBackground(new java.awt.Color(0, 153, 255));
        profileButton.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        profileButton.setForeground(new java.awt.Color(255, 255, 255));
        profileButton.setText("Settings");
        profileButton.addActionListener(this::profileButtonActionPerformed);

        jLayeredPane1.setLayer(titleLabel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(subtitleLabel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(profileButton, javax.swing.JLayeredPane.DEFAULT_LAYER);
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
                .addComponent(profileButton, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                .addComponent(profileButton, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
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

    private void profileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profileButtonActionPerformed
        openSettings();
    }//GEN-LAST:event_profileButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton profileButton;
    private javax.swing.JLabel subtitleLabel;
    private javax.swing.JTabbedPane tabs;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
