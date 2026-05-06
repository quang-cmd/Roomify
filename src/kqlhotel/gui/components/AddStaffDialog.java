package kqlhotel.gui.components;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import javax.swing.*;
import kqlhotel.gui.utils.IconLoader;
import net.miginfocom.swing.MigLayout;
import kqlhotel.bus.staff.StaffBUS;
import kqlhotel.entity.Account;
import kqlhotel.entity.Staff;
import kqlhotel.gui.theme.ThemeColors;
import kqlhotel.gui.utils.IconLoader;

public class AddStaffDialog extends JDialog {

    private final StaffBUS staffBUS;
    private final Runnable onSuccess;

    // Form fields
    private JTextField    tfMaNV;
    private JTextField    tfHoTen;
    private JTextField    tfSdt;
    private ButtonGroup   bgGender;
    private JRadioButton  rbNam, rbNu;
    private JTextField    tfUsername;
    private JPasswordField pfPassword;
    private JComboBox<String> cbVaiTro;
    private JComboBox<String> cbTinhTrang;
    private DatePicker    dpNgayVao;
    private JTextField    tfLuong;

    private JLabel lblError;

    public AddStaffDialog(Window owner, StaffBUS staffBUS, Runnable onSuccess) {
        super(owner, "Thêm nhân viên", ModalityType.APPLICATION_MODAL);
        this.staffBUS  = staffBUS;
        this.onSuccess = onSuccess;

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        RoundedPanel root = new RoundedPanel(16, Color.WHITE, new Color(220, 228, 245), 1f);
        root.setLayout(new MigLayout("insets 0, wrap 1, gap 0", "[fill, 560!]", "[]0[]0[]0[]"));
        root.setOpaque(false);

        root.add(buildHeader(),  "growx");
        root.add(buildForm(),    "grow");
        root.add(buildError(),   "growx");
        root.add(buildFooter(),  "growx");

        setContentPane(root);
        pack();
        setLocationRelativeTo(owner);

        // Auto-generate staff ID with length 5 (NV + 3 digits)
        tfMaNV.setText(String.format("NV%03d", (int)(Math.random() * 1000)));
    }

