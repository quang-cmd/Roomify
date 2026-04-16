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
import kqlhotel.bus.room.PhongBUS;
import kqlhotel.entity.LoaiPhong;
import kqlhotel.entity.Phong;
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

    private final PhongBUS phongBUS = new PhongBUS();
    private List<kqlhotel.entity.Phong> phongList;
    private JLabel subtitle;
    private JPanel statsRow;

    public List<kqlhotel.entity.Phong> getPhongList() { return phongList; }
    public PhongBUS getPhongBUS() { return phongBUS; }

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
        subtitle = new JLabel("Đang tải...");
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
        btnAdd.addActionListener(e -> {
            Window owner = SwingUtilities.getWindowAncestor(this);
            kqlhotel.gui.components.AddRoomDialog dialog = new kqlhotel.gui.components.AddRoomDialog(owner, phongBUS, this::reloadData);
            dialog.setVisible(true);
        });

        header.add(titlePanel);
        header.add(btnSearch, "alignx right,h 44!");
        header.add(btnAdd, "h 44!");

        // ===== 2. Stats Row =====
        statsRow = new JPanel(new MigLayout("insets 0,gap 16", "[grow,fill][grow,fill][grow,fill][grow,fill]", "[]"));
        statsRow.setOpaque(false);

        // ===== 3. Filter Row =====
        filterRow.setOpaque(false);

        // ===== 4. Grid =====
        gridContainer.setOpaque(false);

        reloadData(); // Load real data

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

    public void reloadData() {
        phongList = phongBUS.getAllRooms();
        
        long total = phongList.size();
        long tr = phongBUS.countByStatus(phongList, "Trong");
        long dd = phongBUS.countByStatus(phongList, "DaDat");
        long bt = phongBUS.countByStatus(phongList, "BaoTri");
        long dn = phongBUS.countByStatus(phongList, "DangDon");

        subtitle.setText(total + " phòng tổng cộng - " + tr + " phòng trống");

        // Update Stats Row
        statsRow.removeAll();
        statsRow.add(createStatCard("Trống", String.valueOf(tr), getPct(tr, total), COLOR_TRONG));
        statsRow.add(createStatCard("Đã đặt", String.valueOf(dd), getPct(dd, total), COLOR_DADAT));
        statsRow.add(createStatCard("Bảo trì", String.valueOf(bt), getPct(bt, total), COLOR_BAOTRI));
        statsRow.add(createStatCard("Đang dọn", String.valueOf(dn), getPct(dn, total), COLOR_DANGDON));
        statsRow.revalidate(); statsRow.repaint();

        // Update Filter Row
        filterRow.removeAll();
        filterRow.add(createFilterBtn("Tất cả", String.valueOf(total), true));
        filterRow.add(createFilterBtn("Trống", String.valueOf(tr), false));
        filterRow.add(createFilterBtn("Đã đặt", String.valueOf(dd), false));
        filterRow.add(createFilterBtn("Bảo trì", String.valueOf(bt), false));
        filterRow.add(createFilterBtn("Đang dọn", String.valueOf(dn), false));
        filterRow.revalidate(); filterRow.repaint();

        applyFilter("Tất cả");
    }

    private String getPct(long count, long total) {
        if (total == 0) return "0%";
        return (int)((double)count/total * 100) + "%";
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
        for (kqlhotel.entity.Phong p : phongList) {
            String guiStatus = phongBUS.mapDbStatusToGuiStatus(p.getTrangThaiPhong());
            if (filter.equals("Tất cả") || guiStatus.equals(filter)) {
                gridContainer.add(createRoomCard(p));
            }
        }
        gridContainer.revalidate();
        gridContainer.repaint();
    }

    public JPanel createRoomCard(kqlhotel.entity.Phong p) {
        kqlhotel.entity.LoaiPhong lp = p.getLoaiPhong();
        String guiStatus = phongBUS.mapDbStatusToGuiStatus(p.getTrangThaiPhong());
        Color statusColor = COLOR_TRONG;
        if (guiStatus.equals("Đã đặt")) statusColor = COLOR_DADAT;
        else if (guiStatus.equals("Bảo trì")) statusColor = COLOR_BAOTRI;
        else if (guiStatus.equals("Đang dọn")) statusColor = COLOR_DANGDON;

        RoundedPanel card = new RoundedPanel(16, Color.WHITE, new Color(230, 235, 245), 1f);
        card.setLayout(new MigLayout("wrap 1,insets 16", "[grow,fill]", "[]"));

        // Header
        JPanel topRow = new JPanel(new MigLayout("insets 0", "[][grow,right]", "[]"));
        topRow.setOpaque(false);
        JPanel numGroup = new JPanel(new MigLayout("insets 0,wrap 1,gap 2", "[]", "[]"));
        numGroup.setOpaque(false);
        JLabel roomNo = new JLabel(p.getMaPhong());
        roomNo.setFont(roomNo.getFont().deriveFont(Font.BOLD, 18f));
        roomNo.setForeground(new Color(30, 50, 80));
        JLabel floor = new JLabel("Tầng " + p.getTang());
        floor.setFont(floor.getFont().deriveFont(11f));
        floor.setForeground(new Color(130, 145, 170));
        numGroup.add(roomNo); numGroup.add(floor);
        topRow.add(numGroup);
        topRow.add(new JLabel("🛏") {{ setFont(getFont().deriveFont(20f)); setForeground(new Color(160, 175, 200)); }});

        // Type & Status
        JPanel midRow = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));
        midRow.setOpaque(false);
        midRow.add(makeBadge(lp.getTenLoaiPhong(), new Color(240, 244, 255), new Color(80, 120, 200)));
        midRow.add(makeBadge("• " + guiStatus, new Color(statusColor.getRed(), statusColor.getGreen(), statusColor.getBlue(), 25), statusColor));

        // Info
        JPanel infoRow = new JPanel(new MigLayout("insets 0,gap 12", "[][]", "[]"));
        infoRow.setOpaque(false);
        infoRow.add(new JLabel("👤 " + lp.getSucChuaToiDa() + " khách") {{ setForeground(new Color(130, 145, 170)); setFont(getFont().deriveFont(11f)); }});
        infoRow.add(new JLabel("⛶ " + lp.getDienTich() + "m²") {{ setForeground(new Color(130, 145, 170)); setFont(getFont().deriveFont(11f)); }});

        // Price
        JPanel priceGroup = new JPanel(new MigLayout("insets 0,wrap 1,gap 0", "[]", "[]"));
        priceGroup.setOpaque(false);
        JLabel priceLbl = new JLabel(String.format("%,.0fđ", lp.getGiaPhong()));
        priceLbl.setFont(priceLbl.getFont().deriveFont(Font.BOLD, 14f));
        priceLbl.setForeground(new Color(30, 50, 80));
        priceGroup.add(priceLbl);
        priceGroup.add(new JLabel("/đêm") {{ setFont(getFont().deriveFont(11f)); setForeground(new Color(150, 165, 190)); }});

        card.add(topRow, "growx");
        card.add(midRow, "gapy 12 0,growx");
        card.add(infoRow, "gapy 8 0");
        card.add(priceGroup, "gapy 12 0");

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

}
