package kqlhotel.gui.tabs;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import kqlhotel.bus.statistics.StatisticsBUS;
import kqlhotel.entity.statistics.ExpenseRecord;
import kqlhotel.entity.statistics.HotelKpiPoint;
import kqlhotel.entity.statistics.KpiSummary;
import kqlhotel.entity.statistics.OccupancyPoint;
import kqlhotel.entity.statistics.RecentBooking;
import kqlhotel.entity.statistics.RevenuePoint;
import kqlhotel.entity.statistics.RoomTypeShare;
import kqlhotel.gui.components.PrimaryButton;
import kqlhotel.gui.components.RoundedPanel;
import kqlhotel.gui.components.DatePicker;
import kqlhotel.gui.theme.ThemeColors;
import net.miginfocom.swing.MigLayout;

public class StatisticsPanel extends JPanel {
    private final StatisticsBUS bus = new StatisticsBUS();
    private final NumberFormat moneyFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

    private final MonthlyRevenueChartPanel monthlyRevenueChartPanel = new MonthlyRevenueChartPanel();
    private final AdrTrendChartPanel       adrTrendChartPanel       = new AdrTrendChartPanel();
    private final RoomDistributionPanel    roomDistributionPanel    = new RoomDistributionPanel();
    private final OccupancyTrendPanel      occupancyTrendPanel      = new OccupancyTrendPanel();

    private final Map<String, PrimaryButton> viewButtons  = new LinkedHashMap<>();
    private final CardLayout analyticsCards = new CardLayout();
    private final JPanel analyticsContent = new JPanel(analyticsCards);

    // Date range pickers
    private DatePicker fromDatePicker;
    private DatePicker toDatePicker;

    private String activeView  = "Doanh thu";

    // KPI labels — cập nhật bởi loadData()
    // 4 KPI chuẩn ngành khách sạn: Doanh thu, ADR, RevPAR, TrevPAR
    private final JLabel kpiRevenueValue = new JLabel("--");
    private final JLabel kpiExpenseValue = new JLabel("--");
    private final JLabel kpiExpenseSub   = new JLabel("Chờ dữ liệu");
    private final JLabel kpiProfitValue  = new JLabel("--");
    private final JLabel kpiProfitSub    = new JLabel("Chờ dữ liệu");
    private final JLabel kpiRevenueSub   = new JLabel("Chờ dữ liệu");
    private final JLabel kpiAdrValue     = new JLabel("--");
    private final JLabel kpiAdrSub       = new JLabel("Chờ dữ liệu");
    private final JLabel kpiRevparValue  = new JLabel("--");
    private final JLabel kpiRevparSub    = new JLabel("Chờ dữ liệu");
    private final JLabel kpiTrevparValue = new JLabel("--");
    private final JLabel kpiTrevparSub   = new JLabel("Chờ dữ liệu");

    // Chart subtitle labels — cập nhật động
    private JLabel revenueChartSubtitle;
    private JLabel occupancyChartSubtitle;

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
        JPanel top = new JPanel(new MigLayout("insets 0,gap 8,fillx", "[grow,fill][][][][][][]", "[]"));
        top.setOpaque(false);

        JLabel leftHint = new JLabel("Bộ lọc báo cáo");
        leftHint.setForeground(new Color(102, 124, 160));
        leftHint.setFont(leftHint.getFont().deriveFont(Font.BOLD, 13f));

        // From date picker
        fromDatePicker = new DatePicker();
        fromDatePicker.setSelectedDate(LocalDate.now().minusDays(30));
        fromDatePicker.addDateChangeListener(() -> loadData());

        // To date picker
        toDatePicker = new DatePicker();
        toDatePicker.setSelectedDate(LocalDate.now());
        toDatePicker.addDateChangeListener(() -> loadData());

        JLabel fromLabel = new JLabel("Từ:");
        fromLabel.setForeground(new Color(102, 124, 160));
        JLabel toLabel = new JLabel("Đến:");
        toLabel.setForeground(new Color(102, 124, 160));

