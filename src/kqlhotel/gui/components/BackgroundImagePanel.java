package kqlhotel.gui.components;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class BackgroundImagePanel extends JPanel {
    private Image bg;

    public BackgroundImagePanel(String path) {
        URL url = getClass().getResource(path);
        if (url != null) {
            bg = new ImageIcon(url).getImage();
        }
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (bg != null) {
            g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(10, 20, 35, 90));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }
}