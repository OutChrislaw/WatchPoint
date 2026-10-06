import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * ResidentDashboard.java
 * The window a Resident sees after logging in. It allows the resident to
 * submit a new hazard report and to manage the reports they own.
 */
public class ResidentDashboard extends JFrame {

    private Resident currentUser;
    private ReportManager reportManager;
    private UserManager userManager;

    private JComboBox<String> hazardTypeBox;
    private JTextField cityMunicipalityField;
    private JTextField barangayField;
    private JTextField streetField;
    private JTextField specificPlaceField;
    private JTextField specificDetailField;
    private JTextArea descriptionArea;
    private JTable reportTable;
    private DefaultTableModel reportTableModel;

    public ResidentDashboard(Resident currentUser, ReportManager reportManager, UserManager userManager) {
        this.currentUser = currentUser;
        this.reportManager = reportManager;
        this.userManager = userManager;

        setTitle("WatchPoint - Resident: " + currentUser.getFullName());
        setSize(950, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        hazardTypeBox = new JComboBox<String>();
        hazardTypeBox.addItem("Road Hazard");
        hazardTypeBox.addItem("Flood");
        hazardTypeBox.addItem("Streetlight");
        hazardTypeBox.addItem("Other");

        cityMunicipalityField = new JTextField();
        barangayField = new JTextField();
        streetField = new JTextField();
        specificPlaceField = new JTextField();
        specificDetailField = new JTextField();
        descriptionArea = new JTextArea(3, 20);

        JPanel formPanel = new JPanel(new GridLayout(7, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        formPanel.add(new JLabel("Hazard Type:"));
        formPanel.add(hazardTypeBox);
        formPanel.add(new JLabel("City / Municipality:"));
        formPanel.add(cityMunicipalityField);
        formPanel.add(new JLabel("Barangay:"));
        formPanel.add(barangayField);
        formPanel.add(new JLabel("Street:"));
        formPanel.add(streetField);
        formPanel.add(new JLabel("Specific Place:"));
        formPanel.add(specificPlaceField);
        formPanel.add(new JLabel("Specific Detail (hazard type / water level / pole number):"));
        formPanel.add(specificDetailField);
        formPanel.add(new JLabel("Description:"));
        formPanel.add(new JScrollPane(descriptionArea));

        JButton submitButton = new JButton("Submit Report");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitReport();
            }
        });

        JPanel submitButtonPanel = new JPanel(new FlowLayout());
        submitButtonPanel.add(submitButton);

        JPanel submitTab = new JPanel(new BorderLayout());
        submitTab.add(formPanel, BorderLayout.CENTER);
        submitTab.add(submitButtonPanel, BorderLayout.SOUTH);

        reportTableModel = new DefaultTableModel();
        reportTableModel.addColumn("Report ID");
        reportTableModel.addColumn("Type");
        reportTableModel.addColumn("Location");
        reportTableModel.addColumn("Description");
        reportTableModel.addColumn("Detail");
        reportTableModel.addColumn("Status");
        reportTableModel.addColumn("Date Submitted");

        reportTable = new JTable(reportTableModel);

        JButton updateButton = new JButton("Update Selected Report");
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateSelectedReport();
            }
        });

        JButton deleteButton = new JButton("Delete Selected Report");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedReport();
            }
        });

        JPanel reportButtonPanel = new JPanel(new FlowLayout());
        reportButtonPanel.add(updateButton);
        reportButtonPanel.add(deleteButton);

        JPanel reportsTab = new JPanel(new BorderLayout());
        reportsTab.add(new JScrollPane(reportTable), BorderLayout.CENTER);
        reportsTab.add(reportButtonPanel, BorderLayout.SOUTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Submit Report", submitTab);
        tabs.addTab("My Reports", reportsTab);
        add(tabs, BorderLayout.CENTER);

        JButton logoutButton = new JButton("Log Out");
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(logoutButton);
        add(bottomPanel, BorderLayout.SOUTH);

        showOwnReports();
    }

    public void showOwnReports() {
        reportTableModel.setRowCount(0);

        List<Report> reports = reportManager.getReportsByReporter(currentUser.getUserId());
        for (int i = 0; i < reports.size(); i++) {
            Report report = reports.get(i);
            Object[] row = new Object[7];
            row[0] = report.getReportId();
            row[1] = report.getReportType();
            row[2] = report.getLocation().getFullLocation();
            row[3] = report.getDescription();
            row[4] = report.getSpecificDetail();
            row[5] = report.getStatus().name();
            row[6] = report.getDateSubmitted();
            reportTableModel.addRow(row);
        }
    }

    public void submitReport() {
        String hazardType = (String) hazardTypeBox.getSelectedItem();
        String cityMunicipality = cityMunicipalityField.getText().trim();
        String barangay = barangayField.getText().trim();
        String street = streetField.getText().trim();
        String specificPlace = specificPlaceField.getText().trim();
        String specificDetail = specificDetailField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (cityMunicipality.length() == 0 || barangay.length() == 0 || street.length() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Please enter the city or municipality, barangay, and street.");
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

        cityMunicipalityField.setText("");
        barangayField.setText("");
        streetField.setText("");
        specificPlaceField.setText("");
        specificDetailField.setText("");
        descriptionArea.setText("");

        showOwnReports();
    }

    public void updateSelectedReport() {
        int selectedRow = reportTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select one of your reports first.");
            return;
        }

        String reportId = (String) reportTableModel.getValueAt(selectedRow, 0);
        Report report = reportManager.getReportById(reportId);
        if (report == null) {
            JOptionPane.showMessageDialog(this, "Report not found.");
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
        int selectedRow = reportTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select one of your reports first.");
            return;
        }

        String reportId = (String) reportTableModel.getValueAt(selectedRow, 0);
        int answer = JOptionPane.showConfirmDialog(this,
                "Delete report " + reportId + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        if (reportManager.deleteResidentReport(reportId, currentUser.getUserId())) {
            JOptionPane.showMessageDialog(this, "Report deleted.");
            showOwnReports();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Only your own reports that are still PENDING can be deleted.");
        }
    }

    public void logout() {
        LoginFrame loginFrame = new LoginFrame(userManager, reportManager);
        loginFrame.setVisible(true);
        this.dispose();
    }
}
