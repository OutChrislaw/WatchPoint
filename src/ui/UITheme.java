package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * UITheme.java
 * Colours, fonts and the small widget factories shared by the four GUI classes.
 * Styling only: no data logic, no calculations and no state.
 */
public final class UITheme {

    /** Deep forest green from the Chocolate Hills poster: titles and headings. */
    public static final Color PRIMARY = new Color(0x1D, 0x3F, 0x22);
    public static final Color PRIMARY_DARK = new Color(0x16, 0x32, 0x1A);
    /** Hill green: the primary buttons. */
    public static final Color ACCENT = new Color(0x55, 0x7D, 0x2C);
    public static final Color ACCENT_DARK = new Color(0x44, 0x64, 0x1F);
    /** Bright hill lime: highlights and the Settings button. */
    public static final Color LIME = new Color(0x9C, 0xBF, 0x4A);
    /** Poster sky teal: info accents and the VERIFIED status. */
    public static final Color SKY = new Color(0x2E, 0x9B, 0xA0);
    public static final Color MUTED = new Color(0x5D, 0x6B, 0x52);
    public static final Color TEXT = new Color(0x1F, 0x2E, 0x1B);
    public static final Color BORDER = new Color(0xC6, 0xD2, 0xB8);
    public static final Color CARD = Color.WHITE;
    public static final Color DANGER = new Color(0xC1, 0x50, 0x2B);
    public static final Color PAGE = new Color(0xF3, 0xF5, 0xEA);
    public static final Color ROW_LINE = new Color(0xE2, 0xE8, 0xD6);
    public static final Color SELECTION = new Color(0xDC, 0xE8, 0xC4);
    /** Tabs: the strip is PAGE, a resting tab is TAB_FILL, the open tab is CARD. */
    public static final Color TAB_FILL = new Color(0xE7, 0xED, 0xD9);
    public static final Color TAB_LINE = new Color(0xB7, 0xC6, 0xA5);

    public static final Font FONT_TITLE = new Font("Segoe UI", 1, 24);
    public static final Font FONT_HEADING = new Font("Segoe UI", 1, 18);
    public static final Font FONT_SECTION = new Font("Segoe UI", 1, 15);
    public static final Font FONT_BOLD = new Font("Segoe UI", 1, 12);
    public static final Font FONT_BASE = new Font("Segoe UI", 0, 12);
    public static final Font FONT_SMALL = new Font("Segoe UI", 1, 11);
    public static final Font FONT_VALUE = new Font("Segoe UI", 0, 13);
    public static final Font FONT_TAB = new Font("Segoe UI", 1, 14);

    /** The folder-tab look used by every dashboard. */
    public static void styleTabs(JTabbedPane tabs) {
        tabs.setUI(new FolderTabbedPaneUI());
        tabs.setFont(FONT_TAB);
        tabs.setBackground(PAGE);
        tabs.setForeground(MUTED);
        tabs.setOpaque(true);
        tabs.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    }

