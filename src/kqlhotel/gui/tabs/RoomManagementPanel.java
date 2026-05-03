package kqlhotel.gui.tabs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Window;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import kqlhotel.bus.room.RoomBUS;
import kqlhotel.entity.RoomType;
import kqlhotel.entity.Room;
import kqlhotel.gui.components.PrimaryButton;
import kqlhotel.gui.components.RoundedPanel;
import kqlhotel.entity.Invoice;
import kqlhotel.entity.Customer;
import kqlhotel.gui.theme.ThemeColors;
import kqlhotel.gui.utils.IconLoader;
import net.miginfocom.swing.MigLayout;

public class RoomManagementPanel extends JPanel {
    private static final Color PAGE_BG = new Color(245, 248, 252);
    private final JPanel gridContainer = new JPanel(new java.awt.GridLayout(0, 5, 16, 16));
    private final JPanel filterRow = new JPanel(new MigLayout("insets 0,gap 10", "[]", "[]"));

    private static final Color COLOR_VACANT = new Color(30, 180, 120);
    private static final Color COLOR_MAINTENANCE = new Color(230, 154, 30);
    private static final Color COLOR_OCCUPIED = new Color(239, 68, 68);

    private final RoomBUS roomBUS = new RoomBUS();
    private List<Room> roomList;
    private JLabel subtitle;
    private JPanel statsRow;

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
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(15, 23, 42));
        subtitle = new JLabel("Đang tải...");
        subtitle.setForeground(new Color(100, 116, 139));
        titlePanel.add(title);
        titlePanel.add(subtitle);

        PrimaryButton btnSearch = new PrimaryButton(" Tìm kiếm phòng");
        btnSearch.setBackground(ThemeColors.PRIMARY);
        btnSearch.setForeground(Color.WHITE);

        PrimaryButton btnAdd = new PrimaryButton("+ Thêm phòng");
        btnAdd.setBackground(new Color(17, 24, 39));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.addActionListener(e -> {
            Window owner = SwingUtilities.getWindowAncestor(this);
            kqlhotel.gui.components.AddRoomDialog dialog = new kqlhotel.gui.components.AddRoomDialog(owner, roomBUS, this::reloadData);
            dialog.setVisible(true);
        });

        PrimaryButton btnAddRoomType = new PrimaryButton("+ Thêm loại phòng");
        btnAddRoomType.setBackground(new Color(30, 41, 59));
        btnAddRoomType.setForeground(Color.WHITE);

        header.add(titlePanel);
        header.add(btnSearch, "alignx right,h 44!");
        header.add(btnAddRoomType, "h 44!");
        header.add(btnAdd, "h 44!");

        // ===== 2. Stats Row =====
        statsRow = new JPanel(new MigLayout("insets 0,gap 16", "[grow,fill][grow,fill][grow,fill]", "[]"));
        statsRow.setOpaque(false);

        // ===== 3. Filter Row =====
        filterRow.setOpaque(false);

        // ===== 4. Grid =====
        gridContainer.setOpaque(false);

        reloadData();

        class ScrollableWrapper extends JPanel implements javax.swing.Scrollable {
            public ScrollableWrapper() { super(new BorderLayout()); }
            @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
            @Override public int getScrollableUnitIncrement(java.awt.Rectangle r, int o, int d) { return 16; }
            @Override public int getScrollableBlockIncrement(java.awt.Rectangle r, int o, int d) { return 100; }
            @Override public boolean getScrollableTracksViewportWidth() { return true; }
            @Override public boolean getScrollableTracksViewportHeight() { return false; }
        }

        JPanel gridWrapper = new ScrollableWrapper();
        gridWrapper.setOpaque(false);
        gridWrapper.add(gridContainer, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(gridWrapper);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(header);
        add(statsRow);
        add(filterRow);
        add(scrollPane, "grow");
    }

    private RoundedPanel createStatCard(String label, String count, String percent, Color dotColor) {
        RoundedPanel card = new RoundedPanel(16, Color.WHITE, new Color(226, 232, 240), 1f);
        card.setLayout(new MigLayout("insets 20,wrap 1,gap 8", "[grow,fill]", "[]"));

        JPanel topRow = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));
        topRow.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setForeground(new Color(100, 116, 139));
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JPanel dot = createDot(dotColor);
        topRow.add(lbl);
        topRow.add(dot, "w 8!,h 8!");

        JPanel botRow = new JPanel(new MigLayout("insets 0,gap 10", "[][]", "[]"));
        botRow.setOpaque(false);
        JLabel countLbl = new JLabel(count);
        countLbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        countLbl.setForeground(new Color(15, 23, 42));
        JLabel pctLbl = new JLabel(percent + " tổng cộng");
        pctLbl.setForeground(new Color(148, 163, 184));
        botRow.add(countLbl, "aligny bottom");
        botRow.add(pctLbl, "aligny bottom, pad 0 0 6 0");

        card.add(topRow);
        card.add(botRow);
        return card;
    }

    private JButton createFilterBtn(String label, String filterKey, String badgeText, boolean active) {
        JButton btn = new JButton();
        btn.putClientProperty("filterLabel", label);
        btn.putClientProperty("filterKey", filterKey);
        btn.putClientProperty("filterBadge", badgeText);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        String text = "<html>" + label + " <span style='font-size:10px;'>&nbsp;" + badgeText + "&nbsp;</span></html>";
        btn.setText(text);
        updateFilterBtnStyle(btn, active);
        
        btn.addActionListener(e -> applyFilter(filterKey));
        return btn;
    }

    private void updateFilterBtnStyle(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(new Color(15, 23, 42));
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(15, 23, 42), 1),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)
            ));
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(71, 85, 105));
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)
            ));
        }
    }

    public void reloadData() {
        roomList = roomBUS.getAllRooms();
        
        long total = roomList.size();
        long tr = roomBUS.countByStatus(roomList, "Vacant");
        long dsd = roomBUS.countByStatus(roomList, "Occupied");
        long bt = roomBUS.countByStatus(roomList, "Maintenance");

        subtitle.setText(total + " phòng tổng cộng - " + tr + " phòng trống");

        statsRow.removeAll();
        statsRow.add(createStatCard("Trống", String.valueOf(tr), getPct(tr, total), COLOR_VACANT));
        statsRow.add(createStatCard("Đang sử dụng", String.valueOf(dsd), getPct(dsd, total), COLOR_OCCUPIED));
        statsRow.add(createStatCard("Bảo trì", String.valueOf(bt), getPct(bt, total), COLOR_MAINTENANCE));
        statsRow.revalidate(); statsRow.repaint();

        filterRow.removeAll();
        filterRow.add(createFilterBtn("Tất cả", "All", String.valueOf(total), true));
        filterRow.add(createFilterBtn("Trống", "Vacant", String.valueOf(tr), false));
        filterRow.add(createFilterBtn("Đang sử dụng", "Occupied", String.valueOf(dsd), false));
        filterRow.add(createFilterBtn("Bảo trì", "Maintenance", String.valueOf(bt), false));
        filterRow.revalidate(); filterRow.repaint();

        applyFilter("All");
    }

    private String getPct(long count, long total) {
        if (total == 0) return "0%";
        return Math.round((double)count/total * 100) + "%";
    }

    private void applyFilter(String filterKey) {
        gridContainer.removeAll();
        for (Room p : roomList) {
            String guiStatus = p.getStatus();
            if (filterKey.equals("All") || guiStatus.equals(filterKey)) {
                gridContainer.add(createRoomCard(p));
            }
        }
        gridContainer.revalidate();
        gridContainer.repaint();
        
        for (java.awt.Component c : filterRow.getComponents()) {
            if (c instanceof JButton) {
                JButton b = (JButton) c;
                String key = (String) b.getClientProperty("filterKey");
                updateFilterBtnStyle(b, key.equals(filterKey));
            }
        }
    }

    public JPanel createRoomCard(Room p) {
        RoomType lp = p.getRoomType();
        String guiStatus = p.getStatus();
        Color statusColor = COLOR_VACANT;
        String statusLabel = "Trống";
        
        if (guiStatus.equals("Maintenance")) {
            statusColor = COLOR_MAINTENANCE;
            statusLabel = "Bảo trì";
        } else if (guiStatus.equals("Occupied")) {
            statusColor = COLOR_OCCUPIED;
            statusLabel = "Đang sử dụng";
        }

        Invoice activeInv = null;
        Customer activeCust = null;
        if (guiStatus.equals("Occupied")) {
            activeInv = roomBUS.getActiveInvoiceForRoom(p.getRoomId());
            if (activeInv != null) {
                activeCust = roomBUS.getCustomerByMaKH(activeInv.getMaKhachHang());
            }
        }

        RoundedPanel card = new RoundedPanel(16, Color.WHITE, new Color(226, 232, 240), 1f);
        card.setLayout(new MigLayout("wrap 1,insets 16", "[grow,fill]", "[]"));

        JPanel topRow = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));
        topRow.setOpaque(false);
        JLabel roomNo = new JLabel(p.getRoomId());
        roomNo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        roomNo.setForeground(new Color(15, 23, 42));
        topRow.add(roomNo);
        
        JPanel badgeStatus = new RoundedPanel(16, statusColor, null, 0);
        badgeStatus.setLayout(new BorderLayout());
        badgeStatus.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        JLabel lblStat = new JLabel(statusLabel);
        lblStat.setForeground(Color.WHITE);
        lblStat.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badgeStatus.add(lblStat);
        topRow.add(badgeStatus);

        JPanel midRow = new JPanel(new MigLayout("insets 0,wrap 1,gap 2", "[]", "[]"));
        midRow.setOpaque(false);
        JLabel floor = new JLabel("Tầng " + p.getFloor());
        floor.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        floor.setForeground(new Color(100, 116, 139));
        JLabel type = new JLabel(lp.getRoomTypeName());
        type.setFont(new Font("Segoe UI", Font.BOLD, 12));
        type.setForeground(ThemeColors.PRIMARY);
        midRow.add(floor);
        midRow.add(type);

        JPanel infoRow = new JPanel(new MigLayout("insets 0,gap 10", "[][]", "[]"));
        infoRow.setOpaque(false);
        
        String occupantStr = (activeCust != null) ? activeCust.getHoTenKH() : (lp.getSucChua() + " khách");
        JLabel lblGuest = new JLabel(" " + occupantStr);
        lblGuest.setIcon(IconLoader.loadIcon("khachHang.png", 14, 14));
        lblGuest.setForeground(new Color(130, 145, 170));
        lblGuest.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoRow.add(lblGuest);

        JLabel lblArea = new JLabel(" " + lp.getDienTich() + "m²");
        lblArea.setIcon(IconLoader.loadIcon("location.png", 14, 14));
        lblArea.setForeground(new Color(130, 145, 170));
        lblArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoRow.add(lblArea);

        JPanel priceGroup = new JPanel(new MigLayout("insets 0,wrap 1,gap 0", "[]", "[]"));
        priceGroup.setOpaque(false);
        JLabel priceLbl = new JLabel(String.format("%,.0fđ", lp.getGiaPhong()));
        priceLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        priceLbl.setForeground(new Color(30, 50, 80));
        priceGroup.add(priceLbl);
        priceGroup.add(new JLabel("/đêm") {{ setFont(new Font("Segoe UI", Font.PLAIN, 11)); setForeground(new Color(150, 165, 190)); }});

        card.add(topRow, "growx");
        card.add(midRow, "gapy 12 0,growx");
        card.add(infoRow, "gapy 8 0");
        card.add(priceGroup, "gapy 12 0");

        if (guiStatus.equals("Occupied")) {
            final Invoice finalInv = activeInv;
            final Customer finalCust = activeCust;
            JButton btnDetail = new JButton("Khách & Hóa đơn");
            btnDetail.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btnDetail.setBackground(new Color(41, 121, 255));
            btnDetail.setForeground(Color.WHITE);
            btnDetail.setFocusPainted(false);
            btnDetail.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnDetail.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(41, 121, 255), 1, true),
                BorderFactory.createEmptyBorder(8, 0, 8, 0)
            ));
            
            btnDetail.addActionListener(e -> {
                Window owner = SwingUtilities.getWindowAncestor(this);
                Runnable onCheckout = () -> {
                    java.awt.Container c = this;
                    while (c != null && !(c instanceof kqlhotel.gui.AppFrame)) c = c.getParent();
                    if (c instanceof kqlhotel.gui.AppFrame) {
                        ((kqlhotel.gui.AppFrame) c).navigateToCheckoutWithRoom(p.getRoomId());
                    }
                };
                kqlhotel.gui.components.RoomDetailDialog dialog = new kqlhotel.gui.components.RoomDetailDialog(
                    owner, p, finalInv, finalCust, onCheckout
                );
                dialog.setVisible(true);
            });
            card.add(btnDetail, "gapy 8 0, growx");
        }

        return card;
    }

    private JPanel createDot(Color color) {
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
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

    public RoomBUS getRoomBUS() { return roomBUS; }
    public List<Room> getRoomList() { return roomList; }
}
