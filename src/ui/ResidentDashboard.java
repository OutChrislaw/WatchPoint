package ui;

import manager.UserManager;
import manager.ReportManager;
import manager.LocationManager;
import model.Resident;
import model.Report;
import model.ReportStatus;
import model.Location;

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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * ResidentDashboard.java
 * The window a resident sees after logging in.
 * Tabs: Reports (all reports), Submit Report and My Reports.
 */
public class ResidentDashboard extends javax.swing.JFrame {

    private static final String ALL_MUNICIPALITIES = "All Municipalities";
    private static final String ALL_BARANGAYS = "All Barangays";
    private static final String[] SORT_OPTIONS = {
        "Report ID (Oldest first)", "Report ID (Newest first)",
        "Date and Time (Newest first)", "Date and Time (Oldest first)",
        "Status", "Hazard Type"
    };

    private Resident currentUser;
    private ReportManager reportManager;
    private UserManager userManager;
    private LocationManager locationManager;

    private JComboBox<String> hazardTypeBox;
    private JComboBox<String> cityMunicipalityBox;
    private JComboBox<String> barangayBox;
    private JTextField streetField;
    private JTextField specificPlaceField;
    private JTextField specificDetailField;
    private JTextArea descriptionArea;

    private JTable publicReportTable;
    private DefaultTableModel publicReportTableModel;
    private JScrollPane publicDetailScroll;
    private JComboBox<String> publicFilterCityBox;
    private JComboBox<String> publicFilterBarangayBox;
    private JComboBox<String> publicFilterSortBox;
    private List<Report> publicReports;

    private JTable reportTable;
    private DefaultTableModel reportTableModel;
    private JScrollPane detailScroll;
    private List<Report> currentReports;
    private JPanel reportActions;

    public ResidentDashboard() {
        initComponents();
    }

    public ResidentDashboard(Resident currentUser, ReportManager reportManager, UserManager userManager,
                             LocationManager locationManager) {
        this.currentUser = currentUser;
        this.reportManager = reportManager;
        this.userManager = userManager;
        this.locationManager = locationManager;

        initComponents();

        setTitle("WatchPoint - Resident: " + currentUser.getFullName());
        subtitleLabel.setText("Signed in as " + currentUser.getFullName() + "  -  Resident");

        tabs.addTab("Reports", buildAllReportsTab());
        tabs.addTab("Submit Report", buildSubmitTab());
        tabs.addTab("My Reports", buildReportsTab());

        setSize(1180, 780);
        setMinimumSize(new Dimension(980, 660));
        setLocationRelativeTo(null);

        showAllReports();
        showOwnReports();
    }
    private JPanel buildAllReportsTab() {
        JPanel tab = UITheme.page();
        tab.add(buildFilterRow(), BorderLayout.NORTH);

        publicReportTable = UITheme.dataTable(
                new Object[] { "Report ID", "Type", "Reporter", "Status", "Date & Time" });
        publicReportTableModel = (DefaultTableModel) publicReportTable.getModel();
        publicReportTable.getColumnModel().getColumn(3).setCellRenderer(UITheme.statusRenderer());
        publicReportTable.getSelectionModel().addListSelectionListener(e -> showSelectedPublicReport());

        publicDetailScroll = UITheme.detailScroll();

        JPanel body = new JPanel(new GridLayout(1, 2, 14, 0));
        body.setBackground(UITheme.PAGE);
        body.add(UITheme.borderedScroll(publicReportTable));
        body.add(publicDetailScroll);
        tab.add(body, BorderLayout.CENTER);

        tab.add(UITheme.hint("Reports submitted by residents. Select one on the left to view its details."),
                BorderLayout.SOUTH);
        return tab;
    }

    private JPanel buildFilterRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setBackground(UITheme.PAGE);

        row.add(UITheme.label("City / Municipality:", UITheme.FONT_BASE, UITheme.TEXT));
        publicFilterCityBox = UITheme.combo(ALL_MUNICIPALITIES, locationManager.getMunicipalities());
        publicFilterCityBox.addActionListener(e -> {
            updateFilterBarangays();
            refreshAllReports();
        });
        row.add(publicFilterCityBox);

        row.add(UITheme.label("Barangay:", UITheme.FONT_BASE, UITheme.TEXT));
        publicFilterBarangayBox = UITheme.options(new String[] { ALL_BARANGAYS });
        publicFilterBarangayBox.addActionListener(e -> refreshAllReports());
        row.add(publicFilterBarangayBox);

        row.add(UITheme.label("Sort by:", UITheme.FONT_BASE, UITheme.TEXT));
        publicFilterSortBox = UITheme.options(SORT_OPTIONS);
        publicFilterSortBox.addActionListener(e -> refreshAllReports());
        row.add(publicFilterSortBox);

