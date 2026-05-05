package kqlhotel.gui.components;

import java.awt.*;
import javax.swing.*;
import kqlhotel.bus.room.RoomTypeBUS;
import kqlhotel.entity.RoomType;
import net.miginfocom.swing.MigLayout;

public class AddRoomTypeDialog extends JDialog {
    private final RoomTypeBUS roomTypeBUS;
    private final Runnable onSuccess;

    private JTextField tfRoomTypeId;
    private JTextField tfRoomTypeName;
    private JTextField tfRoomCount;
    private JTextField tfPrice;
    private JTextField tfMaxCapacity;
    private JTextField tfArea;
    private JTextArea taDescription;
    private JTextArea taAmenities;
    private JLabel lblError;

    public AddRoomTypeDialog(Window owner, RoomTypeBUS roomTypeBUS, Runnable onSuccess) {
        super(owner, "Add New Room Type", ModalityType.APPLICATION_MODAL);
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

        JLabel title = new JLabel("Add New Room Type");
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

        form.add(label("Room Type ID", true));
        form.add(label("Room Type Name", true));
        tfRoomTypeId = styledField();
        tfRoomTypeName = styledField();
        form.add(tfRoomTypeId, "h 38!");
        form.add(tfRoomTypeName, "h 38!");

        form.add(label("Price (USD)", true));
        form.add(label("Room Count", true));
        tfPrice = styledField();
        tfPrice.putClientProperty("JTextField.placeholderText", "Ex: 1500");
        tfRoomCount = styledField();
        tfRoomCount.putClientProperty("JTextField.placeholderText", "Ex: 10");
        form.add(tfPrice, "h 38!");
        form.add(tfRoomCount, "h 38!");

        form.add(label("Max Capacity (Persons)", true));
        form.add(label("Area (m²)", true));
        tfMaxCapacity = styledField();
        tfArea = styledField();
        form.add(tfMaxCapacity, "h 38!");
        form.add(tfArea, "h 38!");

        form.add(label("Description", false), "span 2");
        taDescription = new JTextArea(2, 20);
        taDescription.setLineWrap(true);
        taDescription.setWrapStyleWord(true);
        form.add(styledScrollPane(taDescription), "span 2, growx");

        form.add(label("Amenities", false), "span 2");
        taAmenities = new JTextArea(2, 20);
        taAmenities.setLineWrap(true);
        taAmenities.setWrapStyleWord(true);
        form.add(styledScrollPane(taAmenities), "span 2, growx");

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

        JButton btnCancel = new JButton("Cancel");
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

        PrimaryButton btnConfirm = new PrimaryButton("Add Room Type");
        btnConfirm.setBackground(new Color(17, 24, 39));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.addActionListener(e -> onConfirm());

        footer.add(btnCancel, "left");
        footer.add(btnConfirm, "right, h 40!, w 160!");
        return footer;
    }

    private void onConfirm() {
        lblError.setText(" ");
        String ma = tfRoomTypeId.getText().trim();
        String ten = tfRoomTypeName.getText().trim();
        String slStr = tfRoomCount.getText().trim();
        String giaStr = tfPrice.getText().trim();
        String sucChuaStr = tfMaxCapacity.getText().trim();
        String dienTichStr = tfArea.getText().trim();

        if (ma.isEmpty() || ten.isEmpty() || slStr.isEmpty() || giaStr.isEmpty() || sucChuaStr.isEmpty() || dienTichStr.isEmpty()) {
            lblError.setText("⚠ Please fill in all required fields.");
            return;
        }

        int sl, sucChua;
        double gia, dienTich;
        try {
            sl = Integer.parseInt(slStr);
            sucChua = Integer.parseInt(sucChuaStr);
        } catch (NumberFormatException e) {
            lblError.setText("⚠ Room count & capacity must be integers.");
            return;
        }

        try {
            gia = Double.parseDouble(giaStr);
            dienTich = Double.parseDouble(dienTichStr);
        } catch (NumberFormatException e) {
            lblError.setText("⚠ Price & area must be valid numbers.");
            return;
        }

        RoomType rt = new RoomType();
        rt.setRoomTypeId(ma);
        rt.setRoomTypeName(ten);
        rt.setRoomCount(sl);
        rt.setPrice(gia);
        rt.setMaxCapacity(sucChua);
        rt.setArea(dienTich);
        rt.setDescription(taDescription.getText().trim());
        rt.setAmenities(taAmenities.getText().trim());

        if (roomTypeBUS.addRoomType(rt)) {
            if (onSuccess != null) onSuccess.run();
            dispose();
        } else {
            lblError.setText("⚠ Failed to add room type. ID might already exist.");
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
