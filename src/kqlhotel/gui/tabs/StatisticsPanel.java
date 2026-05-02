package kqlhotel.gui.tabs;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import kqlhotel.bus.statistics.StatisticsBUS;
import kqlhotel.entity.statistics.KpiSummary;
import kqlhotel.entity.statistics.OccupancyPoint;
import kqlhotel.entity.statistics.RecentBooking;
import kqlhotel.entity.statistics.RevenuePoint;
import kqlhotel.entity.statistics.RoomTypeShare;
import kqlhotel.gui.components.PrimaryButton;
import kqlhotel.gui.components.RoundedPanel;
import kqlhotel.gui.theme.ThemeColors;
import net.miginfocom.swing.MigLayout;

public class StatisticsPanel extends JPanel {
    private final StatisticsBUS bus = new StatisticsBUS();
    private final NumberFormat moneyFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

    private final MonthlyRevenueChartPanel monthlyRevenueChartPanel = new MonthlyRevenueChartPanel();
    private final RoomDistributionPanel    roomDistributionPanel    = new RoomDistributionPanel();
    private final OccupancyTrendPanel      occupancyTrendPanel      = new OccupancyTrendPanel();

    private final Map<String, PrimaryButton> rangeButtons = new LinkedHashMap<>();
    private final Map<String, PrimaryButton> viewButtons  = new LinkedHashMap<>();
    private final CardLayout analyticsCards = new CardLayout();
    private final JPanel analyticsContent = new JPanel(analyticsCards);

    private String activeRange = "30 ngày";
    private String activeView  = "Doanh thu";

    // KPI labels — cập nhật bởi loadData()
    private final JLabel kpiRevenueValue    = new JLabel("--");
    private final JLabel kpiRevenueSub      = new JLabel("Chờ dữ liệu");
    private final JLabel kpiTotalRoomsValue = new JLabel("--");
    private final JLabel kpiTotalRoomsSub   = new JLabel("Chờ dữ liệu");
    private final JLabel kpiOccupancyValue  = new JLabel("--");
    private final JLabel kpiOccupancySub    = new JLabel("Chờ dữ liệu");
    private final JLabel kpiBookingsValue   = new JLabel("--");
    private final JLabel kpiBookingsSub     = new JLabel("Chờ dữ liệu");

    // Chart subtitle labels — cập nhật động
    private JLabel revenueChartSubtitle;

    // Recent bookings container — GridLayout 2 cột để mọi row grow đều, fill toàn card.
    private final JPanel recentListBox = new JPanel(new GridLayout(0, 2, 10, 8));

    public StatisticsPanel() {
        setOpaque(true);
        setBackground(new Color(236, 241, 247));
        setLayout(new MigLayout("insets 14,gap 8,fill", "[grow,fill]", "[]8[]8[]8[grow,fill]"));

        recentListBox.setOpaque(false);

        add(createTopHeader(),        "growx,wrap");
        add(createKpiRow(),           "growx,wrap");
        add(createViewToolbar(),      "growx,wrap");
        add(createAnalyticsContent(), "grow,pushy,growy");

        loadData();
    }

    // ============================== HEADER ==============================
    private JPanel createTopHeader() {
        JPanel top = new JPanel(new MigLayout("insets 0,gap 8,fillx", "[grow,fill][][][][]", "[]"));
        top.setOpaque(false);

        JLabel leftHint = new JLabel("Bộ lọc báo cáo");
        leftHint.setForeground(new Color(102, 124, 160));
        leftHint.setFont(leftHint.getFont().deriveFont(Font.BOLD, 13f));

        PrimaryButton exportBtn = new PrimaryButton("Xuất báo cáo");
        exportBtn.setBackground(ThemeColors.PREMIUM_ACCENT);
        exportBtn.setForeground(Color.WHITE);
        exportBtn.addActionListener(e -> JOptionPane.showMessageDialog(
            this,
            "Đã tạo báo cáo cho phạm vi: " + activeRange,
            "Xuất báo cáo",
            JOptionPane.INFORMATION_MESSAGE
        ));

        top.add(leftHint, "growx,pushx,aligny center");
        top.add(exportBtn, "h 38!");
        top.add(createRangeButton("7 ngày"),  "h 38!");
        top.add(createRangeButton("30 ngày"), "h 38!");
        top.add(createRangeButton("6 tháng"), "h 38!");

        updateRangeButtons();
        return top;
    }

