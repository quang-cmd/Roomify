package kqlhotel.gui.components;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;
import kqlhotel.gui.theme.ThemeColors;
import kqlhotel.gui.tabs.RoomManagementPanel;
import kqlhotel.entity.Phong;
import kqlhotel.entity.LoaiPhong;
import kqlhotel.bus.room.PhongBUS;

public class RoomSearchDialog extends JDialog {

    private JTextField txtRoomNo;
    private JComboBox<String> cbRoomType;
    private final kqlhotel.dao.room.RoomTypeDAO roomTypeDAO = new kqlhotel.dao.room.RoomTypeDAO();
    private JComboBox<String> cbStatus;
    private JPanel resultContainer;
    private final RoomManagementPanel roomPanel;

    public RoomSearchDialog(Window owner, RoomManagementPanel roomPanel) {
        super(owner, "Tra cứu phòng", ModalityType.APPLICATION_MODAL);
        this.roomPanel = roomPanel;
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        RoundedPanel rootPanel = new RoundedPanel(16, Color.WHITE, ThemeColors.BORDER, 1);
        rootPanel.setLayout(new MigLayout("insets 0, wrap 1, gap 0", "[fill, 800!]", "[][][][grow,fill]"));
        rootPanel.setOpaque(false);

        // Header
        rootPanel.add(createHeader());

        // Fields Row
        rootPanel.add(createFieldsRow(), "growx");

        // Action Buttons Row
        rootPanel.add(createActionRow(), "growx");

        // Result Area
        JPanel resultWrapper = new JPanel(new MigLayout("insets 0 20 20 20", "[grow,fill]", "[grow,fill]"));
        resultWrapper.setOpaque(false);
        resultWrapper.add(createResultArea());
        rootPanel.add(resultWrapper, "grow");

        setContentPane(rootPanel);
        pack();
        setLocationRelativeTo(owner);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new MigLayout("insets 16 20 16 20", "[][grow][]", "[]")) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(235, 243, 255));
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 20, 16, 16);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.BORDER));

        JPanel iconPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeColors.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconPanel.setOpaque(false);
        JLabel iconLbl = new JLabel("", SwingConstants.CENTER);
        try {
            java.net.URL url = getClass().getResource("/kqlhotel/resources/icons/search.png");
            if (url != null) {
                javax.swing.ImageIcon icon = new javax.swing.ImageIcon(url);
                java.awt.Image img = icon.getImage().getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH);
                iconLbl.setIcon(new javax.swing.ImageIcon(img));
            } else {
                iconLbl.setText("🔍");
            }
        } catch (Exception ex) {
            iconLbl.setText("🔍");
        }
        iconLbl.setForeground(Color.WHITE);
        iconLbl.setFont(iconLbl.getFont().deriveFont(18f));
        iconPanel.add(iconLbl);

        JPanel textPanel = new JPanel(new MigLayout("insets 0, wrap 1, gap 2", "[]", "[]"));
        textPanel.setOpaque(false);
        JLabel title = new JLabel("Tra cứu phòng");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        title.setForeground(ThemeColors.TEXT_PRIMARY);
        JLabel subtitle = new JLabel("Tìm kiếm phòng theo số phòng, loại phòng và trạng thái");
        subtitle.setFont(subtitle.getFont().deriveFont(12f));
        subtitle.setForeground(ThemeColors.TEXT_MUTED);
        textPanel.add(title);
        textPanel.add(subtitle);

        JButton btnClose = new JButton("×");
        btnClose.setFont(btnClose.getFont().deriveFont(Font.BOLD, 20f));
        btnClose.setForeground(ThemeColors.TEXT_MUTED);
        btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());
        btnClose.setMargin(new Insets(0, 0, 0, 0));

        header.add(iconPanel, "w 40!, h 40!");
        header.add(textPanel, "growx, gapx 10");
        header.add(btnClose, "top");

        return header;
    }

    private JPanel createFieldsRow() {
        JPanel panel = new JPanel(new MigLayout("insets 20 20 10 20, gap 16", "[grow][grow][grow]", "[]"));
        panel.setOpaque(false);

        // Room No
        JPanel col1 = new JPanel(new MigLayout("insets 0, wrap 1, gap 8", "[grow,fill]", "[][]"));
        col1.setBackground(Color.WHITE);
        JLabel lbl1 = new JLabel("Số phòng");
        lbl1.setForeground(ThemeColors.TEXT_MUTED);
        lbl1.setFont(lbl1.getFont().deriveFont(12f));
        txtRoomNo = new JTextField();
        txtRoomNo.putClientProperty("JTextField.placeholderText", "Ví dụ: 101");
        txtRoomNo.putClientProperty("JComponent.roundRect", true);
        col1.add(lbl1);
        col1.add(txtRoomNo, "h 36!");

        // Room Type
        JPanel col2 = new JPanel(new MigLayout("insets 0, wrap 1, gap 8", "[grow,fill]", "[][]"));
        col2.setBackground(Color.WHITE);
        JLabel lbl2 = new JLabel("Loại phòng");
        lbl2.setForeground(ThemeColors.TEXT_MUTED);
        lbl2.setFont(lbl2.getFont().deriveFont(12f));
        cbRoomType = new JComboBox<>();
        cbRoomType.addItem("Tất cả loại phòng");
        java.util.List<kqlhotel.entity.RoomType> loaiPhongs = roomTypeDAO.getAll();
        for (kqlhotel.entity.RoomType rt : loaiPhongs) {
            cbRoomType.addItem(rt.getTenLoaiPhong());
        }
        cbRoomType.putClientProperty("JComponent.roundRect", true);
        col2.add(lbl2);
        col2.add(cbRoomType, "h 36!");

        // Status
        JPanel col3 = new JPanel(new MigLayout("insets 0, wrap 1, gap 8", "[grow,fill]", "[][]"));
        col3.setBackground(Color.WHITE);
        JLabel lbl3 = new JLabel("Trạng thái phòng");
        lbl3.setForeground(ThemeColors.TEXT_MUTED);
        lbl3.setFont(lbl3.getFont().deriveFont(12f));
        cbStatus = new JComboBox<>(new String[]{"Tất cả trạng thái", "Trống", "Đang sử dụng", "Bảo trì"});
        cbStatus.putClientProperty("JComponent.roundRect", true);
        col3.add(lbl3);
        col3.add(cbStatus, "h 36!");

        panel.add(col1);
        panel.add(col2);
        panel.add(col3);

        return panel;
    }

    private JPanel createActionRow() {
        JPanel panel = new JPanel(new MigLayout("insets 0 20 20 20, gap 12", "[][]", "[]"));
        panel.setOpaque(false);

        PrimaryButton btnSearch = new PrimaryButton(" Tìm kiếm");
        try {
            java.net.URL searchURL = getClass().getResource("/kqlhotel/resources/icons/search.png");
            if (searchURL != null) {
                javax.swing.ImageIcon icon = new javax.swing.ImageIcon(searchURL);
                java.awt.Image img = icon.getImage().getScaledInstance(14, 14, java.awt.Image.SCALE_SMOOTH);
                btnSearch.setIcon(new javax.swing.ImageIcon(img));
            } else {
                btnSearch.setText("🔍 Tìm kiếm");
            }
        } catch (Exception ex) {
            btnSearch.setText("🔍 Tìm kiếm");
        }
        btnSearch.setBackground(new Color(17, 24, 39));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.addActionListener(e -> performSearch());

        JButton btnReset = new JButton("Làm mới");
        btnReset.setFont(btnReset.getFont().deriveFont(13f));
        btnReset.setForeground(ThemeColors.TEXT_MUTED);
        btnReset.setBackground(Color.WHITE);
        btnReset.setFocusPainted(false);
        btnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReset.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeColors.BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)
        ));
        btnReset.addActionListener(e -> resetForm());

        panel.add(btnSearch, "h 36!");
        panel.add(btnReset, "h 36!");

        return panel;
    }

    private JPanel createResultArea() {
        RoundedPanel panel = new RoundedPanel(12, Color.WHITE, ThemeColors.BORDER, 1);
        panel.setLayout(new MigLayout("insets 0, wrap 1, gap 0", "[grow,fill]", "[][grow,fill]"));

        // Result Header
        JPanel resHeader = new JPanel(new MigLayout("insets 16 20 16 20, wrap 1, gap 2", "[]", "[]"));
        resHeader.setOpaque(false);
        resHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 245)));
        JLabel resTitle = new JLabel("Kết quả tra cứu");
        resTitle.setFont(resTitle.getFont().deriveFont(Font.BOLD, 14f));
        resTitle.setForeground(ThemeColors.TEXT_PRIMARY);
        JLabel resSub = new JLabel("Nhập tiêu chí và bấm tìm kiếm");
        resSub.setFont(resSub.getFont().deriveFont(11f));
        resSub.setForeground(ThemeColors.TEXT_PLACEHOLDER);
        resHeader.add(resTitle);
        resHeader.add(resSub);

        // Result Body (Empty state initially)
        resultContainer = new JPanel(new BorderLayout());
        resultContainer.setOpaque(false);
        resultContainer.setPreferredSize(new Dimension(0, 200));

        JPanel emptyState = new JPanel(new MigLayout("insets 40, wrap 1, gap 12", "[center]", "[]"));
        emptyState.setOpaque(false);
        JLabel icon = new JLabel("🛏");
        icon.setFont(icon.getFont().deriveFont(40f));
        icon.setForeground(new Color(200, 210, 225));
        JLabel text = new JLabel("Chưa thực hiện tìm kiếm");
        text.setFont(text.getFont().deriveFont(12f));
        text.setForeground(ThemeColors.TEXT_MUTED);
        emptyState.add(icon);
        emptyState.add(text);

        resultContainer.add(emptyState, BorderLayout.CENTER);

        panel.add(resHeader);
        panel.add(resultContainer);

        return panel;
    }

    private void performSearch() {
        resultContainer.removeAll();

        String roomQuery = txtRoomNo.getText().trim().toLowerCase();
        String typeQuery = (String) cbRoomType.getSelectedItem();
        String statusQuery = (String) cbStatus.getSelectedItem();

        List<Phong> results = new ArrayList<>();
        PhongBUS bus = roomPanel.getPhongBUS();
        List<Phong> currentRooms = roomPanel.getPhongList();

        if (currentRooms == null) currentRooms = new ArrayList<>();

        for (Phong p : currentRooms) {
            String guiStatus = bus.mapDbStatusToGuiStatus(p.getTrangThaiPhong());
            LoaiPhong lp = p.getLoaiPhong();

            boolean matchNo = roomQuery.isEmpty() || p.getMaPhong().toLowerCase().contains(roomQuery);
            boolean matchType = "Tất cả loại phòng".equals(typeQuery) || lp.getTenLoaiPhong().equals(typeQuery);
            boolean matchStatus = "Tất cả trạng thái".equals(statusQuery) || guiStatus.equals(statusQuery);

            if (matchNo && matchType && matchStatus) {
                results.add(p);
            }
        }

        if (results.isEmpty()) {
            JPanel emptyState = new JPanel(new MigLayout("insets 40, wrap 1, gap 12", "[center]", "[]"));
            emptyState.setOpaque(false);
            JLabel icon = new JLabel("🛏");
            icon.setFont(icon.getFont().deriveFont(40f));
            icon.setForeground(new Color(200, 210, 225));
            JLabel text = new JLabel("Không tìm thấy kết quả phù hợp");
            text.setFont(text.getFont().deriveFont(12f));
            text.setForeground(ThemeColors.TEXT_MUTED);
            emptyState.add(icon);
            emptyState.add(text);
            resultContainer.add(emptyState, BorderLayout.CENTER);
        } else {
            JPanel grid = new JPanel(new MigLayout("insets 12 16 16 16, wrap 2, gap 16", "[grow,fill][grow,fill]", "[]"));
            grid.setOpaque(false);
            for (Phong p : results) {
                grid.add(roomPanel.createRoomCard(p));
            }
            JScrollPane scrollPane = new JScrollPane(grid);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            resultContainer.add(scrollPane, BorderLayout.CENTER);
        }

        resultContainer.revalidate();
        resultContainer.repaint();
    }

    private void resetForm() {
        txtRoomNo.setText("");
        cbRoomType.setSelectedIndex(0);
        cbStatus.setSelectedIndex(0);

        resultContainer.removeAll();
        JPanel emptyState = new JPanel(new MigLayout("insets 40, wrap 1, gap 12", "[center]", "[]"));
        emptyState.setOpaque(false);
        JLabel icon = new JLabel("🛏");
        icon.setFont(icon.getFont().deriveFont(40f));
        icon.setForeground(new Color(200, 210, 225));
        JLabel text = new JLabel("Chưa thực hiện tìm kiếm");
        text.setFont(text.getFont().deriveFont(12f));
        text.setForeground(ThemeColors.TEXT_MUTED);
        emptyState.add(icon);
        emptyState.add(text);

        resultContainer.add(emptyState, BorderLayout.CENTER);
        resultContainer.revalidate();
        resultContainer.repaint();
    }

}
