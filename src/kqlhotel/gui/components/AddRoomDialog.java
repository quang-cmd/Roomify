package kqlhotel.gui.components;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import kqlhotel.bus.room.PhongBUS;
import kqlhotel.dao.room.RoomTypeDAO;
import kqlhotel.entity.LoaiPhong;
import kqlhotel.entity.Phong;
import kqlhotel.entity.RoomType;
import kqlhotel.gui.theme.ThemeColors;
import net.miginfocom.swing.MigLayout;

public class AddRoomDialog extends JDialog {
    private final PhongBUS phongBUS;
    private final Runnable onSuccess;
    private final RoomTypeDAO roomTypeDAO = new RoomTypeDAO();

    private JTextField tfMaPhong;
    private JTextField tfTang;
    private JComboBox<RoomTypeWrapper> cbLoaiPhong;
    private JTextField tfTienCoc;
    private JLabel lblGia, lblDienTich, lblSucChua;
    private JLabel lblError;
    private List<RoomType> listLoaiPhong;

    public AddRoomDialog(Window owner, PhongBUS phongBUS, Runnable onSuccess) {
        super(owner, "Thêm phòng mới", ModalityType.APPLICATION_MODAL);
        this.phongBUS = phongBUS;
        this.onSuccess = onSuccess;

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        RoundedPanel root = new RoundedPanel(16, Color.WHITE, new Color(220, 228, 245), 1f);
        root.setLayout(new MigLayout("insets 0, wrap 1, gap 0", "[fill, 500!]", "[]0[]0[]0[]"));
        root.setOpaque(false);

        root.add(buildHeader(), "growx");
        root.add(buildForm(), "grow");
        root.add(buildError(), "growx");
        root.add(buildFooter(), "growx");

        setContentPane(root);
        pack();
        setLocationRelativeTo(owner);
        
        loadRoomTypes();
    }

    private void loadRoomTypes() {
        listLoaiPhong = roomTypeDAO.getAll();
        for (RoomType rt : listLoaiPhong) {
            cbLoaiPhong.addItem(new RoomTypeWrapper(rt));
        }
        updateRoomTypeInfo();
    }

