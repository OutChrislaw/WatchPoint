// AdminFrame.java
// The administrator dashboard. An administrator can view all reports,
// update a report's status (verify, in progress, resolved), and delete
// invalid reports.

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class AdminFrame extends JFrame {

    private Administrator administrator;
    private UserManager userManager;
    private ReportManager reportManager;
    private LocationManager locationManager;

    private DefaultTableModel tableModel;
    private JTable table;
    private JComboBox<String> statusBox;

    public AdminFrame(Administrator administrator, UserManager userManager,
                      ReportManager reportManager, LocationManager locationManager) {
        this.administrator = administrator;
        this.userManager = userManager;
        this.reportManager = reportManager;
        this.locationManager = locationManager;

        setTitle("WatchPoint - Administrator: " + administrator.getFullName());
        setSize(900, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildControlPanel(), BorderLayout.SOUTH);

        refreshTable();
    }

    // The table that shows all reports.
    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));

        String[] columns = {"Report ID", "Type", "Reporter", "Location", "Description",
                "Specific Detail", "Status", "Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    // The bottom controls for updating status, deleting and refreshing.
    private JPanel buildControlPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1));

        JPanel statusPanel = new JPanel();
        statusPanel.add(new JLabel("Set Status To:"));
        statusBox = new JComboBox<String>();
        statusBox.addItem("PENDING");
        statusBox.addItem("VERIFIED");
        statusBox.addItem("IN_PROGRESS");
        statusBox.addItem("RESOLVED");

        JButton updateButton = new JButton("Update Status");
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doUpdateStatus();
            }
        });

        statusPanel.add(statusBox);
        statusPanel.add(updateButton);

        JPanel buttonPanel = new JPanel();
        JButton deleteButton = new JButton("Delete Invalid Report");
        JButton refreshButton = new JButton("Refresh");
        JButton logoutButton = new JButton("Log Out");

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doDelete();
            }
        });
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshTable();
            }
        });
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goToLogin();
            }
        });

        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(logoutButton);

        panel.add(statusPanel);
        panel.add(buttonPanel);
        return panel;
    }

    // Reloads the table with all reports.
    private void refreshTable() {
        tableModel.setRowCount(0);
        ArrayList<Report> reports = reportManager.getAllReports();
        for (int i = 0; i < reports.size(); i++) {
            Report report = reports.get(i);

            // Find the reporter's name using the reporter ID.
            String reporterName = report.getReporterId();
            User reporter = userManager.getUserById(report.getReporterId());
            if (reporter != null) {
                reporterName = reporter.getFullName();
            }

            Object[] row = {
                    report.getReportId(),
                    report.getReportType(),
                    reporterName,
                    report.getLocation(),
                    report.getDescription(),
                    report.getSpecificDetail(),
                    report.getStatus().getLabel(),
                    report.getDateSubmitted()
            };
            tableModel.addRow(row);
        }
    }

    // Updates the status of the selected report.
    private void doUpdateStatus() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a report first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String reportId = (String) tableModel.getValueAt(row, 0);
        String statusText = (String) statusBox.getSelectedItem();
        ReportStatus status = ReportStatus.fromString(statusText);

        boolean success = reportManager.updateReportStatus(reportId, status);
        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Report " + reportId + " is now " + status.getLabel() + ".");
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "Could not update the report.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Deletes the selected report.
    private void doDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a report first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String reportId = (String) tableModel.getValueAt(row, 0);
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete report " + reportId + "?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            boolean success = reportManager.deleteReport(reportId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Report deleted.");
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "Could not delete the report.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Closes this window and reopens the login window.
    private void goToLogin() {
        this.dispose();
        LoginFrame loginFrame = new LoginFrame(userManager, reportManager, locationManager);
        loginFrame.setVisible(true);
    }
}
