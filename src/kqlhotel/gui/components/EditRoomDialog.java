package kqlhotel.gui.components;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import kqlhotel.bus.room.PhongBUS;
import kqlhotel.entity.Phong;
import kqlhotel.entity.Room;
import kqlhotel.entity.RoomType;
import kqlhotel.dao.room.RoomTypeDAO;
import kqlhotel.gui.utils.IconLoader;
import net.miginfocom.swing.MigLayout;

public class EditRoomDialog extends JDialog {
    private final PhongBUS roomBUS;
    private final Phong currentRoom;
    private final Runnable onSuccess;
    private final RoomTypeDAO roomTypeDAO = new RoomTypeDAO();

    private JTextField tfRoomId;
    private JTextField tfFloor;
    private JComboBox<RoomTypeWrapper> cbRoomType;
    private JComboBox<String> cbStatus;
    private JLabel lblPrice, lblArea, lblCapacity;
    private JLabel lblError;
    private List<RoomType> roomTypeList;

    public EditRoomDialog(Window owner, Phong currentRoom, PhongBUS roomBUS, Runnable onSuccess) {
        super(owner, "Chỉnh sửa phòng", ModalityType.APPLICATION_MODAL);
        this.roomBUS = roomBUS;
        this.currentRoom = currentRoom;
        this.onSuccess = onSuccess;

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        RoundedPanel root = new RoundedPanel(16, Color.WHITE, new Color(226, 232, 240), 1f);
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
        populateData();
    }

    private void loadRoomTypes() {
        roomTypeList = roomTypeDAO.getAll();
        for (RoomType rt : roomTypeList) {
            cbRoomType.addItem(new RoomTypeWrapper(rt));
        }
        cbRoomType.addActionListener(e -> updateRoomTypeInfo());
    }

    private void populateData() {
        tfRoomId.setText(currentRoom.getRoomId());
        tfFloor.setText(String.valueOf(currentRoom.getFloor()));

        for (int i = 0; i < cbRoomType.getItemCount(); i++) {
            if (cbRoomType.getItemAt(i).roomType.getRoomTypeId().equals(currentRoom.getRoomType().getRoomTypeId())) {
                cbRoomType.setSelectedIndex(i);
                break;
            }
        }

        cbStatus.setSelectedItem(roomBUS.mapDbStatusToGuiStatus(currentRoom.getStatus()));
        updateRoomTypeInfo();
    }

