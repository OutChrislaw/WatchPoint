import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

/**
 * ResidentDashboard.java
 * The window a Resident sees after logging in. It allows the resident to
 * submit a new hazard report and to manage the reports they own. Reports are
 * shown as a list on the left and a form-style detail view on the right.
 */
public class ResidentDashboard extends JFrame {

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

    private JTable reportTable;
    private DefaultTableModel reportTableModel;
    private ReportDetailPanel detailPanel;
    private List<Report> currentReports;

    public ResidentDashboard(Resident currentUser, ReportManager reportManager, UserManager userManager,
                             LocationManager locationManager) {
        this.currentUser = currentUser;
        this.reportManager = reportManager;
        this.userManager = userManager;
        this.locationManager = locationManager;

        setTitle("WatchPoint - Resident: " + currentUser.getFullName());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(920, 600));
        setSize(1150, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BACKGROUND);

        add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BOLD);
        tabs.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        tabs.addTab("  Submit Report  ", buildSubmitTab());
        tabs.addTab("  My Reports  ", buildReportsTab());
        add(tabs, BorderLayout.CENTER);

        showOwnReports();
    }

    private JPanel buildHeader() {
        JPanel header = UITheme.headerBar("WatchPoint",
                "Signed in as " + currentUser.getFullName() + "   -   Resident");

        JButton logoutButton = UITheme.secondaryButton("Log Out");
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });

        JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        east.setOpaque(false);
        east.add(logoutButton);
        header.add(east, BorderLayout.EAST);
        return header;
    }

    private JPanel buildSubmitTab() {
        hazardTypeBox = new JComboBox<String>();
        hazardTypeBox.addItem("Road Hazard");
        hazardTypeBox.addItem("Flood");
        hazardTypeBox.addItem("Streetlight");
        hazardTypeBox.addItem("Other");
        hazardTypeBox.setFont(UITheme.FONT_BASE);

        cityMunicipalityBox = new JComboBox<String>();
        List<String> municipalities = locationManager.getMunicipalities();
        for (int i = 0; i < municipalities.size(); i++) {
            cityMunicipalityBox.addItem(municipalities.get(i));
        }
        cityMunicipalityBox.setFont(UITheme.FONT_BASE);

        barangayBox = new JComboBox<String>();
        barangayBox.setFont(UITheme.FONT_BASE);

        cityMunicipalityBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateBarangayBox();
            }
        });
        updateBarangayBox();

        streetField = new JTextField(18);
        specificPlaceField = new JTextField(18);
        specificDetailField = new JTextField(18);
        UITheme.styleField(streetField);
        UITheme.styleField(specificPlaceField);
        UITheme.styleField(specificDetailField);

        descriptionArea = new JTextArea(4, 18);
        descriptionArea.setFont(UITheme.FONT_BASE);
        descriptionArea.setForeground(UITheme.TEXT);
        descriptionArea.setBackground(UITheme.CARD);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        descriptionScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
        descriptionScroll.setPreferredSize(new Dimension(200, 96));

        JPanel form = UITheme.card();
        form.setLayout(new GridBagLayout());

        GridBagConstraints titleConstraints = new GridBagConstraints();
        titleConstraints.gridx = 0;
        titleConstraints.gridy = 0;
        titleConstraints.gridwidth = 2;
        titleConstraints.anchor = GridBagConstraints.WEST;
        titleConstraints.insets = new Insets(0, 6, 4, 6);
        form.add(UITheme.sectionLabel("Submit a Hazard Report"), titleConstraints);

        JLabel hint = new JLabel("Fill in where the hazard is and what you observed.");
        hint.setFont(UITheme.FONT_SMALL);
        hint.setForeground(UITheme.MUTED);
        GridBagConstraints hintConstraints = new GridBagConstraints();
        hintConstraints.gridx = 0;
        hintConstraints.gridy = 1;
        hintConstraints.gridwidth = 2;
        hintConstraints.anchor = GridBagConstraints.WEST;
        hintConstraints.insets = new Insets(0, 6, 16, 6);
        form.add(hint, hintConstraints);

        addFormRow(form, 2, "Hazard Type", hazardTypeBox);
        addFormRow(form, 3, "City / Municipality", cityMunicipalityBox);
        addFormRow(form, 4, "Barangay", barangayBox);
        addFormRow(form, 5, "Street", streetField);
        addFormRow(form, 6, "Specific Place", specificPlaceField);
        addFormRow(form, 7, "Specific Detail", specificDetailField);
        addFormRow(form, 8, "Description", descriptionScroll);

        JButton submitButton = UITheme.primaryButton("Submit Report");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitReport();
            }
        });

        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.gridx = 1;
        buttonConstraints.gridy = 9;
        buttonConstraints.anchor = GridBagConstraints.EAST;
        buttonConstraints.insets = new Insets(14, 6, 0, 6);
        form.add(submitButton, buttonConstraints);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(UITheme.BACKGROUND);
        GridBagConstraints wrapperConstraints = new GridBagConstraints();
        wrapperConstraints.gridx = 0;
        wrapperConstraints.gridy = 0;
        wrapperConstraints.weightx = 1.0;
        wrapperConstraints.weighty = 1.0;
        wrapperConstraints.anchor = GridBagConstraints.NORTH;
        wrapperConstraints.insets = new Insets(18, 18, 18, 18);
        wrapper.add(form, wrapperConstraints);

        Dimension preferred = form.getPreferredSize();
        form.setPreferredSize(new Dimension(640, preferred.height));
        return wrapper;
    }

    private void addFormRow(JPanel form, int row, String label, Component field) {
        JLabel caption = new JLabel(label);
        caption.setFont(UITheme.FONT_BOLD);
        caption.setForeground(UITheme.TEXT);

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.NORTHWEST;
        labelConstraints.insets = new Insets(6, 6, 6, 14);
        form.add(caption, labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.weightx = 1.0;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.anchor = GridBagConstraints.NORTHWEST;
        fieldConstraints.insets = new Insets(6, 6, 6, 6);
        form.add(field, fieldConstraints);
    }

    private void updateBarangayBox() {
        barangayBox.removeAllItems();
        if (cityMunicipalityBox.getSelectedItem() == null) {
            return;
        }
        String municipality = cityMunicipalityBox.getSelectedItem().toString();
        List<String> barangays = locationManager.getBarangays(municipality);
        for (int i = 0; i < barangays.size(); i++) {
            barangayBox.addItem(barangays.get(i));
        }
    }

    private JPanel buildReportsTab() {
        reportTableModel = new DefaultTableModel();
        reportTableModel.addColumn("Report ID");
        reportTableModel.addColumn("Type");
        reportTableModel.addColumn("Status");
        reportTableModel.addColumn("Date Submitted");

        reportTable = UITheme.styledTable(reportTableModel);
        reportTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        reportTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        reportTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        reportTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        reportTable.getColumnModel().getColumn(2).setCellRenderer(UITheme.statusCellRenderer());

        reportTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    showSelectedReport();
                }
            }
        });

        detailPanel = new ReportDetailPanel();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                UITheme.scroll(reportTable), detailPanel);
        split.setBorder(null);
        split.setOpaque(false);
        split.setResizeWeight(0.33);
        split.setDividerLocation(340);
        split.setDividerSize(8);
        split.setContinuousLayout(true);

        JPanel tab = new JPanel(new BorderLayout(0, 12));
        tab.setBackground(UITheme.BACKGROUND);
        tab.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        tab.add(split, BorderLayout.CENTER);
        tab.add(buildReportButtonRow(), BorderLayout.SOUTH);
        return tab;
    }

    private JPanel buildReportButtonRow() {
        JButton updateButton = UITheme.secondaryButton("Update Selected Report");
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateSelectedReport();
            }
        });

        JButton deleteButton = UITheme.dangerButton("Delete Selected Report");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedReport();
            }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(updateButton);
        buttons.add(deleteButton);

        JLabel hint = new JLabel("Select one of your reports on the left to view it.");
        hint.setFont(UITheme.FONT_SMALL);
        hint.setForeground(UITheme.MUTED);

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(hint, BorderLayout.WEST);
        row.add(buttons, BorderLayout.EAST);
        return row;
    }

    private void showSelectedReport() {
        Report report = selectedReport();
        if (report == null) {
            detailPanel.clear();
        } else {
            detailPanel.setReport(report);
        }
    }

    private Report selectedReport() {
        int viewRow = reportTable.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = reportTable.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= currentReports.size()) {
            return null;
        }
        return currentReports.get(modelRow);
    }

    public void showOwnReports() {
        reportTableModel.setRowCount(0);
        currentReports = reportManager.getReportsByReporter(currentUser.getUserId());

        for (int i = 0; i < currentReports.size(); i++) {
            Report report = currentReports.get(i);
            Object[] row = new Object[4];
            row[0] = report.getReportId();
            row[1] = report.getReportType();
            row[2] = report.getStatus().name();
            row[3] = report.getDateSubmitted();
            reportTableModel.addRow(row);
        }

        if (currentReports.size() > 0) {
            reportTable.setRowSelectionInterval(0, 0);
        } else {
            reportTable.clearSelection();
            detailPanel.clear();
        }
    }

    public void submitReport() {
        String hazardType = (String) hazardTypeBox.getSelectedItem();
        String cityMunicipality = "";
        if (cityMunicipalityBox.getSelectedItem() != null) {
            cityMunicipality = cityMunicipalityBox.getSelectedItem().toString();
        }
        String barangay = "";
        if (barangayBox.getSelectedItem() != null) {
            barangay = barangayBox.getSelectedItem().toString();
        }
        String street = streetField.getText().trim();
        String specificPlace = specificPlaceField.getText().trim();
        String specificDetail = specificDetailField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (cityMunicipality.length() == 0 || barangay.length() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a City / Municipality and a Barangay.");
            return;
        }
        if (street.length() == 0) {
            JOptionPane.showMessageDialog(this, "Please enter the street.");
            return;
        }
        if (description.length() == 0) {
            JOptionPane.showMessageDialog(this, "Please enter a description of the hazard.");
            return;
        }

        Location location = new Location(cityMunicipality, barangay, street, specificPlace);
        Report report = reportManager.submitReport(currentUser.getUserId(), hazardType,
                location, description, specificDetail);

        JOptionPane.showMessageDialog(this,
                "Report submitted. Your report ID is " + report.getReportId() + ".");

        if (cityMunicipalityBox.getItemCount() > 0) {
            cityMunicipalityBox.setSelectedIndex(0);
        }
        streetField.setText("");
        specificPlaceField.setText("");
        specificDetailField.setText("");
        descriptionArea.setText("");

        showOwnReports();
    }

    public void updateSelectedReport() {
        Report report = selectedReport();
        if (report == null) {
            JOptionPane.showMessageDialog(this, "Please select one of your reports first.");
            return;
        }
        if (report.getStatus() != ReportStatus.PENDING) {
            JOptionPane.showMessageDialog(this,
                    "Only reports that are still PENDING can be updated.");
            return;
        }

        String newDescription = JOptionPane.showInputDialog(this, "New description:", report.getDescription());
        if (newDescription == null) {
            return;
        }
        if (newDescription.trim().length() == 0) {
            JOptionPane.showMessageDialog(this, "The description cannot be empty.");
            return;
        }
        report.setDescription(newDescription.trim());

        if (reportManager.updateResidentReport(report, currentUser.getUserId())) {
            JOptionPane.showMessageDialog(this, "Report updated.");
            showOwnReports();
        } else {
            JOptionPane.showMessageDialog(this, "The report could not be updated.");
        }
    }

    public void deleteSelectedReport() {
        Report report = selectedReport();
        if (report == null) {
            JOptionPane.showMessageDialog(this, "Please select one of your reports first.");
            return;
        }

        int answer = JOptionPane.showConfirmDialog(this,
                "Delete report " + report.getReportId() + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        if (reportManager.deleteResidentReport(report.getReportId(), currentUser.getUserId())) {
            JOptionPane.showMessageDialog(this, "Report deleted.");
            showOwnReports();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Only your own reports that are still PENDING can be deleted.");
        }
    }

    public void logout() {
        LoginFrame loginFrame = new LoginFrame(userManager, reportManager, locationManager);
        loginFrame.setVisible(true);
        this.dispose();
    }
}