    // ===== Header =====
    private JPanel buildHeader() {
        JPanel header = new JPanel(new MigLayout("insets 18 24 18 24", "[][grow][]", "[]")) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(18, 35, 67),
                                                     getWidth(), 0, new Color(36, 60, 110));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 20, 16, 16);
                g2.dispose();
            }
        };
        header.setOpaque(false);

        JPanel icon = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        icon.setOpaque(false);
        JLabel iconLbl = new JLabel("", SwingConstants.CENTER);
        ImageIcon clientIcon = IconLoader.loadIcon("client.png", 24, 24);
        iconLbl.setIcon(clientIcon);
        icon.add(iconLbl);

        JPanel textGroup = new JPanel(new MigLayout("insets 0, wrap 1, gap 2"));
        textGroup.setOpaque(false);
        JLabel title = new JLabel("Thêm nhân viên mới");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Điền đầy đủ thông tin bên dưới");
        sub.setFont(sub.getFont().deriveFont(12f));
        sub.setForeground(new Color(180, 200, 240));
        textGroup.add(title);
        textGroup.add(sub);

        JButton btnClose = new JButton();
        ImageIcon closeIcon = IconLoader.loadIcon("close.png", 16, 16);
        if (closeIcon != null) {
            btnClose.setIcon(closeIcon);
        } else {
            btnClose.setText("x");
            btnClose.setFont(new Font("Segoe UI", Font.BOLD, 18));
        }
        btnClose.setForeground(new Color(203, 213, 225));
        btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.setMargin(new Insets(0, 0, 0, 0));
        btnClose.addActionListener(e -> dispose());

        header.add(icon,      "w 40!, h 40!");
        header.add(textGroup, "growx, gapx 12");
        header.add(btnClose,  "top");
        return header;
    }

    // ===== Form =====
    private JPanel buildForm() {
        JPanel form = new JPanel(new MigLayout(
            "wrap 2, insets 24 28 8 28, gap 14 10",
            "[grow, fill][grow, fill]", "[]"
        ));
        form.setOpaque(false);

        // Row 1: Mã NV | Họ tên
        form.add(label("Mã nhân viên", false));
        form.add(label("Họ và tên",    true));
        tfMaNV  = styledField();
        tfHoTen = styledField();
        form.add(tfMaNV,  "h 36!");
        form.add(tfHoTen, "h 36!");

        // Row 2: SĐT | Giới tính
        form.add(label("Số điện thoại", true));
        form.add(label("Giới tính",     true));
        tfSdt = styledField();
        form.add(tfSdt, "h 36!");

        JPanel genderPanel = new JPanel(new MigLayout("insets 8 12 8 12, gap 16", "[][]", "[]"));
        genderPanel.setBackground(new Color(248, 250, 254));
        genderPanel.setBorder(BorderFactory.createLineBorder(new Color(215, 225, 245), 1));
        rbNam = radio("Nam"); rbNu = radio("Nữ");
        bgGender = new ButtonGroup();
        bgGender.add(rbNam); bgGender.add(rbNu);
        rbNam.setSelected(true);
        genderPanel.add(rbNam); genderPanel.add(rbNu);
        form.add(genderPanel, "h 36!");

        // Row 3: Username | Password
        form.add(label("Tên đăng nhập", true));
        form.add(label("Mật khẩu",      true));
        tfUsername = styledField();
        pfPassword = new JPasswordField();
        stylePasswordField(pfPassword);
        form.add(tfUsername, "h 36!");
        form.add(pfPassword, "h 36!");

        // Row 4: Vai trò | Tình trạng
        form.add(label("Vai trò",    true));
        form.add(label("Tình trạng", true));
        cbVaiTro    = styledCombo(new String[]{"Nhân viên", "Quản lý"});
        cbTinhTrang = styledCombo(new String[]{"Đang hoạt động", "Ngừng hoạt động"});
        form.add(cbVaiTro,    "h 36!");
        form.add(cbTinhTrang, "h 36!");

        // Row 5: Ngày vào | Lương
        form.add(label("Ngày vào làm", true));
        form.add(label("Lương (VNĐ/tháng)", false));
        dpNgayVao = new DatePicker();
        dpNgayVao.setSelectedDate(LocalDate.now());
        tfLuong = styledField();
        tfLuong.putClientProperty("JTextField.placeholderText", "VD: 8500000");
        form.add(dpNgayVao, "h 36!");
        form.add(tfLuong,   "h 36!");

        return form;
    }

    private JPanel buildError() {
        JPanel p = new JPanel(new MigLayout("insets 0 28 8 28", "[grow]", "[]"));
        p.setOpaque(false);
        lblError = new JLabel(" ");
        lblError.setForeground(new Color(200, 50, 50));
        lblError.setFont(lblError.getFont().deriveFont(12f));
        p.add(lblError, "growx");
        return p;
    }

    // ===== Footer =====
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new MigLayout("insets 14 24 14 24", "[grow][]", "[]"));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 235, 248)));

        JButton btnCancel = new JButton("Huỷ");
        btnCancel.setFont(btnCancel.getFont().deriveFont(13f));
        btnCancel.setForeground(new Color(100, 120, 150));
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(215, 225, 245), 1, true),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        btnCancel.addActionListener(e -> dispose());

        PrimaryButton btnConfirm = new PrimaryButton("Xác nhận thêm");
        btnConfirm.setBackground(new Color(18, 35, 67));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.addActionListener(e -> onConfirm());

        footer.add(btnCancel,  "left");
        footer.add(btnConfirm, "right, h 40!, w 160!");
        return footer;
    }

    // ===== Validation & Save =====
    private void onConfirm() {
        lblError.setText(" ");

        String maNV     = tfMaNV.getText().trim();
        String hoTen    = tfHoTen.getText().trim();
        String sdt      = tfSdt.getText().trim();
        String username = tfUsername.getText().trim();
        String password = new String(pfPassword.getPassword()).trim();

        if (maNV.isEmpty() || hoTen.isEmpty() || sdt.isEmpty()
                || username.isEmpty() || password.isEmpty()) {
            lblError.setText("⚠ Vui lòng điền đầy đủ các trường bắt buộc (*).");
            return;
        }

        boolean gender = rbNam.isSelected();

        String roleDisplay = (String) cbVaiTro.getSelectedItem();
        String role = "Quản lý".equals(roleDisplay) ? "QuanLy" : "NhanVien";

        String statusDisplay = (String) cbTinhTrang.getSelectedItem();
        String status = "Đang hoạt động".equals(statusDisplay) ? "DangHoatDong" : "NgungHoatDong";

        LocalDate ngayVao = dpNgayVao.getSelectedDate();

        Double luong = null;
        String luongStr = tfLuong.getText().trim().replaceAll("[^0-9]", "");
        if (!luongStr.isEmpty()) {
            try { luong = Double.parseDouble(luongStr); }
            catch (NumberFormatException ex) {
                lblError.setText("⚠ Lương không hợp lệ.");
                return;
            }
        }

        Account account = new Account(username, password, role, status);
        Staff   staff   = new Staff(maNV, hoTen, sdt, gender, account, ngayVao, luong);

        boolean ok = staffBUS.addStaff(staff);
        if (ok) {
            if (onSuccess != null) onSuccess.run();
            dispose();
        } else {
            lblError.setText("⚠ Thêm thất bại. Tên đăng nhập hoặc Mã NV có thể đã tồn tại.");
        }
    }

    // ===== Helpers =====
    private JPanel label(String text, boolean required) {
        JPanel p = new JPanel(new MigLayout("insets 0", "[][]", "[]"));
        p.setOpaque(false);
        JLabel lbl = new JLabel(text);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 12f));
        lbl.setForeground(new Color(50, 70, 110));
        p.add(lbl);
        if (required) {
            JLabel star = new JLabel(" *");
            star.setForeground(new Color(200, 50, 50));
            star.setFont(star.getFont().deriveFont(Font.BOLD, 12f));
            p.add(star);
        }
        return p;
    }

    private JTextField styledField() {
        JTextField tf = new JTextField();
        tf.setFont(tf.getFont().deriveFont(13f));
        tf.setBackground(new Color(248, 250, 254));
        tf.setForeground(new Color(30, 50, 80));
        tf.setBorder(border(false));
        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { tf.setBorder(border(true));  }
            @Override public void focusLost  (FocusEvent e) { tf.setBorder(border(false)); }
        });
        return tf;
    }

    private void stylePasswordField(JPasswordField pf) {
        pf.setFont(pf.getFont().deriveFont(13f));
        pf.setBackground(new Color(248, 250, 254));
        pf.setForeground(new Color(30, 50, 80));
        pf.setBorder(border(false));
        pf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { pf.setBorder(border(true));  }
            @Override public void focusLost  (FocusEvent e) { pf.setBorder(border(false)); }
        });
    }

    private javax.swing.border.Border border(boolean focused) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(focused ? ThemeColors.PRIMARY : new Color(215, 225, 245), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        );
    }

    private JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(cb.getFont().deriveFont(13f));
        cb.setBackground(new Color(248, 250, 254));
        cb.setForeground(new Color(30, 50, 80));
        cb.setBorder(BorderFactory.createLineBorder(new Color(215, 225, 245), 1));
        cb.setFocusable(false);
        return cb;
    }

    private JRadioButton radio(String text) {
        JRadioButton rb = new JRadioButton(text);
        rb.setOpaque(false);
        rb.setFont(rb.getFont().deriveFont(13f));
        rb.setForeground(new Color(30, 50, 80));
        rb.setFocusPainted(false);
        return rb;
    }
}