        return row;
    }

    private void updateFilterBarangays() {
        publicFilterBarangayBox.removeAllItems();
        publicFilterBarangayBox.addItem(ALL_BARANGAYS);

        String municipality = UITheme.text(publicFilterCityBox);
        if (municipality.length() == 0 || municipality.equals(ALL_MUNICIPALITIES)) {
            return;
        }

        List<String> barangays = locationManager.getBarangays(municipality);
        for (int i = 0; i < barangays.size(); i++) {
            publicFilterBarangayBox.addItem(barangays.get(i));
        }
    }

    public void showAllReports() {
        refreshAllReports();
    }

    private void refreshAllReports() {
        publicReports = reportManager.getReports(filterValue(publicFilterCityBox, ALL_MUNICIPALITIES),
                filterValue(publicFilterBarangayBox, ALL_BARANGAYS),
                UITheme.text(publicFilterSortBox));

        publicReportTableModel.setRowCount(0);
        for (int i = 0; i < publicReports.size(); i++) {
            Report report = publicReports.get(i);
            publicReportTableModel.addRow(new Object[] { report.getReportId(), report.getReportType(),
                userManager.getFullNameById(report.getReporterId()), report.getStatus().name(),
                report.getDateSubmitted() });
        }

        if (publicReportTableModel.getRowCount() > 0) {
            publicReportTable.setRowSelectionInterval(0, 0);
        } else {
            publicDetailScroll.setViewportView(UITheme.emptyDetail("No reports match the selected filters."));
        }
    }

    private static String filterValue(JComboBox<String> box, String allOption) {
        String value = UITheme.text(box);
        if (value.length() == 0 || value.equals(allOption)) {
            return null;
        }
        return value;
    }
    private void showSelectedPublicReport() {
        Report report = selectedPublicReport();
        if (report == null) {
            return;
        }
        publicDetailScroll.setViewportView(reportForm(report));
        publicDetailScroll.getVerticalScrollBar().setValue(0);
    }

    private Report selectedPublicReport() {
        int row = publicReportTable.getSelectedRow();
        if (row < 0 || publicReports == null || row >= publicReports.size()) {
            return null;
        }
        return publicReports.get(row);
    }

    private JPanel buildSubmitTab() {
        JPanel tab = new JPanel(new GridBagLayout());
        tab.setBackground(UITheme.PAGE);

        JPanel content = UITheme.card();
        content.setLayout(new BorderLayout(0, 14));

        JPanel header = new JPanel();
        header.setBackground(Color.WHITE);
        header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.Y_AXIS));
        header.add(UITheme.label("Submit a Hazard Report", UITheme.FONT_HEADING, UITheme.PRIMARY));
        header.add(Box.createVerticalStrut(2));
        header.add(UITheme.label("Fill in where the hazard is and what you observed.",
                UITheme.FONT_BASE, UITheme.MUTED));
        content.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);

        int row = 0;

        hazardTypeBox = UITheme.options(new String[] { "Road Hazard", "Flood", "Streetlight", "Other" });
        UITheme.addFormRow(form, row, "Hazard Type", hazardTypeBox);
        row = row + 2;

        cityMunicipalityBox = UITheme.combo(null, locationManager.getMunicipalities());
        cityMunicipalityBox.addActionListener(e -> updateBarangayBox());
        UITheme.addFormRow(form, row, "City / Municipality", cityMunicipalityBox);
        row = row + 2;

        barangayBox = new JComboBox<String>();
        barangayBox.setFont(UITheme.FONT_BASE);
        UITheme.addFormRow(form, row, "Barangay", barangayBox);
        row = row + 2;
        updateBarangayBox();

        streetField = UITheme.hintField("Optional (e.g. Casa Road)", 20);
        UITheme.addFormRow(form, row, "Street", streetField);
        row = row + 2;

        specificPlaceField = UITheme.hintField("(e.g. Near Water Refiller)", 20);
        UITheme.addFormRow(form, row, "Specific Place", specificPlaceField);
        row = row + 2;

        specificDetailField = UITheme.hintField("(e.g. Pothole, Knee deep, Pole 12)", 20);
        UITheme.addFormRow(form, row, "Specific Detail", specificDetailField);
        row = row + 2;

        descriptionArea = UITheme.hintArea("(e.g. Road blocked due to accident)", 4, 20);
        JScrollPane descriptionScroll = UITheme.borderedScroll(descriptionArea);
        descriptionScroll.setPreferredSize(new Dimension(10, 90));
        UITheme.addFormRow(form, row, "Description", descriptionScroll);

        content.add(form, BorderLayout.CENTER);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonRow.setBackground(Color.WHITE);
        JButton submitButton = UITheme.button("Submit Report", UITheme.ACCENT, Color.WHITE);
        submitButton.addActionListener(e -> submitReport());
        buttonRow.add(submitButton);
        content.add(buttonRow, BorderLayout.SOUTH);

        Dimension preferred = content.getPreferredSize();
        content.setPreferredSize(new Dimension(660, preferred.height));

        tab.add(content);
        return tab;
    }

    private void updateBarangayBox() {
        barangayBox.removeAllItems();
        List<String> barangays = locationManager.getBarangays(UITheme.text(cityMunicipalityBox));
        for (int i = 0; i < barangays.size(); i++) {
            barangayBox.addItem(barangays.get(i));
        }
    }
    private JPanel buildReportsTab() {
        JPanel tab = UITheme.page();

        reportTable = UITheme.dataTable(new Object[] { "Report ID", "Type", "Status", "Date & Time" });
        reportTableModel = (DefaultTableModel) reportTable.getModel();
        reportTable.getColumnModel().getColumn(2).setCellRenderer(UITheme.statusRenderer());
        reportTable.getSelectionModel().addListSelectionListener(e -> showSelectedReport());

        detailScroll = UITheme.detailScroll();

        JPanel body = new JPanel(new GridLayout(1, 2, 14, 0));
        body.setBackground(UITheme.PAGE);
        body.add(UITheme.borderedScroll(reportTable));
        body.add(detailScroll);
        tab.add(body, BorderLayout.CENTER);

        reportActions = buildReportButtonRow();
        tab.add(UITheme.hint("Only reports that are still PENDING can be changed."), BorderLayout.SOUTH);

        return tab;
    }

    private JPanel buildReportButtonRow() {
        JButton updateButton = UITheme.button("Update Selected Report", UITheme.BORDER, UITheme.TEXT);
        updateButton.addActionListener(e -> updateSelectedReport());

        JButton deleteButton = UITheme.button("Delete Selected Report", UITheme.DANGER, Color.WHITE);
        deleteButton.addActionListener(e -> deleteSelectedReport());

        return UITheme.actionRow(updateButton, deleteButton);
    }

    private void showSelectedReport() {
        Report report = selectedReport();
        if (report == null) {
            return;
        }
        detailScroll.setViewportView(reportForm(report));
        detailScroll.getVerticalScrollBar().setValue(0);
    }

    private Report selectedReport() {
        int row = reportTable.getSelectedRow();
        if (row < 0 || currentReports == null || row >= currentReports.size()) {
            return null;
        }
        return currentReports.get(row);
    }

    public void showOwnReports() {
        currentReports = reportManager.getReportsByReporter(currentUser.getUserId());

        reportTableModel.setRowCount(0);
        for (int i = 0; i < currentReports.size(); i++) {
            Report report = currentReports.get(i);
            reportTableModel.addRow(new Object[] { report.getReportId(), report.getReportType(),
                report.getStatus().name(), report.getDateSubmitted() });
        }

        if (reportTableModel.getRowCount() > 0) {
            reportTable.setRowSelectionInterval(0, 0);
        } else {
            detailScroll.setViewportView(UITheme.emptyDetail("You have not submitted any reports yet."));
        }
    }
    public void submitReport() {
        String hazardType = UITheme.text(hazardTypeBox);
        String cityMunicipality = UITheme.text(cityMunicipalityBox);
        String barangay = UITheme.text(barangayBox);
        String street = streetField.getText().trim();
        String specificPlace = specificPlaceField.getText().trim();
        String specificDetail = specificDetailField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (cityMunicipality.length() == 0 || barangay.length() == 0) {
            showMessage("Please select a City / Municipality and a Barangay.");
            return;
        }
        if (description.length() == 0) {
            showMessage("Please enter a description of the hazard.");
            return;
        }
        if (street.length() == 0) {
            street = "N/A";
        }

        Location location = new Location(cityMunicipality, barangay, street, specificPlace);
        Report report = reportManager.submitReport(currentUser.getUserId(), hazardType, location,
                description, specificDetail);

        if (report == null) {
            showMessage("The report could not be saved.");
            return;
        }

        showMessage("Report " + report.getReportId() + " submitted. Thank you!");
        streetField.setText("");
        specificPlaceField.setText("");
        specificDetailField.setText("");
        descriptionArea.setText("");

        showOwnReports();
        showAllReports();
        tabs.setSelectedIndex(2);
    }

    public void updateSelectedReport() {
        Report report = selectedReport();
        if (report == null) {
            showMessage("Please select one of your reports first.");
            return;
        }
        if (report.getStatus() != ReportStatus.PENDING) {
            showMessage("Only reports that are still PENDING can be updated.");
            return;
        }

        ReportUpdateDialog dialog = new ReportUpdateDialog(this, report, currentUser.getUserId(),
                reportManager, locationManager);
        if (dialog.showDialog()) {
            showMessage("Report " + report.getReportId() + " updated.");
            showOwnReports();
            showAllReports();
        }
    }

    public void deleteSelectedReport() {
        Report report = selectedReport();
        if (report == null) {
            showMessage("Please select one of your reports first.");
            return;
        }
        if (report.getStatus() != ReportStatus.PENDING) {
            showMessage("Only reports that are still PENDING can be deleted.");
            return;
        }

        int answer = JOptionPane.showConfirmDialog(this, "Delete report " + report.getReportId() + "?",
                "Delete Report", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        if (reportManager.deleteResidentReport(report.getReportId(), currentUser.getUserId())) {
            showMessage("Report " + report.getReportId() + " deleted.");
            showOwnReports();
            showAllReports();
        } else {
            showMessage("The report could not be deleted.");
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

        body.add(reportActions);
        body.add(Box.createVerticalStrut(12));

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
