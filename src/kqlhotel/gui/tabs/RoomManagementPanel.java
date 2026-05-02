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
import kqlhotel.bus.room.RoomBUS;
import kqlhotel.entity.RoomType;
import kqlhotel.entity.Room;
import kqlhotel.gui.components.PrimaryButton;
import kqlhotel.gui.components.RoundedPanel;
import kqlhotel.dao.invoice.InvoiceDAO;
import kqlhotel.dao.customer.CustomerDAO;
import kqlhotel.entity.Invoice;
import kqlhotel.entity.Customer;
import kqlhotel.gui.theme.ThemeColors;
import net.miginfocom.swing.MigLayout;

public class RoomManagementPanel extends JPanel {
    private static final Color PAGE_BG = new Color(245, 248, 252);
    private final JPanel gridContainer = new JPanel(new java.awt.GridLayout(0, 5, 16, 16));
    private final JPanel filterRow = new JPanel(new MigLayout("insets 0,gap 10", "[]", "[]"));

    // Status colors
    private static final Color COLOR_VACANT = new Color(30, 180, 120);
    private static final Color COLOR_MAINTENANCE = new Color(230, 154, 30);
    private static final Color COLOR_OCCUPIED = new Color(239, 68, 68);

    private final RoomBUS roomBUS = new RoomBUS();
    private List<Room> roomList;
    private JLabel subtitle;
    private JPanel statsRow;

    public List<Room> getRoomList() { return roomList; }
    public RoomBUS getRoomBUS() { return roomBUS; }

    public RoomManagementPanel() {
        setOpaque(false);
        setBackground(PAGE_BG);
        setLayout(new MigLayout("insets 24,gap 20,wrap 1", "[grow,fill]", "[][][][grow,fill]"));

        // ===== 1. Header =====
        JPanel header = new JPanel(new MigLayout("insets 0", "[][grow][][]", "[]"));
        header.setOpaque(false);

        JPanel titlePanel = new JPanel(new MigLayout("insets 0,wrap 1,gap 2", "[]", "[]"));
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("Room Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(15, 23, 42));
        subtitle = new JLabel("Loading...");
        subtitle.setForeground(new Color(100, 116, 139));
        titlePanel.add(title);
        titlePanel.add(subtitle);

        PrimaryButton btnSearch = new PrimaryButton(" Search Room");
        btnSearch.setBackground(ThemeColors.PRIMARY);
        btnSearch.setForeground(Color.WHITE);
        btnSearch.addActionListener(e -> {
            Window owner = SwingUtilities.getWindowAncestor(this);
            // This dialog might need updating too, but for now we keep the call
            // kqlhotel.gui.components.RoomSearchDialog dialog = new kqlhotel.gui.components.RoomSearchDialog(owner, this);
            // dialog.setVisible(true);
        });

        PrimaryButton btnAdd = new PrimaryButton("+ Add Room");
        btnAdd.setBackground(new Color(17, 24, 39));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.addActionListener(e -> {
            Window owner = SwingUtilities.getWindowAncestor(this);
            // dialog needs update
        });

        PrimaryButton btnAddRoomType = new PrimaryButton("+ Add Room Type");
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
        JLabel pctLbl = new JLabel(percent + " of total");
        pctLbl.setForeground(new Color(148, 163, 184));
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
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        String text = "<html>" + label + " <span style='font-size:10px;'>&nbsp;" + badgeText + "&nbsp;</span></html>";
        btn.setText(text);
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
        
        btn.addActionListener(e -> applyFilter(label));
        return btn;
    }

    public void reloadData() {
        roomList = roomBUS.getAllRooms();
        
        long total = roomList.size();
        long tr = roomBUS.countByStatus(roomList, "Trong");
        long dsd = roomBUS.countByStatus(roomList, "DangSuDung");
        long bt = roomBUS.countByStatus(roomList, "BaoTri");

        subtitle.setText(total + " rooms total - " + tr + " vacant rooms");

        statsRow.removeAll();
        statsRow.add(createStatCard("Vacant", String.valueOf(tr), getPct(tr, total), COLOR_VACANT));
        statsRow.add(createStatCard("Occupied", String.valueOf(dsd), getPct(dsd, total), COLOR_OCCUPIED));
        statsRow.add(createStatCard("Maintenance", String.valueOf(bt), getPct(bt, total), COLOR_MAINTENANCE));
        statsRow.revalidate(); statsRow.repaint();

        filterRow.removeAll();
        filterRow.add(createFilterBtn("All", String.valueOf(total), true));
        filterRow.add(createFilterBtn("Vacant", String.valueOf(tr), false));
        filterRow.add(createFilterBtn("Occupied", String.valueOf(dsd), false));
        filterRow.add(createFilterBtn("Maintenance", String.valueOf(bt), false));
        filterRow.revalidate(); filterRow.repaint();

        applyFilter("All");
    }

    private String getPct(long count, long total) {
        if (total == 0) return "0%";
        return Math.round((double)count/total * 100) + "%";
    }

    private void applyFilter(String filter) {
        gridContainer.removeAll();
        for (Room p : roomList) {
            String guiStatus = roomBUS.mapDbStatusToGuiStatus(p.getStatus());
            if (filter.equals("All") || guiStatus.equals(filter)) {
                gridContainer.add(createRoomCard(p));
            }
        }
        gridContainer.revalidate();
        gridContainer.repaint();
    }

    public JPanel createRoomCard(Room p) {
        RoomType lp = p.getRoomType();
        String guiStatus = roomBUS.mapDbStatusToGuiStatus(p.getStatus());
        Color statusColor = COLOR_VACANT;
        if (guiStatus.equals("Maintenance")) statusColor = COLOR_MAINTENANCE;
        else if (guiStatus.equals("Occupied")) statusColor = COLOR_OCCUPIED;

        RoundedPanel card = new RoundedPanel(16, Color.WHITE, new Color(226, 232, 240), 1f);
        card.setLayout(new MigLayout("wrap 1,insets 16", "[grow,fill]", "[]"));

        JLabel roomNo = new JLabel(p.getRoomId());
        roomNo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        roomNo.setForeground(new Color(15, 23, 42));
        
        JLabel floor = new JLabel("Floor " + p.getFloor());
        floor.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        floor.setForeground(new Color(100, 116, 139));

        card.add(roomNo);
        card.add(floor);
        card.add(new JLabel(lp.getRoomTypeName()) {{ setForeground(ThemeColors.PRIMARY); setFont(new Font("Segoe UI", Font.BOLD, 12)); }});
        final Color finalStatusColor = statusColor;
        card.add(new JLabel(guiStatus) {{ setForeground(finalStatusColor); setFont(new Font("Segoe UI", Font.BOLD, 12)); }});
        card.add(new JLabel(String.format("%,.0f USD", lp.getPrice())) {{ setFont(new Font("Segoe UI", Font.BOLD, 14)); }});

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
}