        PrimaryButton exportBtn = new PrimaryButton("Xuất báo cáo");
        exportBtn.setBackground(ThemeColors.PREMIUM_ACCENT);
        exportBtn.setForeground(Color.WHITE);
        exportBtn.addActionListener(e -> exportStatisticsReport());

        PrimaryButton expenseBtn = new PrimaryButton("Quản lý Chi phí");
        expenseBtn.setBackground(ThemeColors.PREMIUM_SURFACE_HOVER);
        expenseBtn.setForeground(ThemeColors.PREMIUM_TEXT_SECONDARY);
        expenseBtn.addActionListener(e -> showExpenseManagerDialog());

        top.add(leftHint, "growx,pushx,aligny center");
        top.add(fromLabel, "aligny center");
        top.add(fromDatePicker, "w 120!,h 32!");
        top.add(toLabel, "aligny center");
        top.add(toDatePicker, "w 120!,h 32!");
        top.add(expenseBtn, "h 38!");
        top.add(exportBtn, "h 38!");

        return top;
    }

    // ============================== KPI ROW ==============================
    private JPanel createKpiRow() {
        // Dùng GridLayout để 4 card luôn có cùng kích thước, không bị xê dịch khi nội dung thay đổi
        JPanel row = new JPanel(new GridLayout(1, 4, 10, 0));
        row.setOpaque(false);

        row.add(kpiCard("Doanh thu", kpiRevenueValue, kpiRevenueSub));
        row.add(kpiCard("Chi phí",   kpiExpenseValue, kpiExpenseSub));
        row.add(kpiCard("Lợi nhuận", kpiProfitValue,  kpiProfitSub));
        row.add(kpiCard("ADR",       kpiAdrValue,     kpiAdrSub));

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
        row.add(createViewButton("ADR Trend"),         "h 34!");
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
        JPanel occupancyTitleBlock = new JPanel(new MigLayout("wrap 1,insets 0,gap 2", "[grow,fill]", "[][]"));
        occupancyTitleBlock.setOpaque(false);
        JLabel occupancyTitle = new JLabel("Tỷ lệ lấp đầy");
        occupancyTitle.setForeground(new Color(14, 30, 62));
        occupancyTitle.setFont(occupancyTitle.getFont().deriveFont(Font.BOLD, 14f));
        occupancyChartSubtitle = new JLabel("7 ngày gần nhất");
        occupancyChartSubtitle.setForeground(new Color(124, 142, 171));
        occupancyChartSubtitle.setFont(occupancyChartSubtitle.getFont().deriveFont(12f));
        occupancyTitleBlock.add(occupancyTitle);
        occupancyTitleBlock.add(occupancyChartSubtitle);
        occupancyCard.add(occupancyTitleBlock, "aligny top");
        occupancyCard.add(occupancyTrendPanel, "grow,push");

        // ADR Trend card
        RoundedPanel adrCard = new RoundedPanel(18, Color.WHITE, new Color(214, 223, 238), 1f);
        adrCard.setLayout(new MigLayout(cardLayout, cardCols, cardRows));
        adrCard.add(sectionTitle("ADR Trend", "Giá phòng trung bình theo ngày"), "aligny top");
        adrCard.add(adrTrendChartPanel, "grow,push");

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
        analyticsContent.add(adrCard,      "ADR Trend");
        analyticsContent.add(recentCard,   "Đặt phòng gần đây");
        analyticsCards.show(analyticsContent, activeView);

        return analyticsContent;
    }

    /** Mở dialog modal với JTable + JScrollPane nội bộ để browse toàn bộ booking. */
    private void showExpenseManagerDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner instanceof Frame ? (Frame) owner : null, "Quản lý Chi phí", true);
        dlg.setSize(1100, 600);
        dlg.setMinimumSize(new java.awt.Dimension(1000, 560));
        dlg.setLocationRelativeTo(owner);
        dlg.setLayout(new MigLayout("insets 14,gap 10,fill", "[grow,fill]", "[][grow,fill][]"));

        String[] cols = {"ID", "Loại", "Tên chi phí", "Số tiền", "Ngày", "Ghi chú"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFocusable(false);
        table.setShowGrid(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 13f));
        reloadExpenseTable(model);

        JLabel title = new JLabel("Danh sách chi phí nhập tay");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        title.setForeground(new Color(14, 30, 62));
        dlg.add(title, "wrap");
        dlg.add(new JScrollPane(table), "grow,wrap");

        JPanel form = new JPanel(new MigLayout("insets 0,gap 8,fillx", "[][150!][][grow,fill][][130!][][110!][]", "[]"));
        form.setOpaque(false);
        JComboBox<String> typeCombo = new JComboBox<>(new String[] {"Điện nước", "Vật tư", "Khác"});
        JTextField nameField = new JTextField();
        JTextField amountField = new JTextField();
        DatePicker datePicker = new DatePicker();
        datePicker.setSelectedDate(LocalDate.now());
        JTextField noteField = new JTextField();
        Runnable clearTableSelection = () -> table.clearSelection();
        typeCombo.addActionListener(e -> clearTableSelection.run());
        nameField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { clearTableSelection.run(); }
        });
        amountField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { clearTableSelection.run(); }
        });
        noteField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { clearTableSelection.run(); }
        });
        datePicker.addDateChangeListener(clearTableSelection);

        PrimaryButton addBtn = new PrimaryButton("Thêm");
        addBtn.setBackground(ThemeColors.PREMIUM_PRIMARY);
        addBtn.setForeground(Color.WHITE);
        PrimaryButton deleteBtn = new PrimaryButton("Xóa");
        deleteBtn.setBackground(new Color(239, 68, 68));
        deleteBtn.setForeground(Color.WHITE);

        form.add(new JLabel("Loại"));
        form.add(typeCombo, "h 34!");
        form.add(new JLabel("Tên"));
        form.add(nameField, "h 34!");
        form.add(new JLabel("Số tiền"));
        form.add(amountField, "h 34!");
        form.add(new JLabel("Ngày"));
        form.add(datePicker, "h 34!");
        form.add(addBtn, "h 34!,wrap");
        form.add(new JLabel("Ghi chú"));
        form.add(noteField, "span 7,growx,h 34!");
        form.add(deleteBtn, "h 34!");
        dlg.add(form, "growx");

        addBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            double amount = parseMoneyInput(amountField.getText());
            if (name.isEmpty() || amount <= 0 || datePicker.getSelectedDate() == null) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập tên, số tiền > 0 và ngày chi.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }
            boolean ok = bus.addExpense((String) typeCombo.getSelectedItem(), name, amount, datePicker.getSelectedDate(), noteField.getText().trim());
            if (!ok) {
                JOptionPane.showMessageDialog(dlg, "Không thêm được chi phí. Kiểm tra bảng ChiPhi trong CSDL.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            nameField.setText("");
            amountField.setText("");
            noteField.setText("");
            reloadExpenseTable(model);
            loadData();
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dlg, "Chọn một dòng chi phí cần xóa.", "Chưa chọn dòng", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = table.convertRowIndexToModel(row);
            int id = ((Number) model.getValueAt(modelRow, 0)).intValue();
            boolean ok = bus.deleteExpense(id);
            if (!ok) {
                JOptionPane.showMessageDialog(dlg, "Không xóa được chi phí.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            reloadExpenseTable(model);
            loadData();
        });

        dlg.setVisible(true);
    }

    private void reloadExpenseTable(DefaultTableModel model) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        model.setRowCount(0);
        for (ExpenseRecord item : bus.loadExpenses()) {
            model.addRow(new Object[] {
                item.getId(),
                item.getType(),
                item.getName(),
                formatVnd(item.getAmount()),
                item.getDate() == null ? "" : item.getDate().format(df),
                item.getNote() == null ? "" : item.getNote()
            });
        }
    }

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

    public void refresh() {
        loadData();
    }

    // ============================== DATA LOADING ==============================
    private void loadData() {
        try {
            LocalDate startDate = fromDatePicker.getSelectedDate();
            LocalDate endDate = toDatePicker.getSelectedDate();

            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String rangeLabel = startDate.format(df) + " - " + endDate.format(df);

            KpiSummary kpi = bus.loadKpis(startDate, endDate);

            kpiRevenueValue.setText(formatVnd(kpi.getRevenue()));
            double manualExpenses = bus.loadManualExpenses(startDate, endDate);
            double salaryExpenses = bus.loadSalaryExpenses(startDate, endDate);
            kpiExpenseValue.setText(formatVnd(kpi.getExpenses()));
            kpiExpenseSub.setText("Lương: " + formatVnd(salaryExpenses) + " | Khác: " + formatVnd(manualExpenses));
            kpiProfitValue.setText(formatVnd(kpi.getProfit()));
            kpiProfitSub.setText("Biên lợi nhuận: " + formatPercent(kpi.getRevenue() == 0 ? 0 : kpi.getProfit() / kpi.getRevenue()));
            kpiRevenueSub.setText("Trong " + rangeLabel);

            kpiAdrValue.setText(formatVnd(bus.loadAdr(startDate, endDate)));
            kpiAdrSub.setText("Giá phòng TB " + rangeLabel);

            kpiRevparValue.setText(formatVnd(bus.loadRevpar(startDate, endDate)));
            kpiRevparSub.setText("Doanh thu/phòng " + rangeLabel);

            kpiTrevparValue.setText(formatVnd(bus.loadTrevpar(startDate, endDate)));
            kpiTrevparSub.setText("Tổng doanh thu/phòng " + rangeLabel);

            monthlyRevenueChartPanel.setData(bus.loadRevenueByRange(startDate, endDate));
            if (revenueChartSubtitle != null) {
                revenueChartSubtitle.setText(rangeLabel);
            }

            adrTrendChartPanel.setData(bus.loadAdrTrend(startDate, endDate));

            roomDistributionPanel.setData(bus.loadRoomTypeDistribution());
            occupancyTrendPanel.setData(bus.loadOccupancyTrend(startDate, endDate));
            if (occupancyChartSubtitle != null) {
                occupancyChartSubtitle.setText(rangeLabel);
            }
            populateRecentList(bus.loadRecentBookings());
        } catch (Exception ex) {
            System.err.println("StatisticsPanel.loadData: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void exportStatisticsReport() {
        LocalDate startDate = fromDatePicker.getSelectedDate();
        LocalDate endDate = toDatePicker.getSelectedDate();
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn khoảng ngày hợp lệ trước khi xuất báo cáo.",
                "Xuất báo cáo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu báo cáo thống kê");
        chooser.setFileFilter(new FileNameExtensionFilter("HTML report (*.html)", "html"));
        chooser.setSelectedFile(new File("BaoCaoThongKe_"
            + startDate.format(DateTimeFormatter.BASIC_ISO_DATE)
            + "_"
            + endDate.format(DateTimeFormatter.BASIC_ISO_DATE)
            + ".html"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selected = chooser.getSelectedFile();
        if (!selected.getName().toLowerCase(Locale.ROOT).endsWith(".html")) {
            selected = new File(selected.getParentFile(), selected.getName() + ".html");
        }

        try {
            String html = buildStatisticsReportHtml(startDate, endDate);
            Files.writeString(selected.toPath(), html, StandardCharsets.UTF_8);
            int option = JOptionPane.showConfirmDialog(this,
                "Đã lưu báo cáo tại:\n" + selected.getAbsolutePath() + "\n\nBạn có muốn mở báo cáo ngay không?",
                "Xuất báo cáo",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
            if (option == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(selected);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Không thể lưu báo cáo: " + ex.getMessage(),
                "Xuất báo cáo",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Không thể tạo báo cáo: " + ex.getMessage(),
                "Xuất báo cáo",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private String buildStatisticsReportHtml(LocalDate startDate, LocalDate endDate) {
        DateTimeFormatter displayDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter displayDateTime = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        KpiSummary kpi = bus.loadKpis(startDate, endDate);
        double manualExpenses = bus.loadManualExpenses(startDate, endDate);
        double salaryExpenses = bus.loadSalaryExpenses(startDate, endDate);
        double adr = bus.loadAdr(startDate, endDate);
        double revpar = bus.loadRevpar(startDate, endDate);
        double trevpar = bus.loadTrevpar(startDate, endDate);
        List<RevenuePoint> revenuePoints = bus.loadRevenueByRange(startDate, endDate);
        List<OccupancyPoint> occupancyPoints = bus.loadOccupancyTrend(startDate, endDate);
        List<HotelKpiPoint> adrPoints = bus.loadAdrTrend(startDate, endDate);
        List<RoomTypeShare> roomShares = bus.loadRoomTypeDistribution();
        List<RecentBooking> recentBookings = bus.loadAllRecentBookings();
        List<ExpenseRecord> expenses = filterExpensesByRange(bus.loadExpenses(), startDate, endDate);

        StringBuilder html = new StringBuilder();
        html.append("<!doctype html><html lang=\"vi\"><head><meta charset=\"UTF-8\">")
            .append("<title>Báo cáo thống kê Roomify</title>")
            .append("<style>")
            .append("body{margin:0;background:#edf2f7;color:#0f172a;font-family:Segoe UI,Arial,sans-serif;}")
            .append(".page{max-width:1180px;margin:28px auto;padding:0 24px 40px;}")
            .append(".hero{background:#172554;color:#fff;border-radius:18px;padding:28px 32px;margin-bottom:18px;}")
            .append(".hero h1{margin:0 0 8px;font-size:30px}.hero p{margin:4px 0;color:#bfdbfe}")
            .append(".grid{display:grid;grid-template-columns:repeat(4,1fr);gap:14px;margin-bottom:18px;}")
            .append(".card{background:#fff;border:1px solid #dbe4f0;border-radius:14px;padding:18px;box-shadow:0 8px 24px rgba(15,23,42,.06);}")
            .append(".label{font-size:13px;color:#64748b;margin-bottom:8px}.value{font-size:25px;font-weight:800}.sub{font-size:12px;color:#64748b;margin-top:8px}")
            .append("section{background:#fff;border:1px solid #dbe4f0;border-radius:14px;padding:18px;margin-top:14px;}")
            .append("h2{font-size:18px;margin:0 0 12px}table{width:100%;border-collapse:collapse;font-size:13px}")
            .append("th,td{padding:10px 12px;border-bottom:1px solid #e2e8f0;text-align:left}th{background:#f8fafc;color:#475569}")
            .append(".num{text-align:right}.muted{color:#64748b}.positive{color:#047857}.negative{color:#dc2626}")
            .append("@media print{body{background:#fff}.page{max-width:none;margin:0}.card,section,.hero{box-shadow:none}}")
            .append("</style></head><body><main class=\"page\">");

        html.append("<div class=\"hero\"><h1>Báo cáo thống kê Roomify</h1>")
            .append("<p>Khoảng thời gian: ").append(escapeHtml(startDate.format(displayDate)))
            .append(" - ").append(escapeHtml(endDate.format(displayDate))).append("</p>")
            .append("<p>Xuất lúc: ").append(escapeHtml(LocalDateTime.now().format(displayDateTime))).append("</p></div>");

        html.append("<div class=\"grid\">")
            .append(metricCard("Doanh thu", formatMoneyFull(kpi.getRevenue()), "Tổng doanh thu đã ghi nhận"))
            .append(metricCard("Chi phí", formatMoneyFull(kpi.getExpenses()), "Lương: " + formatMoneyFull(salaryExpenses) + " | Khác: " + formatMoneyFull(manualExpenses)))
            .append(metricCard("Lợi nhuận", formatMoneyFull(kpi.getProfit()), "Biên lợi nhuận: " + formatPercent(kpi.getRevenue() == 0 ? 0 : kpi.getProfit() / kpi.getRevenue())))
            .append(metricCard("ADR", formatMoneyFull(adr), "Giá phòng trung bình"))
            .append(metricCard("RevPAR", formatMoneyFull(revpar), "Doanh thu/phòng khả dụng"))
            .append(metricCard("TrevPAR", formatMoneyFull(trevpar), "Tổng doanh thu/phòng khả dụng"))
            .append(metricCard("Booking", String.valueOf(kpi.getTotalBookings()), "Số lượt đặt trong kỳ"))
            .append(metricCard("Lấp đầy", formatPercent(kpi.getOccupancyRate()), kpi.getOccupiedRooms() + "/" + kpi.getTotalRooms() + " phòng đang dùng"))
            .append("</div>");

        appendRevenueTable(html, revenuePoints);
        appendExpenseTable(html, expenses);
        appendOccupancyTable(html, occupancyPoints, displayDate);
        appendAdrTable(html, adrPoints, displayDate);
        appendRoomShareTable(html, roomShares);
        appendRecentBookingTable(html, recentBookings, displayDateTime);

        html.append("</main></body></html>");
        return html.toString();
    }

    private List<ExpenseRecord> filterExpensesByRange(List<ExpenseRecord> all, LocalDate startDate, LocalDate endDate) {
        List<ExpenseRecord> filtered = new ArrayList<>();
        if (all == null) {
            return filtered;
        }
        for (ExpenseRecord item : all) {
            if (item.getDate() == null) {
                continue;
            }
            LocalDate date = item.getDate().toLocalDate();
            if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    private String metricCard(String label, String value, String sub) {
        return "<div class=\"card\"><div class=\"label\">" + escapeHtml(label) + "</div><div class=\"value\">"
            + escapeHtml(value) + "</div><div class=\"sub\">" + escapeHtml(sub) + "</div></div>";
    }

    private void appendRevenueTable(StringBuilder html, List<RevenuePoint> points) {
        html.append("<section><h2>Doanh thu và lợi nhuận theo kỳ</h2><table><thead><tr>")
            .append("<th>Kỳ</th><th class=\"num\">Doanh thu</th><th class=\"num\">Lợi nhuận ước tính</th>")
            .append("</tr></thead><tbody>");
        if (points == null || points.isEmpty()) {
            appendEmptyRow(html, 3);
        } else {
            for (RevenuePoint point : points) {
                html.append("<tr><td>").append(escapeHtml(point.getLabel())).append("</td><td class=\"num\">")
                    .append(escapeHtml(formatMoneyFull(point.getRevenue()))).append("</td><td class=\"num ")
                    .append(point.getProfit() >= 0 ? "positive" : "negative").append("\">")
                    .append(escapeHtml(formatMoneyFull(point.getProfit()))).append("</td></tr>");
            }
        }
        html.append("</tbody></table></section>");
    }

    private void appendExpenseTable(StringBuilder html, List<ExpenseRecord> expenses) {
        html.append("<section><h2>Chi phí nhập tay</h2><table><thead><tr>")
            .append("<th>ID</th><th>Loại</th><th>Tên chi phí</th><th class=\"num\">Số tiền</th><th>Ngày</th><th>Ghi chú</th>")
            .append("</tr></thead><tbody>");
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if (expenses == null || expenses.isEmpty()) {
            appendEmptyRow(html, 6);
        } else {
            for (ExpenseRecord item : expenses) {
                html.append("<tr><td>").append(item.getId()).append("</td><td>").append(escapeHtml(item.getType()))
                    .append("</td><td>").append(escapeHtml(item.getName())).append("</td><td class=\"num\">")
                    .append(escapeHtml(formatMoneyFull(item.getAmount()))).append("</td><td>")
                    .append(item.getDate() == null ? "" : escapeHtml(item.getDate().format(df))).append("</td><td>")
                    .append(escapeHtml(item.getNote())).append("</td></tr>");
            }
        }
        html.append("</tbody></table></section>");
    }

    private void appendOccupancyTable(StringBuilder html, List<OccupancyPoint> points, DateTimeFormatter df) {
        html.append("<section><h2>Tỷ lệ lấp đầy</h2><table><thead><tr>")
            .append("<th>Ngày</th><th class=\"num\">Phòng sử dụng</th><th class=\"num\">Tổng phòng</th><th class=\"num\">Tỷ lệ</th>")
            .append("</tr></thead><tbody>");
        if (points == null || points.isEmpty()) {
            appendEmptyRow(html, 4);
        } else {
            for (OccupancyPoint point : points) {
                html.append("<tr><td>").append(escapeHtml(point.getDate().format(df))).append("</td><td class=\"num\">")
                    .append(point.getOccupiedRooms()).append("</td><td class=\"num\">").append(point.getTotalRooms())
                    .append("</td><td class=\"num\">").append(escapeHtml(formatPercent(point.getRate()))).append("</td></tr>");
            }
        }
        html.append("</tbody></table></section>");
    }

    private void appendAdrTable(StringBuilder html, List<HotelKpiPoint> points, DateTimeFormatter df) {
        html.append("<section><h2>ADR / RevPAR / TrevPAR</h2><table><thead><tr>")
            .append("<th>Ngày</th><th class=\"num\">ADR</th><th class=\"num\">RevPAR</th><th class=\"num\">TrevPAR</th>")
            .append("</tr></thead><tbody>");
        if (points == null || points.isEmpty()) {
            appendEmptyRow(html, 4);
        } else {
            for (HotelKpiPoint point : points) {
                html.append("<tr><td>").append(escapeHtml(point.getDate().format(df))).append("</td><td class=\"num\">")
                    .append(escapeHtml(formatMoneyFull(point.getAdr()))).append("</td><td class=\"num\">")
                    .append(escapeHtml(formatMoneyFull(point.getRevpar()))).append("</td><td class=\"num\">")
                    .append(escapeHtml(formatMoneyFull(point.getTrevpar()))).append("</td></tr>");
            }
        }
        html.append("</tbody></table></section>");
    }

    private void appendRoomShareTable(StringBuilder html, List<RoomTypeShare> shares) {
        html.append("<section><h2>Phân bổ loại phòng</h2><table><thead><tr>")
            .append("<th>Loại phòng</th><th class=\"num\">Số lượng</th>")
            .append("</tr></thead><tbody>");
        if (shares == null || shares.isEmpty()) {
            appendEmptyRow(html, 2);
        } else {
            for (RoomTypeShare share : shares) {
                html.append("<tr><td>").append(escapeHtml(share.getLabel())).append("</td><td class=\"num\">")
                    .append(share.getCount()).append("</td></tr>");
            }
        }
        html.append("</tbody></table></section>");
    }

    private void appendRecentBookingTable(StringBuilder html, List<RecentBooking> bookings, DateTimeFormatter df) {
        html.append("<section><h2>Đặt phòng gần đây</h2><table><thead><tr>")
            .append("<th>Phòng</th><th>Khách</th><th>Loại phòng</th><th>Trạng thái</th><th>Ngày đặt</th>")
            .append("</tr></thead><tbody>");
        if (bookings == null || bookings.isEmpty()) {
            appendEmptyRow(html, 5);
        } else {
            for (RecentBooking booking : bookings) {
                html.append("<tr><td>").append(escapeHtml(booking.getRoomCode())).append("</td><td>")
                    .append(escapeHtml(booking.getGuestName())).append("</td><td>")
                    .append(escapeHtml(booking.getRoomType())).append("</td><td>")
                    .append(escapeHtml(booking.getStatus())).append("</td><td>")
                    .append(booking.getBookingDate() == null ? "" : escapeHtml(booking.getBookingDate().format(df)))
                    .append("</td></tr>");
            }
        }
        html.append("</tbody></table></section>");
    }

    private void appendEmptyRow(StringBuilder html, int columns) {
        html.append("<tr><td class=\"muted\" colspan=\"").append(columns).append("\">Không có dữ liệu</td></tr>");
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
            case "Đang ở":
            case "Dang o":
                return new Color(34, 197, 94);
            case "Sắp nhận":
            case "Sap nhan":
                return new Color(59, 130, 246);
            case "Đã trả":
            case "Da tra":
                return new Color(124, 142, 171);
            case "Đã hủy":
            case "Da huy":
                return new Color(239, 68, 68);
            default:        return new Color(124, 142, 171);
        }
    }

    private String formatVnd(double v) {
        if (v <= 0) return "0 đ";
        if (v >= 1_000_000_000) return String.format("%.2f tỷ", v / 1_000_000_000.0);
        if (v >= 1_000_000)     return String.format("%.1f tr", v / 1_000_000.0);
        return moneyFormat.format(v) + " đ";
    }

    private String formatMoneyFull(double value) {
        return moneyFormat.format(Math.round(value)) + " đ";
    }

    private String formatPercent(double value) {
        return String.format(Locale.forLanguageTag("vi-VN"), "%.1f%%", value * 100);
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }

    private double parseMoneyInput(String raw) {
        if (raw == null) {
            return 0;
        }
        String cleaned = raw.replaceAll("[^0-9]", "");
        if (cleaned.isEmpty()) {
            return 0;
        }
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException ex) {
            return 0;
        }
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

    // ============================== ADR TREND CHART ==============================
    private static final class AdrTrendChartPanel extends JPanel {
        private List<HotelKpiPoint> data = Collections.emptyList();
        private final Color lineColor = new Color(16, 185, 129);
        private final Color fillColor = new Color(16, 185, 129, 40);
        private final Color gridColor = new Color(229, 236, 246);
        private final Color textColor = new Color(100, 116, 139);

        private AdrTrendChartPanel() {
            setOpaque(false);
        }

        void setData(List<HotelKpiPoint> newData) {
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
            int left = 60, right = 20, top = 24, bottom = 40;
            int chartW = w - left - right;
            int chartH = h - top - bottom;

            // Find max ADR for scaling
            double maxAdr = 0;
            for (HotelKpiPoint p : data) {
                maxAdr = Math.max(maxAdr, p.getAdr());
            }
            maxAdr = maxAdr <= 0 ? 1_000_000 : maxAdr * 1.1;

            // Grid lines
            g2.setColor(gridColor);
            for (int i = 0; i <= 4; i++) {
                int y = top + i * chartH / 4;
                g2.drawLine(left, y, w - right, y);
            }

            // Y-axis labels
            g2.setColor(textColor);
            g2.setFont(g2.getFont().deriveFont(10f));
            for (int i = 0; i <= 4; i++) {
                double val = maxAdr * (4 - i) / 4.0;
                String label = formatAxis(val);
                int labelW = g2.getFontMetrics().stringWidth(label);
                int y = top + i * chartH / 4 + 4;
                g2.drawString(label, left - labelW - 8, y);
            }

            // Calculate points
            int n = data.size();
            int[] xs = new int[n];
            int[] ys = new int[n];
            for (int i = 0; i < n; i++) {
                xs[i] = left + (i * chartW) / Math.max(1, n - 1);
                double adr = data.get(i).getAdr();
                ys[i] = top + (int) ((1 - adr / maxAdr) * chartH);
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

            // X-axis labels
            if (n > 0) {
                g2.setFont(g2.getFont().deriveFont(10f));
                g2.setColor(textColor);
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

        private static String formatAxis(double v) {
            if (v <= 0) return "0";
            if (v >= 1_000_000) return String.format("%.1fM", v / 1_000_000.0);
            if (v >= 1_000) return String.format("%.0fK", v / 1_000.0);
            return String.format("%.0f", v);
        }
    }
}
