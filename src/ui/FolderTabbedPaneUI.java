package ui;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Rectangle;

import javax.swing.plaf.basic.BasicTabbedPaneUI;

/**
 * FolderTabbedPaneUI.java
 * The folder-tab look shared by every dashboard. Each tab is a 40 pixel
 * trapezoid with slanted top corners: the open tab is white and joins the page
 * below it, the other tabs sit on a tinted strip. Styling only.
 */
public class FolderTabbedPaneUI extends BasicTabbedPaneUI {

    private static final int TAB_HEIGHT = 40;
    private static final int SLANT = 12;
    private static final int EXTRA_WIDTH = 46;

    @Override
    protected void installDefaults() {
        super.installDefaults();
        tabInsets = new Insets(10, 20, 10, 20);
        selectedTabPadInsets = new Insets(0, 0, 0, 0);
        tabAreaInsets = new Insets(8, 10, 0, 10);
        contentBorderInsets = new Insets(1, 1, 1, 1);
        tabRunOverlay = 0;

        tabPane.setFont(UITheme.FONT_TAB);
        tabPane.setBackground(UITheme.PAGE);
        tabPane.setForeground(UITheme.MUTED);
        tabPane.setOpaque(true);
    }

    @Override
    protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
        return Math.max(TAB_HEIGHT, super.calculateTabHeight(tabPlacement, tabIndex, fontHeight));
    }

    @Override
    protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
        return super.calculateTabWidth(tabPlacement, tabIndex, metrics) + EXTRA_WIDTH;
    }

    @Override
    protected int getTabLabelShiftX(int tabPlacement, int tabIndex, boolean isSelected) {
        return 0;
    }

    @Override
    protected int getTabLabelShiftY(int tabPlacement, int tabIndex, boolean isSelected) {
        return 0;
    }

    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y,
                                      int w, int h, boolean isSelected) {
        g.setColor(isSelected ? UITheme.CARD : UITheme.TAB_FILL);
        g.fillPolygon(tabShape(x, y, w, h));
    }

    @Override
    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y,
                                  int w, int h, boolean isSelected) {
        g.setColor(isSelected ? UITheme.TAB_LINE : UITheme.BORDER);

        g.drawLine(x, y + h - 1, x + SLANT, y);
        g.drawLine(x + SLANT, y, x + w - SLANT - 1, y);
        g.drawLine(x + w - SLANT - 1, y, x + w - 1, y + h - 1);

        if (!isSelected) {
            g.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
        }
    }

    @Override
    protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics,
                             int tabIndex, String title, Rectangle textRect, boolean isSelected) {
        g.setFont(font);
        g.setColor(isSelected ? UITheme.PRIMARY : UITheme.MUTED);
        g.drawString(title, textRect.x, textRect.y + metrics.getAscent());
    }

    @Override
    protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex,
                                       Rectangle iconRect, Rectangle textRect, boolean isSelected) {
        // the open tab already shows which page is displayed, so no focus box
    }

    @Override
    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
        Insets insets = tabPane.getInsets();
        int x = insets.left;
        int y = insets.top;
        int w = tabPane.getWidth() - insets.left - insets.right;
        int h = tabPane.getHeight() - insets.top - insets.bottom;
        int tabArea = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);

        g.setColor(UITheme.BORDER);
        g.drawRect(x, y + tabArea, w - 1, h - tabArea - 1);

        if (selectedIndex >= 0 && selectedIndex < rects.length && rects[selectedIndex] != null) {
            Rectangle bounds = rects[selectedIndex];
            g.setColor(UITheme.CARD);
            g.drawLine(bounds.x + 1, bounds.y + bounds.height - 1,
                    bounds.x + bounds.width - 2, bounds.y + bounds.height - 1);
        }
    }

    private Polygon tabShape(int x, int y, int w, int h) {
        Polygon polygon = new Polygon();
        polygon.addPoint(x, y + h - 1);
        polygon.addPoint(x + SLANT, y);
        polygon.addPoint(x + w - SLANT - 1, y);
        polygon.addPoint(x + w - 1, y + h - 1);
        return polygon;
    }
}