    private void updateRoomTypeInfo() {
        RoomTypeWrapper wrapper = (RoomTypeWrapper) cbLoaiPhong.getSelectedItem();
        if (wrapper != null) {
            RoomType rt = wrapper.roomType;
            lblGia.setText(String.format("%,.0f VNĐ/đêm", rt.getGiaPhong()));
            lblDienTich.setText(rt.getDienTich() + " m²");
            lblSucChua.setText(rt.getSucChuaToiDa() + " người");
        }
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new MigLayout("insets 18 24 18 24", "[][grow][]", "[]")) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(17, 24, 39), getWidth(), 0, new Color(40, 50, 70));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 20, 16, 16);
                g2.dispose();
            }
        };
        header.setOpaque(false);

        JLabel title = new JLabel("Thêm phòng mới");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        title.setForeground(Color.WHITE);

        JButton btnClose = new JButton("×");
        btnClose.setFont(btnClose.getFont().deriveFont(Font.BOLD, 24f));
        btnClose.setForeground(new Color(180, 190, 210));
        btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());

        header.add(new JLabel("🏨"), "w 32!, h 32!");
        header.add(title, "gapx 8");
        header.add(btnClose, "top");
        return header;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new MigLayout("wrap 2, insets 24 28 8 28, gap 14 12", "[grow,fill][grow,fill]", "[]"));
        form.setOpaque(false);

        form.add(label("Mã phòng", true));
        form.add(label("Tầng", true));
        tfMaPhong = styledField();
        tfTang = styledField();
        form.add(tfMaPhong, "h 38!");
        form.add(tfTang, "h 38!");

        form.add(label("Loại phòng", true), "span 2");
        cbLoaiPhong = new JComboBox<>();
        styleCombo(cbLoaiPhong);
        cbLoaiPhong.addActionListener(e -> updateRoomTypeInfo());
        form.add(cbLoaiPhong, "span 2, h 38!");

        // Info area
        JPanel infoArea = new JPanel(new MigLayout("insets 12, gap 20", "[][][]", "[]"));
        infoArea.setBackground(new Color(245, 248, 254));
        infoArea.setBorder(BorderFactory.createLineBorder(new Color(225, 231, 245)));
        
        lblGia = infoLabel("0 VNĐ");
        lblDienTich = infoLabel("0 m²");
        lblSucChua = infoLabel("0 người");
        
        infoArea.add(infoItem("Giá:", lblGia));
        infoArea.add(infoItem("Diện tích:", lblDienTich));
        infoArea.add(infoItem("Sức chứa:", lblSucChua));
        form.add(infoArea, "span 2, growx");

        form.add(label("Tiền cọc (VNĐ)", false), "span 2");
        tfTienCoc = styledField();
        tfTienCoc.putClientProperty("JTextField.placeholderText", "0");
        form.add(tfTienCoc, "span 2, h 38!");

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

        PrimaryButton btnConfirm = new PrimaryButton("✔ Thêm phòng");
        btnConfirm.setBackground(new Color(17, 24, 39));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.addActionListener(e -> onConfirm());

        footer.add(btnCancel, "left");
        footer.add(btnConfirm, "right, h 40!, w 150!");
        return footer;
    }

    private void onConfirm() {
        lblError.setText(" ");
        String ma = tfMaPhong.getText().trim();
        String tangStr = tfTang.getText().trim();
        RoomTypeWrapper wrapper = (RoomTypeWrapper) cbLoaiPhong.getSelectedItem();

        if (ma.isEmpty() || tangStr.isEmpty() || wrapper == null) {
            lblError.setText("⚠ Vui lòng nhập đầy đủ các trường bắt buộc.");
            return;
        }

        int tang;
        try {
            tang = Integer.parseInt(tangStr);
        } catch (NumberFormatException e) {
            lblError.setText("⚠ Tầng phải là số nguyên.");
            return;
        }

        Double tienCoc = 0.0;
        String cocStr = tfTienCoc.getText().trim();
        if (!cocStr.isEmpty()) {
            try {
                tienCoc = Double.parseDouble(cocStr);
            } catch (NumberFormatException e) {
                lblError.setText("⚠ Tiền cọc không hợp lệ.");
                return;
            }
        }

        // Map RoomType (entity used by RoomTypeDAO) to LoaiPhong (entity used by Phong)
        RoomType rt = wrapper.roomType;
        LoaiPhong lp = new LoaiPhong(rt.getMaLoaiPhong(), rt.getTenLoaiPhong(), rt.getSoLuongPhong(), rt.getGiaPhong(), rt.getSucChuaToiDa(), rt.getDienTich(), rt.getMoTa(), rt.getTienNghi());

        Phong p = new Phong(ma, tienCoc, lp, tang, "Trong");

        if (phongBUS.addRoom(p)) {
            if (onSuccess != null) onSuccess.run();
            dispose();
        } else {
            lblError.setText("⚠ Thêm phòng thất bại. Có thể mã phòng đã tồn tại.");
        }
    }

    private JLabel label(String text, boolean req) {
        JLabel l = new JLabel(text + (req ? " *" : ""));
        l.setFont(l.getFont().deriveFont(Font.BOLD, 12f));
        l.setForeground(new Color(70, 80, 100));
        return l;
    }

    private JTextField styledField() {
        JTextField tf = new JTextField();
        tf.setFont(tf.getFont().deriveFont(13f));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 230, 245), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        tf.setBackground(new Color(250, 252, 255));
        return tf;
    }

    private void styleCombo(JComboBox<?> cb) {
        cb.setFont(cb.getFont().deriveFont(13f));
        cb.setBackground(new Color(250, 252, 255));
    }

    private JLabel infoLabel(String txt) {
        JLabel l = new JLabel(txt);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 13f));
        l.setForeground(new Color(30, 40, 60));
        return l;
    }

    private JPanel infoItem(String title, JLabel value) {
        JPanel p = new JPanel(new MigLayout("insets 0, wrap 1, gap 2"));
        p.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(t.getFont().deriveFont(11f));
        t.setForeground(new Color(130, 145, 170));
        p.add(t);
        p.add(value);
        return p;
    }

    private static class RoomTypeWrapper {
        final RoomType roomType;
        RoomTypeWrapper(RoomType rt) { this.roomType = rt; }
        @Override public String toString() { return roomType.getTenLoaiPhong(); }
    }
}
