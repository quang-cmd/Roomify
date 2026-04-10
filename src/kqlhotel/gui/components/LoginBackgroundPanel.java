package kqlhotel.gui.components;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

public class LoginBackgroundPanel extends JPanel {
    private static final Color NAVY_BASE = new Color(20, 28, 47); // #141C2F
    private static final Color NAVY_EDGE = new Color(30, 41, 59); // #1E293B

    public LoginBackgroundPanel() {
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int w = getWidth();
        int h = getHeight();

        GradientPaint bg = new GradientPaint(0, 0, NAVY_EDGE, w, h, NAVY_BASE);
        g2.setPaint(bg);
        g2.fillRect(0, 0, w, h);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.24f));
        g2.setColor(new Color(53, 73, 110));
        g2.fillRoundRect(w / 2 - 170, h / 2 - 55, 340, 110, 110, 110);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.18f));
        g2.setColor(new Color(64, 89, 132));
        g2.fillOval(w - 220, -80, 280, 280);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.16f));
        g2.setColor(new Color(52, 72, 108));
        g2.fillOval(-120, h - 240, 320, 320);

        g2.setComposite(AlphaComposite.SrcOver);
        g2.dispose();
    }
}
