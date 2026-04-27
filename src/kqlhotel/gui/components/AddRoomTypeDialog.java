package kqlhotel.gui.components;

import java.awt.*;
import javax.swing.*;
import kqlhotel.bus.room.RoomTypeBUS;
import kqlhotel.entity.RoomType;
import net.miginfocom.swing.MigLayout;

public class AddRoomTypeDialog extends JDialog {
    private final RoomTypeBUS roomTypeBUS;
    private final Runnable onSuccess;

    private JTextField tfMaLoaiPhong;
    private JTextField tfTenLoaiPhong;
    private JTextField tfSoLuongPhong;
    private JTextField tfGiaPhong;
    private JTextField tfSucChuaToiDa;
    private JTextField tfDienTich;
    private JTextArea taMoTa;
    private JTextArea taTienNghi;
    private JLabel lblError;

    public AddRoomTypeDialog(Window owner, RoomTypeBUS roomTypeBUS, Runnable onSuccess) {
        super(owner, "Thêm loại phòng mới", ModalityType.APPLICATION_MODAL);
        this.roomTypeBUS = roomTypeBUS;
        this.onSuccess = onSuccess;

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        RoundedPanel root = new RoundedPanel(16, Color.WHITE, new Color(220, 228, 245), 1f);
        root.setLayout(new MigLayout("insets 0, wrap 1, gap 0", "[fill, 600!]", "[]0[]0[]0[]"));
        root.setOpaque(false);

        root.add(buildHeader(), "growx");
        root.add(buildForm(), "grow");
        root.add(buildError(), "growx");
        root.add(buildFooter(), "growx");

        setContentPane(root);
        pack();
        setLocationRelativeTo(owner);
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

        JLabel title = new JLabel("Thêm loại phòng mới");
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

        header.add(new JLabel("🏷"), "w 32!, h 32!");
        header.add(title, "gapx 8");
        header.add(btnClose, "top");
        return header;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new MigLayout("wrap 2, insets 24 28 8 28, gap 14 12", "[grow,fill][grow,fill]", "[]"));
        form.setOpaque(false);

        form.add(label("Mã loại phòng", true));
        form.add(label("Tên loại phòng", true));
        tfMaLoaiPhong = styledField();
        tfTenLoaiPhong = styledField();
        form.add(tfMaLoaiPhong, "h 38!");
        form.add(tfTenLoaiPhong, "h 38!");

        form.add(label("Giá phòng (VNĐ)", true));
        form.add(label("Số lượng phòng", true));
        tfGiaPhong = styledField();
        tfGiaPhong.putClientProperty("JTextField.placeholderText", "Ví dụ: 1500000");
        tfSoLuongPhong = styledField();
        tfSoLuongPhong.putClientProperty("JTextField.placeholderText", "Ví dụ: 10");
        form.add(tfGiaPhong, "h 38!");
        form.add(tfSoLuongPhong, "h 38!");

        form.add(label("Sức chứa tối đa (Người)", true));
        form.add(label("Diện tích (m²)", true));
        tfSucChuaToiDa = styledField();
        tfDienTich = styledField();
        form.add(tfSucChuaToiDa, "h 38!");
        form.add(tfDienTich, "h 38!");

        form.add(label("Mô tả", false), "span 2");
        taMoTa = new JTextArea(2, 20);
        taMoTa.setLineWrap(true);
        taMoTa.setWrapStyleWord(true);
        form.add(styledScrollPane(taMoTa), "span 2, growx");

        form.add(label("Tiện nghi", false), "span 2");
        taTienNghi = new JTextArea(2, 20);
        taTienNghi.setLineWrap(true);
        taTienNghi.setWrapStyleWord(true);
        form.add(styledScrollPane(taTienNghi), "span 2, growx");

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

        PrimaryButton btnConfirm = new PrimaryButton("✔ Thêm loại phòng");
        btnConfirm.setBackground(new Color(17, 24, 39));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.addActionListener(e -> onConfirm());

        footer.add(btnCancel, "left");
        footer.add(btnConfirm, "right, h 40!, w 160!");
        return footer;
    }

    private void onConfirm() {
        lblError.setText(" ");
        String ma = tfMaLoaiPhong.getText().trim();
        String ten = tfTenLoaiPhong.getText().trim();
        String slStr = tfSoLuongPhong.getText().trim();
        String giaStr = tfGiaPhong.getText().trim();
        String sucChuaStr = tfSucChuaToiDa.getText().trim();
        String dienTichStr = tfDienTich.getText().trim();

        if (ma.isEmpty() || ten.isEmpty() || slStr.isEmpty() || giaStr.isEmpty() || sucChuaStr.isEmpty() || dienTichStr.isEmpty()) {
            lblError.setText("⚠ Vui lòng nhập đầy đủ các trường bắt buộc.");
            return;
        }

        int sl, sucChua;
        double gia, dienTich;
        try {
            sl = Integer.parseInt(slStr);
            sucChua = Integer.parseInt(sucChuaStr);
        } catch (NumberFormatException e) {
            lblError.setText("⚠ Số lượng & sức chứa phải là số nguyên.");
            return;
        }

        try {
            gia = Double.parseDouble(giaStr);
            dienTich = Double.parseDouble(dienTichStr);
        } catch (NumberFormatException e) {
            lblError.setText("⚠ Giá phòng & diện tích phải là số hợp lệ.");
            return;
        }

        RoomType rt = new RoomType();
        rt.setMaLoaiPhong(ma);
        rt.setTenLoaiPhong(ten);
        rt.setSoLuongPhong(sl);
        rt.setGiaPhong(gia);
        rt.setSucChuaToiDa(sucChua);
        rt.setDienTich(dienTich);
        rt.setMoTa(taMoTa.getText().trim());
        rt.setTienNghi(taTienNghi.getText().trim());

        if (roomTypeBUS.addRoomType(rt)) {
            if (onSuccess != null) onSuccess.run();
            dispose();
        } else {
            lblError.setText("⚠ Thêm loại phòng thất bại. Có thể mã loại phòng đã tồn tại.");
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

    private JScrollPane styledScrollPane(JTextArea ta) {
        ta.setFont(ta.getFont().deriveFont(13f));
        ta.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        ta.setBackground(new Color(250, 252, 255));
        JScrollPane sp = new JScrollPane(ta);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220, 230, 245), 1));
        return sp;
    }
}