    private UITheme() {
    }
    public static JPanel page() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(PAGE);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        return panel;
    }

    public static JPanel card() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(20, 22, 20, 22)));
        return panel;
    }

    public static JLabel label(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    public static JLabel hint(String text) {
        return label(text, FONT_BASE, MUTED);
    }

    public static JButton button(String text, Color background, Color foreground) {
        JButton button = new JButton(text) {
            private static final long serialVersionUID = 1L;

            @Override
            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                return new Dimension(size.width, Math.max(size.height, 32));
            }
        };
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFont(FONT_BOLD);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(shade(background)),
                BorderFactory.createEmptyBorder(5, 14, 5, 14)));
        return button;
    }

    /** A slightly darker version of a colour, used for button edges. */
    public static Color shade(Color color) {
        if (color == null) {
            return BORDER;
        }
        return new Color(Math.max((int) (color.getRed() * 0.78), 0),
                Math.max((int) (color.getGreen() * 0.78), 0),
                Math.max((int) (color.getBlue() * 0.78), 0));
    }

    public static JComboBox<String> combo(String firstItem, List<String> items) {
        JComboBox<String> box = new JComboBox<String>();
        box.setFont(FONT_BASE);
        if (firstItem != null) {
            box.addItem(firstItem);
        }
        for (int i = 0; i < items.size(); i++) {
            box.addItem(items.get(i));
        }
        return box;
    }

    public static JComboBox<String> options(String[] values) {
        JComboBox<String> box = new JComboBox<String>();
        box.setFont(FONT_BASE);
        for (int i = 0; i < values.length; i++) {
            box.addItem(values[i]);
        }
        return box;
    }

    public static String text(JComboBox<String> box) {
        if (box == null || box.getSelectedItem() == null) {
            return "";
        }
        return box.getSelectedItem().toString();
    }

    public static JScrollPane borderedScroll(Component view) {
        JScrollPane pane = new JScrollPane(view);
        pane.setBorder(BorderFactory.createLineBorder(BORDER));
        pane.getViewport().setBackground(CARD);
        pane.getVerticalScrollBar().setUnitIncrement(16);
        return pane;
    }

    public static JScrollPane detailScroll() {
        JScrollPane pane = borderedScroll(null);
        pane.setBorder(null);
        return pane;
    }

    public static JPanel detailPane(JScrollPane scroll, JPanel actions) {
        JPanel pane = new JPanel(new BorderLayout());
        pane.setBackground(CARD);
        pane.setBorder(BorderFactory.createLineBorder(BORDER));

        actions.setBackground(CARD);
        actions.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));

        pane.add(scroll, BorderLayout.CENTER);
        pane.add(actions, BorderLayout.SOUTH);
        return pane;
    }

    public static JTable dataTable(Object[] columns) {
        JTable table = new JTable(new DefaultTableModel(columns, 0));
        table.setFont(FONT_BASE);
        table.setForeground(TEXT);
        table.setBackground(CARD);
        table.setRowHeight(28);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(ROW_LINE);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(SELECTION);
        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);
        table.setDefaultEditor(Object.class, null);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(10, 32));
        header.setDefaultRenderer(new HeaderRenderer());
        return table;
    }
    public static JPanel fieldBox(String caption, String value) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(CARD);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(FONT_SMALL);
        captionLabel.setForeground(MUTED);

        JLabel valueLabel = new JLabel(display(value));
        valueLabel.setFont(FONT_VALUE);
        valueLabel.setForeground(TEXT);

        box.add(captionLabel, BorderLayout.NORTH);
        box.add(valueLabel, BorderLayout.CENTER);

        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, box.getPreferredSize().height));
        return box;
    }

    public static JPanel valueBox(String value) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(CARD);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        box.add(label(display(value), FONT_VALUE, TEXT), BorderLayout.CENTER);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        return box;
    }

    public static JPanel descriptionBox(String text) {
        String value = display(text);

        JTextArea area = new JTextArea(value);
        area.setFont(FONT_VALUE);
        area.setForeground(TEXT);
        area.setBackground(CARD);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setColumns(30);
        area.setBorder(null);

        int rows = value.length() / 70 + 2;
        if (rows < 3) {
            rows = 3;
        }
        if (rows > 14) {
            rows = 14;
        }
        area.setRows(rows);

        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(CARD);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        box.add(area, BorderLayout.CENTER);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        return box;
    }

    public static JPanel twoColumns(JPanel left, JPanel right) {
        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setBackground(CARD);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(left);
        row.add(right);
        return row;
    }

    public static void addSection(JPanel body, String title, String[][] fields) {
        body.add(label(title, FONT_SECTION, PRIMARY));
        body.add(Box.createVerticalStrut(8));

        for (int i = 0; i < fields.length; i = i + 2) {
            if (i + 1 < fields.length) {
                body.add(twoColumns(fieldBox(fields[i][0], fields[i][1]),
                        fieldBox(fields[i + 1][0], fields[i + 1][1])));
            } else {
                body.add(fieldBox(fields[i][0], fields[i][1]));
            }
            body.add(Box.createVerticalStrut(12));
        }
        body.add(Box.createVerticalStrut(6));
    }

    public static JPanel cardBody() {
        return new BodyPanel();
    }

    public static JPanel emptyDetail(String message) {
        JPanel body = cardBody();
        body.add(label(message, FONT_BASE, MUTED));
        return body;
    }

    public static void addFormRow(JPanel form, int row, String caption, Component field) {
        GridBagConstraints captionConstraints = new GridBagConstraints();
        captionConstraints.gridx = 0;
        captionConstraints.gridy = row;
        captionConstraints.gridwidth = 2;
        captionConstraints.weightx = 1.0;
        captionConstraints.fill = GridBagConstraints.HORIZONTAL;
        captionConstraints.anchor = GridBagConstraints.WEST;
        captionConstraints.insets = new Insets(6, 0, 4, 0);
        form.add(label(caption, FONT_BOLD, MUTED), captionConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 0;
        fieldConstraints.gridy = row + 1;
        fieldConstraints.gridwidth = 2;
        fieldConstraints.weightx = 1.0;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.anchor = GridBagConstraints.WEST;
        fieldConstraints.insets = new Insets(0, 0, 8, 0);
        form.add(field, fieldConstraints);
    }
    public static JPanel actionRow(javax.swing.JComponent... items) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        row.setBackground(CARD);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        for (int i = 0; i < items.length; i++) {
            if (i > 0) {
                row.add(Box.createHorizontalStrut(8));
            }
            row.add(items[i]);
        }
        return row;
    }

    public static JPasswordField passwordField(int columns) {
        JPasswordField field = new JPasswordField(columns);
        field.setFont(FONT_BASE);
        field.setForeground(TEXT);
        field.setBackground(CARD);
        field.setEchoChar('\u2022');
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    public static void selectItem(JComboBox<String> box, String value) {
        if (value == null || value.length() == 0) {
            return;
        }
        for (int i = 0; i < box.getItemCount(); i++) {
            if (box.getItemAt(i).equals(value)) {
                box.setSelectedIndex(i);
                return;
            }
        }
    }

    public static String display(String text) {
        if (text == null || text.trim().length() == 0) {
            return "-";
        }
        return text;
    }

    public static Color statusColor(String status) {
        if ("VERIFIED".equals(status)) {
            return SKY;
        }
        if ("IN_PROGRESS".equals(status)) {
            return new Color(0x45, 0x68, 0x9B);
        }
        if ("RESOLVED".equals(status)) {
            return new Color(0x2A, 0x6B, 0x39);
        }
        if ("PENDING".equals(status)) {
            return new Color(0xA9, 0x76, 0x1B);
        }
        return MUTED;
    }

    public static String detailCaption(String reportType) {
        if (reportType.equals("Road Hazard")) {
            return "Road Hazard Type";
        }
        if (reportType.equals("Flood")) {
            return "Water Level";
        }
        if (reportType.equals("Streetlight")) {
            return "Pole Number";
        }
        return "Hazard Category";
    }

    public static JTextField textField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(FONT_BASE);
        field.setForeground(TEXT);
        field.setBackground(CARD);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    /**
     * Lets the field keep digits only and stops it at maxDigits characters, so a
     * contact number cannot receive letters, spaces or dashes while typing or
     * when text is pasted into it.
     */
    public static void digitsOnly(JTextField field, final int maxDigits) {
        final javax.swing.text.PlainDocument document = new javax.swing.text.PlainDocument();
        document.setDocumentFilter(new javax.swing.text.DocumentFilter() {
            @Override
            public void insertString(FilterBypass bypass, int offset, String text,
                                     javax.swing.text.AttributeSet attributes)
                    throws javax.swing.text.BadLocationException {
                replace(bypass, offset, 0, text, attributes);
            }

            @Override
            public void replace(FilterBypass bypass, int offset, int length, String text,
                                javax.swing.text.AttributeSet attributes)
                    throws javax.swing.text.BadLocationException {
                javax.swing.text.Document target = bypass.getDocument();
                String current = target.getText(0, target.getLength());
                String insert = text == null ? "" : text;
                String updated = current.substring(0, offset) + insert
                        + current.substring(offset + length);
                updated = updated.replaceAll("[^0-9]", "");
                if (updated.length() > maxDigits) {
                    updated = updated.substring(0, maxDigits);
                }
                bypass.replace(0, current.length(), updated, attributes);
            }
        });
        field.setDocument(document);
    }

    /**
     * Stops the field at maxLength characters.
     */
    public static void maxLength(JTextField field, final int maxLength) {
        final javax.swing.text.PlainDocument document = new javax.swing.text.PlainDocument();
        document.setDocumentFilter(new javax.swing.text.DocumentFilter() {
            @Override
            public void insertString(FilterBypass bypass, int offset, String text,
                                     javax.swing.text.AttributeSet attributes)
                    throws javax.swing.text.BadLocationException {
                replace(bypass, offset, 0, text, attributes);
            }

            @Override
            public void replace(FilterBypass bypass, int offset, int length, String text,
                                javax.swing.text.AttributeSet attributes)
                    throws javax.swing.text.BadLocationException {
                javax.swing.text.Document target = bypass.getDocument();
                String current = target.getText(0, target.getLength());
                String insert = text == null ? "" : text;
                String updated = current.substring(0, offset) + insert
                        + current.substring(offset + length);
                if (updated.length() > maxLength) {
                    updated = updated.substring(0, maxLength);
                }
                bypass.replace(0, current.length(), updated, attributes);
            }
        });
        field.setDocument(document);
    }

    public static JTextField hintField(final String hint, int columns) {
        JTextField field = new JTextField(columns) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                if (getText().length() == 0) {
                    java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(MUTED);
                    g2.setFont(getFont());
                    g2.drawString(hint, getInsets().left,
                            (getHeight() - g2.getFontMetrics().getHeight()) / 2
                            + g2.getFontMetrics().getAscent());
                    g2.dispose();
                }
            }
        };
        field.setFont(FONT_BASE);
        field.setForeground(TEXT);
        field.setBackground(CARD);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    public static JTextArea hintArea(final String hint, int rows, int columns) {
        JTextArea area = new JTextArea(rows, columns) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                if (getText().length() == 0) {
                    java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(MUTED);
                    g2.setFont(getFont());
                    g2.drawString(hint, getInsets().left + 2,
                            getInsets().top + g2.getFontMetrics().getAscent());
                    g2.dispose();
                }
            }
        };
        area.setFont(FONT_BASE);
        area.setForeground(TEXT);
        area.setBackground(CARD);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return area;
    }

    private static final class HeaderRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);
            label.setOpaque(true);
            label.setBackground(PRIMARY);
            label.setForeground(CARD);
            label.setFont(FONT_BOLD);
            label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            return label;
        }
    }

    private static final class StatusRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);
            label.setFont(FONT_SMALL);
            label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            if (isSelected) {
                label.setForeground(TEXT);
            } else if (value != null) {
                label.setForeground(statusColor(value.toString()));
            }
            return label;
        }
    }

    public static DefaultTableCellRenderer statusRenderer() {
        return new StatusRenderer();
    }

    private static final class BodyPanel extends JPanel implements javax.swing.Scrollable {
        private static final long serialVersionUID = 1L;

        BodyPanel() {
            setBackground(CARD);
            setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
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
