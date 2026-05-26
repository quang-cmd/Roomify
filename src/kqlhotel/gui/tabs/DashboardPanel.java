package kqlhotel.gui.tabs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.ScrollPaneConstants;
import net.miginfocom.swing.MigLayout;
import kqlhotel.bus.shift.ShiftBUS;
import kqlhotel.bus.statistics.StatisticsBUS;
import kqlhotel.entity.statistics.KpiSummary;
import kqlhotel.entity.statistics.RevenuePoint;
import kqlhotel.entity.shift.ShiftInfo;
import kqlhotel.entity.shift.ShiftReconciliationRow;
import kqlhotel.gui.components.PrimaryButton;
import kqlhotel.gui.theme.ThemeColors;

public class DashboardPanel extends JPanel {

    private final ShiftBUS shiftBUS = new ShiftBUS();
    private final StatisticsBUS statisticsBUS = new StatisticsBUS();

    private final JLabel lblCaHienTai   = kpiValue("--");
    private final JLabel lblGioCa       = kpiValue("--");
    private final JLabel lblNhanVien    = kpiValue("--");
    private final JLabel lblTienMoCa    = kpiValue("--");
    private final JLabel lblDoanhThu    = kpiValue("--");
    private final JLabel lblGiaoDich    = kpiValue("--");
    private final JLabel lblPhongSapNhan = kpiValue("--");
    private final JLabel lblBookingSapNhan = kpiValue("--");
    private final JLabel lblLoiNhuanThang = kpiValue("--");
    private final JLabel lblLoiNhuanBar = kpiValue("--");
    private final JLabel lblSoSanhLoiNhuan = kpiValue("--");
    private final JLabel lblDoanhThuThang = kpiValue("--");
    private final JLabel lblChiPhiThang = kpiValue("--");
    private final JProgressBar barPhongSapNhan = progressBar(new Color(0x0EA5E9));
    private final JProgressBar barDoanhThu = progressBar(new Color(0x2563EB));
    private final JProgressBar barChiPhi = progressBar(new Color(0xDC2626));
    private final JProgressBar barLoiNhuan = progressBar(new Color(0x0F766E));
    private final JComboBox<String> revenueRangeCombo = new JComboBox<>(new String[] {
        "7 ngày", "1 tháng", "Quý 1", "Quý 2", "Quý 3", "Quý 4"
    });
    private final RevenueChartPanel revenueChartPanel = new RevenueChartPanel();
    private final DefaultTableModel shiftTableModel = new DefaultTableModel(
        new Object[] {"Mã ca", "Nhân viên", "Ca", "Mở ca", "Tiền đầu ca", "Doanh thu", "Tiền kết ca", "Chênh lệch", "Trạng thái"},
        0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable shiftTable = new JTable(shiftTableModel);
    private boolean loadedOnce;
    private volatile boolean refreshInProgress;
    private final JLabel lblTrangThai   = new JLabel("Đang tải...");

    public DashboardPanel() {
        setLayout(new MigLayout("insets 24, gap 16, fill", "[grow 70,fill][grow 30,fill]", "[]12[240!,fill]16[grow,fill]"));
        setBackground(ThemeColors.PREMIUM_BG);

        add(buildHeader(), "span 2,growx,wrap");
        add(buildShiftReconciliationSection(), "grow");
        add(buildVisualOverviewSection(), "grow,wrap");
        add(buildRevenueChartSection(), "span 2,grow");

    }

    @Override
    public void addNotify() {
        super.addNotify();

        if (!kqlhotel.gui.Permission.isQuanLy()) {
            return;
        }

        if (!loadedOnce) {
            loadedOnce = true;
            SwingUtilities.invokeLater(this::refresh);
        }
    }

    private JPanel buildHeader() {
        JPanel row = new JPanel(new MigLayout("insets 0, gap 12", "[grow,fill][]", "[]"));
        row.setOpaque(false);

        lblTrangThai.setFont(lblTrangThai.getFont().deriveFont(12f));
        lblTrangThai.setForeground(ThemeColors.PREMIUM_TEXT_MUTED);

        JButton btnRefresh = new PrimaryButton("Làm mới");
        btnRefresh.setFont(btnRefresh.getFont().deriveFont(Font.BOLD, 12f));
        btnRefresh.setBackground(ThemeColors.PREMIUM_PRIMARY);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setOpaque(false);
        btnRefresh.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btnRefresh.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> refresh());

        row.add(lblTrangThai, "aligny center");
        row.add(btnRefresh, "aligny center");
        return row;
    }

