import java.awt.Component;
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
 * RegistrationFrame.java
 * The window where a new resident creates a WatchPoint account.
 */
public class RegistrationFrame extends JFrame {

    private JTextField fullNameField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField addressField;
    private JTextField contactNumberField;
    private UserManager userManager;

    public RegistrationFrame(UserManager userManager) {
        this.userManager = userManager;

        setTitle("WatchPoint - Resident Registration");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(720, 560));
        setSize(880, 680);
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

        JLabel title = new JLabel("Create your account");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.PRIMARY);
        c.gridy = 0;
        c.insets = new Insets(0, 0, 4, 0);
        card.add(title, c);

        JLabel subtitle = new JLabel("Register as a resident of WatchPoint");
        subtitle.setFont(UITheme.FONT_SUBTITLE);
        subtitle.setForeground(UITheme.MUTED);
        c.gridy = 1;
        c.insets = new Insets(0, 0, 20, 0);
        card.add(subtitle, c);

        fullNameField = new JTextField(20);
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        addressField = new JTextField(20);
        contactNumberField = new JTextField(20);
        UITheme.styleField(fullNameField);
        UITheme.styleField(usernameField);
        UITheme.styleField(passwordField);
        UITheme.styleField(addressField);
        UITheme.styleField(contactNumberField);

        int row = 2;
        row = addField(card, row, "Full Name", fullNameField);
        row = addField(card, row, "Username", usernameField);
        row = addField(card, row, "Password", passwordField);
        row = addField(card, row, "Address", addressField);
        row = addField(card, row, "Contact Number", contactNumberField);

        JButton registerButton = UITheme.primaryButton("Register");
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                register();
            }
        });
        c.gridy = row;
        c.insets = new Insets(10, 0, 0, 0);
        card.add(registerButton, c);

        Dimension preferred = card.getPreferredSize();
        card.setPreferredSize(new Dimension(420, preferred.height));
        return card;
    }

    private int addField(JPanel card, int row, String caption, Component field) {
        GridBagConstraints captionConstraints = new GridBagConstraints();
        captionConstraints.gridx = 0;
        captionConstraints.gridy = row;
        captionConstraints.weightx = 1.0;
        captionConstraints.fill = GridBagConstraints.HORIZONTAL;
        captionConstraints.anchor = GridBagConstraints.WEST;
        captionConstraints.insets = new Insets(0, 0, 4, 0);
        card.add(UITheme.fieldCaption(caption), captionConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 0;
        fieldConstraints.gridy = row + 1;
        fieldConstraints.weightx = 1.0;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.anchor = GridBagConstraints.WEST;
        fieldConstraints.insets = new Insets(0, 0, 14, 0);
        card.add(field, fieldConstraints);

        return row + 2;
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

