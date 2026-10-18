/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
/**
 *
 * @author CHAN
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * ResidentDashboard.java
 * The window a resident sees after logging in.
 * Tabs: Reports (all reports), Submit Report and My Reports.
 */
public class ResidentDashboard extends javax.swing.JFrame {

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
    private static final String[] SORT_OPTIONS = {
        "Report ID (Oldest first)", "Report ID (Newest first)",
        "Date and Time (Newest first)", "Date and Time (Oldest first)",
        "Status", "Hazard Type"
    };

    private JPanel buildAllReportsTab() {
        JPanel tab = page();
        tab.add(buildFilterRow(), BorderLayout.NORTH);

        publicReportTable = dataTable(new Object[] { "Report ID", "Type", "Reporter", "Status", "Date & Time" });
        publicReportTableModel = (DefaultTableModel) publicReportTable.getModel();
        publicReportTable.getColumnModel().getColumn(3).setCellRenderer(new StatusRenderer());
        publicReportTable.getSelectionModel().addListSelectionListener(e -> showSelectedPublicReport());

        publicDetailScroll = detailScroll();

        JPanel body = new JPanel(new GridLayout(1, 2, 14, 0));
        body.setBackground(PAGE);
        body.add(borderedScroll(publicReportTable));
        body.add(publicDetailScroll);
        tab.add(body, BorderLayout.CENTER);

        tab.add(hint("Reports submitted by residents. Select one on the left to view its details."), BorderLayout.SOUTH);
        return tab;
    }

    private JPanel buildFilterRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setBackground(PAGE);

        row.add(label("City / Municipality:", FONT_BASE, TEXT));
        publicFilterCityBox = combo(ALL_MUNICIPALITIES, locationManager.getMunicipalities());
        publicFilterCityBox.addActionListener(e -> {
            updateFilterBarangays();
            refreshAllReports();
        });
        row.add(publicFilterCityBox);

        row.add(label("Barangay:", FONT_BASE, TEXT));
        publicFilterBarangayBox = new JComboBox<String>();
        publicFilterBarangayBox.setFont(FONT_BASE);
        publicFilterBarangayBox.addItem(ALL_BARANGAYS);
        publicFilterBarangayBox.addActionListener(e -> refreshAllReports());
        row.add(publicFilterBarangayBox);

        row.add(label("Sort by:", FONT_BASE, TEXT));
        publicFilterSortBox = new JComboBox<String>();
        publicFilterSortBox.setFont(FONT_BASE);
        for (int i = 0; i < SORT_OPTIONS.length; i++) {
            publicFilterSortBox.addItem(SORT_OPTIONS[i]);
        }
        publicFilterSortBox.addActionListener(e -> refreshAllReports());
        row.add(publicFilterSortBox);

