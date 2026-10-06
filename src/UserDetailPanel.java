import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

/**
 * UserDetailPanel.java
 * Shows a single user account in a form-style layout (captions and values)
 * instead of as a row inside a table. Used by the administrator dashboard.
 */
public class UserDetailPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JLabel headerLabel;
    private JPanel badgeHolder;
    private JPanel body;
    private JScrollPane bodyScroll;

    public UserDetailPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.CARD);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(20, 22, 20, 22)));

        headerLabel = new JLabel("No user selected");
        headerLabel.setFont(UITheme.FONT_TITLE);
        headerLabel.setForeground(UITheme.PRIMARY);
        headerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        badgeHolder = new JPanel();
        badgeHolder.setOpaque(false);
        badgeHolder.setLayout(new BoxLayout(badgeHolder, BoxLayout.X_AXIS));
        badgeHolder.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        header.add(headerLabel);
        header.add(Box.createVerticalStrut(8));
        header.add(badgeHolder);
        add(header, BorderLayout.NORTH);

        body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        bodyScroll = new JScrollPane(body);
        bodyScroll.setBorder(null);
        bodyScroll.setOpaque(false);
        bodyScroll.getViewport().setOpaque(false);
        bodyScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        bodyScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(bodyScroll, BorderLayout.CENTER);

        clear();
    }

    public void clear() {
        headerLabel.setText("No user selected");
        badgeHolder.removeAll();
        badgeHolder.revalidate();
        badgeHolder.repaint();

        body.removeAll();
        JLabel hint = new JLabel("Select a user from the list to view the account details.");
        hint.setFont(UITheme.FONT_BASE);
        hint.setForeground(UITheme.MUTED);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(hint);
        body.revalidate();
        body.repaint();
    }

    public void setUser(User user) {
        if (user == null) {
            clear();
            return;
        }

        headerLabel.setText(user.getFullName());

        badgeHolder.removeAll();
        Color typeColor = (user instanceof Administrator) ? UITheme.PRIMARY : UITheme.ACCENT;
        JLabel badge = UITheme.pill(user.getUserType(), typeColor);
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);
        badgeHolder.add(badge);
        badgeHolder.revalidate();
        badgeHolder.repaint();

        body.removeAll();

        body.add(UITheme.sectionLabel("Account Information"));
        body.add(Box.createVerticalStrut(12));
        JPanel infoGrid = new GridBagLayoutPanel();
        addField(infoGrid, 0, "User ID", user.getUserId());
        addField(infoGrid, 1, "Account Type", user.getUserType());
        addField(infoGrid, 2, "Username", user.getUsername());
        addField(infoGrid, 3, "Full Name", user.getFullName());
        body.add(infoGrid);

        if (user instanceof Resident) {
            Resident resident = (Resident) user;
            body.add(Box.createVerticalStrut(22));
            body.add(UITheme.sectionLabel("Resident Details"));
            body.add(Box.createVerticalStrut(12));
            JPanel residentGrid = new GridBagLayoutPanel();
            addField(residentGrid, 0, "Address", resident.getAddress());
            addField(residentGrid, 1, "Contact Number", resident.getContactNumber());
            body.add(residentGrid);
        }

        body.revalidate();
        body.repaint();
        bodyScroll.getVerticalScrollBar().setValue(0);
    }

    private void addField(JPanel grid, int index, String caption, String value) {
        int column = index % 2;
        int row = index / 2;

        JPanel block = new JPanel();
        block.setOpaque(false);
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.add(UITheme.fieldCaption(caption));
        block.add(Box.createVerticalStrut(2));
        block.add(UITheme.fieldValue(value));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = column;
        constraints.gridy = row;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.insets = new Insets(0, 0, 16, 24);
        grid.add(block, constraints);
    }

    /** A JPanel that already uses a GridBagLayout and left alignment. */
    private static final class GridBagLayoutPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        GridBagLayoutPanel() {
            super(new GridBagLayout());
            setOpaque(false);
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }
    }
}

