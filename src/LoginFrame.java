import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * LoginFrame.java
 * The first window of WatchPoint. It checks the username and password
 * of a user and opens the correct dashboard.
 */
public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private UserManager userManager;
    private ReportManager reportManager;

    public LoginFrame(UserManager userManager, ReportManager reportManager) {
        this.userManager = userManager;
        this.reportManager = reportManager;

        setTitle("WatchPoint - Log In");
        setSize(380, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        usernameField = new JTextField();
        passwordField = new JPasswordField();
        loginButton = new JButton("Log In");
        JButton registerButton = new JButton("Register as Resident");

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        form.add(new JLabel("Username:"));
        form.add(usernameField);
        form.add(new JLabel("Password:"));
        form.add(passwordField);
        form.add(loginButton);
        form.add(registerButton);
        add(form, BorderLayout.CENTER);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openRegistration();
            }
        });
    }

    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        User user = userManager.login(username, password);

        if (user == null) {
            showMessage("Invalid username or password.");
            return;
        }

        if (user.getUserType().equals("Administrator")) {
            AdminDashboard dashboard = new AdminDashboard((Administrator) user, reportManager, userManager);
            dashboard.setVisible(true);
        } else {
            ResidentDashboard dashboard = new ResidentDashboard((Resident) user, reportManager, userManager);
            dashboard.setVisible(true);
        }

        this.dispose();
    }

    public void openRegistration() {
        RegistrationFrame registrationFrame = new RegistrationFrame(userManager);
        registrationFrame.setVisible(true);
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}