    private PrimaryButton createRangeButton(String text) {
        PrimaryButton btn = new PrimaryButton(text);
        btn.addActionListener(e -> {
            activeRange = text;
            updateRangeButtons();
            loadData(); // <-- reload khi đổi range
        });
        rangeButtons.put(text, btn);
        return btn;
    }

    private void updateRangeButtons() {
        for (Map.Entry<String, PrimaryButton> item : rangeButtons.entrySet()) {
            boolean active = item.getKey().equals(activeRange);
            item.getValue().setBackground(active ? ThemeColors.PREMIUM_PRIMARY : ThemeColors.PREMIUM_SURFACE_HOVER);
            item.getValue().setForeground(active ? Color.WHITE : ThemeColors.PREMIUM_TEXT_SECONDARY);
        }
    }

    // ============================== KPI ROW ==============================
    private JPanel createKpiRow() {
        // Dùng GridLayout để 4 card luôn có cùng kích thước, không bị xê dịch khi nội dung thay đổi
        JPanel row = new JPanel(new GridLayout(1, 4, 10, 0));
        row.setOpaque(false);

        row.add(kpiCard("Doanh thu",     kpiRevenueValue,    kpiRevenueSub));
        row.add(kpiCard("Tổng phòng",    kpiTotalRoomsValue, kpiTotalRoomsSub));
        row.add(kpiCard("Tỷ lệ lấp đầy", kpiOccupancyValue,  kpiOccupancySub));
        row.add(kpiCard("Đặt phòng",     kpiBookingsValue,   kpiBookingsSub));

        return row;
    }

