import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

/**
 * AdminDashboard.java
 * The window an Administrator sees after logging in. Reports and users are
 * shown as a list on the left and a form-style detail view on the right.
 */
public class AdminDashboard extends JFrame {

    private Administrator currentUser;
    private ReportManager reportManager;
    private UserManager userManager;
    private LocationManager locationManager;

    private JTable reportTable;
    private DefaultTableModel reportTableModel;
    private ReportDetailPanel reportDetailPanel;
    private List<Report> currentReports;

    private JComboBox<String> statusBox;

    private JTable userTable;
    private DefaultTableModel userTableModel;
    private UserDetailPanel userDetailPanel;
    private List<User> currentUsers;

    public AdminDashboard(Administrator currentUser, ReportManager reportManager, UserManager userManager,
                          LocationManager locationManager) {
        this.currentUser = currentUser;
        this.reportManager = reportManager;
        this.userManager = userManager;
        this.locationManager = locationManager;

        setTitle("WatchPoint - Administrator: " + currentUser.getFullName());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(960, 620));
        setSize(1180, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BACKGROUND);

        add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BOLD);
        tabs.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        tabs.addTab("  Reports  ", buildReportsTab());
        tabs.addTab("  Users  ", buildUsersTab());
        add(tabs, BorderLayout.CENTER);

        showAllReports();
        manageUsers();
    }

    private JPanel buildHeader() {
        JPanel header = UITheme.headerBar("WatchPoint",
                "Signed in as " + currentUser.getFullName() + "   -   Administrator");

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

    private JPanel buildReportsTab() {
        reportTableModel = new DefaultTableModel();
        reportTableModel.addColumn("Report ID");
        reportTableModel.addColumn("Type");
        reportTableModel.addColumn("Reporter ID");
        reportTableModel.addColumn("Status");
        reportTableModel.addColumn("Date Submitted");

        reportTable = UITheme.styledTable(reportTableModel);
        reportTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        reportTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        reportTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        reportTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        reportTable.getColumnModel().getColumn(4).setPreferredWidth(140);
        reportTable.getColumnModel().getColumn(3).setCellRenderer(UITheme.statusCellRenderer());

        reportTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    showSelectedReport();
                }
            }
        });

        reportDetailPanel = new ReportDetailPanel();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                UITheme.scroll(reportTable), reportDetailPanel);
        split.setBorder(null);
        split.setOpaque(false);
        split.setResizeWeight(0.38);
        split.setDividerLocation(400);
        split.setDividerSize(8);
        split.setContinuousLayout(true);

        JPanel tab = new JPanel(new BorderLayout(0, 12));
        tab.setBackground(UITheme.BACKGROUND);
        tab.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        tab.add(split, BorderLayout.CENTER);
        tab.add(buildReportControls(), BorderLayout.SOUTH);
        return tab;
    }

    private JPanel buildReportControls() {
        statusBox = new JComboBox<String>();
        statusBox.addItem("PENDING");
        statusBox.addItem("VERIFIED");
        statusBox.addItem("IN_PROGRESS");
        statusBox.addItem("RESOLVED");
        statusBox.setFont(UITheme.FONT_BASE);

        JLabel caption = new JLabel("Set status to:");
        caption.setFont(UITheme.FONT_BASE);
        caption.setForeground(UITheme.TEXT);

        JButton updateStatusButton = UITheme.primaryButton("Update Status");
        updateStatusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateReportStatus();
            }
        });

        JButton deleteButton = UITheme.dangerButton("Delete Invalid Report");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteInvalidReport();
            }
        });

        JLabel hint = new JLabel("Select a report on the left to review it.");
        hint.setFont(UITheme.FONT_SMALL);
        hint.setForeground(UITheme.MUTED);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);
        controls.add(caption);
        controls.add(statusBox);
        controls.add(updateStatusButton);
        controls.add(deleteButton);

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(hint, BorderLayout.WEST);
        row.add(controls, BorderLayout.EAST);
        return row;
    }

    private void showSelectedReport() {
        Report report = selectedReport();
        if (report == null) {
            reportDetailPanel.clear();
        } else {
            reportDetailPanel.setReport(report);
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

    private JPanel buildUsersTab() {
        userTableModel = new DefaultTableModel();
        userTableModel.addColumn("User ID");
        userTableModel.addColumn("Type");
        userTableModel.addColumn("Username");
        userTableModel.addColumn("Full Name");

        userTable = UITheme.styledTable(userTableModel);
        userTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        userTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        userTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        userTable.getColumnModel().getColumn(3).setPreferredWidth(180);

        userTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    showSelectedUser();
                }
            }
        });

        userDetailPanel = new UserDetailPanel();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                UITheme.scroll(userTable), userDetailPanel);
        split.setBorder(null);
        split.setOpaque(false);
        split.setResizeWeight(0.38);
        split.setDividerLocation(400);
        split.setDividerSize(8);
        split.setContinuousLayout(true);

        JPanel tab = new JPanel(new BorderLayout(0, 12));
        tab.setBackground(UITheme.BACKGROUND);
        tab.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        tab.add(split, BorderLayout.CENTER);
        tab.add(buildUserControls(), BorderLayout.SOUTH);
        return tab;
    }

    private JPanel buildUserControls() {
        JButton createUserButton = UITheme.primaryButton("Create User");
        createUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createUser();
            }
        });

        JButton updateUserButton = UITheme.secondaryButton("Update User");
        updateUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateUser();
            }
        });

        JButton deleteUserButton = UITheme.dangerButton("Delete User");
        deleteUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteUser();
            }
        });

        JLabel hint = new JLabel("Select a user on the left to view the account.");
        hint.setFont(UITheme.FONT_SMALL);
        hint.setForeground(UITheme.MUTED);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);
        controls.add(createUserButton);
        controls.add(updateUserButton);
        controls.add(deleteUserButton);

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(hint, BorderLayout.WEST);
        row.add(controls, BorderLayout.EAST);
        return row;
    }

    private void showSelectedUser() {
        User user = selectedUser();
        if (user == null) {
            userDetailPanel.clear();
        } else {
            userDetailPanel.setUser(user);
        }
    }

    private User selectedUser() {
        int viewRow = userTable.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = userTable.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= currentUsers.size()) {
            return null;
        }
        return currentUsers.get(modelRow);
    }

    public void showAllReports() {
        reportTableModel.setRowCount(0);
        currentReports = reportManager.getAllReports();

        for (int i = 0; i < currentReports.size(); i++) {
            Report report = currentReports.get(i);
            Object[] row = new Object[5];
            row[0] = report.getReportId();
            row[1] = report.getReportType();
            row[2] = report.getReporterId();
            row[3] = report.getStatus().name();
            row[4] = report.getDateSubmitted();
            reportTableModel.addRow(row);
        }

        if (currentReports.size() > 0) {
            reportTable.setRowSelectionInterval(0, 0);
        } else {
            reportTable.clearSelection();
            reportDetailPanel.clear();
        }
    }

    public void updateReportStatus() {
        Report report = selectedReport();
        if (report == null) {
            JOptionPane.showMessageDialog(this, "Please select a report first.");
            return;
        }

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

        if (reportManager.updateReportStatus(report.getReportId(), status)) {
            JOptionPane.showMessageDialog(this,
                    "Report " + report.getReportId() + " is now " + status.name() + ".");
            showAllReports();
        } else {
            JOptionPane.showMessageDialog(this, "The report status could not be updated.");
        }
    }

    public void deleteInvalidReport() {
        Report report = selectedReport();
        if (report == null) {
            JOptionPane.showMessageDialog(this, "Please select a report first.");
            return;
        }

        int answer = JOptionPane.showConfirmDialog(this,
                "Delete report " + report.getReportId() + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        if (reportManager.deleteInvalidReport(report.getReportId())) {
            JOptionPane.showMessageDialog(this, "Report deleted.");
            showAllReports();
        } else {
            JOptionPane.showMessageDialog(this, "The report could not be deleted.");
        }
    }

    public void manageUsers() {
        userTableModel.setRowCount(0);
        currentUsers = userManager.getAllUsers();

        for (int i = 0; i < currentUsers.size(); i++) {
            User user = currentUsers.get(i);
            Object[] row = new Object[4];
            row[0] = user.getUserId();
            row[1] = user.getUserType();
            row[2] = user.getUsername();
            row[3] = user.getFullName();
            userTableModel.addRow(row);
        }

        if (currentUsers.size() > 0) {
            userTable.setRowSelectionInterval(0, 0);
        } else {
            userTable.clearSelection();
            userDetailPanel.clear();
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
        User existing = selectedUser();
        if (existing == null) {
            JOptionPane.showMessageDialog(this, "Please select a user first.");
            return;
        }

        String userId = existing.getUserId();

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
        User existing = selectedUser();
        if (existing == null) {
            JOptionPane.showMessageDialog(this, "Please select a user first.");
            return;
        }

        String userId = existing.getUserId();
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
        LoginFrame loginFrame = new LoginFrame(userManager, reportManager, locationManager);
        loginFrame.setVisible(true);
        this.dispose();
    }
}




