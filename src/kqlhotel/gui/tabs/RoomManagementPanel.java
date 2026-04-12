package kqlhotel.gui.tabs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import kqlhotel.gui.components.PrimaryButton;
import kqlhotel.gui.components.RoundedPanel;
import kqlhotel.gui.theme.ThemeColors;
import net.miginfocom.swing.MigLayout;

public class RoomManagementPanel extends JPanel {
    private static final Color PAGE_BG = new Color(245, 248, 252);
    private final JPanel gridContainer = new JPanel(new java.awt.GridLayout(0, 5, 16, 16));
    private final JPanel filterRow = new JPanel(new MigLayout("insets 0,gap 10", "[]", "[]"));

    // Status colors
    private static final Color COLOR_TRONG = new Color(30, 180, 120);
    private static final Color COLOR_DADAT = new Color(49, 130, 206);
    private static final Color COLOR_BAOTRI = new Color(230, 154, 30);
    private static final Color COLOR_DANGDON = new Color(143, 97, 255);

    public final List<RoomData> mockData = Arrays.asList(
        new RoomData("101", "Tầng 1", "Deluxe", "Trống", "2 khách", "28m²", "1.200.000đ", COLOR_TRONG),
        new RoomData("102", "Tầng 1", "Deluxe", "Đã đặt", "2 khách", "28m²", "1.200.000đ", COLOR_DADAT),
        new RoomData("103", "Tầng 1", "Deluxe", "Bảo trì", "2 khách", "28m²", "1.200.000đ", COLOR_BAOTRI),
        new RoomData("104", "Tầng 1", "Deluxe", "Trống", "2 khách", "28m²", "1.200.000đ", COLOR_TRONG),
        new RoomData("105", "Tầng 1", "Deluxe", "Đang dọn", "2 khách", "28m²", "1.200.000đ", COLOR_DANGDON),
        new RoomData("201", "Tầng 2", "Grand Premium 1", "Trống", "3 khách", "40m²", "2.200.000đ", COLOR_TRONG),
        new RoomData("202", "Tầng 2", "Grand Premium 1", "Đã đặt", "3 khách", "40m²", "2.200.000đ", COLOR_DADAT),
        new RoomData("203", "Tầng 2", "Grand Premium 1", "Trống", "3 khách", "40m²", "2.200.000đ", COLOR_TRONG),
        new RoomData("204", "Tầng 2", "Grand Premium 1", "Đã đặt", "3 khách", "40m²", "2.200.000đ", COLOR_DADAT),
        new RoomData("301", "Tầng 3", "Grand Premium 2", "Trống", "4 khách", "55m²", "3.200.000đ", COLOR_TRONG),
        new RoomData("302", "Tầng 3", "Grand Premium 2", "Bảo trì", "4 khách", "55m²", "3.200.000đ", COLOR_BAOTRI),
        new RoomData("303", "Tầng 3", "Grand Premium 2", "Trống", "4 khách", "55m²", "3.200.000đ", COLOR_TRONG),
        new RoomData("401", "Tầng 4", "Suite", "Trống", "4 khách", "85m²", "5.500.000đ", COLOR_TRONG),
        new RoomData("402", "Tầng 4", "Suite", "Đã đặt", "4 khách", "85m²", "5.500.000đ", COLOR_DADAT),
        new RoomData("403", "Tầng 4", "Suite", "Trống", "4 khách", "85m²", "5.500.000đ", COLOR_TRONG)
    );

