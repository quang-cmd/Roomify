package kqlhotel.gui.components;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;
import kqlhotel.gui.theme.ThemeColors;

public class BackgroundPanel extends JPanel {
    public BackgroundPanel() {
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        GradientPaint gp = new GradientPaint(0, 0, ThemeColors.BG_SECONDARY, w, h, ThemeColors.BG_PRIMARY);
        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);

        g2.setColor(new Color(95, 125, 170, 28));
        g2.fillRoundRect(w - 320, 30, 240, 240, 120, 120);
        g2.setColor(new Color(95, 125, 170, 20));
        g2.fillRoundRect(40, h - 180, 220, 220, 110, 110);

        g2.dispose();
        super.paintComponent(g);
    }
}
