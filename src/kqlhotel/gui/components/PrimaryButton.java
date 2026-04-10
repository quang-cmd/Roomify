package kqlhotel.gui.components;

import java.awt.Color;
import java.awt.Cursor;
import javax.swing.JButton;
import kqlhotel.gui.theme.ThemeColors;

public class PrimaryButton extends JButton {
    public PrimaryButton(String text) {
        super(text);
        setBackground(ThemeColors.ACCENT);
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setBorderPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
