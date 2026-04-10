package kqlhotel.gui.theme;

import com.formdev.flatlaf.intellijthemes.FlatArcIJTheme;
import java.awt.Color;
import java.awt.Font;
import javax.swing.UIManager;

public final class UiTheme {
    private UiTheme() {
    }

    public static void setup() {
        FlatArcIJTheme.setup();

        UIManager.put("Panel.background", ThemeColors.BG_PRIMARY);
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 12);
        UIManager.put("Component.focusColor", ThemeColors.ACCENT);
        UIManager.put("Button.default.background", ThemeColors.ACCENT);
        UIManager.put("Button.default.foreground", Color.WHITE);
        UIManager.put("Button.background", ThemeColors.SURFACE_LIGHT);
        UIManager.put("Button.foreground", ThemeColors.TEXT_PRIMARY);
        UIManager.put("TextField.background", ThemeColors.SURFACE_LIGHT);
        UIManager.put("TextField.foreground", ThemeColors.TEXT_PRIMARY);
        UIManager.put("TextField.placeholderForeground", ThemeColors.TEXT_PLACEHOLDER);
        UIManager.put("PasswordField.background", ThemeColors.SURFACE_LIGHT);
        UIManager.put("PasswordField.foreground", ThemeColors.TEXT_PRIMARY);
        UIManager.put("PasswordField.placeholderForeground", ThemeColors.TEXT_PLACEHOLDER);
        UIManager.put("TextField.borderColor", ThemeColors.BORDER);
        UIManager.put("PasswordField.borderColor", ThemeColors.BORDER);
        UIManager.put("Label.foreground", ThemeColors.TEXT_PRIMARY);
        UIManager.put("Component.borderColor", ThemeColors.BORDER);
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));
    }
}
