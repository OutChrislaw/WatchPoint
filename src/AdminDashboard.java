import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * AdminDashboard.java
 * The window an Administrator sees after logging in. It allows the admin to
 * manage all hazard reports and all user accounts of WatchPoint.
 */
public class AdminDashboard extends JFrame {

    private Administrator currentUser;
    private ReportManager reportManager;
    private UserManager userManager;

    private JTable reportTable;
    private DefaultTableModel reportTableModel;
    private JComboBox<String> statusBox;
    private JTable userTable;
    private DefaultTableModel userTableModel;

    public AdminDashboard(Administrator currentUser, ReportManager reportManager, UserManager userManager) {
        this.currentUser = currentUser;
        this.reportManager = reportManager;
        this.userManager = userManager;

        setTitle("WatchPoint - Administrator: " + currentUser.getFullName());
        setSize(980, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        reportTableModel = new DefaultTableModel();
        reportTableModel.addColumn("Report ID");
        reportTableModel.addColumn("Type");
        reportTableModel.addColumn("Reporter ID");
        reportTableModel.addColumn("Location");
        reportTableModel.addColumn("Description");
        reportTableModel.addColumn("Detail");
        reportTableModel.addColumn("Status");
        reportTableModel.addColumn("Date Submitted");

        reportTable = new JTable(reportTableModel);

        statusBox = new JComboBox<String>();
        statusBox.addItem("PENDING");
        statusBox.addItem("VERIFIED");
        statusBox.addItem("IN_PROGRESS");
        statusBox.addItem("RESOLVED");

        JButton updateStatusButton = new JButton("Update Status");
        updateStatusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateReportStatus();
            }
        });

        JButton deleteInvalidReportButton = new JButton("Delete Invalid Report");
        deleteInvalidReportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteInvalidReport();
            }
        });

        JPanel reportButtonPanel = new JPanel(new FlowLayout());
        reportButtonPanel.add(new JLabel("New Status:"));
        reportButtonPanel.add(statusBox);
        reportButtonPanel.add(updateStatusButton);
        reportButtonPanel.add(deleteInvalidReportButton);

        JPanel reportsTab = new JPanel(new BorderLayout());
        reportsTab.add(new JScrollPane(reportTable), BorderLayout.CENTER);
        reportsTab.add(reportButtonPanel, BorderLayout.SOUTH);

        userTableModel = new DefaultTableModel();
        userTableModel.addColumn("User ID");
        userTableModel.addColumn("Type");
        userTableModel.addColumn("Username");
        userTableModel.addColumn("Full Name");

        userTable = new JTable(userTableModel);

        JButton createUserButton = new JButton("Create User");
        createUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createUser();
            }
        });

        JButton updateUserButton = new JButton("Update User");
        updateUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateUser();
            }
        });

        JButton deleteUserButton = new JButton("Delete User");
        deleteUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteUser();
            }
        });

        JPanel userButtonPanel = new JPanel(new FlowLayout());
        userButtonPanel.add(createUserButton);
        userButtonPanel.add(updateUserButton);
        userButtonPanel.add(deleteUserButton);

        JPanel usersTab = new JPanel(new BorderLayout());
        usersTab.add(new JScrollPane(userTable), BorderLayout.CENTER);
        usersTab.add(userButtonPanel, BorderLayout.SOUTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Reports", reportsTab);
        tabs.addTab("Users", usersTab);
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

        showAllReports();
        manageUsers();
    }

    public void showAllReports() {
        reportTableModel.setRowCount(0);

        List<Report> reports = reportManager.getAllReports();
        for (int i = 0; i < reports.size(); i++) {
            Report report = reports.get(i);
            Object[] row = new Object[8];
            row[0] = report.getReportId();
            row[1] = report.getReportType();
            row[2] = report.getReporterId();
            row[3] = report.getLocation().getFullLocation();
            row[4] = report.getDescription();
            row[5] = report.getSpecificDetail();
            row[6] = report.getStatus().name();
            row[7] = report.getDateSubmitted();
            reportTableModel.addRow(row);
        }
    }

    public void updateReportStatus() {
        int selectedRow = reportTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a report first.");
            return;
        }

        String reportId = (String) reportTableModel.getValueAt(selectedRow, 0);
        String statusText = (String) statusBox.getSelectedItem();

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

        if (reportManager.updateReportStatus(reportId, status)) {
            JOptionPane.showMessageDialog(this,
                    "Report " + reportId + " is now " + status.name() + ".");
            showAllReports();
        } else {
            JOptionPane.showMessageDialog(this, "The report status could not be updated.");
        }
    }

    public void deleteInvalidReport() {
        int selectedRow = reportTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a report first.");
            return;
        }

        String reportId = (String) reportTableModel.getValueAt(selectedRow, 0);
        int answer = JOptionPane.showConfirmDialog(this,
                "Delete report " + reportId + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        if (reportManager.deleteInvalidReport(reportId)) {
            JOptionPane.showMessageDialog(this, "Report deleted.");
            showAllReports();
        } else {
            JOptionPane.showMessageDialog(this, "The report could not be deleted.");
        }
    }

    public void manageUsers() {
        userTableModel.setRowCount(0);

        List<User> users = userManager.getAllUsers();
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            Object[] row = new Object[4];
            row[0] = user.getUserId();
            row[1] = user.getUserType();
            row[2] = user.getUsername();
            row[3] = user.getFullName();
            userTableModel.addRow(row);
        }
    }

    public void createUser() {
        String[] types = {"Resident", "Administrator"};
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
            user = new Resident(userId, username.trim(), password, fullName.trim(),
                    address, contactNumber);
        } else {
            user = new Administrator(userId, username.trim(), password, fullName.trim());
        }

        if (userManager.addUser(user)) {
            JOptionPane.showMessageDialog(this, "User created with ID " + userId + ".");
            manageUsers();
        } else {
            JOptionPane.showMessageDialog(this, "That username is already taken.");
        }
    }

    public void updateUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user first.");
            return;
        }

        String userId = (String) userTableModel.getValueAt(selectedRow, 0);
        User existing = userManager.getUserById(userId);
        if (existing == null) {
            JOptionPane.showMessageDialog(this, "User not found.");
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
            updatedUser = new Resident(userId, existing.getUsername(), password,
                    fullName.trim(), address, contactNumber);
        } else {
            updatedUser = new Administrator(userId, existing.getUsername(), password, fullName.trim());
        }

        if (userManager.updateUser(updatedUser)) {
            JOptionPane.showMessageDialog(this, "User updated.");
            manageUsers();
        } else {
            JOptionPane.showMessageDialog(this, "The user could not be updated.");
        }
    }

    public void deleteUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user first.");
            return;
        }

        String userId = (String) userTableModel.getValueAt(selectedRow, 0);
        int answer = JOptionPane.showConfirmDialog(this,
                "Delete user " + userId + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        if (userManager.deleteUser(userId)) {
            JOptionPane.showMessageDialog(this, "User deleted.");
            manageUsers();
        } else {
            JOptionPane.showMessageDialog(this, "The user could not be deleted.");
        }
    }

    public void logout() {
        LoginFrame loginFrame = new LoginFrame(userManager, reportManager);
        loginFrame.setVisible(true);
        this.dispose();
    }
}
