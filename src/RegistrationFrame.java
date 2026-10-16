import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
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
    private JComboBox<String> cityMunicipalityBox;
    private JComboBox<String> barangayBox;
    private JTextField contactNumberField;
    private UserManager userManager;
    private LocationManager locationManager;

    public RegistrationFrame(UserManager userManager, LocationManager locationManager) {
        this.userManager = userManager;
        this.locationManager = locationManager;

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
        contactNumberField = new JTextField(20);
        UITheme.styleField(fullNameField);
        UITheme.styleField(usernameField);
        UITheme.styleField(passwordField);
        UITheme.styleField(contactNumberField);

        cityMunicipalityBox = new JComboBox<String>();
        cityMunicipalityBox.setFont(UITheme.FONT_BASE);
        List<String> municipalities = locationManager.getMunicipalities();
        for (int i = 0; i < municipalities.size(); i++) {
            cityMunicipalityBox.addItem(municipalities.get(i));
        }

        barangayBox = new JComboBox<String>();
        barangayBox.setFont(UITheme.FONT_BASE);

        cityMunicipalityBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateBarangayBox();
            }
        });
        updateBarangayBox();

        int row = 2;
        row = addField(card, row, "Full Name", fullNameField);
        row = addField(card, row, "Username", usernameField);
        row = addField(card, row, "Password", passwordField);
        row = addField(card, row, "City / Municipality", cityMunicipalityBox);
        row = addField(card, row, "Barangay", barangayBox);
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

    private void updateBarangayBox() {
        barangayBox.removeAllItems();
        if (cityMunicipalityBox.getSelectedItem() == null) {
            return;
        }
        String municipality = cityMunicipalityBox.getSelectedItem().toString();
        List<String> barangays = locationManager.getBarangays(municipality);
        for (int i = 0; i < barangays.size(); i++) {
            barangayBox.addItem(barangays.get(i));
        }
    }

    public void register() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String contactNumber = contactNumberField.getText().trim();

        String cityMunicipality = "";
        if (cityMunicipalityBox.getSelectedItem() != null) {
            cityMunicipality = cityMunicipalityBox.getSelectedItem().toString();
        }
        String barangay = "";
        if (barangayBox.getSelectedItem() != null) {
            barangay = barangayBox.getSelectedItem().toString();
        }

        if (cityMunicipality.length() == 0 || barangay.length() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a City / Municipality and a Barangay.");
            return;
        }

        String address = barangay + ", " + cityMunicipality;

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
        if (cityMunicipalityBox.getItemCount() > 0) {
            cityMunicipalityBox.setSelectedIndex(0);
        }
        contactNumberField.setText("");
    }
}

