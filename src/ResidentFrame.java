// ResidentFrame.java
// The resident dashboard. A resident can submit a hazard report and view
// the reports they submitted. All locations are inside Bohol.

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
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
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

public class ResidentFrame extends JFrame {

    private Resident resident;
    private UserManager userManager;
    private ReportManager reportManager;
    private LocationManager locationManager;

    // Submit report fields.
    private JComboBox<String> hazardTypeBox;
    private JComboBox<String> cityBox;
    private JComboBox<String> barangayBox;
    private JComboBox<String> streetBox;
    private JTextArea descriptionArea;
    private JTextField specificDetailField;

    // Table that shows the resident's reports.
    private DefaultTableModel tableModel;

    public ResidentFrame(Resident resident, UserManager userManager,
                         ReportManager reportManager, LocationManager locationManager) {
        this.resident = resident;
        this.userManager = userManager;
        this.reportManager = reportManager;
        this.locationManager = locationManager;

        setTitle("WatchPoint - Resident: " + resident.getFullName());
        setSize(760, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Submit Report", buildSubmitPanel());
        tabs.addTab("My Reports", buildViewPanel());
        add(tabs, BorderLayout.CENTER);

        JButton logoutButton = new JButton("Log Out");
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goToLogin();
            }
        });
        JPanel bottom = new JPanel();
        bottom.add(logoutButton);
        add(bottom, BorderLayout.SOUTH);

        refreshTable();
    }

    // Builds the Submit Report tab.
    private JPanel buildSubmitPanel() {
        JPanel panel = new JPanel(new GridLayout(7, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        hazardTypeBox = new JComboBox<String>();
        hazardTypeBox.addItem("Road Hazard");
        hazardTypeBox.addItem("Streetlight");
        hazardTypeBox.addItem("Flood");
        hazardTypeBox.addItem("Other");

        // Province is fixed to Bohol (shown as a read-only label).
        JLabel provinceLabel = new JLabel(Location.PROVINCE);

        // Create ALL the location dropdowns first, before filling them.
        // (fillCities() also fills the barangay and street boxes, so those
        //  must already exist or we would get a NullPointerException.)
        cityBox = new JComboBox<String>();
        barangayBox = new JComboBox<String>();
        streetBox = new JComboBox<String>();

        // Now it is safe to fill them from LocationManager.
        fillCities();

        descriptionArea = new JTextArea(3, 20);
        specificDetailField = new JTextField();
        JButton submitButton = new JButton("Submit Report");

        panel.add(new JLabel("Hazard Type:"));
        panel.add(hazardTypeBox);
        panel.add(new JLabel("Province:"));
        panel.add(provinceLabel);
        panel.add(new JLabel("City / Municipality:"));
        panel.add(cityBox);
        panel.add(new JLabel("Barangay:"));
        panel.add(barangayBox);
        panel.add(new JLabel("Street:"));
        panel.add(streetBox);
        panel.add(new JLabel("Description:"));
        panel.add(new JScrollPane(descriptionArea));
        panel.add(new JLabel("Specific Detail:"));
        panel.add(specificDetailField);

        // When the city changes, reload the barangay list.
        cityBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    fillBarangays();
                }
            }
        });

        // When the barangay changes, reload the street list.
        barangayBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    fillStreets();
                }
            }
        });

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doSubmit();
            }
        });

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(panel, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(submitButton);
        wrapper.add(buttonPanel, BorderLayout.SOUTH);

        return wrapper;
    }

    // Builds the My Reports tab with a table.
    private JPanel buildViewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        String[] columns = {"Report ID", "Type", "Location", "Description",
                "Specific Detail", "Status", "Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only table
            }
        };
        JTable table = new JTable(tableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshTable();
            }
        });
        JPanel bottom = new JPanel();
        bottom.add(refreshButton);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    // Fills the city/municipality dropdown.
    private void fillCities() {
        ArrayList<String> cities = locationManager.getCitiesAndMunicipalities();
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>();
        for (int i = 0; i < cities.size(); i++) {
            model.addElement(cities.get(i));
        }
        cityBox.setModel(model);
        fillBarangays(); // keep the next dropdown in sync
    }

    // Fills the barangay dropdown based on the selected city/municipality.
    private void fillBarangays() {
        String city = (String) cityBox.getSelectedItem();
        if (city == null) {
            return;
        }
        ArrayList<String> barangays = locationManager.getBarangays(city);
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>();
        for (int i = 0; i < barangays.size(); i++) {
            model.addElement(barangays.get(i));
        }
        barangayBox.setModel(model);
        fillStreets(); // keep the next dropdown in sync
    }

    // Fills the street dropdown based on the selected barangay.
    private void fillStreets() {
        String city = (String) cityBox.getSelectedItem();
        String barangay = (String) barangayBox.getSelectedItem();
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>();
        if (city != null && barangay != null) {
            ArrayList<String> streets = locationManager.getStreets(city, barangay);
            for (int i = 0; i < streets.size(); i++) {
                model.addElement(streets.get(i));
            }
        }
        streetBox.setModel(model);
    }

    // Handles the Submit Report button.
    private void doSubmit() {
        String hazardType = (String) hazardTypeBox.getSelectedItem();
        String city = (String) cityBox.getSelectedItem();
        String barangay = (String) barangayBox.getSelectedItem();
        String street = (String) streetBox.getSelectedItem();
        String description = descriptionArea.getText().trim();
        String specificDetail = specificDetailField.getText().trim();

        if (city == null || barangay == null) {
            JOptionPane.showMessageDialog(this, "Please choose a city/municipality and barangay.",
                    "Missing Location", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (description.length() == 0) {
            JOptionPane.showMessageDialog(this, "Please enter a description.",
                    "Missing Description", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String fullAddress = locationManager.buildFullAddress(city, barangay, street);

        Report report = reportManager.submitReport(resident.getUserId(), hazardType,
                fullAddress, description, specificDetail);

        JOptionPane.showMessageDialog(this,
                "Report submitted! Your report ID is " + report.getReportId() + ".",
                "Report Submitted", JOptionPane.INFORMATION_MESSAGE);

        // Clear the form.
        descriptionArea.setText("");
        specificDetailField.setText("");
        refreshTable();
    }

    // Reloads the table with this resident's own reports.
    private void refreshTable() {
        if (tableModel == null) {
            return;
        }
        tableModel.setRowCount(0);
        ArrayList<Report> reports = reportManager.getReportsByReporter(resident.getUserId());
        for (int i = 0; i < reports.size(); i++) {
            Report report = reports.get(i);
            // getReportType() and getSpecificDetail() are polymorphic calls:
            // the object's real subclass decides what text is returned.
            Object[] row = {
                    report.getReportId(),
                    report.getReportType(),
                    report.getLocation(),
                    report.getDescription(),
                    report.getSpecificDetail(),
                    report.getStatus().getLabel(),
                    report.getDateSubmitted()
            };
            tableModel.addRow(row);
        }
    }

    // Closes this window and reopens the login window.
    private void goToLogin() {
        this.dispose();
        LoginFrame loginFrame = new LoginFrame(userManager, reportManager, locationManager);
        loginFrame.setVisible(true);
    }
}