    private RoundedPanel kpiCard(String title, JLabel valueLabel, JLabel subLabel) {
        RoundedPanel card = new RoundedPanel(18, Color.WHITE, new Color(214, 223, 238), 1f);
        card.setLayout(new MigLayout("wrap 1,insets 14,gap 4", "[grow,fill]", "[]"));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(new Color(87, 109, 146));
        titleLabel.setFont(titleLabel.getFont().deriveFont(14f));

        valueLabel.setForeground(new Color(14, 30, 62));
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 28f));

        subLabel.setForeground(new Color(124, 142, 171));
        subLabel.setFont(subLabel.getFont().deriveFont(13f));

        card.add(titleLabel);
        card.add(valueLabel);
        card.add(subLabel);
        return card;
    }

    // ============================== VIEW TOOLBAR ==============================
    private JPanel createViewToolbar() {
        JPanel row = new JPanel(new MigLayout("insets 0,gap 8,fillx", "[grow,fill][][][][]", "[]"));
        row.setOpaque(false);

        JLabel title = new JLabel("Hiển thị nhanh");
        title.setForeground(new Color(102, 124, 160));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 13f));

        row.add(title, "pushx,growx");
        row.add(createViewButton("Doanh thu"),         "h 34!");
        row.add(createViewButton("Phân bố phòng"),     "h 34!");
        row.add(createViewButton("Tỷ lệ lấp đầy"),     "h 34!");
        row.add(createViewButton("Đặt phòng gần đây"), "h 34!");

        updateViewButtons();
        return row;
    }

    private PrimaryButton createViewButton(String name) {
        PrimaryButton btn = new PrimaryButton(name);
        btn.addActionListener(e -> {
            activeView = name;
            analyticsCards.show(analyticsContent, name);
            updateViewButtons();
        });
        viewButtons.put(name, btn);
        return btn;
    }

    private void updateViewButtons() {
        for (Map.Entry<String, PrimaryButton> item : viewButtons.entrySet()) {
            boolean active = item.getKey().equals(activeView);
            item.getValue().setBackground(active ? ThemeColors.PREMIUM_PRIMARY : ThemeColors.PREMIUM_SURFACE_HOVER);
            item.getValue().setForeground(active ? Color.WHITE : ThemeColors.PREMIUM_TEXT_SECONDARY);
        }
    }

    // ============================== ANALYTICS CARDS ==============================
    private JPanel createAnalyticsContent() {
        analyticsContent.setOpaque(false);

        // Card layout dùng row template [title-natural][content-grow] để title luôn top
        // và content fill phần còn lại, không có khoảng trắng thổn.
        String cardLayout = "wrap 1,insets 12,gap 6,fill";
        String cardCols = "[grow,fill]";
        String cardRows = "[]push[grow,fill]";

        RoundedPanel revenueCard = new RoundedPanel(18, Color.WHITE, new Color(214, 223, 238), 1f);
        revenueCard.setLayout(new MigLayout(cardLayout, cardCols, cardRows));
        // Tạo title block thủ công để giữ reference đến subtitle
        JPanel revenueTitleBlock = new JPanel(new MigLayout("wrap 1,insets 0,gap 2", "[grow,fill]", "[][]"));
        revenueTitleBlock.setOpaque(false);
        JLabel revenueTitle = new JLabel("Doanh thu");
        revenueTitle.setForeground(new Color(14, 30, 62));
        revenueTitle.setFont(revenueTitle.getFont().deriveFont(Font.BOLD, 14f));
        revenueChartSubtitle = new JLabel("7 ngày gần nhất");
        revenueChartSubtitle.setForeground(new Color(124, 142, 171));
        revenueChartSubtitle.setFont(revenueChartSubtitle.getFont().deriveFont(12f));
        revenueTitleBlock.add(revenueTitle);
        revenueTitleBlock.add(revenueChartSubtitle);
        revenueCard.add(revenueTitleBlock, "aligny top");
        revenueCard.add(monthlyRevenueChartPanel, "grow,push");

        RoundedPanel roomDistCard = new RoundedPanel(18, Color.WHITE, new Color(214, 223, 238), 1f);
        roomDistCard.setLayout(new MigLayout(cardLayout, cardCols, cardRows));
        roomDistCard.add(sectionTitle("Phân bố loại phòng", "Theo số phòng vật lý"), "aligny top");
        roomDistCard.add(roomDistributionPanel, "grow,push");

        RoundedPanel occupancyCard = new RoundedPanel(18, Color.WHITE, new Color(214, 223, 238), 1f);
        occupancyCard.setLayout(new MigLayout(cardLayout, cardCols, cardRows));
        occupancyCard.add(sectionTitle("Tỷ lệ lấp đầy", "Theo ngày trong tháng"), "aligny top");
        occupancyCard.add(occupancyTrendPanel, "grow,push");

        // Recent card dùng layout 2 cột để title bên trái và nút "Xem tất cả" bên phải
        // nằm cùng row, đều aligny top — title không bị MigLayout center theo chiều cao button.
        RoundedPanel recentCard = new RoundedPanel(18, Color.WHITE, new Color(214, 223, 238), 1f);
        recentCard.setLayout(new MigLayout("insets 12,gap 6,fill",
                                           "[grow,fill][]",
                                           "[]push[grow,fill]"));
        PrimaryButton viewAll = new PrimaryButton("Xem tất cả");
        viewAll.setBackground(ThemeColors.PREMIUM_SURFACE_HOVER);
        viewAll.setForeground(ThemeColors.PREMIUM_TEXT_SECONDARY);
        viewAll.addActionListener(e -> showAllRecentBookingsDialog());
        recentCard.add(sectionTitle("Đặt phòng gần đây", "Top 8 mới nhất"), "aligny top,growx");
        recentCard.add(viewAll, "aligny top,h 30!,wrap");
        recentCard.add(recentListBox, "span 2,grow,push");

        analyticsContent.add(revenueCard,  "Doanh thu");
        analyticsContent.add(roomDistCard, "Phân bố phòng");
        analyticsContent.add(occupancyCard,"Tỷ lệ lấp đầy");
        analyticsContent.add(recentCard,   "Đặt phòng gần đây");
        analyticsCards.show(analyticsContent, activeView);

        return analyticsContent;
    }

    /** Mở dialog modal với JTable + JScrollPane nội bộ để browse toàn bộ booking. */
    private void showAllRecentBookingsDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner instanceof Frame ? (Frame) owner : null,
                                  "Tất cả đặt phòng gần đây", true);
        dlg.setSize(820, 520);
        dlg.setLocationRelativeTo(owner);

        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String[] cols = {"Phòng", "Khách", "Loại phòng", "Trạng thái", "Ngày đặt"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        List<RecentBooking> all = bus.loadAllRecentBookings();
        for (RecentBooking b : all) {
            model.addRow(new Object[] {
                b.getRoomCode(),
                b.getGuestName(),
                b.getRoomType(),
                b.getStatus(),
                b.getBookingDate() == null ? "" : b.getBookingDate().format(df)
            });
        }

        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 13f));
        table.setFont(table.getFont().deriveFont(13f));

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));

        JLabel countLbl = new JLabel("  Tổng " + all.size() + " kết quả");
        countLbl.setBorder(BorderFactory.createEmptyBorder(10, 12, 6, 12));
        countLbl.setForeground(new Color(102, 124, 160));
        countLbl.setFont(countLbl.getFont().deriveFont(Font.BOLD, 13f));

        dlg.setLayout(new BorderLayout());
        dlg.add(countLbl, BorderLayout.NORTH);
        dlg.add(sp,       BorderLayout.CENTER);
        dlg.setVisible(true);
    }

    private JPanel sectionTitle(String title, String subtitle) {
        JPanel p = new JPanel(new MigLayout("insets 0,wrap 1,gap 2", "[grow,fill]", "[]"));
        p.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setForeground(new Color(14, 30, 62));
        t.setFont(t.getFont().deriveFont(Font.BOLD, 18f));
        JLabel s = new JLabel(subtitle);
        s.setForeground(new Color(124, 142, 171));
        s.setFont(s.getFont().deriveFont(12f));
        p.add(t);
        if (!subtitle.isEmpty()) {
            p.add(s);
        }
        return p;
    }

    private RoundedPanel recentItem(String room, String guest, String type, String status, Color statusColor) {
        RoundedPanel row = new RoundedPanel(14, new Color(248, 250, 254), new Color(225, 232, 244), 1f);
        row.setLayout(new MigLayout("insets 18 20 18 20,gap 14", "[72!][grow,fill][]", "[grow,fill]"));

        // Badge phòng: nền nhạt + chữ navy để nổi bật khi row cao hơn.
        RoundedPanel roomBadge = new RoundedPanel(10, new Color(232, 240, 254), new Color(199, 218, 247), 1f);
        roomBadge.setLayout(new MigLayout("insets 6 4 6 4,fill", "[grow,fill]", "[grow,fill]"));
        JLabel roomLbl = new JLabel(room, JLabel.CENTER);
        roomLbl.setForeground(new Color(37, 99, 235));
        roomLbl.setFont(roomLbl.getFont().deriveFont(Font.BOLD, 18f));
        roomBadge.add(roomLbl, "grow");

        JPanel guestWrap = new JPanel(new MigLayout("insets 0,wrap 1,gap 4", "[grow,fill]", "[]"));
        guestWrap.setOpaque(false);
        JLabel name = new JLabel(guest);
        name.setForeground(new Color(14, 30, 62));
        name.setFont(name.getFont().deriveFont(Font.BOLD, 16f));
        JLabel roomType = new JLabel(type);
        roomType.setForeground(new Color(124, 142, 171));
        roomType.setFont(roomType.getFont().deriveFont(14f));
        guestWrap.add(name);
        guestWrap.add(roomType);

        JLabel statusLbl = new JLabel(status);
        statusLbl.setForeground(statusColor);
        statusLbl.setFont(statusLbl.getFont().deriveFont(Font.BOLD, 14f));

        row.add(roomBadge, "growy,aligny center,h 44!");
        row.add(guestWrap, "growx,aligny center");
        row.add(statusLbl, "aligny center");
        return row;
    }

    // ============================== DATA LOADING ==============================
    private void loadData() {
        try {
            int days = StatisticsBUS.rangeToDays(activeRange);
            KpiSummary kpi = bus.loadKpis(days);

            kpiRevenueValue.setText(formatVnd(kpi.getRevenue()));
            kpiRevenueSub.setText("Trong " + activeRange);

            kpiTotalRoomsValue.setText(String.valueOf(kpi.getTotalRooms()));
            kpiTotalRoomsSub.setText("Phòng vật lý hiện có");

            kpiOccupancyValue.setText(String.format("%.0f%%", kpi.getOccupancyRate() * 100));
            kpiOccupancySub.setText(kpi.getOccupiedRooms() + " / " + kpi.getTotalRooms() + " đang dùng");

            kpiBookingsValue.setText(String.valueOf(kpi.getTotalBookings()));
            kpiBookingsSub.setText("Đặt phòng " + activeRange);

            monthlyRevenueChartPanel.setData(bus.loadRevenueByRange(days));
            if (revenueChartSubtitle != null) {
                revenueChartSubtitle.setText(activeRange + " gần nhất");
            }
            roomDistributionPanel.setData(bus.loadRoomTypeDistribution());
            occupancyTrendPanel.setData(bus.loadOccupancyTrend(Math.min(days, 30))); // Tối đa 30 điểm trên chart
            populateRecentList(bus.loadRecentBookings());
        } catch (Exception ex) {
            System.err.println("StatisticsPanel.loadData: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void populateRecentList(List<RecentBooking> items) {
        recentListBox.removeAll();
        if (items == null || items.isEmpty()) {
            JLabel empty = new JLabel("Chưa có dữ liệu đặt phòng gần đây", JLabel.CENTER);
            empty.setForeground(new Color(124, 142, 171));
            empty.setFont(empty.getFont().deriveFont(14f));
            recentListBox.setLayout(new java.awt.BorderLayout());
            recentListBox.add(empty, java.awt.BorderLayout.CENTER);
        } else {
            recentListBox.setLayout(new GridLayout(0, 2, 10, 8));
            for (RecentBooking b : items) {
                recentListBox.add(
                    recentItem(b.getRoomCode(), b.getGuestName(), b.getRoomType(),
                               b.getStatus(), colorForStatus(b.getStatus()))
                );
            }
        }
        recentListBox.revalidate();
        recentListBox.repaint();
    }

    private Color colorForStatus(String s) {
        if (s == null) return new Color(124, 142, 171);
        switch (s) {
            case "Đang ở":  return new Color(34, 197, 94);
            case "Sắp đến": return new Color(59, 130, 246);
            case "Đã xong": return new Color(124, 142, 171);
            default:        return new Color(124, 142, 171);
        }
    }

    private String formatVnd(double v) {
        if (v <= 0) return "0 đ";
        if (v >= 1_000_000_000) return String.format("%.2f tỷ", v / 1_000_000_000.0);
        if (v >= 1_000_000)     return String.format("%.1f tr", v / 1_000_000.0);
        return moneyFormat.format(v) + " đ";
    }

    // ============================== INNER CHART CLASSES ==============================
    private static final class MonthlyRevenueChartPanel extends JPanel {
        private List<RevenuePoint> data = Collections.emptyList();

        private MonthlyRevenueChartPanel() {
            setOpaque(false);
        }

        void setData(List<RevenuePoint> newData) {
            this.data = newData == null ? Collections.emptyList() : newData;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int left = 56, right = 16, top = 22, bottom = 38;
            int chartW = w - left - right;
            int chartH = h - top - bottom;

            double maxVal = 0;
            for (RevenuePoint p : data) maxVal = Math.max(maxVal, p.getRevenue());

            // Luôn vẽ grid và axes, dù có dữ liệu hay không
            double max = (data.isEmpty() || maxVal <= 0) ? 1_000_000.0 : niceCeil(maxVal * 1.1);

            g2.setFont(g2.getFont().deriveFont(11f));
            for (int i = 0; i <= 4; i++) {
                int y = top + (chartH * i / 4);
                g2.setColor(new Color(229, 236, 246));
                g2.drawLine(left, y, left + chartW, y);
                double val = max * (4 - i) / 4.0;
                g2.setColor(new Color(129, 145, 176));
                g2.drawString(formatAxis(val), 4, y + 4);
            }

            // Vẽ cột nếu có dữ liệu
            if (!data.isEmpty() && maxVal > 0) {
                int n = data.size();
                int gap = chartW / n;
                int bw = Math.max(22, gap / 3);
                for (int i = 0; i < n; i++) {
                    RevenuePoint p = data.get(i);
                    int barH = (int) (chartH * (p.getRevenue() / max));
                    int x = left + i * gap + (gap - bw) / 2;
                    int y = top + chartH - barH;
                    g2.setColor(new Color(59, 130, 246, 230));
                    g2.fillRoundRect(x, y, bw, barH, 10, 10);
                    g2.setColor(new Color(129, 145, 176));
                    g2.drawString(p.getLabel(), x - 4, top + chartH + 20);
                }
            }

            g2.dispose();
        }

        private static double niceCeil(double v) {
            double step;
            if      (v >= 1_000_000_000) step = 200_000_000;
            else if (v >=   500_000_000) step = 100_000_000;
            else if (v >=   100_000_000) step =  50_000_000;
            else                         step =  10_000_000;
            return Math.ceil(v / step) * step;
        }

        private static String formatAxis(double v) {
            if (v <= 0)             return "0";
            if (v >= 1_000_000_000) return String.format("%.1ftỷ", v / 1_000_000_000.0);
            return ((long) (v / 1_000_000)) + "tr";
        }
    }

    private static final class RoomDistributionPanel extends JPanel {
        private List<RoomTypeShare> data = Collections.emptyList();
        private final Color[] palette = {
            new Color(59, 130, 246),
            new Color(16, 185, 129),
            new Color(124, 87, 235),
            new Color(245, 158, 11),
            new Color(236, 72, 153),
            new Color(20, 184, 166),
            new Color(244, 63, 94),
            new Color(99, 102, 241)
        };

        private RoomDistributionPanel() {
            setOpaque(false);
        }

        void setData(List<RoomTypeShare> newData) {
            this.data = newData == null ? Collections.emptyList() : newData;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            // Donut scale theo card: chiếm ~62% chiều cao panel, chừa chỗ legend bên dưới.
            int legendRows = Math.max(1, data.size());
            int legendBlock = legendRows * 22 + 12;
            int donutArea = Math.max(120, h - legendBlock);
            int radius = Math.max(60, Math.min(donutArea, w) / 2 - 16);
            int inner  = (int) (radius * 0.58);
            int cx = w / 2;
            int cy = donutArea / 2 + 8;

            int total = 0;
            for (RoomTypeShare s : data) total += s.getCount();

            if (data.isEmpty() || total == 0) {
                g2.setColor(new Color(124, 142, 171));
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14f));
                g2.drawString("Chưa có dữ liệu phân bố phòng", 24, h / 2);
                g2.dispose();
                return;
            }

            double start = 0;
            for (int i = 0; i < data.size(); i++) {
                double extent = data.get(i).getCount() * 360.0 / total;
                g2.setColor(palette[i % palette.length]);
                g2.fillArc(cx - radius, cy - radius, radius * 2, radius * 2,
                           (int) Math.round(start), (int) Math.round(extent));
                start += extent;
            }

            g2.setColor(Color.WHITE);
            g2.fillOval(cx - inner, cy - inner, inner * 2, inner * 2);

            int legendBoxWidth = Math.min(420, w - 48);
            int legendX = Math.max(24, (w - legendBoxWidth) / 2);
            int legendY = cy + radius + 28;
            for (int i = 0; i < data.size(); i++) {
                int y = legendY + i * 22;
                g2.setColor(palette[i % palette.length]);
                g2.fillOval(legendX, y - 9, 10, 10);
                g2.setColor(new Color(59, 79, 114));
                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));
                g2.drawString(data.get(i).getLabel(), legendX + 18, y);
                g2.setColor(new Color(14, 30, 62));
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 13f));
                g2.drawString(data.get(i).getCount() + " phòng", legendX + legendBoxWidth - 60, y);
            }

            g2.dispose();
        }
    }

    private static final class OccupancyTrendPanel extends JPanel {
        private List<OccupancyPoint> data = Collections.emptyList();
        private final Color lineColor = new Color(59, 130, 246);
        private final Color fillColor = new Color(59, 130, 246, 40);
        private final Color gridColor = new Color(229, 236, 246);
        private final Color textColor = new Color(100, 116, 139);

        private OccupancyTrendPanel() {
            setOpaque(false);
        }

        void setData(List<OccupancyPoint> newData) {
            this.data = newData == null ? Collections.emptyList() : newData;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int left = 50, right = 20, top = 24, bottom = 40;
            int chartW = w - left - right;
            int chartH = h - top - bottom;

            // Grid lines
            g2.setColor(gridColor);
            for (int i = 0; i <= 4; i++) {
                int y = top + i * chartH / 4;
                g2.drawLine(left, y, w - right, y);
            }

            // Calculate points
            int n = data.size();
            int[] xs = new int[n];
            int[] ys = new int[n];
            for (int i = 0; i < n; i++) {
                xs[i] = left + (i * chartW) / Math.max(1, n - 1);
                double rate = data.get(i).getRate();
                ys[i] = top + (int) ((1 - rate) * chartH);
            }

            // Fill area under line
            if (n > 1) {
                int[] fillXs = new int[n + 2];
                int[] fillYs = new int[n + 2];
                System.arraycopy(xs, 0, fillXs, 0, n);
                System.arraycopy(ys, 0, fillYs, 0, n);
                fillXs[n] = xs[n - 1];
                fillYs[n] = top + chartH;
                fillXs[n + 1] = xs[0];
                fillYs[n + 1] = top + chartH;
                g2.setColor(fillColor);
                g2.fillPolygon(fillXs, fillYs, n + 2);
            }

            // Draw line
            g2.setColor(lineColor);
            g2.setStroke(new java.awt.BasicStroke(2.5f));
            for (int i = 0; i < n - 1; i++) {
                g2.drawLine(xs[i], ys[i], xs[i + 1], ys[i + 1]);
            }

            // Draw points
            for (int i = 0; i < n; i++) {
                g2.setColor(Color.WHITE);
                g2.fillOval(xs[i] - 4, ys[i] - 4, 8, 8);
                g2.setColor(lineColor);
                g2.drawOval(xs[i] - 4, ys[i] - 4, 8, 8);
            }

            // Y-axis labels (100% ở trên, 0% ở dưới)
            g2.setColor(textColor);
            g2.setFont(g2.getFont().deriveFont(10f));
            for (int i = 0; i <= 4; i++) {
                String label = ((4 - i) * 25) + "%";
                int y = top + i * chartH / 4 + 4;
                g2.drawString(label, left - 40, y);
            }

            // X-axis labels (first, middle, last date)
            if (n > 0) {
                g2.setFont(g2.getFont().deriveFont(10f));
                String first = data.get(0).getLabel();
                String last = data.get(n - 1).getLabel();
                g2.drawString(first, left, h - 16);
                int lastW = g2.getFontMetrics().stringWidth(last);
                g2.drawString(last, w - right - lastW, h - 16);
                if (n > 2) {
                    String mid = data.get(n / 2).getLabel();
                    int midW = g2.getFontMetrics().stringWidth(mid);
                    g2.drawString(mid, left + chartW / 2 - midW / 2, h - 16);
                }
            }

            g2.dispose();
        }
    }
}
