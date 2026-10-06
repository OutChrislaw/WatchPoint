import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * UITheme.java
 * Central place for the colours, fonts, and reusable widgets of WatchPoint.
 * Keeping them here makes every window of the system look consistent and
 * lets the interface reflow nicely when a window is maximised.
 */
public final class UITheme {

    // ---- Colours ----
    public static final Color PRIMARY = new Color(15, 61, 92);
    public static final Color PRIMARY_DARK = new Color(10, 45, 70);
    public static final Color ACCENT = new Color(30, 136, 229);
    public static final Color ACCENT_DARK = new Color(21, 101, 192);
    public static final Color BACKGROUND = new Color(244, 246, 248);
    public static final Color CARD = Color.WHITE;
    public static final Color FIELD_BG = new Color(247, 249, 251);
    public static final Color TEXT = new Color(33, 43, 54);
    public static final Color MUTED = new Color(122, 132, 142);
    public static final Color BORDER = new Color(222, 226, 230);
    public static final Color HOVER_GRAY = new Color(224, 228, 232);
    public static final Color DANGER = new Color(198, 40, 40);
    public static final Color DANGER_DARK = new Color(155, 27, 27);

    public static final Color STATUS_PENDING = new Color(245, 166, 35);
    public static final Color STATUS_VERIFIED = new Color(30, 136, 229);
    public static final Color STATUS_IN_PROGRESS = new Color(142, 36, 170);
    public static final Color STATUS_RESOLVED = new Color(46, 125, 50);

    // ---- Fonts ----
    private static final String FAMILY = resolveFamily();

    public static final Font FONT_BASE = new Font(FAMILY, Font.PLAIN, 14);
    public static final Font FONT_BOLD = new Font(FAMILY, Font.BOLD, 14);
    public static final Font FONT_SMALL = new Font(FAMILY, Font.PLAIN, 12);
    public static final Font FONT_SMALL_BOLD = new Font(FAMILY, Font.BOLD, 12);
    public static final Font FONT_TITLE = new Font(FAMILY, Font.BOLD, 24);
    public static final Font FONT_SUBTITLE = new Font(FAMILY, Font.PLAIN, 13);
    public static final Font FONT_SECTION = new Font(FAMILY, Font.BOLD, 15);
    public static final Font FONT_VALUE = new Font(FAMILY, Font.PLAIN, 15);

    private UITheme() {
    }

    private static String resolveFamily() {
        Set<String> available = new HashSet<String>();
        try {
            String[] names = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getAvailableFontFamilyNames();
            for (int i = 0; i < names.length; i++) {
                available.add(names[i]);
            }
        } catch (Exception e) {
            // fall back to the default font below
        }
        String[] candidates = { "Arial", "Calibri", "Tahoma", "Verdana" };
        for (int i = 0; i < candidates.length; i++) {
            if (available.contains(candidates[i])) {
                return candidates[i];
            }
        }
        return "SansSerif";
    }

