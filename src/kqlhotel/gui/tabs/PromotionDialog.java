package kqlhotel.gui.tabs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Window;
import java.time.LocalDateTime;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import kqlhotel.bus.promotion.PromotionsBUS;
import kqlhotel.entity.Promotion;
import kqlhotel.gui.components.DatePicker;
import kqlhotel.gui.components.PrimaryButton;
import kqlhotel.gui.theme.ThemeColors;
import net.miginfocom.swing.MigLayout;

public class PromotionDialog extends JDialog {

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

        setSize(450, 600);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel panel = new JPanel(new MigLayout("wrap 1, insets 20", "[grow,fill]", "[]"));
        panel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel(isEditMode ? "Chỉnh sửa khuyến mãi" : "Tạo khuyến mãi mới");
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 20f));
        lblTitle.setForeground(new Color(24, 40, 66));
        panel.add(lblTitle, "gapy 0 20");

        txtMaKM.setEditable(!isEditMode);
        
        panel.add(createInputGroup("Mã khuyến mãi *", txtMaKM));
        panel.add(createInputGroup("Tên chương trình *", txtTenKM));
        
        JPanel rowThongSo = new JPanel(new MigLayout("insets 0", "[grow,fill][grow,fill][grow,fill]", "[]"));
        rowThongSo.setOpaque(false);
        rowThongSo.add(createInputGroup("Loại giảm *", cbLoaiKM));
        rowThongSo.add(createInputGroup("Mức giảm *", txtTienKM));
        rowThongSo.add(createInputGroup("Giảm tối đa", txtGiaTriToiDa));
        panel.add(rowThongSo);

        JPanel rowDate = new JPanel(new MigLayout("insets 0", "[grow,fill][grow,fill]", "[]"));
        rowDate.setOpaque(false);
        rowDate.add(createInputGroup("Ngày bắt đầu", dpNgayBatDau));
        rowDate.add(createInputGroup("Ngày kết thúc", dpNgayKetThuc));
        panel.add(rowDate);

        panel.add(createInputGroup("Trạng thái", cbTrangThai));
        panel.add(createConditionInputGroup());

        // Buttons
        JPanel actionPanel = new JPanel(new MigLayout("insets 20 0 0 0", "[grow,fill][]", "[]"));
        actionPanel.setOpaque(false);

        PrimaryButton btnSave = new PrimaryButton(isEditMode ? "Lưu thay đổi" : "Tạo mới");
        btnSave.setArc(8);
        btnSave.addActionListener(e -> savePromotion());

        PrimaryButton btnCancel = new PrimaryButton("Hủy");
        btnCancel.setArc(8);
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setForeground(new Color(100, 115, 135));
        btnCancel.setBorder(BorderFactory.createLineBorder(new Color(225, 231, 245), 2));
        btnCancel.addActionListener(e -> dispose());

        actionPanel.add(btnCancel, "w 130!, h 38!");
        actionPanel.add(btnSave, "w 130!, h 38!");

        panel.add(actionPanel);
        
        getContentPane().add(panel, BorderLayout.CENTER);
    }

    private JPanel createInputGroup(String label, java.awt.Component comp) {
        JPanel p = new JPanel(new MigLayout("wrap 1, insets 0, gap 4", "[grow,fill]", "[]"));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setForeground(new Color(110, 125, 145));
        l.setFont(l.getFont().deriveFont(12f));
        p.add(l);
        if (comp instanceof JTextField || comp instanceof JComboBox) {
            comp.setPreferredSize(new java.awt.Dimension(0, 36));
        }
        p.add(comp);
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
            JOptionPane.showMessageDialog(this, "Mức giảm và giá trị tối đa phải là số.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createConditionInputGroup() {
        JPanel p = new JPanel(new MigLayout("wrap 1, insets 0, gap 4", "[grow,fill]", "[]"));
        p.setOpaque(false);

        JLabel l = new JLabel("Điều kiện");
        l.setForeground(new Color(110, 125, 145));
        l.setFont(l.getFont().deriveFont(12f));
        p.add(l);

        JPanel row = new JPanel(new MigLayout("insets 0, gap 6", "[][grow,fill]", "[]"));
        row.setOpaque(false);

        JLabel prefix = new JLabel("Lớn hơn");
        prefix.setForeground(new Color(24, 40, 66));
        prefix.setFont(prefix.getFont().deriveFont(Font.BOLD, 13f));

        txtDieuKien.setPreferredSize(new java.awt.Dimension(0, 36));
        txtDieuKien.putClientProperty("JTextField.placeholderText", "Nhập số tiền, ví dụ: 3000000");

        row.add(prefix);
        row.add(txtDieuKien, "h 36!");

        p.add(row);
        return p;
    }
}