    public RoomManagementPanel() {
        setOpaque(false);
        setBackground(PAGE_BG);
        setLayout(new MigLayout("insets 24,gap 20,wrap 1", "[grow,fill]", "[][][][grow,fill]"));

        // ===== 1. Header =====
        JPanel header = new JPanel(new MigLayout("insets 0", "[][grow][][]", "[]"));
        header.setOpaque(false);

        JPanel titlePanel = new JPanel(new MigLayout("insets 0,wrap 1,gap 2", "[]", "[]"));
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("Quản lý phòng");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        title.setForeground(new Color(24, 40, 66));
        JLabel subtitle = new JLabel("15 phòng tổng cộng - 8 phòng trống");
        subtitle.setForeground(new Color(150, 165, 190));
        titlePanel.add(title);
        titlePanel.add(subtitle);

        PrimaryButton btnSearch = new PrimaryButton("🔍 Tra cứu phòng");
        btnSearch.setBackground(ThemeColors.PRIMARY);
        btnSearch.setForeground(Color.WHITE);
        btnSearch.addActionListener(e -> {
            Window owner = SwingUtilities.getWindowAncestor(this);
            kqlhotel.gui.components.RoomSearchDialog dialog = new kqlhotel.gui.components.RoomSearchDialog(owner, this);
            dialog.setVisible(true);
        });

        PrimaryButton btnAdd = new PrimaryButton("+ Thêm phòng");
        btnAdd.setBackground(new Color(17, 24, 39));
        btnAdd.setForeground(Color.WHITE);

        header.add(titlePanel);
        header.add(btnSearch, "alignx right,h 44!");
        header.add(btnAdd, "h 44!");

        // ===== 2. Stats Row =====
        JPanel statsRow = new JPanel(new MigLayout("insets 0,gap 16", "[grow,fill][grow,fill][grow,fill][grow,fill]", "[]"));
        statsRow.setOpaque(false);
        statsRow.add(createStatCard("Trống", "8", "53%", COLOR_TRONG));
        statsRow.add(createStatCard("Đã đặt", "4", "27%", COLOR_DADAT));
        statsRow.add(createStatCard("Bảo trì", "2", "13%", COLOR_BAOTRI));
        statsRow.add(createStatCard("Đang dọn", "1", "7%", COLOR_DANGDON));

        // ===== 3. Filter Row =====
        filterRow.setOpaque(false);
        filterRow.add(createFilterBtn("Tất cả", "15", true));
        filterRow.add(createFilterBtn("Trống", "8", false));
        filterRow.add(createFilterBtn("Đã đặt", "4", false));
        filterRow.add(createFilterBtn("Bảo trì", "2", false));
        filterRow.add(createFilterBtn("Đang dọn", "1", false));

        // ===== 4. Grid =====
        gridContainer.setOpaque(false);
        for (RoomData data : mockData) {
            gridContainer.add(createRoomCard(data));
        }

        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setOpaque(false);
        gridWrapper.add(gridContainer, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(gridWrapper);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // ===== Assemble =====
        add(header);
        add(statsRow);
        add(filterRow);
        add(scrollPane, "grow");
    }

    private RoundedPanel createStatCard(String label, String count, String percent, Color dotColor) {
        RoundedPanel card = new RoundedPanel(16, Color.WHITE, new Color(225, 231, 245), 1f);
        card.setLayout(new MigLayout("insets 20,wrap 1,gap 8", "[grow,fill]", "[]"));

        JPanel topRow = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));
        topRow.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setForeground(new Color(130, 145, 170));
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 13f));
        JPanel dot = createDot(dotColor);
        topRow.add(lbl);
        topRow.add(dot, "w 8!,h 8!");

        JPanel botRow = new JPanel(new MigLayout("insets 0,gap 10", "[][]", "[]"));
        botRow.setOpaque(false);
        JLabel countLbl = new JLabel(count);
        countLbl.setFont(countLbl.getFont().deriveFont(Font.BOLD, 28f));
        countLbl.setForeground(new Color(24, 40, 66));
        JLabel pctLbl = new JLabel(percent + " tổng phòng");
        pctLbl.setForeground(new Color(150, 165, 190));
        botRow.add(countLbl, "aligny bottom");
        botRow.add(pctLbl, "aligny bottom, pad 0 0 6 0");

        card.add(topRow);
        card.add(botRow);
        return card;
    }

    private JButton createFilterBtn(String label, String badgeText, boolean active) {
        JButton btn = new JButton();
        btn.putClientProperty("filterLabel", label);
        btn.putClientProperty("filterBadge", badgeText);
        btn.setFont(btn.getFont().deriveFont(13f));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        String text = "<html>" + label + " <span style='color:" 
                    + (active ? "#A0B0E0" : "#A0B0C0") + ";font-size:10px;'>&nbsp;" 
                    + badgeText + "&nbsp;</span></html>";
        btn.setText(text);
        if (active) {
            btn.setBackground(new Color(18, 35, 67));
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(18, 35, 67), 1),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)
            ));
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(100, 120, 150));
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 230, 245), 1),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)
            ));
        }
        
        btn.addActionListener(e -> applyFilter(label));
        
        return btn;
    }

    private void applyFilter(String filter) {
        for (int i = 0; i < filterRow.getComponentCount(); i++) {
            if (filterRow.getComponent(i) instanceof JButton) {
                JButton btn = (JButton) filterRow.getComponent(i);
                String label = (String) btn.getClientProperty("filterLabel");
                String badge = (String) btn.getClientProperty("filterBadge");
                boolean active = label.equals(filter);
                
                String text = "<html>" + label + " <span style='color:" 
                            + (active ? "#A0B0E0" : "#A0B0C0") + ";font-size:10px;'>&nbsp;" 
                            + badge + "&nbsp;</span></html>";
                btn.setText(text);
                if (active) {
                    btn.setBackground(new Color(18, 35, 67));
                    btn.setForeground(Color.WHITE);
                    btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(18, 35, 67), 1),
                        BorderFactory.createEmptyBorder(6, 16, 6, 16)
                    ));
                } else {
                    btn.setBackground(Color.WHITE);
                    btn.setForeground(new Color(100, 120, 150));
                    btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(220, 230, 245), 1),
                        BorderFactory.createEmptyBorder(6, 16, 6, 16)
                    ));
                }
            }
        }

        gridContainer.removeAll();
        for (RoomData data : mockData) {
            if (filter.equals("Tất cả") || data.status.equals(filter)) {
                gridContainer.add(createRoomCard(data));
            }
        }
        gridContainer.revalidate();
        gridContainer.repaint();
    }

    public JPanel createRoomCard(RoomData data) {
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
            // Hiển thị nút "Chi tiết" nhỏ
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
                Window owner = SwingUtilities.getWindowAncestor(this);
                kqlhotel.gui.components.RoomDetailDialog dialog = new kqlhotel.gui.components.RoomDetailDialog(owner, data.roomNo, data.roomType, data.floor, data.price);
                dialog.setVisible(true);
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

    private JPanel createDot(Color color) {
        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        return dot;
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

    public static final class RoomData {
        public final String roomNo, floor, roomType, status, guests, area, price;
        public final Color statusColor;

        public RoomData(String roomNo, String floor, String roomType, String status, String guests, String area, String price, Color color) {
            this.roomNo = roomNo; this.floor = floor; this.roomType = roomType;
            this.status = status; this.guests = guests; this.area = area;
            this.price = price; this.statusColor = color;
        }
    }
}
