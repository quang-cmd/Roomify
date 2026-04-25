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

        // ===== Global =====
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("Panel.background", ThemeColors.BG_PRIMARY);
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("ProgressBar.arc", 10);
        UIManager.put("Component.focusColor", ThemeColors.PRIMARY);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.borderColor", ThemeColors.BORDER);
        UIManager.put("Component.disabledBorderColor", ThemeColors.BORDER_SOFT);

        // ===== Buttons =====
        UIManager.put("Button.default.background", ThemeColors.ACCENT);
        UIManager.put("Button.default.foreground", Color.WHITE);
        UIManager.put("Button.default.hoverBackground", ThemeColors.ACCENT_DARK);
        UIManager.put("Button.background", ThemeColors.SURFACE);
        UIManager.put("Button.foreground", ThemeColors.TEXT_PRIMARY);
        UIManager.put("Button.hoverBackground", ThemeColors.SURFACE_HOVER);
        UIManager.put("Button.borderColor", ThemeColors.BORDER);

        // ===== Inputs =====
        UIManager.put("TextField.background", ThemeColors.SURFACE);
        UIManager.put("TextField.foreground", ThemeColors.TEXT_PRIMARY);
        UIManager.put("TextField.placeholderForeground", ThemeColors.TEXT_PLACEHOLDER);
        UIManager.put("TextField.borderColor", ThemeColors.BORDER);
        UIManager.put("TextField.focusedBorderColor", ThemeColors.PRIMARY);
        UIManager.put("PasswordField.background", ThemeColors.SURFACE);
        UIManager.put("PasswordField.foreground", ThemeColors.TEXT_PRIMARY);
        UIManager.put("PasswordField.placeholderForeground", ThemeColors.TEXT_PLACEHOLDER);
        UIManager.put("PasswordField.borderColor", ThemeColors.BORDER);
        UIManager.put("PasswordField.focusedBorderColor", ThemeColors.PRIMARY);
        UIManager.put("FormattedTextField.background", ThemeColors.SURFACE);
        UIManager.put("TextArea.background", ThemeColors.SURFACE);
        UIManager.put("ComboBox.background", ThemeColors.SURFACE);
        UIManager.put("ComboBox.foreground", ThemeColors.TEXT_PRIMARY);
        UIManager.put("ComboBox.buttonBackground", ThemeColors.SURFACE);
        UIManager.put("Spinner.background", ThemeColors.SURFACE);

        // ===== Labels =====
        UIManager.put("Label.foreground", ThemeColors.TEXT_PRIMARY);

        // ===== Tables =====
        UIManager.put("Table.background", ThemeColors.SURFACE);
        UIManager.put("Table.foreground", ThemeColors.TEXT_PRIMARY);
        UIManager.put("Table.alternateRowColor", ThemeColors.SURFACE_LIGHT);
        UIManager.put("Table.gridColor", ThemeColors.BORDER_SOFT);
        UIManager.put("Table.selectionBackground", ThemeColors.PRIMARY_SOFT);
        UIManager.put("Table.selectionForeground", ThemeColors.TEXT_PRIMARY);
        UIManager.put("TableHeader.background", ThemeColors.SURFACE_LIGHT);
        UIManager.put("TableHeader.foreground", ThemeColors.TEXT_SECONDARY);
        UIManager.put("TableHeader.bottomSeparatorColor", ThemeColors.BORDER);

        // ===== ScrollBar =====
        UIManager.put("ScrollBar.thumb", ThemeColors.BORDER);
        UIManager.put("ScrollBar.hoverThumbColor", ThemeColors.TEXT_PLACEHOLDER);
        UIManager.put("ScrollBar.track", ThemeColors.BG_PRIMARY);
        UIManager.put("ScrollBar.width", 10);

        // ===== Tabs =====
        UIManager.put("TabbedPane.selectedBackground", ThemeColors.SURFACE);
        UIManager.put("TabbedPane.underlineColor", ThemeColors.PRIMARY);
        UIManager.put("TabbedPane.hoverColor", ThemeColors.SURFACE_HOVER);

        // ===== Tooltip & Popup =====
        UIManager.put("ToolTip.background", ThemeColors.TEXT_PRIMARY);
        UIManager.put("ToolTip.foreground", ThemeColors.TEXT_ON_DARK);
        UIManager.put("PopupMenu.background", ThemeColors.SURFACE);
        UIManager.put("PopupMenu.borderColor", ThemeColors.BORDER);

        // ===== OptionPane =====
        UIManager.put("OptionPane.background", ThemeColors.SURFACE);
        UIManager.put("OptionPane.messageForeground", ThemeColors.TEXT_PRIMARY);
    }
}
