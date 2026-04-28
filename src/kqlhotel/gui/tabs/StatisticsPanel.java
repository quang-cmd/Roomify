package kqlhotel.gui.tabs;

import java.awt.BasicStroke;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import kqlhotel.gui.components.PrimaryButton;
import kqlhotel.gui.components.RoundedPanel;
import kqlhotel.gui.theme.ThemeColors;
import net.miginfocom.swing.MigLayout;

public class StatisticsPanel extends JPanel {
    private final MonthlyRevenueChartPanel monthlyRevenueChartPanel = new MonthlyRevenueChartPanel();
    private final RoomDistributionPanel roomDistributionPanel = new RoomDistributionPanel();
    private final OccupancyTrendPanel occupancyTrendPanel = new OccupancyTrendPanel();
    private final Map<String, PrimaryButton> rangeButtons = new LinkedHashMap<>();
    private final Map<String, PrimaryButton> viewButtons = new LinkedHashMap<>();
    private final CardLayout analyticsCards = new CardLayout();
    private final JPanel analyticsContent = new JPanel(analyticsCards);
    private String activeRange = "30 ngày";
    private String activeView = "Doanh thu";

    public StatisticsPanel() {
        setOpaque(true);
        setBackground(new Color(236, 241, 247));
        setLayout(new MigLayout("insets 14,gap 8,fill", "[grow,fill]", "[]8[]8[]8[grow,fill]"));

        add(createTopHeader(), "growx,wrap");
        add(createKpiRow(), "growx,wrap");
        add(createViewToolbar(), "growx,wrap");
        add(createAnalyticsContent(), "grow,pushy,growy");
    }

    private JPanel createTopHeader() {
        JPanel top = new JPanel(new MigLayout("insets 0,gap 8,fillx", "[grow,fill][][][][]", "[]"));
        top.setOpaque(false);

        JLabel leftHint = new JLabel("Bộ lọc báo cáo");
        leftHint.setForeground(new Color(102, 124, 160));
        leftHint.setFont(leftHint.getFont().deriveFont(Font.BOLD, 13f));

        // Export = secondary CTA -> violet accent (distinct from range/view buttons)
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
        top.add(createRangeButton("7 ngày"), "h 38!");
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

    private JPanel createKpiRow() {
        JPanel row = new JPanel(new MigLayout("insets 0,gap 10,fillx", "[grow,fill][grow,fill][grow,fill][grow,fill]", "[]"));
        row.setOpaque(false);

        row.add(kpiCard("Doanh thu", "--", "Chờ dữ liệu", false));
        row.add(kpiCard("Tổng phòng", "--", "Chờ dữ liệu", false));
        row.add(kpiCard("Tỷ lệ lấp đầy", "--", "Chờ dữ liệu", false));
        row.add(kpiCard("Khách mới", "--", "Chờ dữ liệu", false));

        return row;
    }

    private JPanel createViewToolbar() {
        JPanel row = new JPanel(new MigLayout("insets 0,gap 8,fillx", "[grow,fill][][][][]", "[]"));
        row.setOpaque(false);

        JLabel title = new JLabel("Hiển thị nhanh");
        title.setForeground(new Color(102, 124, 160));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 13f));

        row.add(title, "pushx,growx");
        row.add(createViewButton("Doanh thu"), "h 34!");
        row.add(createViewButton("Phân bố phòng"), "h 34!");
        row.add(createViewButton("Tỷ lệ lấp đầy"), "h 34!");
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

    private JPanel createAnalyticsContent() {
        analyticsContent.setOpaque(false);

        RoundedPanel revenueCard = new RoundedPanel(18, Color.WHITE, new Color(214, 223, 238), 1f);
        revenueCard.setLayout(new MigLayout("wrap 1,insets 12,gap 6,fill", "[grow,fill]", "[]"));
        revenueCard.add(sectionTitle("Doanh thu theo tháng", "6 tháng gần nhất"));
        revenueCard.add(monthlyRevenueChartPanel, "grow, h 250!");

        RoundedPanel roomDistCard = new RoundedPanel(18, Color.WHITE, new Color(214, 223, 238), 1f);
        roomDistCard.setLayout(new MigLayout("wrap 1,insets 12,gap 6,fill", "[grow,fill]", "[]"));
        roomDistCard.add(sectionTitle("Phân bố loại phòng", "Tổng 15 phòng"));
        roomDistCard.add(roomDistributionPanel, "h 220!, growx, aligny top");

        RoundedPanel occupancyCard = new RoundedPanel(18, Color.WHITE, new Color(214, 223, 238), 1f);
        occupancyCard.setLayout(new MigLayout("wrap 1,insets 12,gap 6,fill", "[grow,fill]", "[]"));
        occupancyCard.add(sectionTitle("Tỷ lệ lấp đầy", "Theo ngày trong tháng 3/2026"));
        occupancyCard.add(occupancyTrendPanel, "grow, h 220!");

        RoundedPanel recentCard = new RoundedPanel(18, Color.WHITE, new Color(214, 223, 238), 1f);
        recentCard.setLayout(new MigLayout("wrap 1,insets 12,gap 6,fill", "[grow,fill]", "[]"));
        recentCard.add(sectionTitle("Đặt phòng gần đây", ""));
        recentCard.add(recentBookingList(), "grow");

        analyticsContent.add(revenueCard, "Doanh thu");
        analyticsContent.add(roomDistCard, "Phân bố phòng");
        analyticsContent.add(occupancyCard, "Tỷ lệ lấp đầy");
        analyticsContent.add(recentCard, "Đặt phòng gần đây");
        analyticsCards.show(analyticsContent, activeView);

        return analyticsContent;
    }