    private JPanel buildShiftSection() {
        JPanel section = new JPanel(new MigLayout("insets 20, gap 12, wrap 1", "[grow,fill]", "[]12[]"));
        section.setBackground(ThemeColors.PREMIUM_SURFACE);
        section.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.PREMIUM_BORDER, 1, true),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));

        JLabel sectionTitle = new JLabel("Hoạt động ca làm việc");
        sectionTitle.setFont(sectionTitle.getFont().deriveFont(Font.BOLD, 15f));
        sectionTitle.setForeground(ThemeColors.PREMIUM_TEXT_PRIMARY);
        section.add(sectionTitle);

        JPanel kpiRow = new JPanel(new MigLayout("insets 0, gap 12", "[grow,fill][grow,fill][grow,fill][grow,fill][grow,fill]", "[]"));
        kpiRow.setOpaque(false);

        kpiRow.add(kpiCard("Ca hiện tại",     lblCaHienTai,  new Color(0xDBEAFE), new Color(0x1E3A8A)));
        kpiRow.add(kpiCard("Giờ làm việc",    lblGioCa,      new Color(0xFEF3C7), new Color(0x92400E)));
        kpiRow.add(kpiCard("Nhân viên trực",  lblNhanVien,   new Color(0xD1FAE5), new Color(0x065F46)));
        kpiRow.add(kpiCard("Tiền mở ca",      lblTienMoCa,   new Color(0xEDE9FE), new Color(0x5B21B6)));
        kpiRow.add(kpiCard("Doanh thu ca",    lblDoanhThu,   new Color(0xFCE7F3), new Color(0x9D174D)));

        section.add(kpiRow);

        JPanel row2 = new JPanel(new MigLayout("insets 0, gap 12", "[grow,fill][grow,fill][grow,fill][grow,fill][grow,fill]", "[]"));
        row2.setOpaque(false);
        row2.setVisible(false);
        row2.add(kpiCard("Số giao dịch", lblGiaoDich, new Color(0xFFEDD5), new Color(0x9A3412)));
        section.add(row2);

        return section;
    }

    private JPanel buildCompactShiftSection() {
        JPanel section = new JPanel(new MigLayout("insets 14, gap 8, wrap 2, fill", "[grow,fill][grow,fill]", "[]6[]6[]6[]"));
        section.setBackground(ThemeColors.PREMIUM_SURFACE);
        section.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.PREMIUM_BORDER, 1, true),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));

        JLabel sectionTitle = new JLabel("Hoạt động ca làm việc");
        sectionTitle.setFont(sectionTitle.getFont().deriveFont(Font.BOLD, 15f));
        sectionTitle.setForeground(ThemeColors.PREMIUM_TEXT_PRIMARY);
        section.add(sectionTitle, "span 2");
        section.add(kpiCard("Ca hiện tại", lblCaHienTai, new Color(0xDBEAFE), new Color(0x1E3A8A)));
        section.add(kpiCard("Giờ làm việc", lblGioCa, new Color(0xFEF3C7), new Color(0x92400E)));
        section.add(kpiCard("Nhân viên trực", lblNhanVien, new Color(0xD1FAE5), new Color(0x065F46)));
        section.add(kpiCard("Tiền mở ca", lblTienMoCa, new Color(0xEDE9FE), new Color(0x5B21B6)));
        section.add(kpiCard("Doanh thu ca", lblDoanhThu, new Color(0xFCE7F3), new Color(0x9D174D)));
        return section;
    }

    private JPanel buildShiftReconciliationSection() {
        JPanel section = new JPanel(new MigLayout("insets 14, gap 8, fill", "[grow,fill]", "[]8[grow,fill]"));
        section.setBackground(ThemeColors.PREMIUM_SURFACE);
        section.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.PREMIUM_BORDER, 1, true),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));

        JPanel titleRow = new JPanel(new MigLayout("insets 0, gap 8", "[grow,fill][]", "[]"));
        titleRow.setOpaque(false);

        JLabel sectionTitle = new JLabel("Đối soát ca làm việc");
        sectionTitle.setFont(sectionTitle.getFont().deriveFont(Font.BOLD, 15f));
        sectionTitle.setForeground(ThemeColors.PREMIUM_TEXT_PRIMARY);

        JButton viewAllBtn = new JButton("Xem tất cả");
        viewAllBtn.setFocusPainted(false);
        viewAllBtn.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
        viewAllBtn.setBackground(new Color(248, 250, 252));
        viewAllBtn.setForeground(new Color(15, 23, 42));
        viewAllBtn.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        viewAllBtn.addActionListener(e -> showAllShiftReconciliationsDialog());

        titleRow.add(sectionTitle, "aligny center");
        titleRow.add(viewAllBtn, "aligny center");
        section.add(titleRow, "wrap");

        configureShiftTable();
        JScrollPane scroll = new JScrollPane(shiftTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        section.add(scroll, "grow");

        return section;
    }

    private void configureShiftTable() {
        shiftTable.setRowHeight(32);
        shiftTable.setFillsViewportHeight(true);
        shiftTable.setShowGrid(true);
        shiftTable.setGridColor(new Color(226, 232, 240));
        shiftTable.setSelectionBackground(new Color(219, 234, 254));
        shiftTable.setSelectionForeground(new Color(15, 23, 42));
        shiftTable.getTableHeader().setReorderingAllowed(false);
        shiftTable.getTableHeader().setBackground(new Color(241, 245, 249));
        shiftTable.getTableHeader().setForeground(new Color(15, 23, 42));
        shiftTable.getTableHeader().setFont(shiftTable.getTableHeader().getFont().deriveFont(Font.BOLD, 12f));
        shiftTable.setFont(shiftTable.getFont().deriveFont(12f));

        DefaultTableCellRenderer renderer = new ShiftTableRenderer();
        for (int i = 0; i < shiftTable.getColumnCount(); i++) {
            shiftTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        shiftTable.getColumnModel().getColumn(0).setPreferredWidth(58);
        shiftTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        shiftTable.getColumnModel().getColumn(2).setPreferredWidth(76);
        shiftTable.getColumnModel().getColumn(3).setPreferredWidth(94);
        shiftTable.getColumnModel().getColumn(4).setPreferredWidth(88);
        shiftTable.getColumnModel().getColumn(5).setPreferredWidth(88);
        shiftTable.getColumnModel().getColumn(6).setPreferredWidth(88);
        shiftTable.getColumnModel().getColumn(7).setPreferredWidth(84);
        shiftTable.getColumnModel().getColumn(8).setPreferredWidth(88);
    }

    private JPanel buildOverviewSection() {
        JPanel section = new JPanel(new MigLayout("insets 20, gap 12, wrap 1", "[grow,fill]", "[]12[]"));
        section.setBackground(ThemeColors.PREMIUM_SURFACE);
        section.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.PREMIUM_BORDER, 1, true),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));

        JLabel sectionTitle = new JLabel("Tổng quan vận hành");
        sectionTitle.setFont(sectionTitle.getFont().deriveFont(Font.BOLD, 15f));
        sectionTitle.setForeground(ThemeColors.PREMIUM_TEXT_PRIMARY);
        section.add(sectionTitle);

        JPanel kpiRow = new JPanel(new MigLayout("insets 0, gap 12", "[grow,fill][grow,fill][grow,fill][grow,fill][grow,fill][grow,fill]", "[]"));
        kpiRow.setOpaque(false);
        kpiRow.add(kpiCard("Phòng sắp nhận hôm nay", lblPhongSapNhan, new Color(0xE0F2FE), new Color(0x075985)));
        kpiRow.add(kpiCard("Booking sắp nhận", lblBookingSapNhan, new Color(0xDCFCE7), new Color(0x166534)));
        kpiRow.add(kpiCard("Lợi nhuận tháng này", lblLoiNhuanThang, new Color(0xF0FDFA), new Color(0x0F766E)));
        kpiRow.add(kpiCard("So với tháng trước", lblSoSanhLoiNhuan, new Color(0xFEF9C3), new Color(0x854D0E)));
        kpiRow.add(kpiCard("Doanh thu tháng", lblDoanhThuThang, new Color(0xEEF2FF), new Color(0x3730A3)));
        kpiRow.add(kpiCard("Chi phí tháng", lblChiPhiThang, new Color(0xFEE2E2), new Color(0x991B1B)));
        section.add(kpiRow);

        return section;
    }

    private JPanel buildVisualOverviewSection() {
        JPanel section = new JPanel(new MigLayout("insets 14, gap 10, fill", "[grow,fill]", "[]8[]8[]"));
        section.setBackground(ThemeColors.PREMIUM_SURFACE);
        section.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.PREMIUM_BORDER, 1, true),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));

        JLabel sectionTitle = new JLabel("Tổng quan vận hành");
        sectionTitle.setFont(sectionTitle.getFont().deriveFont(Font.BOLD, 15f));
        sectionTitle.setForeground(ThemeColors.PREMIUM_TEXT_PRIMARY);
        section.add(sectionTitle, "wrap");

        JPanel arrivalPanel = visualPanel(new Color(0xF0F9FF), new Color(0x0EA5E9));
        arrivalPanel.setLayout(new MigLayout("insets 12, gap 5, wrap 1", "[grow,fill]", "[]4[]1[]8[]"));
        lblPhongSapNhan.setFont(lblPhongSapNhan.getFont().deriveFont(Font.BOLD, 22f));
        lblBookingSapNhan.setFont(lblBookingSapNhan.getFont().deriveFont(Font.BOLD, 13f));
        arrivalPanel.add(sectionHeading("Nhịp nhận phòng hôm nay", new Color(0x075985)));
        arrivalPanel.add(lblPhongSapNhan);
        arrivalPanel.add(lblBookingSapNhan);
        arrivalPanel.add(barPhongSapNhan, "h 10!");
        arrivalPanel.add(subtleText("Theo lịch đặt phòng còn chờ nhận phòng"));

        JPanel financePanel = visualPanel(new Color(0xF8FAFC), new Color(0x64748B));
        financePanel.setLayout(new MigLayout("insets 12, gap 6, wrap 1", "[grow,fill]", "[]3[]1[]6[]6[]6[]"));
        lblLoiNhuanThang.setFont(lblLoiNhuanThang.getFont().deriveFont(Font.BOLD, 22f));
        lblSoSanhLoiNhuan.setFont(lblSoSanhLoiNhuan.getFont().deriveFont(Font.BOLD, 13f));
        financePanel.add(sectionHeading("Hiệu quả tài chính tháng", new Color(0x0F172A)));
        financePanel.add(lblLoiNhuanThang);
        financePanel.add(lblSoSanhLoiNhuan);
        financePanel.add(barRow("Doanh thu", lblDoanhThuThang, barDoanhThu));
        financePanel.add(barRow("Chi phí", lblChiPhiThang, barChiPhi));
        financePanel.add(barRow("Lợi nhuận", lblLoiNhuanBar, barLoiNhuan));

        section.add(arrivalPanel, "growx");
        section.add(financePanel, "growx");
        return section;
    }

    private JPanel buildRevenueChartSection() {
        JPanel section = new JPanel(new MigLayout("insets 20, gap 12, fill", "[grow,fill][]", "[]14[grow,fill]"));
        section.setBackground(ThemeColors.PREMIUM_SURFACE);
        section.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.PREMIUM_BORDER, 1, true),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));

        JLabel title = new JLabel("Doanh thu theo thời gian thực");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));
        title.setForeground(ThemeColors.PREMIUM_TEXT_PRIMARY);

        revenueRangeCombo.setFocusable(false);
        revenueRangeCombo.addActionListener(e -> refresh());

        section.add(title, "aligny center");
        section.add(revenueRangeCombo, "w 140!,h 34!,wrap");
        section.add(revenueChartPanel, "span 2,grow");
        return section;
    }

    private JPanel visualPanel(Color bg, Color accent) {
        JPanel panel = new JPanel();
        panel.setBackground(bg);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accent, 1, true),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        return panel;
    }

    private JLabel sectionHeading(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 15f));
        return label;
    }

    private JLabel subtleText(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(ThemeColors.PREMIUM_TEXT_MUTED);
        label.setFont(label.getFont().deriveFont(12f));
        return label;
    }

    private JPanel barRow(String title, JLabel valueLabel, JProgressBar bar) {
        JPanel row = new JPanel(new MigLayout("insets 0, gap 8", "[90!][grow,fill][120!]", "[]"));
        row.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(ThemeColors.PREMIUM_TEXT_SECONDARY);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 12f));
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 13f));
        row.add(titleLabel);
        row.add(bar, "h 12!");
        row.add(valueLabel, "alignx right");
        return row;
    }

    private JPanel kpiCard(String label, JLabel valueLabel, Color bg, Color accent) {
        JPanel card = new JPanel(new MigLayout("insets 11, wrap 1, gap 2", "[grow,fill]", "[]2[]"));
        card.setBackground(bg);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accent.brighter(), 1, true),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        card.setPreferredSize(new Dimension(0, 62));

        JLabel lbl = new JLabel(label);
        lbl.setFont(lbl.getFont().deriveFont(11f));
        lbl.setForeground(accent);

        valueLabel.setForeground(accent);

        card.add(lbl);
        card.add(valueLabel);
        return card;
    }

    private static JLabel kpiValue(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.LEFT);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 16f));
        return lbl;
    }

    private static JProgressBar progressBar(Color color) {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(0);
        bar.setStringPainted(false);
        bar.setBorderPainted(false);
        bar.setForeground(color);
        bar.setBackground(new Color(226, 232, 240));
        return bar;
    }

    public void refresh() {
        if (refreshInProgress) {
            return;
        }
        refreshInProgress = true;
        lblTrangThai.setText("Đang tải...");
        new Thread(() -> {
            try {
                ShiftInfo info = shiftBUS.getCurrentShift();
                DashboardMetrics metrics = loadDashboardMetrics();
                SwingUtilities.invokeLater(() -> {
                    try {
                        applyShiftInfo(info);
                        applyDashboardMetrics(metrics);
                        revalidate();
                        repaint();
                    } finally {
                        refreshInProgress = false;
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    lblTrangThai.setText("Khong tai duoc dashboard: " + ex.getMessage());
                    revalidate();
                    repaint();
                    refreshInProgress = false;
                });
            }
        }, "dashboard-refresh").start();
    }

    private DashboardMetrics loadDashboardMetrics() {
        LocalDate today = LocalDate.now();
        YearMonth thisMonth = YearMonth.from(today);
        YearMonth lastMonth = thisMonth.minusMonths(1);
        LocalDate[] revenueRange = resolveRevenueRange((String) revenueRangeCombo.getSelectedItem(), today);

        KpiSummary current = statisticsBUS.loadKpis(thisMonth.atDay(1), thisMonth.atEndOfMonth());
        KpiSummary previous = statisticsBUS.loadKpis(lastMonth.atDay(1), lastMonth.atEndOfMonth());
        List<RevenuePoint> revenuePoints = statisticsBUS.loadRevenueByRange(revenueRange[0], revenueRange[1]);

        return new DashboardMetrics(
            statisticsBUS.loadUpcomingCheckInRooms(today),
            statisticsBUS.loadUpcomingCheckInBookings(today),
            current.getTotalRooms(),
            current.getRevenue(),
            current.getExpenses(),
            current.getProfit(),
            previous.getProfit(),
            revenuePoints,
            shiftBUS.getActiveAndAssignedShiftReconciliations(5)
        );
    }

    private LocalDate[] resolveRevenueRange(String range, LocalDate today) {
        if ("1 tháng".equals(range)) {
            return new LocalDate[] { today.minusDays(29), today };
        }
        if ("Quý 1".equals(range)) {
            return quarterRange(today.getYear(), 1, 3);
        }
        if ("Quý 2".equals(range)) {
            return quarterRange(today.getYear(), 4, 6);
        }
        if ("Quý 3".equals(range)) {
            return quarterRange(today.getYear(), 7, 9);
        }
        if ("Quý 4".equals(range)) {
            return quarterRange(today.getYear(), 10, 12);
        }
        return new LocalDate[] { today.minusDays(6), today };
    }

    private LocalDate[] quarterRange(int year, int startMonth, int endMonth) {
        return new LocalDate[] {
            LocalDate.of(year, startMonth, 1),
            YearMonth.of(year, endMonth).atEndOfMonth()
        };
    }

    private void applyShiftInfo(ShiftInfo info) {
        if (info == null) {
            lblCaHienTai.setText("Không có");
            lblGioCa.setText("--");
            lblNhanVien.setText("--");
            lblTienMoCa.setText("--");
            lblDoanhThu.setText("--");
            lblGiaoDich.setText("--");
            lblTrangThai.setText("Không tìm thấy ca đang mở");
            return;
        }

        String tenCa = switch (info.loaiCa) {
            case "CaSang"  -> "Ca sáng";
            case "CaChieu" -> "Ca chiều";
            case "CaToi"   -> "Ca tối";
            default        -> info.loaiCa;
        };

        lblCaHienTai.setText(tenCa);
        lblGioCa.setText(info.gioBatDau + " – " + info.gioKetThuc);
        lblNhanVien.setText(info.hoTenNV);
        lblTienMoCa.setText(formatVND(info.tienMoCa));
        lblDoanhThu.setText(formatVND(info.doanhThu));
        lblGiaoDich.setText(info.soGiaoDich + " giao dịch");
        lblTrangThai.setText("Cập nhật lúc " + java.time.LocalTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    private void applyDashboardMetrics(DashboardMetrics metrics) {
        lblPhongSapNhan.setText(metrics.upcomingRooms + " phòng");
        lblBookingSapNhan.setText(metrics.upcomingBookings + " booking");
        lblLoiNhuanThang.setText(formatVND(metrics.currentProfit));
        lblLoiNhuanBar.setText(formatVND(metrics.currentProfit));
        lblSoSanhLoiNhuan.setText(formatProfitDelta(metrics.currentProfit, metrics.previousProfit));
        lblDoanhThuThang.setText(formatVND(metrics.currentRevenue));
        lblChiPhiThang.setText(formatVND(metrics.currentExpenses));
        setProgress(barPhongSapNhan, metrics.upcomingRooms, Math.max(1, metrics.totalRooms));

        double maxFinance = Math.max(Math.abs(metrics.currentRevenue),
            Math.max(Math.abs(metrics.currentExpenses), Math.abs(metrics.currentProfit)));
        int financeMax = (int) Math.max(1, Math.round(maxFinance));
        setProgress(barDoanhThu, metrics.currentRevenue, financeMax);
        setProgress(barChiPhi, metrics.currentExpenses, financeMax);
        setProgress(barLoiNhuan, Math.abs(metrics.currentProfit), financeMax);

        if (metrics.currentProfit < 0) {
            lblLoiNhuanThang.setForeground(new Color(0xDC2626));
            lblLoiNhuanBar.setForeground(new Color(0xDC2626));
            barLoiNhuan.setForeground(new Color(0xDC2626));
        } else {
            lblLoiNhuanThang.setForeground(new Color(0x0F172A));
            lblLoiNhuanBar.setForeground(new Color(0x0F172A));
            barLoiNhuan.setForeground(new Color(0x0F766E));
        }

        double delta = metrics.currentProfit - metrics.previousProfit;
        if (delta < 0) {
            lblSoSanhLoiNhuan.setForeground(new Color(0xDC2626));
        } else if (delta > 0) {
            lblSoSanhLoiNhuan.setForeground(new Color(0x16A34A));
        } else {
            lblSoSanhLoiNhuan.setForeground(new Color(0x64748B));
        }

        revenueChartPanel.setData(metrics.revenuePoints);
        applyShiftReconciliationRows(metrics.shiftRows);
    }

    private void applyShiftReconciliationRows(List<ShiftReconciliationRow> rows) {
        fillShiftRows(shiftTableModel, rows);
    }

    private void showAllShiftReconciliationsDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Tất cả đối soát ca làm việc");
        dialog.setModal(false);
        dialog.setSize(1080, 560);
        dialog.setLocationRelativeTo(this);

        DefaultTableModel model = new DefaultTableModel(
            new Object[] {"Mã ca", "Nhân viên", "Ca", "Mở ca", "Tiền đầu ca", "Doanh thu", "Tiền kết ca", "Chênh lệch", "Trạng thái"},
            0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(34);
        table.setFillsViewportHeight(true);
        table.setShowGrid(true);
        table.setGridColor(new Color(226, 232, 240));
        table.setCellSelectionEnabled(false);
        table.setRowSelectionAllowed(false);
        table.setColumnSelectionAllowed(false);
        table.setFocusable(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 12f));

        DefaultTableCellRenderer renderer = new ShiftTableRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        List<ShiftReconciliationRow> rows = shiftBUS.getRecentShiftReconciliations(200);
        fillShiftRows(model, rows);

        JPanel content = new JPanel(new MigLayout("insets 16, gap 10, fill", "[grow,fill]", "[]8[grow,fill]"));
        content.setBackground(ThemeColors.PREMIUM_BG);
        JLabel title = new JLabel("Tất cả đối soát ca làm việc");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        title.setForeground(ThemeColors.PREMIUM_TEXT_PRIMARY);
        content.add(title, "wrap");
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        scroll.getViewport().setBackground(Color.WHITE);
        content.add(scroll, "grow");

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

    private void fillShiftRows(DefaultTableModel model, List<ShiftReconciliationRow> rows) {
        model.setRowCount(0);
        if (rows == null || rows.isEmpty()) {
            model.addRow(new Object[] {"--", "Chưa có dữ liệu", "--", "--", "--", "--", "--", "--", "--"});
            return;
        }

        for (ShiftReconciliationRow row : rows) {
            boolean closed = "DaKet".equalsIgnoreCase(row.trangThai);
            double expectedCash = row.tienMoCa + row.doanhThuTienMat;
            double diff = row.tienKetCa - expectedCash;
            model.addRow(new Object[] {
                row.maPC,
                row.hoTenNV,
                displayShiftName(row.loaiCa),
                formatDateTime(row.thoiGianMoCa),
                formatVND(row.tienMoCa),
                formatVND(row.doanhThuHeThong),
                closed ? formatVND(row.tienKetCa) : "--",
                closed ? formatSignedVND(diff) : "--",
                displayShiftStatus(row.trangThai)
            });
        }
    }

    private static void setProgress(JProgressBar bar, double value, double max) {
        int maxValue = (int) Math.max(1, Math.round(max));
        int barValue = (int) Math.max(0, Math.min(maxValue, Math.round(value)));
        bar.setMaximum(maxValue);
        bar.setValue(barValue);
    }

    private static String formatProfitDelta(double current, double previous) {
        double delta = current - previous;
        if (previous == 0) {
            return (delta >= 0 ? "+" : "-") + formatVND(Math.abs(delta));
        }
        double percent = (delta / Math.abs(previous)) * 100.0;
        return String.format("%s%.1f%%", percent >= 0 ? "+" : "", percent);
    }

    private static String formatVND(double amount) {
        return String.format("%,.0f đ", amount).replace(',', '.');
    }
    private static String formatSignedVND(double amount) {
        if (amount == 0) {
            return "0 đ";
        }
        return (amount > 0 ? "+" : "-") + formatVND(Math.abs(amount));
    }

    private static String formatDateTime(LocalDateTime value) {
        if (value == null) {
            return "--";
        }
        return value.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
    }

    private static String displayShiftName(String loaiCa) {
        if ("CaSang".equals(loaiCa)) {
            return "Ca sáng";
        }
        if ("CaChieu".equals(loaiCa)) {
            return "Ca chiều";
        }
        if ("CaToi".equals(loaiCa)) {
            return "Ca tối";
        }
        return loaiCa == null ? "--" : loaiCa;
    }

    private static String displayShiftStatus(String trangThai) {
        if ("DangMo".equalsIgnoreCase(trangThai)) {
            return "Đang mở";
        }
        if ("DaKet".equalsIgnoreCase(trangThai)) {
            return "Đã kết";
        }
        if ("DaPhanCong".equalsIgnoreCase(trangThai)) {
            return "Đã phân công";
        }
        return trangThai == null ? "--" : trangThai;
    }

    private static String formatCompactVND(double amount) {
        double abs = Math.abs(amount);
        String sign = amount < 0 ? "-" : "";
        if (abs >= 1_000_000_000) return sign + String.format("%.1f tỷ", abs / 1_000_000_000.0);
        if (abs >= 1_000_000) return sign + String.format("%.1f tr", abs / 1_000_000.0);
        if (abs >= 1_000) return sign + String.format("%.0fK", abs / 1_000.0);
        return sign + String.format("%.0f", abs);
    }

    private static final class ShiftTableRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            setHorizontalAlignment(column >= 4 && column <= 7 ? SwingConstants.RIGHT : SwingConstants.LEFT);

            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                c.setForeground(new Color(15, 23, 42));
            }

            String text = value == null ? "" : value.toString();
            if (column == 7 && !"--".equals(text)) {
                if (text.startsWith("-")) {
                    c.setForeground(new Color(220, 38, 38));
                } else if (text.startsWith("+")) {
                    c.setForeground(new Color(217, 119, 6));
                } else {
                    c.setForeground(new Color(22, 163, 74));
                }
            }
            if (column == 8) {
                if (text.contains("Đang")) {
                    c.setForeground(new Color(37, 99, 235));
                } else if (text.contains("kết")) {
                    c.setForeground(new Color(22, 163, 74));
                } else {
                    c.setForeground(new Color(100, 116, 139));
                }
            }
            return c;
        }
    }

    private static final class RevenueChartPanel extends JPanel {
        private List<RevenuePoint> data = Collections.emptyList();

        private RevenueChartPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(0, 360));
        }

        private void setData(List<RevenuePoint> data) {
            this.data = data == null ? Collections.emptyList() : data;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int left = 64, right = 24, top = 18, bottom = 42;
            int chartW = Math.max(1, w - left - right);
            int chartH = Math.max(1, h - top - bottom);

            double max = 0;
            double min = 0;
            for (RevenuePoint point : data) {
                max = Math.max(max, Math.max(point.getRevenue(), point.getProfit()));
                min = Math.min(min, point.getProfit());
            }
            max = (max <= 0 && min >= 0) ? 1_000_000 : max * 1.15;
            min = min < 0 ? min * 1.15 : 0;

            double range = max - min;
            if (range == 0) range = 1_000_000;

            // Draw Legend
            int legendX = w - right - 240;
            int legendY = top - 12;
            g2.setFont(g2.getFont().deriveFont(11f));
            
            g2.setColor(new Color(147, 197, 253)); // Blue-300
            g2.fillRect(legendX, legendY, 12, 12);
            g2.setColor(new Color(100, 116, 139));
            g2.drawString("Doanh thu", legendX + 18, legendY + 11);
            
            legendX += 85;
            g2.setColor(new Color(37, 99, 235)); // Blue-600
            g2.fillRect(legendX, legendY, 12, 12);
            g2.setColor(new Color(100, 116, 139));
            g2.drawString("Lời", legendX + 18, legendY + 11);
            
            legendX += 55;
            g2.setColor(new Color(220, 38, 38)); // Red-600
            g2.fillRect(legendX, legendY, 12, 12);
            g2.setColor(new Color(100, 116, 139));
            g2.drawString("Lỗ", legendX + 18, legendY + 11);

            g2.setFont(g2.getFont().deriveFont(11f));
            for (int i = 0; i <= 4; i++) {
                int y = top + chartH * i / 4;
                double value = max - range * i / 4.0;
                
                g2.setColor(new Color(226, 232, 240));
                g2.drawLine(left, y, left + chartW, y);
                
                g2.setColor(new Color(100, 116, 139));
                g2.drawString(formatCompactVND(value), 8, y + 4);
            }

            int zeroY = top + (int) Math.round(chartH * (max / range));
            if (zeroY >= top && zeroY <= top + chartH) {
                g2.setColor(new Color(148, 163, 184));
                g2.drawLine(left, zeroY, left + chartW, zeroY);
            }

            if (data.isEmpty()) {
                g2.setColor(new Color(100, 116, 139));
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14f));
                g2.drawString("Chưa có dữ liệu", left + 16, top + chartH / 2);
                g2.dispose();
                return;
            }

            int n = data.size();
            int step = Math.max(1, chartW / n);
            int barW = Math.max(8, Math.min(28, step / 2));
            for (int i = 0; i < n; i++) {
                RevenuePoint point = data.get(i);
                int x = left + i * step + (step - barW) / 2;

                int revH = (int) Math.round(chartH * (point.getRevenue() / range));
                if (revH > 0) {
                    int revY = zeroY - revH;
                    g2.setColor(new Color(147, 197, 253)); // Blue-300
                    g2.fillRoundRect(x, revY, barW, revH, 8, 8);
                    if (revH > 4) g2.fillRect(x, revY + revH - 4, barW, 4);
                }

                double profit = point.getProfit();
                int profH = (int) Math.round(chartH * (Math.abs(profit) / range));
                if (profH > 0) {
                    if (profit >= 0) {
                        int profY = zeroY - profH;
                        g2.setColor(new Color(37, 99, 235)); // Blue-600
                        g2.fillRoundRect(x, profY, barW, profH, 8, 8);
                        if (profH > 4) g2.fillRect(x, profY + profH - 4, barW, 4);
                    } else {
                        int profY = zeroY;
                        g2.setColor(new Color(220, 38, 38)); // Red-600
                        g2.fillRoundRect(x, profY, barW, profH, 8, 8);
                        if (profH > 4) g2.fillRect(x, profY, barW, 4);
                    }
                }

                if (i == 0 || i == n - 1 || (n <= 12 && i % 2 == 0) || (n <= 31 && i % 5 == 0)) {
                    g2.setColor(new Color(100, 116, 139));
                    String label = point.getLabel();
                    int labelW = g2.getFontMetrics().stringWidth(label);
                    g2.drawString(label, x + barW / 2 - labelW / 2, top + chartH + 22);
                }
            }
            g2.dispose();
        }
    }

    private static final class DashboardMetrics {
        private final int upcomingRooms;
        private final int upcomingBookings;
        private final int totalRooms;
        private final double currentRevenue;
        private final double currentExpenses;
        private final double currentProfit;
        private final double previousProfit;
        private final List<RevenuePoint> revenuePoints;
        private final List<ShiftReconciliationRow> shiftRows;

        private DashboardMetrics(int upcomingRooms, int upcomingBookings, int totalRooms, double currentRevenue,
                                 double currentExpenses, double currentProfit, double previousProfit,
                                 List<RevenuePoint> revenuePoints,
                                 List<ShiftReconciliationRow> shiftRows) {
            this.upcomingRooms = upcomingRooms;
            this.upcomingBookings = upcomingBookings;
            this.totalRooms = totalRooms;
            this.currentRevenue = currentRevenue;
            this.currentExpenses = currentExpenses;
            this.currentProfit = currentProfit;
            this.previousProfit = previousProfit;
            this.revenuePoints = revenuePoints == null ? Collections.emptyList() : revenuePoints;
            this.shiftRows = shiftRows == null ? Collections.emptyList() : shiftRows;
        }
    }
}
