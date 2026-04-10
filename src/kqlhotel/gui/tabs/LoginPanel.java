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

        RoundedPanel logoBox = new RoundedPanel(12, ThemeColors.TEXT_PRIMARY, new Color(255, 255, 255, 26), 1f);
        logoBox.setLayout(new BorderLayout());
        JLabel logo = new JLabel("KH", SwingConstants.CENTER);
        logo.setForeground(ThemeColors.ACCENT);
        logo.setFont(logo.getFont().deriveFont(28f));
        logoBox.add(logo, BorderLayout.CENTER);

        JLabel brand = new JLabel("KQL HOTEL", SwingConstants.CENTER);
        brand.setForeground(ThemeColors.TEXT_PRIMARY);
        brand.setFont(brand.getFont().deriveFont(32f));

        JLabel subtitle = new JLabel("Hệ thống quản lý khách sạn", SwingConstants.CENTER);
        subtitle.setForeground(ThemeColors.TEXT_MUTED);

        JLabel userLb = new JLabel("Tên đăng nhập");
        userLb.setForeground(ThemeColors.TEXT_MUTED);
        AppTextField usernameField = new AppTextField();
        usernameField.putClientProperty("JTextField.placeholderText", "Nhập tên đăng nhập");
        usernameField.setBackground(ThemeColors.SURFACE_LIGHT);
        usernameField.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel passLb = new JLabel("Mật khẩu");
        passLb.setForeground(ThemeColors.TEXT_MUTED);
        JPasswordField passwordField = new JPasswordField();
        passwordField.putClientProperty("JTextField.placeholderText", "Nhập mật khẩu");
        passwordField.setBackground(ThemeColors.SURFACE_LIGHT);
        passwordField.setForeground(ThemeColors.TEXT_PRIMARY);
        usernameField.addActionListener(e -> attemptLogin(usernameField.getText(), passwordField.getPassword()));

        PrimaryButton loginButton = new PrimaryButton("Đăng nhập");
        loginButton.setBackground(ThemeColors.ACCENT);
        loginButton.setForeground(Color.WHITE);
        loginButton.addActionListener(e -> attemptLogin(usernameField.getText(), passwordField.getPassword()));
        passwordField.addActionListener(e -> attemptLogin(usernameField.getText(), passwordField.getPassword()));

        RoundedPanel demo = new RoundedPanel(12, ThemeColors.DEMO_BG, ThemeColors.DEMO_BORDER, 1f);
        demo.setLayout(new BorderLayout());
        JLabel demoText = new JLabel("Tài khoản demo: admin / mật khẩu: admin123");
        demoText.setForeground(ThemeColors.DEMO_TEXT);
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