    private void updateRoomTypeInfo() {
        RoomTypeWrapper wrapper = (RoomTypeWrapper) cbRoomType.getSelectedItem();
        if (wrapper != null) {
            RoomType rt = wrapper.roomType;
            lblPrice.setText(String.format("%,.0f VNĐ/đêm", rt.getPrice()));
            lblArea.setText(rt.getArea() + " m²");
            lblCapacity.setText(rt.getMaxCapacity() + " khách");
        }
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new MigLayout("insets 18 24 18 24", "[][grow][]", "[]")) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(15, 23, 42), getWidth(), 0, new Color(30, 41, 59));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 20, 16, 16);
                g2.dispose();
            }
        };
        header.setOpaque(false);

        JLabel title = new JLabel("Chỉnh sửa phòng " + currentRoom.getRoomId());
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

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
        btnClose.addActionListener(e -> dispose());

        header.add(new JLabel("🏨"), "w 32!, h 32!");
        header.add(title, "gapx 8");
        header.add(btnClose, "top");
        return header;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new MigLayout("wrap 2, insets 24 28 8 28, gap 14 12", "[grow,fill][grow,fill]", "[]"));
        form.setOpaque(false);

        form.add(label("Mã phòng (Chỉ đọc)", false));
        form.add(label("Tầng", true));
        tfRoomId = styledField();
        tfRoomId.setEditable(false);
        tfRoomId.setForeground(Color.GRAY);
        tfFloor = styledField();
        form.add(tfRoomId, "h 38!");
        form.add(tfFloor, "h 38!");

        form.add(label("Loại phòng", true), "span 2");
        cbRoomType = new JComboBox<>();
        form.add(cbRoomType, "span 2, h 38!");

        JPanel infoArea = new JPanel(new MigLayout("insets 12, gap 20", "[][][]", "[]"));
        infoArea.setBackground(new Color(248, 250, 252));
        infoArea.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        lblPrice = infoLabel("0 VNĐ");
        lblArea = infoLabel("0 m²");
        lblCapacity = infoLabel("0 khách");

        infoArea.add(infoItem("Giá:", lblPrice));
        infoArea.add(infoItem("Diện tích:", lblArea));
        infoArea.add(infoItem("Sức chứa:", lblCapacity));
        form.add(infoArea, "span 2, growx");

        form.add(label("Trạng thái", true), "span 2");

        cbStatus = new JComboBox<>(new String[]{"Trống", "Đang sử dụng", "Bảo trì"});

        form.add(cbStatus, "span 2, h 38!");

        return form;
    }

    private JPanel buildError() {
        JPanel p = new JPanel(new MigLayout("insets 0 28 8 28", "[grow]", "[]"));
        p.setOpaque(false);
        lblError = new JLabel(" ");
        lblError.setForeground(new Color(220, 38, 38));
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.add(lblError, "growx");
        return p;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new MigLayout("insets 14 24 14 24", "[grow][]", "[]"));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCancel.setForeground(new Color(100, 116, 139));
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        btnCancel.addActionListener(e -> dispose());

        PrimaryButton btnConfirm = new PrimaryButton("Lưu thay đổi");
        btnConfirm.setBackground(new Color(15, 23, 42));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.addActionListener(e -> onConfirm());

        footer.add(btnCancel, "left");
        footer.add(btnConfirm, "right, h 40!, w 150!");
        return footer;
    }

    private void onConfirm() {
        lblError.setText(" ");
        String id = tfRoomId.getText().trim();
        String floorStr = tfFloor.getText().trim();
        RoomTypeWrapper wrapper = (RoomTypeWrapper) cbRoomType.getSelectedItem();

        if (id.isEmpty() || floorStr.isEmpty() || wrapper == null) {
            lblError.setText("⚠ Vui lòng điền đầy đủ các trường bắt buộc.");
            return;
        }

        int floor;
        try {
            floor = Integer.parseInt(floorStr);
        } catch (NumberFormatException e) {
            lblError.setText("⚠ Tầng phải là số nguyên.");
            return;
        }

        String guiStatus = (String) cbStatus.getSelectedItem();
        String dbStatus = roomBUS.mapGuiStatusToDbStatus(guiStatus);

        RoomType rt = wrapper.roomType;
        Room r = new Room(id, 0.0, rt, floor, dbStatus);

        if (roomBUS.updateRoom(r)) {
            if (onSuccess != null) onSuccess.run();
            dispose();
        } else {
            lblError.setText("⚠ Cập nhật thất bại.");
        }
    }

    private JLabel label(String text, boolean req) {
        JLabel l = new JLabel("<html>" + text + (req ? " <font color='red'>*</font>" : "") + "</html>");
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(new Color(71, 85, 105));
        return l;
    }

    private JTextField styledField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        tf.setBackground(new Color(248, 250, 252));
        return tf;
    }

    private JLabel infoLabel(String txt) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(new Color(15, 23, 42));
        return l;
    }

    private JPanel infoItem(String title, JLabel value) {
        JPanel p = new JPanel(new MigLayout("insets 0, wrap 1, gap 2"));
        p.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        t.setForeground(new Color(100, 116, 139));
        p.add(t);
        p.add(value);
        return p;
    }

    private static class RoomTypeWrapper {
        final RoomType roomType;
        RoomTypeWrapper(RoomType rt) { this.roomType = rt; }
        @Override public String toString() { return roomType.getRoomTypeName(); }
    }
}