package kqlhotel.gui.components;

import java.awt.*;
import javax.swing.JButton;
import kqlhotel.gui.theme.ThemeColors;

public class PrimaryButton extends JButton {
    private int arc = 12;

    private boolean hovered = false;

    public PrimaryButton(String text) {
        super(text);
        setContentAreaFilled(false);
        setBackground(ThemeColors.ACCENT);
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setBorderPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setArc(20); // Mặc định bo tròn nhiều hơn theo yêu cầu

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    public void setArc(int arc) { this.arc = arc; }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int w = getWidth();
        int h = getHeight();

        if (hovered) {
            // Draw a subtle shadow
            g2.setColor(new Color(0, 0, 0, 40));
            g2.fillRoundRect(2, 2, w - 2, h - 2, arc, arc);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, w - 2, h - 2, arc, arc);
            
            // Draw border if set and not empty
            if (getBorder() != null && !(getBorder() instanceof javax.swing.border.EmptyBorder)) {
                g2.setColor(getForeground()); // Simple fallback, or try to get border color
                // Actually, standard borders are hard to extract color from. 
                // But we can just use super.paintBorder if we set it to true temporarily.
            }
        } else {
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, w, h, arc, arc);
        }
        
        super.paintComponent(g);
        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
        if (getBorder() != null && !(getBorder() instanceof javax.swing.border.EmptyBorder)) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int w = hovered ? getWidth() - 2 : getWidth();
            int h = hovered ? getHeight() - 2 : getHeight();
            
            // If it's a line border, we try to draw it rounded
            if (getBorder() instanceof javax.swing.border.LineBorder) {
                g2.setColor(((javax.swing.border.LineBorder)getBorder()).getLineColor());
                g2.setStroke(new BasicStroke(((javax.swing.border.LineBorder)getBorder()).getThickness()));
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
            } else {
                super.paintBorder(g);
            }
            g2.dispose();
        }
    }
}
