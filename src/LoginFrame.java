// LoginFrame.java
// The first window the user sees. It has two tabs: Log In and Register.

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {

    private UserManager userManager;
    private ReportManager reportManager;
    private LocationManager locationManager;

    // Login fields.
    private JTextField loginUsernameField;
    private JPasswordField loginPasswordField;

    // Register fields.
    private JTextField regFullNameField;
    private JTextField regUsernameField;
    private JPasswordField regPasswordField;
    private JTextField regAddressField;
    private JTextField regContactField;

    public LoginFrame(UserManager userManager, ReportManager reportManager,
                      LocationManager locationManager) {
        this.userManager = userManager;
        this.reportManager = reportManager;
        this.locationManager = locationManager;

        setTitle("WatchPoint - Login");
        setSize(420, 340);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("WatchPoint - Community Safety Reporting", JLabel.CENTER);
        title.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8));
        add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Log In", buildLoginPanel());
        tabs.addTab("Register", buildRegisterPanel());
        add(tabs, BorderLayout.CENTER);
    }

    // Builds the Log In tab.
    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        loginUsernameField = new JTextField();
        loginPasswordField = new JPasswordField();
        JButton loginButton = new JButton("Log In");

        panel.add(new JLabel("Username:"));
        panel.add(loginUsernameField);
        panel.add(new JLabel("Password:"));
        panel.add(loginPasswordField);
        panel.add(new JLabel(""));
        panel.add(loginButton);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doLogin();
            }
        });

        return panel;
    }

    // Builds the Register tab.
    private JPanel buildRegisterPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        regFullNameField = new JTextField();
        regUsernameField = new JTextField();
        regPasswordField = new JPasswordField();
        regAddressField = new JTextField();
        regContactField = new JTextField();
        JButton registerButton = new JButton("Register");

        panel.add(new JLabel("Full Name:"));
        panel.add(regFullNameField);
        panel.add(new JLabel("Username:"));
        panel.add(regUsernameField);
        panel.add(new JLabel("Password:"));
        panel.add(regPasswordField);
        panel.add(new JLabel("Address:"));
        panel.add(regAddressField);
        panel.add(new JLabel("Contact Number:"));
        panel.add(regContactField);
        panel.add(new JLabel(""));
        panel.add(registerButton);

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doRegister();
            }
        });

        return panel;
    }

    // Handles the Log In button.
    private void doLogin() {
        String username = loginUsernameField.getText().trim();
        String password = new String(loginPasswordField.getPassword());

        User user = userManager.login(username, password);

        if (user == null) {
            JOptionPane.showMessageDialog(this, "Invalid username or password.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Close the login window FIRST so the dashboard is not hidden behind it.
        this.dispose();

        // Decide which window to open based on the user type.
        // getUserType() is polymorphic: it returns a different value per subclass.
        JFrame dashboard;
        if (user.getUserType().equals("Administrator")) {
            dashboard = new AdminFrame((Administrator) user, userManager,
                    reportManager, locationManager);
        } else {
            dashboard = new ResidentFrame((Resident) user, userManager,
                    reportManager, locationManager);
        }

        // Show the dashboard and bring it to the front.
        dashboard.setVisible(true);
        dashboard.toFront();
        dashboard.requestFocus();

        JOptionPane.showMessageDialog(dashboard, "Welcome, " + user.getFullName() + "!");
    }

    // Handles the Register button.
    private void doRegister() {
        String fullName = regFullNameField.getText().trim();
        String username = regUsernameField.getText().trim();
        String password = new String(regPasswordField.getPassword());
        String address = regAddressField.getText().trim();
        String contact = regContactField.getText().trim();

        boolean success = userManager.registerResident(fullName, username, password,
                address, contact);

        if (success) {
            JOptionPane.showMessageDialog(this, "Account created! You may now log in.",
                    "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
            // Clear the fields.
            regFullNameField.setText("");
            regUsernameField.setText("");
            regPasswordField.setText("");
            regAddressField.setText("");
            regContactField.setText("");
        } else {
            JOptionPane.showMessageDialog(this,
                    "Registration failed. Please fill in the required fields, "
                            + "or choose a different username.",
                    "Registration Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