        return row;
    }

    private void updateFilterBarangays() {
        publicFilterBarangayBox.removeAllItems();
        publicFilterBarangayBox.addItem(ALL_BARANGAYS);

        if (publicFilterCityBox.getSelectedItem() == null) {
            return;
        }

        String municipality = publicFilterCityBox.getSelectedItem().toString();
        if (municipality.equals(ALL_MUNICIPALITIES)) {
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
        publicReports = selectedReports(reportManager.getAllReports(),
                publicFilterCityBox, publicFilterBarangayBox, publicFilterSortBox);

        publicReportTableModel.setRowCount(0);
        for (int i = 0; i < publicReports.size(); i++) {
            Report report = publicReports.get(i);
            publicReportTableModel.addRow(new Object[] { report.getReportId(), report.getReportType(),
                reporterName(report), report.getStatus().name(), report.getDateSubmitted() });
        }

        if (publicReportTableModel.getRowCount() > 0) {
            publicReportTable.setRowSelectionInterval(0, 0);
        } else {
            publicDetailScroll.setViewportView(emptyDetail("No reports match the selected filters."));
        }
    }

    private static List<Report> selectedReports(List<Report> reports, JComboBox<String> cityBox,
                                               JComboBox<String> barangayBox, JComboBox<String> sortBox) {
        String municipality = selected(cityBox, ALL_MUNICIPALITIES);
        String barangay = selected(barangayBox, ALL_BARANGAYS);

        List<Report> result = new ArrayList<Report>();
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
        tab.setBackground(PAGE);

        JPanel form = card();
        form.setLayout(new GridBagLayout());

        GridBagConstraints headingConstraints = new GridBagConstraints();
        headingConstraints.gridx = 0;
        headingConstraints.gridy = 0;
        headingConstraints.gridwidth = 2;
        headingConstraints.weightx = 1.0;
        headingConstraints.fill = GridBagConstraints.HORIZONTAL;
        headingConstraints.anchor = GridBagConstraints.WEST;
        headingConstraints.insets = new Insets(0, 0, 2, 0);
        form.add(label("Submit a Hazard Report", FONT_HEADING, PRIMARY), headingConstraints);

        GridBagConstraints subtitleConstraints = new GridBagConstraints();
        subtitleConstraints.gridx = 0;
        subtitleConstraints.gridy = 1;
        subtitleConstraints.gridwidth = 2;
        subtitleConstraints.weightx = 1.0;
        subtitleConstraints.fill = GridBagConstraints.HORIZONTAL;
        subtitleConstraints.anchor = GridBagConstraints.WEST;
        subtitleConstraints.insets = new Insets(0, 0, 16, 0);
        form.add(label("Fill in where the hazard is and what you observed.", FONT_BASE, MUTED),
                subtitleConstraints);

        int row = 2;

        hazardTypeBox = options(new String[] { "Road Hazard", "Flood", "Streetlight", "Other" });
        addFormRow(form, row, "Hazard Type", hazardTypeBox);
        row = row + 2;

        cityMunicipalityBox = combo(null, locationManager.getMunicipalities());
        cityMunicipalityBox.addActionListener(e -> updateBarangayBox());
        addFormRow(form, row, "City / Municipality", cityMunicipalityBox);
        row = row + 2;

        barangayBox = new JComboBox<String>();
        barangayBox.setFont(FONT_BASE);
        addFormRow(form, row, "Barangay", barangayBox);
        row = row + 2;
        updateBarangayBox();

        streetField = hintField("Optional (e.g. Casa Road)", 20);
        addFormRow(form, row, "Street", streetField);
        row = row + 2;

        specificPlaceField = hintField("(e.g. Near Water Refiller)", 20);
        addFormRow(form, row, "Specific Place", specificPlaceField);
        row = row + 2;

        specificDetailField = hintField("(e.g. Pothole, Knee deep, Pole 12)", 20);
        addFormRow(form, row, "Specific Detail", specificDetailField);
        row = row + 2;

        descriptionArea = hintArea("(e.g. Road blocked due to accident)", 4, 20);
        JScrollPane descriptionScroll = borderedScroll(descriptionArea);
        descriptionScroll.setPreferredSize(new Dimension(10, 90));
        addFormRow(form, row, "Description", descriptionScroll);
        row = row + 2;

        JButton submitButton = button("Submit Report", ACCENT, Color.WHITE);
        submitButton.addActionListener(e -> submitReport());

        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.gridx = 0;
        buttonConstraints.gridy = row;
        buttonConstraints.gridwidth = 2;
        buttonConstraints.anchor = GridBagConstraints.EAST;
        buttonConstraints.insets = new Insets(8, 0, 0, 0);
        form.add(submitButton, buttonConstraints);

        Dimension preferred = form.getPreferredSize();
        form.setPreferredSize(new Dimension(660, preferred.height));

        tab.add(form);
        return tab;
    }

    private static void addFormRow(JPanel form, int row, String caption, Component field) {
        GridBagConstraints captionConstraints = new GridBagConstraints();
        captionConstraints.gridx = 0;
        captionConstraints.gridy = row;
        captionConstraints.gridwidth = 2;
        captionConstraints.weightx = 1.0;
        captionConstraints.fill = GridBagConstraints.HORIZONTAL;
        captionConstraints.anchor = GridBagConstraints.WEST;
        captionConstraints.insets = new Insets(6, 0, 4, 0);
        form.add(label(caption, FONT_BOLD, MUTED), captionConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 0;
        fieldConstraints.gridy = row + 1;
        fieldConstraints.gridwidth = 2;
        fieldConstraints.weightx = 1.0;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.anchor = GridBagConstraints.WEST;
        fieldConstraints.insets = new Insets(0, 0, 8, 0);
        form.add(field, fieldConstraints);
    }

    private void updateBarangayBox() {
        barangayBox.removeAllItems();
        if (locationManager == null || cityMunicipalityBox.getSelectedItem() == null) {
            return;
        }

        List<String> barangays = locationManager.getBarangays(cityMunicipalityBox.getSelectedItem().toString());
        for (int i = 0; i < barangays.size(); i++) {
            barangayBox.addItem(barangays.get(i));
        }
    }
    private JPanel buildReportsTab() {
        JPanel tab = page();

        reportTable = dataTable(new Object[] { "Report ID", "Type", "Status", "Date & Time" });
        reportTableModel = (DefaultTableModel) reportTable.getModel();
        reportTable.getColumnModel().getColumn(2).setCellRenderer(new StatusRenderer());
        reportTable.getSelectionModel().addListSelectionListener(e -> showSelectedReport());

        detailScroll = detailScroll();

        JPanel body = new JPanel(new GridLayout(1, 2, 14, 0));
        body.setBackground(PAGE);
        body.add(borderedScroll(reportTable));
        body.add(detailScroll);
        tab.add(body, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(16, 0));
        south.setBackground(PAGE);
        south.add(buildReportButtonRow(), BorderLayout.WEST);
        south.add(hint("Only reports that are still PENDING can be changed."), BorderLayout.CENTER);
        tab.add(south, BorderLayout.SOUTH);

        return tab;
    }

    private JPanel buildReportButtonRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setBackground(PAGE);

        JButton updateButton = button("Update Selected Report", BORDER, TEXT);
        updateButton.addActionListener(e -> updateSelectedReport());
        row.add(updateButton);

        JButton deleteButton = button("Delete Selected Report", DANGER, Color.WHITE);
        deleteButton.addActionListener(e -> deleteSelectedReport());
        row.add(deleteButton);

        return row;
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
            detailScroll.setViewportView(emptyDetail("You have not submitted any reports yet."));
        }
    }
    public void submitReport() {
        String hazardType = text(hazardTypeBox);
        String cityMunicipality = text(cityMunicipalityBox);
        String barangay = text(barangayBox);
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
        final Report report = selectedReport();
        if (report == null) {
            showMessage("Please select one of your reports first.");
            return;
        }
        if (report.getStatus() != ReportStatus.PENDING) {
            showMessage("Only reports that are still PENDING can be updated.");
            return;
        }

        final JDialog dialog = new JDialog(this, "Update Report " + report.getReportId(), true);
        dialog.setLayout(new BorderLayout());

        JPanel form = card();
        form.setLayout(new GridBagLayout());

        GridBagConstraints headingConstraints = new GridBagConstraints();
        headingConstraints.gridx = 0;
        headingConstraints.gridy = 0;
        headingConstraints.gridwidth = 2;
        headingConstraints.weightx = 1.0;
        headingConstraints.fill = GridBagConstraints.HORIZONTAL;
        headingConstraints.anchor = GridBagConstraints.WEST;
        headingConstraints.insets = new Insets(0, 0, 2, 0);
        form.add(label("Update Report " + report.getReportId(), FONT_HEADING, PRIMARY), headingConstraints);

        GridBagConstraints subtitleConstraints = new GridBagConstraints();
        subtitleConstraints.gridx = 0;
        subtitleConstraints.gridy = 1;
        subtitleConstraints.gridwidth = 2;
        subtitleConstraints.weightx = 1.0;
        subtitleConstraints.fill = GridBagConstraints.HORIZONTAL;
        subtitleConstraints.anchor = GridBagConstraints.WEST;
        subtitleConstraints.insets = new Insets(0, 0, 16, 0);
        form.add(label("Change the details you want, then press Update Report.", FONT_BASE, MUTED),
                subtitleConstraints);

        int row = 2;

        JComboBox<String> dialogTypeBox = options(new String[] { report.getReportType() });
        dialogTypeBox.setEnabled(false);
        addFormRow(form, row, "Hazard Type", dialogTypeBox);
        row = row + 2;

        final JComboBox<String> dialogCityBox = combo(null, locationManager.getMunicipalities());
        dialogCityBox.setSelectedItem(report.getLocation().getCityMunicipality());
        addFormRow(form, row, "City / Municipality", dialogCityBox);
        row = row + 2;

        final JComboBox<String> dialogBarangayBox = new JComboBox<String>();
        dialogBarangayBox.setFont(FONT_BASE);
        List<String> barangays = locationManager.getBarangays(report.getLocation().getCityMunicipality());
        for (int i = 0; i < barangays.size(); i++) {
            dialogBarangayBox.addItem(barangays.get(i));
        }
        dialogBarangayBox.setSelectedItem(report.getLocation().getBarangay());
        dialogCityBox.addActionListener(e -> {
            dialogBarangayBox.removeAllItems();
            List<String> list = locationManager.getBarangays(text(dialogCityBox));
            for (int i = 0; i < list.size(); i++) {
                dialogBarangayBox.addItem(list.get(i));
            }
        });
        addFormRow(form, row, "Barangay", dialogBarangayBox);
        row = row + 2;

        final JTextField dialogStreetField = hintField("Optional (e.g. Casa Road)", 20);
        dialogStreetField.setText(report.getLocation().getStreet());
        addFormRow(form, row, "Street", dialogStreetField);
        row = row + 2;

        final JTextField dialogPlaceField = hintField("(e.g. Near Water Refiller)", 20);
        dialogPlaceField.setText(report.getLocation().getSpecificPlace());
        addFormRow(form, row, "Specific Place", dialogPlaceField);
        row = row + 2;

        final JTextField dialogDetailField = hintField("(e.g. Pothole, Knee deep, Pole 12)", 20);
        dialogDetailField.setText(report.getSpecificDetail());
        addFormRow(form, row, "Specific Detail", dialogDetailField);
        row = row + 2;

        final JTextArea dialogDescriptionArea = hintArea("(e.g. Road blocked due to accident)", 4, 20);
        dialogDescriptionArea.setText(report.getDescription());
        JScrollPane descriptionScroll = borderedScroll(dialogDescriptionArea);
        descriptionScroll.setPreferredSize(new Dimension(10, 90));
        addFormRow(form, row, "Description", descriptionScroll);
        row = row + 2;

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonRow.setBackground(Color.WHITE);

        JButton cancelButton = button("Cancel", BORDER, TEXT);
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonRow.add(cancelButton);

        JButton updateButton = button("Update Report", ACCENT, Color.WHITE);
        updateButton.addActionListener(e -> {
            String city = text(dialogCityBox);
            String barangay = text(dialogBarangayBox);
            String street = dialogStreetField.getText().trim();
            String place = dialogPlaceField.getText().trim();
            String detail = dialogDetailField.getText().trim();
            String description = dialogDescriptionArea.getText().trim();

            if (city.length() == 0 || barangay.length() == 0) {
                JOptionPane.showMessageDialog(dialog,
                        "Please select a City / Municipality and a Barangay.");
                return;
            }
            if (description.length() == 0) {
                JOptionPane.showMessageDialog(dialog, "Please enter a description of the hazard.");
                return;
            }
            if (street.length() == 0) {
                street = "N/A";
            }

            report.setLocation(new Location(city, barangay, street, place));
            report.setSpecificDetail(detail);
            report.setDescription(description);

            if (reportManager.updateResidentReport(report, currentUser.getUserId())) {
                dialog.dispose();
                showMessage("Report " + report.getReportId() + " updated.");
                showOwnReports();
                showAllReports();
            } else {
                JOptionPane.showMessageDialog(dialog, "The report could not be updated.");
            }
        });
        buttonRow.add(updateButton);

        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.gridx = 0;
        buttonConstraints.gridy = row;
        buttonConstraints.gridwidth = 2;
        buttonConstraints.anchor = GridBagConstraints.EAST;
        buttonConstraints.insets = new Insets(8, 0, 0, 0);
        form.add(buttonRow, buttonConstraints);

        dialog.add(form, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
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
    private static JPanel page() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(PAGE);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        return panel;
    }

    private static JPanel card() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
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

    private static JTextField hintField(final String hint, int columns) {
        JTextField field = new JTextField(columns) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                if (getText().length() == 0) {
                    java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(MUTED);
                    g2.setFont(getFont());
                    g2.drawString(hint, getInsets().left,
                            (getHeight() - g2.getFontMetrics().getHeight()) / 2
                            + g2.getFontMetrics().getAscent());
                    g2.dispose();
                }
            }
        };
        field.setFont(FONT_BASE);
        field.setForeground(TEXT);
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    private static JTextArea hintArea(final String hint, int rows, int columns) {
        JTextArea area = new JTextArea(rows, columns) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                if (getText().length() == 0) {
                    java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(MUTED);
                    g2.setFont(getFont());
                    g2.drawString(hint, getInsets().left + 2,
                            getInsets().top + g2.getFontMetrics().getAscent());
                    g2.dispose();
                }
            }
        };
        area.setFont(FONT_BASE);
        area.setForeground(TEXT);
        area.setBackground(Color.WHITE);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return area;
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
