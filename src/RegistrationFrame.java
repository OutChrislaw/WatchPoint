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
 * RegistrationFrame.java
 * The window where a new resident creates a WatchPoint account.
 */
public class RegistrationFrame extends JFrame {

    private JTextField fullNameField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField addressField;
    private JTextField contactNumberField;
    private JButton registerButton;
    private UserManager userManager;

    public RegistrationFrame(UserManager userManager) {
        this.userManager = userManager;

        setTitle("WatchPoint - Resident Registration");
        setSize(420, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        fullNameField = new JTextField();
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        addressField = new JTextField();
        contactNumberField = new JTextField();
        registerButton = new JButton("Register");

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        form.add(new JLabel("Full Name:"));
        form.add(fullNameField);
        form.add(new JLabel("Username:"));
        form.add(usernameField);
        form.add(new JLabel("Password:"));
        form.add(passwordField);
        form.add(new JLabel("Address:"));
        form.add(addressField);
        form.add(new JLabel("Contact Number:"));
        form.add(contactNumberField);
        form.add(new JLabel(""));
        form.add(registerButton);
        add(form, BorderLayout.CENTER);

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                register();
            }
        });
    }

    public void register() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String address = addressField.getText().trim();
        String contactNumber = contactNumberField.getText().trim();

        if (userManager.registerResident(fullName, username, password, address, contactNumber)) {
            JOptionPane.showMessageDialog(this, "Account created. You may now log in.");
            clearFields();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Registration failed. Please fill in the required fields or use another username.");
        }
    }

    public void clearFields() {
        fullNameField.setText("");
        usernameField.setText("");
        passwordField.setText("");
        addressField.setText("");
        contactNumberField.setText("");
    }
}
