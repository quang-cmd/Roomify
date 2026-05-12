package kqlhotel.gui.tabs;

import java.awt.BorderLayout;
import javax.swing.SwingUtilities;
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
import kqlhotel.gui.utils.IconLoader;
import kqlhotel.gui.Session;
import javax.swing.ImageIcon;
import kqlhotel.gui.theme.ThemeColors;
import net.miginfocom.swing.MigLayout;
import kqlhotel.service.EmailService;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class LoginPanel extends LoginBackgroundPanel {
    private final Runnable onLoginSuccess;

    private AppTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox showPasswordCheck;
    private char defaultEchoChar;

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

        ImageIcon logoIcon = IconLoader.loadIconKeepRatio("logo.png", 100);
        JLabel logoLabel = new JLabel(logoIcon, SwingConstants.CENTER);

        JLabel brand = new JLabel("KQL HOTEL", SwingConstants.CENTER);
        brand.setForeground(ThemeColors.PREMIUM_TEXT_PRIMARY);
        brand.setFont(brand.getFont().deriveFont(java.awt.Font.BOLD, 30f));

        JLabel subtitle = new JLabel("Hệ thống quản lý khách sạn", SwingConstants.CENTER);
        subtitle.setForeground(ThemeColors.PREMIUM_TEXT_MUTED);

        JLabel userLb = new JLabel("Tên đăng nhập");
        userLb.setForeground(ThemeColors.PREMIUM_TEXT_SECONDARY);
        userLb.setFont(userLb.getFont().deriveFont(java.awt.Font.BOLD, 12f));

        usernameField = new AppTextField();
        usernameField.putClientProperty("JTextField.placeholderText", "Nhập tên đăng nhập");
        usernameField.setBackground(ThemeColors.PREMIUM_SURFACE_HOVER);
        usernameField.setForeground(ThemeColors.PREMIUM_TEXT_PRIMARY);

        JLabel passLb = new JLabel("Mật khẩu");
        passLb.setForeground(ThemeColors.PREMIUM_TEXT_SECONDARY);
        passLb.setFont(passLb.getFont().deriveFont(java.awt.Font.BOLD, 12f));

        passwordField = new JPasswordField();
        passwordField.putClientProperty("JTextField.placeholderText", "Nhập mật khẩu");
        passwordField.setBackground(ThemeColors.PREMIUM_SURFACE_HOVER);
        passwordField.setForeground(ThemeColors.PREMIUM_TEXT_PRIMARY);

        defaultEchoChar = passwordField.getEchoChar();

        showPasswordCheck = new JCheckBox("Hiện mật khẩu");
        showPasswordCheck.setOpaque(false);
        showPasswordCheck.setForeground(ThemeColors.PREMIUM_TEXT_SECONDARY);
        showPasswordCheck.setFont(showPasswordCheck.getFont().deriveFont(java.awt.Font.PLAIN, 12f));

        showPasswordCheck.addActionListener(e -> {
            if (showPasswordCheck.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar(defaultEchoChar);
            }
        });

        PrimaryButton loginButton = new PrimaryButton("Đăng nhập");
        loginButton.setBackground(ThemeColors.PREMIUM_PRIMARY);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(loginButton.getFont().deriveFont(java.awt.Font.BOLD, 14f));

        JLabel forgotPassword = new JLabel("Quên mật khẩu?", SwingConstants.CENTER);
        forgotPassword.setForeground(ThemeColors.PREMIUM_ACCENT);
        forgotPassword.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        forgotPassword.setFont(forgotPassword.getFont().deriveFont(java.awt.Font.PLAIN, 12f));

        forgotPassword.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showForgotPasswordDialog();
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                forgotPassword.setForeground(ThemeColors.PREMIUM_PRIMARY);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                forgotPassword.setForeground(ThemeColors.PREMIUM_ACCENT);
            }
        });

        usernameField.addActionListener(e -> attemptLogin(usernameField.getText(), passwordField.getPassword()));
        passwordField.addActionListener(e -> attemptLogin(usernameField.getText(), passwordField.getPassword()));
        loginButton.addActionListener(e -> attemptLogin(usernameField.getText(), passwordField.getPassword()));

        if (logoIcon != null) {
            card.add(logoLabel, "alignx center,gapy 8 0");
        } else {
            JLabel logo = new JLabel("KH", SwingConstants.CENTER);
            logo.setForeground(ThemeColors.PREMIUM_PRIMARY);
            logo.setFont(logo.getFont().deriveFont(java.awt.Font.BOLD, 48f));
            card.add(logo, "w 100!,h 100!,alignx center,gapy 8 0");
        }
        card.add(brand, "gapy 8 0");
        card.add(subtitle, "gapy 0 10");
        card.add(userLb, "gapy 6 0");
        card.add(usernameField, "h 40");
        card.add(passLb, "gapy 6 0");
        card.add(passwordField, "h 40");
        card.add(showPasswordCheck, "gapy 0 4");
        card.add(loginButton, "h 44,gapy 8 2");
        card.add(forgotPassword, "alignx center,gapy 0 8");

        add(card, "w 420!,h 540!,alignx center,aligny center");

        usernameField.requestFocusInWindow();
    }

    private void attemptLogin(String username, char[] passwordValue) {
        String normalizedUsername = username == null ? "" : username.trim();
        String password = new String(passwordValue);

        if (normalizedUsername.isEmpty() || password.trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng nhập đủ tên đăng nhập và mật khẩu.",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE
            );
            Arrays.fill(passwordValue, '\0');
            return;
        }

        try {
            java.sql.Connection con = kqlhotel.dao.ConnectDB.getInstance().getConnection();

            String sql =
                    "SELECT tenDangNhap, matKhau, vaiTro, trangThaiTK " +
                            "FROM TaiKhoan " +
                            "WHERE tenDangNhap = ?";

            java.sql.PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, normalizedUsername);

            java.sql.ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Tên đăng nhập không tồn tại.",
                        "Đăng nhập thất bại",
                        JOptionPane.WARNING_MESSAGE
                );
                Arrays.fill(passwordValue, '\0');
                return;
            }

            String dbPassword = rs.getString("matKhau");
            String trangThai = rs.getString("trangThaiTK");

            if (!"DangHoatDong".equalsIgnoreCase(trangThai)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Tài khoản đã bị ngừng hoạt động.",
                        "Đăng nhập thất bại",
                        JOptionPane.WARNING_MESSAGE
                );
                Arrays.fill(passwordValue, '\0');
                return;
            }

            if (!password.equals(dbPassword)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Sai mật khẩu.",
                        "Đăng nhập thất bại",
                        JOptionPane.WARNING_MESSAGE
                );
                Arrays.fill(passwordValue, '\0');
                return;
            }

            Arrays.fill(passwordValue, '\0');

