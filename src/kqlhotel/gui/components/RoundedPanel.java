package kqlhotel.gui.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import javax.swing.JPanel;

public class RoundedPanel extends JPanel {
    private final int arc;
    private final Color backgroundColor;
    private final Color borderColor;
    private final float borderWidth;
    private final Color shadowColor;
    private final int shadowSize;
    private boolean hoverEffectEnabled = false;
    private boolean hovered = false;

    public RoundedPanel(int arc, Color backgroundColor, Color borderColor, float borderWidth) {
        this(arc, backgroundColor, borderColor, borderWidth, null, 0);
    }

    public RoundedPanel(
        int arc,
        Color backgroundColor,
        Color borderColor,
        float borderWidth,
        Color shadowColor,
        int shadowSize
    ) {
        this.arc = arc;
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        this.borderWidth = borderWidth;
        this.shadowColor = shadowColor;
        this.shadowSize = shadowSize;
        setOpaque(false);
    }

    public void setHoverEffectEnabled(boolean enabled) {
        this.hoverEffectEnabled = enabled;
        if (enabled) {
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
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth() - 1;
        int height = getHeight() - 1;
        int currentShadowSize = shadowSize;
        Color currentShadowColor = shadowColor;

        if (hoverEffectEnabled && hovered) {
            currentShadowSize = Math.max(shadowSize, 4);
            currentShadowColor = (shadowColor != null) ? shadowColor : new Color(0, 0, 0, 40);
        }

        if (currentShadowColor != null && currentShadowSize > 0) {
            g2.setColor(currentShadowColor);
            g2.fillRoundRect(0, currentShadowSize, width - currentShadowSize, height - currentShadowSize, arc, arc);
        }

        int drawWidth = currentShadowSize > 0 ? width - currentShadowSize : width;
        int drawHeight = currentShadowSize > 0 ? height - currentShadowSize : height;
        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, drawWidth, drawHeight, arc, arc);

        if (borderColor != null && borderWidth > 0) {
            Stroke oldStroke = g2.getStroke();
            g2.setStroke(new BasicStroke(borderWidth));
            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, drawWidth, drawHeight, arc, arc);
            g2.setStroke(oldStroke);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
