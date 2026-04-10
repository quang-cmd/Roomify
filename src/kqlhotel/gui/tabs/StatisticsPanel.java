package kqlhotel.gui.tabs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import kqlhotel.gui.components.RoundedPanel;
import kqlhotel.gui.theme.ThemeColors;
import net.miginfocom.swing.MigLayout;

public class StatisticsPanel extends JPanel {
    private final JLabel revenueValue = new JLabel();
    private final JLabel occupancyValue = new JLabel();
    private final JLabel bookingValue = new JLabel();
    private final JLabel guestValue = new JLabel();
    private final TrendChartPanel trendChartPanel = new TrendChartPanel();
    private final DefaultTableModel tableModel;
    private final Map<String, StatsData> dataByRange = new LinkedHashMap<>();

    public StatisticsPanel() {
        setOpaque(false);
        setLayout(new MigLayout("insets 20,gap 14", "[grow]", "[]"));

        seedData();

        RoundedPanel filterCard = new RoundedPanel(16, new Color(29, 46, 78), new Color(255, 255, 255, 20), 1f);
        filterCard.setLayout(new MigLayout("insets 14", "[][220!]push", "[]"));

        JLabel filterLabel = new JLabel("Khoảng thời gian");
        filterLabel.setForeground(new Color(199, 214, 242));

        JComboBox<String> rangeCombo = new JComboBox<>(new String[]{"Hôm nay", "7 ngày", "30 ngày"});
        rangeCombo.addActionListener(e -> applyData((String) rangeCombo.getSelectedItem()));

        filterCard.add(filterLabel);
        filterCard.add(rangeCombo);

        JPanel summaryGrid = new JPanel(new MigLayout("insets 0,gap 12", "[grow,fill][grow,fill][grow,fill][grow,fill]", "[]"));
        summaryGrid.setOpaque(false);
        summaryGrid.add(summaryCard("Doanh thu", revenueValue, new Color(62, 127, 255)));
        summaryGrid.add(summaryCard("Công suất phòng", occupancyValue, new Color(30, 180, 120)));
        summaryGrid.add(summaryCard("Lượt đặt phòng", bookingValue, new Color(143, 97, 255)));
        summaryGrid.add(summaryCard("Khách đang lưu trú", guestValue, new Color(230, 154, 30)));

        RoundedPanel tableCard = new RoundedPanel(16, new Color(29, 46, 78), new Color(255, 255, 255, 20), 1f);
        tableCard.setLayout(new MigLayout("wrap 1,insets 14,gap 10", "[grow,fill]", "[]"));

        JLabel tableTitle = new JLabel("Hiệu suất loại phòng");
        tableTitle.setForeground(new Color(239, 244, 255));
        tableTitle.setFont(tableTitle.getFont().deriveFont(20f));

        tableModel = new DefaultTableModel(new Object[]{"Loại phòng", "Doanh thu", "Tỉ lệ lấp đầy", "Số đêm bán"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setForeground(new Color(18, 32, 58));
        table.getTableHeader().setBackground(new Color(230, 238, 252));
        table.getTableHeader().setForeground(new Color(30, 53, 86));

        JScrollPane scrollPane = new JScrollPane(table);

        tableCard.add(tableTitle);
        tableCard.add(scrollPane, "h 260!");

        RoundedPanel chartCard = new RoundedPanel(16, new Color(29, 46, 78), new Color(255, 255, 255, 20), 1f);
        chartCard.setLayout(new MigLayout("wrap 1,insets 14,gap 10", "[grow,fill]", "[]"));
        JLabel chartTitle = new JLabel("Xu hướng doanh thu & công suất phòng");
        chartTitle.setForeground(new Color(239, 244, 255));
        chartTitle.setFont(chartTitle.getFont().deriveFont(20f));
        chartCard.add(chartTitle);
        chartCard.add(trendChartPanel, "h 220!");

        add(filterCard, "growx");
        add(summaryGrid, "growx");
        add(chartCard, "growx");
        add(tableCard, "grow");

        applyData("Hôm nay");
    }

    private RoundedPanel summaryCard(String title, JLabel valueLabel, Color accent) {
        RoundedPanel card = new RoundedPanel(14, new Color(29, 46, 78), new Color(255, 255, 255, 20), 1f);
        card.setLayout(new MigLayout("wrap 1,insets 12,gap 6", "[grow,fill]", "[]"));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(new Color(149, 167, 202));

        valueLabel.setForeground(new Color(239, 244, 255));
        valueLabel.setFont(valueLabel.getFont().deriveFont(28f));

        JLabel accentBar = new JLabel(" ");
        accentBar.setOpaque(true);
        accentBar.setBackground(accent);

        card.add(titleLabel);
        card.add(valueLabel);
        card.add(accentBar, "h 4!");
        return card;
    }

    private void applyData(String key) {
        StatsData data = dataByRange.getOrDefault(key, dataByRange.get("Hôm nay"));
        revenueValue.setText(data.revenue);
        occupancyValue.setText(data.occupancy);
        bookingValue.setText(data.bookings);
        guestValue.setText(data.guests);

        tableModel.setRowCount(0);
        for (Object[] row : data.rows) {
            tableModel.addRow(row);
        }

        trendChartPanel.setData(data.chartLabels, data.revenueTrend, data.occupancyTrend);
    }

    private void seedData() {
        dataByRange.put("Hôm nay", new StatsData(
            "28.400.000đ",
            "62%",
            "18",
            "41",
            new Object[][]{
                {"Deluxe", "9.600.000đ", "60%", "8"},
                {"Grand Premium 1", "6.600.000đ", "50%", "3"},
                {"Grand Premium 2", "6.400.000đ", "67%", "2"},
                {"Suite", "5.800.000đ", "67%", "1"}
            },
            new String[]{"T2", "T3", "T4", "T5", "T6", "T7", "CN"},
            new int[]{3, 4, 5, 4, 3, 5, 4},
            new int[]{55, 60, 66, 62, 58, 67, 61}
        ));

        dataByRange.put("7 ngày", new StatsData(
            "178.900.000đ",
            "71%",
            "112",
            "267",
            new Object[][]{
                {"Deluxe", "58.000.000đ", "73%", "49"},
                {"Grand Premium 1", "45.200.000đ", "69%", "21"},
                {"Grand Premium 2", "41.500.000đ", "72%", "13"},
                {"Suite", "34.200.000đ", "68%", "7"}
            },
            new String[]{"Tuần 1", "Tuần 2", "Tuần 3", "Tuần 4"},
            new int[]{38, 41, 45, 55},
            new int[]{64, 68, 72, 71}
        ));

        dataByRange.put("30 ngày", new StatsData(
            "736.200.000đ",
            "76%",
            "487",
            "1.128",
            new Object[][]{
                {"Deluxe", "228.000.000đ", "79%", "191"},
                {"Grand Premium 1", "193.600.000đ", "76%", "88"},
                {"Grand Premium 2", "179.100.000đ", "77%", "56"},
                {"Suite", "135.500.000đ", "72%", "26"}
            },
            new String[]{"Tuần 1", "Tuần 2", "Tuần 3", "Tuần 4"},
            new int[]{160, 172, 188, 216},
            new int[]{72, 74, 77, 81}
        ));
    }

    private static final class StatsData {
        private final String revenue;
        private final String occupancy;
        private final String bookings;
        private final String guests;
        private final Object[][] rows;
        private final String[] chartLabels;
        private final int[] revenueTrend;
        private final int[] occupancyTrend;

        private StatsData(
            String revenue,
            String occupancy,
            String bookings,
            String guests,
            Object[][] rows,
            String[] chartLabels,
            int[] revenueTrend,
            int[] occupancyTrend
        ) {
            this.revenue = revenue;
            this.occupancy = occupancy;
            this.bookings = bookings;
            this.guests = guests;
            this.rows = rows;
            this.chartLabels = chartLabels;
            this.revenueTrend = revenueTrend;
            this.occupancyTrend = occupancyTrend;
        }
    }

    private static final class TrendChartPanel extends JPanel {
        private String[] labels = new String[0];
        private int[] revenue = new int[0];
        private int[] occupancy = new int[0];

        private TrendChartPanel() {
            setOpaque(false);
        }

        private void setData(String[] labels, int[] revenue, int[] occupancy) {
            this.labels = labels;
            this.revenue = revenue;
            this.occupancy = occupancy;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int left = 42;
            int right = 20;
            int top = 12;
            int bottom = 34;
            int chartW = w - left - right;
            int chartH = h - top - bottom;

            g2.setColor(new Color(84, 107, 149));
            g2.drawLine(left, top + chartH, left + chartW, top + chartH);

            if (labels.length == 0) {
                g2.dispose();
                return;
            }

            int maxRevenue = 1;
            for (int value : revenue) {
                if (value > maxRevenue) {
                    maxRevenue = value;
                }
            }

            int points = labels.length;
            int barWidth = Math.max(16, chartW / (points * 2));
            int gap = chartW / points;

            int[] lineX = new int[points];
            int[] lineY = new int[points];

            for (int i = 0; i < points; i++) {
                int xCenter = left + gap * i + gap / 2;

                int rev = i < revenue.length ? revenue[i] : 0;
                int revHeight = (int) (chartH * (rev / (double) maxRevenue));
                int barX = xCenter - barWidth / 2;
                int barY = top + chartH - revHeight;
                g2.setColor(new Color(62, 127, 255, 220));
                g2.fillRoundRect(barX, barY, barWidth, revHeight, 8, 8);

                int occ = i < occupancy.length ? occupancy[i] : 0;
                lineX[i] = xCenter;
                lineY[i] = top + chartH - (int) (chartH * (occ / 100.0));

                g2.setColor(new Color(149, 167, 202));
                g2.drawString(labels[i], xCenter - 12, top + chartH + 20);
            }

            g2.setStroke(new BasicStroke(3f));
            g2.setColor(new Color(30, 180, 120));
            for (int i = 1; i < points; i++) {
                g2.drawLine(lineX[i - 1], lineY[i - 1], lineX[i], lineY[i]);
            }
            for (int i = 0; i < points; i++) {
                g2.fillOval(lineX[i] - 4, lineY[i] - 4, 8, 8);
            }

            g2.setColor(new Color(199, 214, 242));
            g2.drawString("Doanh thu", left, top + 12);
            g2.setColor(new Color(30, 180, 120));
            g2.drawString("Công suất phòng", left + 90, top + 12);

            g2.dispose();
        }
    }
}
