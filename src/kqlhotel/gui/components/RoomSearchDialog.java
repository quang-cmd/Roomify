package kqlhotel.gui.components;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;
import kqlhotel.gui.theme.ThemeColors;
import kqlhotel.gui.tabs.RoomManagementPanel;

public class RoomSearchDialog extends JDialog {

    private JTextField txtRoomNo;
    private JComboBox<String> cbRoomType;
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
        JLabel iconLbl = new JLabel("🔍", SwingConstants.CENTER);
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
        cbRoomType = new JComboBox<>(new String[]{"Tất cả loại phòng", "Deluxe", "Grand Premium 1", "Grand Premium 2", "Suite"});
        cbRoomType.putClientProperty("JComponent.roundRect", true);
        col2.add(lbl2);
        col2.add(cbRoomType, "h 36!");

        // Status
        JPanel col3 = new JPanel(new MigLayout("insets 0, wrap 1, gap 8", "[grow,fill]", "[][]"));
        col3.setBackground(Color.WHITE);
        JLabel lbl3 = new JLabel("Trạng thái phòng");
        lbl3.setForeground(ThemeColors.TEXT_MUTED);
        lbl3.setFont(lbl3.getFont().deriveFont(12f));
        cbStatus = new JComboBox<>(new String[]{"Tất cả trạng thái", "Trống", "Đã đặt", "Bảo trì", "Đang dọn"});
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

        PrimaryButton btnSearch = new PrimaryButton("🔍 Tìm kiếm");
        btnSearch.setBackground(new Color(17, 24, 39));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.addActionListener(e -> performSearch());

        JButton btnReset = new JButton("🔄 Làm mới");
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

        List<RoomManagementPanel.RoomData> results = new ArrayList<>();
        for (RoomManagementPanel.RoomData data : roomPanel.mockData) {
            boolean matchNo = roomQuery.isEmpty() || data.roomNo.toLowerCase().contains(roomQuery);
            boolean matchType = "Tất cả loại phòng".equals(typeQuery) || data.roomType.equals(typeQuery);
            boolean matchStatus = "Tất cả trạng thái".equals(statusQuery) || data.status.equals(statusQuery);
            if (matchNo && matchType && matchStatus) {
                results.add(data);
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
            for (RoomManagementPanel.RoomData data : results) {
                grid.add(roomPanel.createRoomCard(data));
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

    private JPanel createRoomCard(RoomData data) {
        RoundedPanel card = new RoundedPanel(16, Color.WHITE, new Color(230, 235, 245), 1f);
        card.setLayout(new MigLayout("wrap 1,insets 16", "[grow,fill]", "[]"));

        // ===== Header (Number & Bed icon) =====
        JPanel topRow = new JPanel(new MigLayout("insets 0", "[][grow,right]", "[]"));
        topRow.setOpaque(false);
        
        JPanel numGroup = new JPanel(new MigLayout("insets 0,wrap 1,gap 2", "[]", "[]"));
        numGroup.setOpaque(false);
        JLabel roomNo = new JLabel(data.roomNo);
        roomNo.setFont(roomNo.getFont().deriveFont(Font.BOLD, 18f));
        roomNo.setForeground(new Color(30, 50, 80));
        JLabel floor = new JLabel(data.floor);
        floor.setFont(floor.getFont().deriveFont(11f));
        floor.setForeground(new Color(130, 145, 170));
        numGroup.add(roomNo);
        numGroup.add(floor);

        JLabel bedIcon = new JLabel("🛏");
        bedIcon.setFont(bedIcon.getFont().deriveFont(20f));
        bedIcon.setForeground(new Color(160, 175, 200));

        topRow.add(numGroup);
        topRow.add(bedIcon);

        // ===== Type & Status =====
        JPanel midRow = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));
        midRow.setOpaque(false);
        
        Color badgeBg = new Color(data.statusColor.getRed(), data.statusColor.getGreen(), data.statusColor.getBlue(), 25);
        JPanel typeBadge = makeBadge(data.roomType, new Color(240, 244, 255), new Color(80, 120, 200));
        JPanel statusBadge = makeBadge("• " + data.status, badgeBg, data.statusColor);
        
        midRow.add(typeBadge, "left");
        midRow.add(statusBadge, "right");

        // ===== Info Row (Guests & Area) =====
        JPanel infoRow = new JPanel(new MigLayout("insets 0,gap 12", "[][]", "[]"));
        infoRow.setOpaque(false);
        JLabel guests = new JLabel("👤 " + data.guests);
        guests.setForeground(new Color(130, 145, 170));
        guests.setFont(guests.getFont().deriveFont(11f));
        JLabel area = new JLabel("⛶ " + data.area);
        area.setForeground(new Color(130, 145, 170));
        area.setFont(area.getFont().deriveFont(11f));
        infoRow.add(guests);
        infoRow.add(area);

        // ===== Footer (Price & Action) =====
        JPanel botRow = new JPanel(new MigLayout("insets 0", "[grow,fill][]", "[]"));
        botRow.setOpaque(false);
        
        JPanel priceGroup = new JPanel(new MigLayout("insets 0,wrap 1,gap 0", "[]", "[]"));
        priceGroup.setOpaque(false);
        JLabel priceLbl = new JLabel(data.price);
        priceLbl.setFont(priceLbl.getFont().deriveFont(Font.BOLD, 14f));
        priceLbl.setForeground(new Color(30, 50, 80));
        JLabel night = new JLabel("/đêm");
        night.setFont(night.getFont().deriveFont(11f));
        night.setForeground(new Color(150, 165, 190));
        priceGroup.add(priceLbl);
        priceGroup.add(night);

        botRow.add(priceGroup, "aligny center");
        
        // Buttons
        if (data.status.equals("Trống")) {
            JButton btn = new JButton("✎ Chi tiết");
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(100, 120, 150));
            btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            botRow.add(btn, "h 32!");
        } else if (data.status.equals("Đã đặt") || data.status.equals("Đang dọn")) {
            PrimaryButton btn = new PrimaryButton("👤 Khách & Hóa đơn");
            btn.setBackground(ThemeColors.PRIMARY);
            btn.setForeground(Color.WHITE);
            btn.addActionListener(e -> {
                // Show room detail dialog
                JOptionPane.showMessageDialog(this, "Chi tiết phòng " + data.roomNo, "Thông tin", JOptionPane.INFORMATION_MESSAGE);
            });
            botRow.add(btn, "span 2,growx,h 36!,gapy 6 0");
        } else {
            JButton btn = new JButton("Chi tiết");
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(100, 120, 150));
            btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            botRow.add(btn, "h 32!");
        }

        card.add(topRow, "growx");
        card.add(midRow, "gapy 12 0,growx");
        card.add(infoRow, "gapy 8 0");
        
        if (data.status.equals("Đã đặt") || data.status.equals("Đang dọn")) {
            card.add(priceGroup, "gapy 12 0");
            card.add(botRow, "gapy 4 0,growx");
        } else {
            card.add(botRow, "gapy 12 0,growx");
        }

        return card;
    }

    private JPanel makeBadge(String text, Color bg, Color fg) {
        JPanel badge = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setForeground(fg);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 10f));
        badge.add(lbl);
        return badge;
    }

    private static final class RoomData {
        final String roomNo, floor, roomType, status, guests, area, price;
        final Color statusColor;

        RoomData(String roomNo, String floor, String roomType, String status, String guests, String area, String price, Color color) {
            this.roomNo = roomNo; this.floor = floor; this.roomType = roomType;
            this.status = status; this.guests = guests; this.area = area;
            this.price = price; this.statusColor = color;
        }
    }
}
