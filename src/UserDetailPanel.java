import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.ScrollPaneConstants;

/**
 * UserDetailPanel.java
 * Shows a single user account in a form-style layout. Every piece of
 * information sits inside its own bordered box so the fields line up and
 * the spacing between them is even. Used by the administrator dashboard.
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
                BorderFactory.createEmptyBorder(22, 28, 22, 28)));

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

        body = new BodyPanel();
        body.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));

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
        body.add(Box.createVerticalStrut(10));
        JPanel infoGrid = fieldGrid();
        infoGrid.add(UITheme.fieldBox("User ID", user.getUserId()));
        infoGrid.add(UITheme.fieldBox("Account Type", user.getUserType()));
        infoGrid.add(UITheme.fieldBox("Username", user.getUsername()));
        infoGrid.add(UITheme.fieldBox("Full Name", user.getFullName()));
        body.add(infoGrid);

        if (user instanceof Resident) {
            Resident resident = (Resident) user;
            body.add(Box.createVerticalStrut(20));
            body.add(UITheme.sectionLabel("Resident Details"));
            body.add(Box.createVerticalStrut(10));
            JPanel residentGrid = fieldGrid();
            residentGrid.add(UITheme.fieldBox("Address", resident.getAddress()));
            residentGrid.add(UITheme.fieldBox("Contact Number", resident.getContactNumber()));
            body.add(residentGrid);
        }

        body.revalidate();
        body.repaint();
        bodyScroll.getVerticalScrollBar().setValue(0);
    }

    private JPanel fieldGrid() {
        JPanel grid = new JPanel(new GridLayout(0, 2, 14, 14));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        return grid;
    }

    /** A vertical body that always stretches to the width of the scroll pane. */
    private static final class BodyPanel extends JPanel implements Scrollable {
        private static final long serialVersionUID = 1L;

        BodyPanel() {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}

