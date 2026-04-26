package kqlhotel.gui.tabs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Arrays;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;
import kqlhotel.gui.components.AppTextField;
import kqlhotel.gui.components.LoginBackgroundPanel;
import kqlhotel.gui.components.PrimaryButton;
import kqlhotel.gui.components.RoundedPanel;
import kqlhotel.gui.theme.ThemeColors;
import net.miginfocom.swing.MigLayout;

public class LoginPanel extends LoginBackgroundPanel {
    private final Runnable onLoginSuccess;

    public LoginPanel(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        setLayout(new MigLayout("insets 40", "[grow]", "[grow]"));

        RoundedPanel card = new RoundedPanel(
            16,
            ThemeColors.SURFACE,
            ThemeColors.BORDER_SOFT,
            1f,
            new Color(17, 24, 39, 20),
            6
        );
        card.setLayout(new MigLayout("wrap 1,insets 28 28 22 28,gap 10", "[grow,fill]", "[]"));
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Premium login: deep navy logo box with white "KH" letters
        RoundedPanel logoBox = new RoundedPanel(14, ThemeColors.PREMIUM_PRIMARY, ThemeColors.withAlpha(Color.WHITE, 24), 1f);
        logoBox.setLayout(new BorderLayout());
        JLabel logo = new JLabel("KH", SwingConstants.CENTER);
        logo.setForeground(Color.WHITE);
        logo.setFont(logo.getFont().deriveFont(java.awt.Font.BOLD, 28f));
        logoBox.add(logo, BorderLayout.CENTER);

        JLabel brand = new JLabel("KQL HOTEL", SwingConstants.CENTER);
        brand.setForeground(ThemeColors.PREMIUM_TEXT_PRIMARY);
        brand.setFont(brand.getFont().deriveFont(java.awt.Font.BOLD, 30f));

        JLabel subtitle = new JLabel("Hệ thống quản lý khách sạn", SwingConstants.CENTER);
        subtitle.setForeground(ThemeColors.PREMIUM_TEXT_MUTED);

        JLabel userLb = new JLabel("Tên đăng nhập");
        userLb.setForeground(ThemeColors.PREMIUM_TEXT_SECONDARY);
        userLb.setFont(userLb.getFont().deriveFont(java.awt.Font.BOLD, 12f));
        AppTextField usernameField = new AppTextField();
        usernameField.putClientProperty("JTextField.placeholderText", "Nhập tên đăng nhập");
        usernameField.setBackground(ThemeColors.PREMIUM_SURFACE_HOVER);
        usernameField.setForeground(ThemeColors.PREMIUM_TEXT_PRIMARY);

        JLabel passLb = new JLabel("Mật khẩu");
        passLb.setForeground(ThemeColors.PREMIUM_TEXT_SECONDARY);
        passLb.setFont(passLb.getFont().deriveFont(java.awt.Font.BOLD, 12f));
        JPasswordField passwordField = new JPasswordField();
        passwordField.putClientProperty("JTextField.placeholderText", "Nhập mật khẩu");
        passwordField.setBackground(ThemeColors.PREMIUM_SURFACE_HOVER);
        passwordField.setForeground(ThemeColors.PREMIUM_TEXT_PRIMARY);
        usernameField.addActionListener(e -> attemptLogin(usernameField.getText(), passwordField.getPassword()));

        // Primary CTA in deep navy (Premium brand) instead of amber
        PrimaryButton loginButton = new PrimaryButton("Đăng nhập");
        loginButton.setBackground(ThemeColors.PREMIUM_PRIMARY);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(loginButton.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        loginButton.addActionListener(e -> attemptLogin(usernameField.getText(), passwordField.getPassword()));
        passwordField.addActionListener(e -> attemptLogin(usernameField.getText(), passwordField.getPassword()));

        // Demo hint card uses violet accent soft tone for a modern note feel
        RoundedPanel demo = new RoundedPanel(12, ThemeColors.PREMIUM_ACCENT_SOFT, ThemeColors.withAlpha(ThemeColors.PREMIUM_ACCENT, 80), 1f);
        demo.setLayout(new BorderLayout());
        JLabel demoText = new JLabel("Tài khoản demo: admin / mật khẩu: admin123");
        demoText.setForeground(ThemeColors.PREMIUM_ACCENT_DARK);
        demoText.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        demo.add(demoText, BorderLayout.CENTER);

        card.add(logoBox, "w 64!,h 64!,alignx center,gapy 8 0");
        card.add(brand, "gapy 8 0");
        card.add(subtitle, "gapy 0 10");
        card.add(userLb, "gapy 6 0");
        card.add(usernameField, "h 40");
        card.add(passLb, "gapy 6 0");
        card.add(passwordField, "h 40");
        card.add(loginButton, "h 44,gapy 8 6");
        card.add(demo, "gapy 8");

        add(card, "w 420!,h 520!,alignx center,aligny center");

        usernameField.requestFocusInWindow();
    }

    private void attemptLogin(String username, char[] passwordValue) {
        String normalizedUsername = username == null ? "" : username.trim();
        String password = new String(passwordValue);
        boolean validInput = !normalizedUsername.isEmpty() && password.trim().length() > 0;

        if (!validInput) {
            JOptionPane.showMessageDialog(
                this,
                "Vui lòng nhập đủ tên đăng nhập và mật khẩu.",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE
            );
            Arrays.fill(passwordValue, '\0');
            return;
        }

        boolean success = "admin".equals(normalizedUsername) && "admin123".equals(password);

        if (!success) {
            JOptionPane.showMessageDialog(
                this,
                "Đăng nhập thất bại. Vui lòng kiểm tra lại tên đăng nhập hoặc mật khẩu.",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE
            );
            Arrays.fill(passwordValue, '\0');
            return;
        }

        Arrays.fill(passwordValue, '\0');

        if (onLoginSuccess != null) {
            onLoginSuccess.run();
        }
    }
}