// Tạo Account từ tài khoản đăng nhập
            kqlhotel.entity.Account acc = new kqlhotel.entity.Account(
                    rs.getString("tenDangNhap"),
                    rs.getString("matKhau"),
                    rs.getString("vaiTro"),
                    rs.getString("trangThaiTK")
            );

            // Tìm nhân viên theo tenDangNhap
            kqlhotel.entity.Staff foundStaff = null;
            kqlhotel.bus.staff.StaffBUS staffBUS = new kqlhotel.bus.staff.StaffBUS();

            for (kqlhotel.entity.Staff s : staffBUS.getAll()) {
                if (s.getAccount() != null
                        && s.getAccount().getUsername() != null
                        && s.getAccount().getUsername().equals(acc.getUsername())) {
                    foundStaff = s;
                    break;
                }
            }

            // Lưu user đang đăng nhập
            Session.currentAccount = acc;
            Session.currentStaff = foundStaff;

            // Chuyển màn hình
            if (onLoginSuccess != null) {
                onLoginSuccess.run();
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi kết nối hoặc truy vấn database.",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void showForgotPasswordDialog() {
        String input = JOptionPane.showInputDialog(
                this,
                "Nhập Mã NV hoặc Tên nhân viên:",
                "Quên mật khẩu",
                JOptionPane.PLAIN_MESSAGE
        );

        if (input == null || input.trim().isEmpty()) {
            return;
        }

        input = input.trim();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn gửi yêu cầu cấp lại mật khẩu không?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            java.sql.Connection con = kqlhotel.dao.ConnectDB.getInstance().getConnection();

            String sql =
                    "SELECT nv.maNV, nv.hoTenNV, nv.sdt, nv.tenDangNhap " +
                            "FROM NhanVien nv " +
                            "JOIN TaiKhoan tk ON nv.tenDangNhap = tk.tenDangNhap " +
                            "WHERE nv.maNV = ? OR nv.hoTenNV LIKE ?";

            java.sql.PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, input);
            ps.setString(2, "%" + input + "%");

            java.sql.ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Không tìm thấy nhân viên.",
                        "Thông báo",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            String maNV = rs.getString("maNV");
            String hoTenNV = rs.getString("hoTenNV");
            String sdt = rs.getString("sdt");
            String tenDangNhap = rs.getString("tenDangNhap");

            EmailService.sendPasswordResetRequestToManager(
                    maNV,
                    hoTenNV,
                    sdt,
                    tenDangNhap
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Đã gửi yêu cầu đến quản lý.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống.");
        }
    }

    private String generateOtp() {
        int otp = 100000 + new java.util.Random().nextInt(900000);
        return String.valueOf(otp);
    }

    public void resetForm() {
        if (usernameField != null) {
            usernameField.setText("");
        }

        if (passwordField != null) {
            passwordField.setText("");
            passwordField.setEchoChar(defaultEchoChar);
        }

        if (showPasswordCheck != null) {
            showPasswordCheck.setSelected(false);
        }

        SwingUtilities.invokeLater(() -> {
            if (usernameField != null) {
                usernameField.requestFocusInWindow();
            }
        });
    }
}
