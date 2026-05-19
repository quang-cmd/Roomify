package kqlhotel.gui.tabs;

import java.awt.*;
import java.time.LocalDateTime;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import kqlhotel.bus.promotion.PromotionsBUS;
import kqlhotel.entity.Promotion;
import kqlhotel.gui.components.DatePicker;
import kqlhotel.gui.components.PrimaryButton;
import net.miginfocom.swing.MigLayout;

public class PromotionDialog extends JDialog {

    private static final Color NAVY = new Color(24, 34, 52);
    private static final Color GOLD = new Color(196, 142, 45);
    private static final Color SOFT_BG = new Color(255, 250, 242);
    private static final Color TEXT_MUTED = new Color(105, 118, 138);

    private final JTextField txtMaKM = new JTextField();
    private final JTextField txtTenKM = new JTextField();
    private final JComboBox<String> cbLoaiKM = new JComboBox<>(new String[]{"- Chọn loại -", "VNĐ", "%"});
    private final JTextField txtTienKM = new JTextField();
    private final JTextField txtGiaTriToiDa = new JTextField();
    private final DatePicker dpNgayBatDau = new DatePicker();
    private final DatePicker dpNgayKetThuc = new DatePicker();
    private final JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{"DangHoatDong", "SapDienRa", "HetHan"});
    private final JTextField txtDieuKien = new JTextField();

    private final PromotionsBUS bus = new PromotionsBUS();
    private final Runnable onSuccess;
    private final boolean isEditMode;
    private final Promotion editingKM;

    public PromotionDialog(Window owner, Promotion km, Runnable onSuccess) {
        super(owner, km == null ? "Tạo khuyến mãi mới" : "Chỉnh sửa khuyến mãi", ModalityType.APPLICATION_MODAL);

        this.onSuccess = onSuccess;
        this.isEditMode = (km != null);
        this.editingKM = km;

        initComponents();
        if (isEditMode) loadData();

        setSize(660, 880);
        setMinimumSize(new Dimension(540, 650));
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(SOFT_BG);
        root.setBorder(new EmptyBorder(0, 0, 0, 0));

        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createFormPanel(), BorderLayout.CENTER);
        root.add(createActionPanel(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new MigLayout(
                "insets 22 26 18 26, fillx",
                "[grow,fill][]",
                "[]"
        ));
        header.setBackground(NAVY);

        JPanel titleBox = new JPanel(new MigLayout("wrap 1,insets 0,gap 2", "[grow,fill]", "[]"));
        titleBox.setOpaque(false);

        JLabel title = new JLabel(isEditMode ? "Chỉnh sửa khuyến mãi" : "Tạo khuyến mãi mới");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(255, 215, 120));

        JLabel sub = new JLabel("Thiết lập ưu đãi dành cho khách hàng KQL HOTEL");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(220, 228, 240));

        titleBox.add(title);
        titleBox.add(sub);

        JLabel icon = new JLabel("🎁", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        icon.setForeground(GOLD);

        header.add(titleBox, "growx");
        header.add(icon, "w 54!, h 54!");

        return header;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new MigLayout(
                "wrap 1, insets 24 26 18 26, gap 14, fillx",
                "[grow,fill]",
                "[]"
        ));
        panel.setBackground(SOFT_BG);

        txtMaKM.setEditable(!isEditMode);

        panel.add(createSectionLabel("Thông tin chương trình"));
        panel.add(createInputGroup("Mã khuyến mãi *", txtMaKM));
        panel.add(createInputGroup("Tên chương trình *", txtTenKM));

        panel.add(createSectionLabel("Giá trị ưu đãi"));

        JPanel rowThongSo = new JPanel(new MigLayout(
                "insets 0, gap 12, fillx",
                "[grow,fill][grow,fill][grow,fill]",
                "[]"
        ));
        rowThongSo.setOpaque(false);
        rowThongSo.add(createInputGroup("Loại giảm *", cbLoaiKM));
        rowThongSo.add(createInputGroup("Mức giảm *", txtTienKM));
        rowThongSo.add(createInputGroup("Giảm tối đa", txtGiaTriToiDa));
        panel.add(rowThongSo);

        panel.add(createSectionLabel("Thời gian & điều kiện"));

        JPanel rowDate = new JPanel(new MigLayout(
                "insets 0, gap 12, fillx",
                "[grow,fill][grow,fill]",
                "[]"
        ));
        rowDate.setOpaque(false);
        rowDate.add(createInputGroup("Ngày bắt đầu", dpNgayBatDau));
        rowDate.add(createInputGroup("Ngày kết thúc", dpNgayKetThuc));
        panel.add(rowDate);

        JPanel rowStatus = new JPanel(new MigLayout(
                "insets 0, gap 12, fillx",
                "[grow,fill][grow,fill]",
                "[]"
        ));
        rowStatus.setOpaque(false);
        rowStatus.add(createInputGroup("Trạng thái", cbTrangThai));
        rowStatus.add(createConditionInputGroup());
        panel.add(rowStatus);

        return panel;
    }

    private JPanel createActionPanel() {
        JPanel actionPanel = new JPanel(new MigLayout(
                "insets 16 26 22 26, fillx",
                "[grow,fill][][ ]",
                "[]"
        ));
        actionPanel.setBackground(SOFT_BG);

        PrimaryButton btnCancel = new PrimaryButton("Hủy");
        btnCancel.setArc(14);
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setForeground(TEXT_MUTED);
        btnCancel.setBorder(BorderFactory.createLineBorder(new Color(225, 231, 245), 2));
        btnCancel.addActionListener(e -> dispose());

        PrimaryButton btnSave = new PrimaryButton(isEditMode ? "Lưu thay đổi" : "Tạo khuyến mãi");
        btnSave.setArc(14);
        btnSave.setBackground(new Color(30, 160, 90));
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> savePromotion());

        actionPanel.add(new JLabel(), "growx");
        actionPanel.add(btnCancel, "w 130!, h 42!");
        actionPanel.add(btnSave, "w 160!, h 42!");

        return actionPanel;
    }

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text.toUpperCase());
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(GOLD);
        label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 200, 140)));
        return label;
    }

    private JPanel createInputGroup(String label, java.awt.Component comp) {
        JPanel p = new JPanel(new MigLayout("wrap 1, insets 0, gap 5", "[grow,fill]", "[]"));
        p.setOpaque(false);

        JLabel l = new JLabel(label);
        l.setForeground(TEXT_MUTED);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));

        styleInput(comp);

        p.add(l);
        p.add(comp, "h 38!");

        return p;
    }

    private void styleInput(java.awt.Component comp) {
        comp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comp.setPreferredSize(new Dimension(0, 38));

        if (comp instanceof JTextField tf) {
            tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 225, 235), 1),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
            ));
            tf.setBackground(Color.WHITE);
            tf.setForeground(NAVY);
        }

        if (comp instanceof JComboBox<?> cb) {
            cb.setBackground(Color.WHITE);
            cb.setForeground(NAVY);
        }
    }

    private JPanel createConditionInputGroup() {
        JPanel p = new JPanel(new MigLayout("wrap 1, insets 0, gap 5", "[grow,fill]", "[]"));
        p.setOpaque(false);

        JLabel l = new JLabel("Điều kiện");
        l.setForeground(TEXT_MUTED);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        p.add(l);

        JPanel row = new JPanel(new MigLayout("insets 0, gap 6, fillx", "[][grow,fill]", "[]"));
        row.setOpaque(false);

        JLabel prefix = new JLabel("Lớn hơn");
        prefix.setForeground(NAVY);
        prefix.setFont(new Font("Segoe UI", Font.BOLD, 13));

        styleInput(txtDieuKien);
        txtDieuKien.putClientProperty("JTextField.placeholderText", "Ví dụ: 3000000");

        row.add(prefix);
        row.add(txtDieuKien, "h 38!");

        p.add(row, "growx");
        return p;
    }

    private void loadData() {
        txtMaKM.setText(editingKM.getMaKM());
        txtTenKM.setText(editingKM.getTenKM());

        if ("TheoPhanTram".equals(editingKM.getLoaiKM())) {
            cbLoaiKM.setSelectedItem("%");
        } else {
            cbLoaiKM.setSelectedItem("VNĐ");
        }

        txtTienKM.setText(String.valueOf((int) editingKM.getTienKhuyenMai()));
        txtGiaTriToiDa.setText(String.valueOf((int) editingKM.getGiaTriToiDa()));
        dpNgayBatDau.setSelectedDate(editingKM.getNgayBatDau().toLocalDate());
        dpNgayKetThuc.setSelectedDate(editingKM.getNgayKetThuc().toLocalDate());
        cbTrangThai.setSelectedItem(editingKM.getTrangThaiKM());
        txtDieuKien.setText(String.valueOf((long) editingKM.getDieuKienApDung()));
    }

    private void savePromotion() {
        if (txtMaKM.getText().isBlank()
                || txtTenKM.getText().isBlank()
                || cbLoaiKM.getSelectedIndex() == 0
                || txtTienKM.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ các trường bắt buộc (*)", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (dpNgayBatDau.getSelectedDate() == null || dpNgayKetThuc.getSelectedDate() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày bắt đầu và ngày kết thúc.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Promotion km = isEditMode ? editingKM : new Promotion();

            km.setMaKM(txtMaKM.getText().trim().toUpperCase());
            km.setTenKM(txtTenKM.getText().trim());

            String loai = cbLoaiKM.getSelectedItem().toString();
            if ("%".equals(loai)) {
                km.setLoaiKM("TheoPhanTram");
            } else {
                km.setLoaiKM("TheoTien");
            }

            double mucGiam = Double.parseDouble(txtTienKM.getText().trim());
            double giamToiDa = txtGiaTriToiDa.getText().isBlank()
                    ? 0
                    : Double.parseDouble(txtGiaTriToiDa.getText().trim());

            if (mucGiam < 0 || giamToiDa < 0) {
                JOptionPane.showMessageDialog(this, "Mức giảm và giảm tối đa không được âm.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if ("TheoPhanTram".equals(km.getLoaiKM()) && mucGiam > 100) {
                JOptionPane.showMessageDialog(this, "Giảm theo phần trăm không được lớn hơn 100%.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }

            km.setTienKhuyenMai(mucGiam);
            km.setGiaTriToiDa(giamToiDa);

            km.setNgayBatDau(dpNgayBatDau.getSelectedDate().atStartOfDay());
            km.setNgayKetThuc(dpNgayKetThuc.getSelectedDate().atTime(23, 59, 59));

            if (!km.getNgayKetThuc().isAfter(km.getNgayBatDau())) {
                JOptionPane.showMessageDialog(this, "Ngày kết thúc phải sau ngày bắt đầu.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }

            km.setTrangThaiKM(cbTrangThai.getSelectedItem().toString());

            double dieuKien = txtDieuKien.getText().isBlank()
                    ? 0
                    : Double.parseDouble(txtDieuKien.getText().trim());

            if (dieuKien < 0) {
                JOptionPane.showMessageDialog(this, "Điều kiện không được âm.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }

            km.setDieuKienApDung(dieuKien);

            boolean success = isEditMode ? bus.updatePromotion(km) : bus.createPromotion(km);

            if (success) {
                if (onSuccess != null) {
                    onSuccess.run();
                }
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại. Vui lòng kiểm tra lại thông tin hoặc mã bị trùng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Mức giảm, giá trị tối đa và điều kiện phải là số.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        }
    }
}