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

    public static final Color PRIMARY = new Color(0, 51, 102);
    public static final Color MUTED = new Color(102, 102, 102);
    public static final Color TEXT = new Color(51, 51, 51);
    public static final Color ACCENT = new Color(0, 153, 255);
    public static final Color BORDER = new Color(204, 204, 204);
    public static final Color DANGER = new Color(198, 40, 40);
    public static final Color PAGE = new Color(238, 238, 238);
    public static final Color ROW_LINE = new Color(226, 226, 226);
    public static final Color SELECTION = new Color(204, 229, 255);

    public static final Font FONT_TITLE = new Font("Segoe UI", 1, 24);
    public static final Font FONT_HEADING = new Font("Segoe UI", 1, 18);
    public static final Font FONT_SECTION = new Font("Segoe UI", 1, 15);
    public static final Font FONT_BOLD = new Font("Segoe UI", 1, 12);
    public static final Font FONT_BASE = new Font("Segoe UI", 0, 12);
    public static final Font FONT_SMALL = new Font("Segoe UI", 1, 11);
    public static final Font FONT_VALUE = new Font("Segoe UI", 0, 13);

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
        panel.setBackground(Color.WHITE);
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
        return button;
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
        pane.getViewport().setBackground(Color.WHITE);
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
        pane.setBackground(Color.WHITE);
        pane.setBorder(BorderFactory.createLineBorder(BORDER));

        actions.setBackground(Color.WHITE);
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
        table.setBackground(Color.WHITE);
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
        box.setBackground(Color.WHITE);
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
        return box;
    }

    public static JPanel valueBox(String value) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(Color.WHITE);
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
        area.setBackground(Color.WHITE);
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
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        box.add(area, BorderLayout.CENTER);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        return box;
    }

    public static JPanel twoColumns(JPanel left, JPanel right) {
        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setBackground(Color.WHITE);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(left);
        row.add(right);
        return row;
    }

    public static void addSection(JPanel body, String title, String[][] fields) {
        body.add(label(title, FONT_SECTION, PRIMARY));
        body.add(Box.createVerticalStrut(8));

        for (int i = 0; i < fields.length; i = i + 2) {
            body.add(twoColumns(fieldBox(fields[i][0], fields[i][1]),
                    fieldBox(fields[i + 1][0], fields[i + 1][1])));
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
        row.setBackground(Color.WHITE);
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
        field.setBackground(Color.WHITE);
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
            return new Color(0, 102, 204);
        }
        if ("IN_PROGRESS".equals(status)) {
            return new Color(123, 31, 162);
        }
        if ("RESOLVED".equals(status)) {
            return new Color(46, 125, 50);
        }
        if ("PENDING".equals(status)) {
            return new Color(230, 126, 0);
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
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
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
        field.setBackground(Color.WHITE);
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
        area.setBackground(Color.WHITE);
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
            label.setForeground(Color.WHITE);
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
            setBackground(Color.WHITE);
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