    public static void applyGlobalDefaults() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                // keep the default look and feel
            }
        }

        UIManager.put("Label.font", FONT_BASE);
        UIManager.put("Button.font", FONT_BOLD);
        UIManager.put("TextField.font", FONT_BASE);
        UIManager.put("PasswordField.font", FONT_BASE);
        UIManager.put("TextArea.font", FONT_BASE);
        UIManager.put("ComboBox.font", FONT_BASE);
        UIManager.put("Table.font", FONT_BASE);
        UIManager.put("TableHeader.font", FONT_BOLD);
        UIManager.put("TabbedPane.font", FONT_BOLD);
        UIManager.put("OptionPane.messageFont", FONT_BASE);
        UIManager.put("OptionPane.buttonFont", FONT_BOLD);
        UIManager.put("ToolTip.font", FONT_SMALL);
    }

    // ---- Reusable widgets ----

    /** A coloured top bar with a title and a subtitle. Callers may add an
     *  extra component to BorderLayout.EAST (such as a Log Out button). */
    public static JPanel headerBar(String title, String subtitle) {
        JPanel bar = new JPanel(new BorderLayout(16, 0));
        bar.setBackground(PRIMARY);
        bar.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(FONT_SUBTITLE);
        subtitleLabel.setForeground(new Color(196, 214, 230));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        text.add(titleLabel);
        text.add(Box.createVerticalStrut(2));
        text.add(subtitleLabel);

        bar.add(text, BorderLayout.WEST);
        return bar;
    }

    /** A white "card" panel with a thin border and inner padding. */
    public static JPanel card() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(20, 22, 20, 22)));
        return panel;
    }

    public static JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SECTION);
        label.setForeground(PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    public static JLabel fieldCaption(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SMALL_BOLD);
        label.setForeground(MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    public static JLabel fieldValue(String text) {
        JLabel label = new JLabel(display(text));
        label.setFont(FONT_VALUE);
        label.setForeground(TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    /** Returns "-" when the text is empty, otherwise the original text. */
    public static String display(String text) {
        if (text == null || text.trim().length() == 0) {
            return "-";
        }
        return text;
    }

    /** A bordered container for one piece of information (caption above value). */
    public static JPanel fieldBox(String caption, String value) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(FIELD_BG);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(9, 12, 9, 12)));

        JLabel captionLabel = new JLabel(caption.toUpperCase());
        captionLabel.setFont(FONT_SMALL_BOLD);
        captionLabel.setForeground(MUTED);

        JLabel valueLabel = new JLabel(display(value));
        valueLabel.setFont(FONT_VALUE);
        valueLabel.setForeground(TEXT);

        box.add(captionLabel, BorderLayout.NORTH);
        box.add(valueLabel, BorderLayout.CENTER);
        return box;
    }

    /** A bordered container holding only a value (for a full-width field). */
    public static JPanel valueBox(String value) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(FIELD_BG);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(11, 12, 11, 12)));

        JLabel valueLabel = new JLabel(display(value));
        valueLabel.setFont(FONT_VALUE);
        valueLabel.setForeground(TEXT);
        box.add(valueLabel, BorderLayout.CENTER);
        return box;
    }

    /** A small rounded "pill" label, typically used for a report status. */
    public static JLabel pill(String text, Color fill) {
        return new PillLabel(text, fill);
    }

    public static void styleField(JTextField field) {
        field.setFont(FONT_BASE);
        field.setBackground(CARD);
        field.setForeground(TEXT);
        field.setCaretColor(TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)));
    }

    public static JScrollPane scroll(Component view) {
        JScrollPane pane = new JScrollPane(view);
        pane.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
        pane.setBackground(CARD);
        pane.getViewport().setBackground(CARD);
        pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pane.getVerticalScrollBar().setUnitIncrement(16);
        return pane;
    }

    // ---- Buttons ----

    public static JButton primaryButton(String text) {
        return new FlatButton(text, ACCENT, Color.WHITE, ACCENT_DARK);
    }

    public static JButton secondaryButton(String text) {
        return new FlatButton(text, new Color(233, 236, 239), TEXT, HOVER_GRAY);
    }

    public static JButton dangerButton(String text) {
        return new FlatButton(text, DANGER, Color.WHITE, DANGER_DARK);
    }

    // ---- Tables ----

    public static JTable styledTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        styleTable(table);
        return table;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_BASE);
        table.setForeground(TEXT);
        table.setBackground(CARD);
        table.setRowHeight(32);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(235, 238, 241));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(new Color(210, 230, 250));
        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setDefaultRenderer(Object.class, new CellRenderer());

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(10, 36));
        header.setDefaultRenderer(new HeaderRenderer());
    }

    public static DefaultTableCellRenderer statusCellRenderer() {
        return new StatusCellRenderer();
    }

    // ---- Status colours ----

    public static Color statusColor(ReportStatus status) {
        if (status == ReportStatus.VERIFIED) {
            return STATUS_VERIFIED;
        }
        if (status == ReportStatus.IN_PROGRESS) {
            return STATUS_IN_PROGRESS;
        }
        if (status == ReportStatus.RESOLVED) {
            return STATUS_RESOLVED;
        }
        return STATUS_PENDING;
    }

    public static Color colorForStatusText(String status) {
        if (status == null) {
            return MUTED;
        }
        if (status.equals("VERIFIED")) {
            return STATUS_VERIFIED;
        }
        if (status.equals("IN_PROGRESS")) {
            return STATUS_IN_PROGRESS;
        }
        if (status.equals("RESOLVED")) {
            return STATUS_RESOLVED;
        }
        if (status.equals("PENDING")) {
            return STATUS_PENDING;
        }
        return MUTED;
    }

    // ---- Inner helper widgets ----

    /** A flat, rounded button that paints its own colours so the theme is
     *  respected no matter which look and feel is active. */
    private static final class FlatButton extends JButton {
        private static final long serialVersionUID = 1L;

        private final Color normalColor;
        private final Color hoverColor;
        private boolean hovered = false;

        FlatButton(String text, Color normalColor, Color foreground, Color hoverColor) {
            super(text);
            this.normalColor = normalColor;
            this.hoverColor = hoverColor;
            setFont(FONT_BOLD);
            setForeground(foreground);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setRolloverEnabled(true);
            setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            Color fill;
            if (!isEnabled()) {
                fill = new Color(200, 205, 210);
            } else if (getModel().isPressed()) {
                fill = hoverColor.darker();
            } else if (hovered) {
                fill = hoverColor;
            } else {
                fill = normalColor;
            }

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** A small rounded label, used for report statuses and account types. */
    private static final class PillLabel extends JLabel {
        private static final long serialVersionUID = 1L;

        private final Color fill;

        PillLabel(String text, Color fill) {
            super(text);
            this.fill = fill;
            setFont(FONT_SMALL_BOLD);
            setForeground(Color.WHITE);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(5, 14, 5, 14));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            int arc = getHeight();
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Paints table headers with the primary colour. */
    private static final class HeaderRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);
            label.setOpaque(true);
            label.setBackground(PRIMARY);
            label.setForeground(Color.WHITE);
            label.setFont(FONT_BOLD);
            label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            return label;
        }
    }

    /** Adds left padding to regular table cells. */
    private static final class CellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);
            label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            if (isSelected) {
                label.setForeground(TEXT);
            }
            return label;
        }
    }

    /** Colours the text of a status cell according to the status. */
    private static final class StatusCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);
            label.setFont(FONT_SMALL_BOLD);
            label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            if (isSelected) {
                label.setForeground(TEXT);
            } else if (value != null) {
                label.setForeground(colorForStatusText(value.toString()));
            }
            return label;
        }
    }
}



