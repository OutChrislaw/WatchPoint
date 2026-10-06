import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
    private UserManager userManager;
    private ReportManager reportManager;
    private LocationManager locationManager;

    public LoginFrame(UserManager userManager, ReportManager reportManager, LocationManager locationManager) {
        this.userManager = userManager;
        this.reportManager = reportManager;
        this.locationManager = locationManager;

        setTitle("WatchPoint - Log In");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(720, 520));
        setSize(880, 600);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(UITheme.BACKGROUND);
        setContentPane(root);
        root.add(buildCard());
    }

    private JPanel buildCard() {
        JPanel card = UITheme.card();
        card.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        JLabel title = new JLabel("WatchPoint");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.PRIMARY);
        c.gridy = 0;
        c.insets = new Insets(0, 0, 4, 0);
        card.add(title, c);

        JLabel subtitle = new JLabel("Community Safety Reporting System");
        subtitle.setFont(UITheme.FONT_SUBTITLE);
        subtitle.setForeground(UITheme.MUTED);
        c.gridy = 1;
        c.insets = new Insets(0, 0, 24, 0);
        card.add(subtitle, c);

        JLabel usernameCaption = UITheme.fieldCaption("Username");
        c.gridy = 2;
        c.insets = new Insets(0, 0, 4, 0);
        card.add(usernameCaption, c);

        usernameField = new JTextField(20);
        UITheme.styleField(usernameField);
        c.gridy = 3;
        c.insets = new Insets(0, 0, 16, 0);
        card.add(usernameField, c);

        JLabel passwordCaption = UITheme.fieldCaption("Password");
        c.gridy = 4;
        c.insets = new Insets(0, 0, 4, 0);
        card.add(passwordCaption, c);

        passwordField = new JPasswordField(20);
        UITheme.styleField(passwordField);
        c.gridy = 5;
        c.insets = new Insets(0, 0, 24, 0);
        card.add(passwordField, c);

        JButton loginButton = UITheme.primaryButton("Log In");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });
        c.gridy = 6;
        c.insets = new Insets(0, 0, 10, 0);
        card.add(loginButton, c);

        JButton registerButton = UITheme.secondaryButton("Register as Resident");
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openRegistration();
            }
        });
        c.gridy = 7;
        c.insets = new Insets(0, 0, 0, 0);
        card.add(registerButton, c);

        Dimension preferred = card.getPreferredSize();
        card.setPreferredSize(new Dimension(400, preferred.height));
        return card;
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
            AdminDashboard dashboard = new AdminDashboard((Administrator) user, reportManager, userManager,
                    locationManager);
            dashboard.setVisible(true);
        } else {
            ResidentDashboard dashboard = new ResidentDashboard((Resident) user, reportManager, userManager,
                    locationManager);
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