    private RoundedPanel kpiCard(String title, String value, String sub, boolean pill) {
        RoundedPanel card = new RoundedPanel(18, Color.WHITE, new Color(214, 223, 238), 1f);
        card.setLayout(new MigLayout("wrap 1,insets 14,gap 4", "[grow,fill]", "[]"));

        JPanel head = new JPanel(new MigLayout("insets 0", "[grow,fill][]", "[]"));
        head.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(new Color(87, 109, 146));
        titleLabel.setFont(titleLabel.getFont().deriveFont(14f));
        head.add(titleLabel);

        if (pill) {
            RoundedPanel badge = new RoundedPanel(12, new Color(231, 245, 237), new Color(231, 245, 237), 1f);
            badge.setLayout(new MigLayout("insets 4 10 4 10", "[]", "[]"));
            JLabel s = new JLabel(sub);
            s.setForeground(new Color(21, 128, 61));
            s.setFont(s.getFont().deriveFont(Font.BOLD, 13f));
            badge.add(s);
            head.add(badge);
        }

        JLabel valueLabel = new JLabel(value);
        valueLabel.setForeground(new Color(14, 30, 62));
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 30f));

        JLabel subLabel = new JLabel(pill ? "" : sub);
        subLabel.setForeground(new Color(124, 142, 171));
        subLabel.setFont(subLabel.getFont().deriveFont(13f));

        card.add(head);
        card.add(valueLabel);
        card.add(subLabel);
        return card;
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

    private JPanel recentBookingList() {
        JPanel list = new JPanel(new MigLayout("insets 0,wrap 1,gap 8", "[grow,fill]", "[]"));
        list.setOpaque(false);
        JLabel empty = new JLabel("Chưa có dữ liệu đặt phòng gần đây");
        empty.setForeground(new Color(124, 142, 171));
        empty.setFont(empty.getFont().deriveFont(14f));
        list.add(empty, "alignx center, gapy 18 0");
        return list;
    }

    private RoundedPanel recentItem(String room, String guest, String type, String status, Color statusColor) {
        RoundedPanel row = new RoundedPanel(14, new Color(248, 250, 254), new Color(225, 232, 244), 1f);
        row.setLayout(new MigLayout("insets 10 12 10 12", "[50!][grow,fill][]", "[]"));

        JLabel roomLbl = new JLabel(room, JLabel.CENTER);
        roomLbl.setForeground(new Color(59, 130, 246));
        roomLbl.setFont(roomLbl.getFont().deriveFont(Font.BOLD, 16f));

        JPanel guestWrap = new JPanel(new MigLayout("insets 0,wrap 1,gap 1", "[grow,fill]", "[]"));
        guestWrap.setOpaque(false);
        JLabel name = new JLabel(guest);
        name.setForeground(new Color(14, 30, 62));
        name.setFont(name.getFont().deriveFont(Font.BOLD, 15f));
        JLabel roomType = new JLabel(type);
        roomType.setForeground(new Color(124, 142, 171));
        roomType.setFont(roomType.getFont().deriveFont(13f));
        guestWrap.add(name);
        guestWrap.add(roomType);

        JLabel statusLbl = new JLabel(status);
        statusLbl.setForeground(statusColor);
        statusLbl.setFont(statusLbl.getFont().deriveFont(Font.BOLD, 13f));

        row.add(roomLbl, "aligny center");
        row.add(guestWrap, "growx");
        row.add(statusLbl, "aligny center");
        return row;
    }

    private static final class MonthlyRevenueChartPanel extends JPanel {
        private final String[] labels = new String[0];
        private final int[] revenue = new int[0];

        private MonthlyRevenueChartPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int left = 42;
            int right = 16;
            int top = 22;
            int bottom = 38;
            int chartW = w - left - right;
            int chartH = h - top - bottom;

            if (labels.length == 0 || revenue.length == 0) {
                g2.setColor(new Color(124, 142, 171));
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14f));
                g2.drawString("Chưa có dữ liệu doanh thu", left + 20, top + chartH / 2);
                g2.dispose();
                return;
            }

            g2.setColor(new Color(229, 236, 246));
            for (int i = 0; i <= 4; i++) {
                int y = top + (chartH * i / 4);
                g2.drawLine(left, y, left + chartW, y);
            }

            int max = 320;
            int n = labels.length;
            int gap = chartW / n;
            int bw = Math.max(22, gap / 3);

            g2.setColor(new Color(59, 130, 246, 230));
            for (int i = 0; i < n; i++) {
                int value = revenue[i];
                int barH = (int) (chartH * (value / (double) max));
                int x = left + i * gap + (gap - bw) / 2;
                int y = top + chartH - barH;
                g2.fillRoundRect(x, y, bw, barH, 10, 10);

                g2.setColor(new Color(129, 145, 176));
                g2.drawString(labels[i], x - 2, top + chartH + 20);
                g2.setColor(new Color(59, 130, 246, 230));
            }

            g2.setColor(new Color(129, 145, 176));
            g2.drawString("0", 18, top + chartH + 3);
            g2.drawString("80tr", 8, top + chartH - chartH / 4 + 3);
            g2.drawString("160tr", 4, top + chartH - chartH / 2 + 3);
            g2.drawString("240tr", 4, top + chartH - chartH * 3 / 4 + 3);
            g2.drawString("320tr", 4, top + 3);

            g2.dispose();
        }
    }

    private static final class RoomDistributionPanel extends JPanel {
        private final String[] labels = new String[0];
        private final int[] values = new int[0];
        private final Color[] colors = {
            new Color(59, 130, 246),
            new Color(16, 185, 129),
            new Color(124, 87, 235),
            new Color(245, 158, 11)
        };

        private RoomDistributionPanel() {
            setOpaque(false);
            setLayout(new MigLayout("insets 0", "[grow,fill]", "[180!][grow,fill]"));
        }

        @Override
        public java.awt.Dimension getPreferredSize() {
            return new java.awt.Dimension(640, 220);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int cx = getWidth() / 2;
            int cy = 86;
            int radius = 52;
            int inner = 30;
            int total = 0;
            for (int v : values) {
                total += v;
            }

            if (values.length == 0 || total == 0) {
                g2.setColor(new Color(124, 142, 171));
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14f));
                g2.drawString("Chưa có dữ liệu phân bố phòng", 24, 110);
                g2.dispose();
                return;
            }

            double start = 0;
            for (int i = 0; i < values.length; i++) {
                double extent = values[i] * 360.0 / total;
                g2.setColor(colors[i]);
                g2.fillArc(cx - radius, cy - radius, radius * 2, radius * 2, (int) Math.round(start), (int) Math.round(extent));
                start += extent;
            }

            g2.setColor(Color.WHITE);
            g2.fillOval(cx - inner, cy - inner, inner * 2, inner * 2);

            int legendY = 156;
            int legendBoxWidth = 360;
            int legendX = Math.max(24, (getWidth() - legendBoxWidth) / 2);
            for (int i = 0; i < labels.length; i++) {
                int y = legendY + i * 20;
                g2.setColor(colors[i]);
                g2.fillOval(legendX, y - 9, 10, 10);
                g2.setColor(new Color(59, 79, 114));
                g2.drawString(labels[i], legendX + 18, y);
                g2.setColor(new Color(14, 30, 62));
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 13f));
                g2.drawString(values[i] + " phòng", legendX + legendBoxWidth - 54, y);
                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));
            }

            g2.dispose();
        }
    }

    private static final class OccupancyTrendPanel extends JPanel {
        private final String[] labels = new String[0];
        private final int[] values = new int[0];

        private OccupancyTrendPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int left = 42;
            int right = 16;
            int top = 20;
            int bottom = 36;
            int chartW = w - left - right;
            int chartH = h - top - bottom;

            if (labels.length < 2 || values.length < 2) {
                g2.setColor(new Color(124, 142, 171));
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14f));
                g2.drawString("Chưa có dữ liệu tỷ lệ lấp đầy", left + 20, top + chartH / 2);
                g2.dispose();
                return;
            }

            g2.setColor(new Color(229, 236, 246));
            for (int i = 0; i <= 3; i++) {
                int y = top + i * chartH / 3;
                g2.drawLine(left, y, left + chartW, y);
            }

            int n = labels.length;
            int gap = chartW / (n - 1);
            int[] px = new int[n];
            int[] py = new int[n];

            for (int i = 0; i < n; i++) {
                px[i] = left + i * gap;
                py[i] = top + chartH - (int) ((values[i] - 50) * chartH / 50.0);
            }

            g2.setStroke(new BasicStroke(3f));
            g2.setColor(new Color(124, 87, 235));
            for (int i = 1; i < n; i++) {
                g2.drawLine(px[i - 1], py[i - 1], px[i], py[i]);
            }
            for (int i = 0; i < n; i++) {
                g2.fillOval(px[i] - 4, py[i] - 4, 8, 8);
                g2.setColor(new Color(129, 145, 176));
                g2.drawString(labels[i], px[i] - 15, top + chartH + 20);
                g2.setColor(new Color(124, 87, 235));
            }

            g2.setColor(new Color(129, 145, 176));
            g2.drawString("50%", 10, top + chartH + 4);
            g2.drawString("65%", 10, top + chartH - chartH / 3 + 4);
            g2.drawString("80%", 10, top + chartH - chartH * 2 / 3 + 4);
            g2.drawString("100%", 6, top + 4);

            g2.dispose();
        }
    }
}
