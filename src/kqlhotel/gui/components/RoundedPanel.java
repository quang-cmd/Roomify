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

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth() - 1;
        int height = getHeight() - 1;

        if (shadowColor != null && shadowSize > 0) {
            g2.setColor(shadowColor);
            g2.fillRoundRect(0, shadowSize, width - shadowSize, height - shadowSize, arc, arc);
        }

        int drawWidth = shadowSize > 0 ? width - shadowSize : width;
        int drawHeight = shadowSize > 0 ? height - shadowSize : height;
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
