package kqlhotel.gui.tabs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import kqlhotel.bus.checkin.CheckInService;
import kqlhotel.bus.checkin.CheckInServiceProvider;
import kqlhotel.bus.checkin.model.ArrivalDto;
import kqlhotel.bus.checkin.model.CheckInResult;
import kqlhotel.gui.components.BackgroundPanel;
import kqlhotel.gui.components.DatePicker;
import kqlhotel.gui.components.PrimaryButton;
import kqlhotel.gui.components.RoundedPanel;
import kqlhotel.gui.theme.ThemeColors;
import net.miginfocom.swing.MigLayout;

/**
 * Front-desk check-in screen. Lists today's expected arrivals and lets the
 * receptionist confirm physical guest arrival, which materializes
 * ChiTietHoaDon records and flips the room status.
 */
public class CheckInPanel extends BackgroundPanel {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DecimalFormat MONEY_FMT = new DecimalFormat("#,###");

    private final CheckInService checkInService = CheckInServiceProvider.getInstance();
    private final ArrivalsTableModel tableModel = new ArrivalsTableModel();
    private final JTextField keywordField = new JTextField();
    private final DatePicker fromPicker = new DatePicker();
    private final DatePicker toPicker = new DatePicker();
    private final JLabel summaryLabel = new JLabel();
    private final JLabel totalMetric = new JLabel("0");
    private final JLabel pendingMetric = new JLabel("0");
    private final JLabel checkedMetric = new JLabel("0");
    private final JLabel dueMetric = new JLabel("0");
    private final JLabel overdueMetric = new JLabel("0");

    public CheckInPanel() {
        setLayout(new MigLayout("insets 24,wrap 1,gap 16", "[grow,fill]", "[][grow,fill]"));
        add(buildFilterCard(), "growx");
        add(buildTableCard(), "grow,push");
        // Default: arrivals expected today
        LocalDate today = LocalDate.now();
        fromPicker.setSelectedDate(today);
        toPicker.setSelectedDate(today);

        fromPicker.addDateChangeListener(() -> {
            LocalDate from = fromPicker.getSelectedDate();
            if (from != null) {
                LocalDate to = toPicker.getSelectedDate();
                if (to == null || to.isBefore(from)) {
                    toPicker.setSelectedDate(from);
                }
            }
        });
        reload();
    }

    private static String safe(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    private static boolean isOverdue(ArrivalDto arrival) {
        return arrival != null
            && !arrival.isCheckedIn()
            && arrival.getNgayTraDuKien() != null
            && LocalDateTime.now().isAfter(arrival.getNgayTraDuKien());
    }

    private static String resolveStatus(ArrivalDto arrival) {
        if (arrival == null) return "";
        if (arrival.isCheckedIn()) return "Đã nhận";
        if (isOverdue(arrival)) return "Quá hạn";
        LocalDate today = LocalDate.now();
        LocalDate expected = arrival.getNgayNhanDuKien() == null ? today : arrival.getNgayNhanDuKien().toLocalDate();
        if (expected.equals(today)) return "Đến hạn";
        if (expected.isBefore(today)) return "Trễ nhận";
        return "Sắp nhận";
    }

    private static Color statusForeground(String status) {
        if ("Đã nhận".equals(status)) return ThemeColors.SUCCESS;
        if ("Quá hạn".equals(status)) return new Color(220, 38, 38);
        if ("Trễ nhận".equals(status) || "Đến hạn".equals(status)) return new Color(217, 119, 6);
        return ThemeColors.PRIMARY;
    }

    private static Color statusBackground(String status) {
        if ("Đã nhận".equals(status)) return ThemeColors.SUCCESS_SOFT;
        if ("Quá hạn".equals(status)) return new Color(254, 226, 226);
        if ("Trễ nhận".equals(status) || "Đến hạn".equals(status)) return new Color(255, 237, 213);
        return ThemeColors.PRIMARY_SOFT;
    }

    private static Color rowBackground(ArrivalDto arrival, boolean selected) {
        if (selected) return ThemeColors.PRIMARY_SOFT;
        String status = resolveStatus(arrival);
        if ("Quá hạn".equals(status)) return new Color(255, 247, 247);
        if ("Trễ nhận".equals(status) || "Đến hạn".equals(status)) return new Color(255, 252, 242);
        return ThemeColors.SURFACE;
    }

    // -----------------------------------------------------------
    // Filter card
    // -----------------------------------------------------------
    private RoundedPanel buildFilterCard() {
        RoundedPanel card = new RoundedPanel(14, ThemeColors.SURFACE, ThemeColors.BORDER_SOFT, 1f);
        card.setLayout(new MigLayout("insets 18 22,gap 14",
            "[grow,fill][180!][180!][100!][130!]", "[]"));

        JLabel title = new JLabel("Danh sách khách đến");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        title.setForeground(ThemeColors.TEXT_PRIMARY);
        JLabel sub = new JLabel("Chọn ngày và tìm theo mã/họ tên/SĐT/CCCD để xác nhận nhận phòng.");
        sub.setForeground(ThemeColors.TEXT_MUTED);
        sub.setFont(sub.getFont().deriveFont(12f));

        JPanel titleWrap = new JPanel(new MigLayout("wrap 1,insets 0,gap 2", "[grow,fill]", "[][]"));
        titleWrap.setOpaque(false);
        titleWrap.add(title);
        titleWrap.add(sub);

        keywordField.putClientProperty("JTextField.placeholderText", "Mã đặt phòng / Tên / SĐT / CCCD");
        keywordField.setPreferredSize(new Dimension(0, 36));
        keywordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.BORDER, 1, true),
            new EmptyBorder(0, 12, 0, 12)));
        keywordField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) reload();
            }
        });

        JButton searchBtn = new JButton("Tìm");
        styleSecondaryButton(searchBtn);
        searchBtn.addActionListener(this::onSearch);

        PrimaryButton refreshBtn = new PrimaryButton("Hôm nay");
        refreshBtn.addActionListener(e -> {
            LocalDate today = LocalDate.now();
            fromPicker.setSelectedDate(today);
            toPicker.setSelectedDate(today);
            keywordField.setText("");
            reload();
        });

        card.add(titleWrap, "spanx 5, wrap, gapbottom 6");
        card.add(keywordField, "h 36!");
        card.add(fromPicker, "h 36!");
        card.add(toPicker, "h 36!");
        card.add(searchBtn, "h 36!");
        card.add(refreshBtn, "h 36!");
        return card;
    }

    private void styleSecondaryButton(JButton btn) {
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 13f));
        btn.setBackground(ThemeColors.SURFACE);
        btn.setForeground(ThemeColors.TEXT_PRIMARY);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.BORDER, 1, true),
            new EmptyBorder(0, 18, 0, 18)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // -----------------------------------------------------------
    // Table card
    // -----------------------------------------------------------
    private RoundedPanel buildTableCard() {
        RoundedPanel card = new RoundedPanel(14, ThemeColors.SURFACE, ThemeColors.BORDER_SOFT, 1f);
        card.setLayout(new MigLayout("insets 18 22,wrap 1,gap 12", "[grow,fill]", "[][][grow,fill]"));

        summaryLabel.setForeground(ThemeColors.TEXT_MUTED);
        summaryLabel.setFont(summaryLabel.getFont().deriveFont(12f));

        JPanel metrics = new JPanel(new MigLayout("insets 0,gap 10", "[fill][fill][fill][fill][fill]", "[]"));
        metrics.setOpaque(false);
        metrics.add(metricCard("Tổng booking", totalMetric, new Color(235, 248, 255), new Color(49, 130, 206)));
        metrics.add(metricCard("Chưa nhận", pendingMetric, new Color(238, 246, 255), new Color(30, 64, 175)));
        metrics.add(metricCard("Đã nhận", checkedMetric, ThemeColors.SUCCESS_SOFT, ThemeColors.SUCCESS));
        metrics.add(metricCard("Đến hạn", dueMetric, new Color(255, 247, 237), new Color(217, 119, 6)));
        metrics.add(metricCard("Quá hạn", overdueMetric, new Color(254, 242, 242), new Color(220, 38, 38)));

        JTable table = new JTable(tableModel);
        table.setRowHeight(44);
        table.setFillsViewportHeight(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(ThemeColors.BORDER_SOFT);
        table.setSelectionBackground(ThemeColors.PRIMARY_SOFT);
        table.setSelectionForeground(ThemeColors.TEXT_PRIMARY);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 12f));
        table.getTableHeader().setBackground(ThemeColors.SURFACE_LIGHT);
        table.getTableHeader().setForeground(ThemeColors.TEXT_SECONDARY);

        // Column widths
        int[] widths = {100, 180, 120, 150, 75, 80, 130, 120, 110, 130};
        for (int i = 0; i < widths.length && i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        ArrivalCellRenderer standardRenderer = new ArrivalCellRenderer(SwingConstants.LEFT);
        ArrivalCellRenderer centerRenderer = new ArrivalCellRenderer(SwingConstants.CENTER);
        for (int col = 0; col < table.getColumnModel().getColumnCount(); col++) {
            if (col < table.getColumnModel().getColumnCount()) {
                table.getColumnModel().getColumn(col).setCellRenderer(standardRenderer);
            }
        }
        for (int col : new int[]{0, 4, 5, 6, 7}) {
            table.getColumnModel().getColumn(col).setCellRenderer(centerRenderer);
        }
        table.getColumnModel().getColumn(8).setCellRenderer(new StatusBadgeRenderer());

        // Action button column
        int actionCol = tableModel.getColumnCount() - 1;
        table.getColumnModel().getColumn(actionCol).setCellRenderer(new ActionButtonRenderer());
        table.getColumnModel().getColumn(actionCol).setCellEditor(new ActionButtonEditor(new JCheckBox()));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER_SOFT, 1, true));
        scroll.getViewport().setBackground(ThemeColors.SURFACE);

        card.add(summaryLabel);
        card.add(metrics, "growx");
        card.add(scroll, "grow,push");
        return card;
    }

    private RoundedPanel metricCard(String title, JLabel valueLabel, Color bg, Color fg) {
        RoundedPanel card = new RoundedPanel(12, bg, new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 70), 1f);
        card.setLayout(new MigLayout("insets 10 14,wrap 1,gap 2", "[110!]", "[][]"));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 12f));
        titleLabel.setForeground(ThemeColors.TEXT_SECONDARY);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 20f));
        valueLabel.setForeground(fg);
        card.add(titleLabel);
        card.add(valueLabel);
        return card;
    }

    // -----------------------------------------------------------
    // Actions
    // -----------------------------------------------------------
    private void onSearch(ActionEvent e) { reload(); }

    private void reload() {
        LocalDate from = fromPicker.getSelectedDate();
        LocalDate to = toPicker.getSelectedDate();
        if (from == null || to == null) {
            JOptionPane.showMessageDialog(this,
                "Ngày không hợp lệ.",
                "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (to.isBefore(from)) {
            JOptionPane.showMessageDialog(this,
                "Ngày kết thúc phải >= ngày bắt đầu.",
                "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<ArrivalDto> arrivals = checkInService.findArrivals(from, to, keywordField.getText());
        tableModel.setData(arrivals);
        long pending = arrivals.stream().filter(a -> !a.isCheckedIn()).count();
        long checked = arrivals.stream().filter(ArrivalDto::isCheckedIn).count();
        long due = arrivals.stream().filter(a -> "Đến hạn".equals(resolveStatus(a))).count();
        long overdue = arrivals.stream().filter(a -> "Quá hạn".equals(resolveStatus(a))).count();
        totalMetric.setText(String.valueOf(arrivals.size()));
        pendingMetric.setText(String.valueOf(pending));
        checkedMetric.setText(String.valueOf(checked));
        dueMetric.setText(String.valueOf(due));
        overdueMetric.setText(String.valueOf(overdue));
        summaryLabel.setText(String.format(
            "Tìm thấy %d booking trong khoảng %s — %s · %d chưa nhận phòng · %d đã nhận",
            arrivals.size(),
            from.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            to.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            pending,
            checked));
    }

    private void confirm(ArrivalDto arrival) {
        if (arrival.isCheckedIn()) {
            JOptionPane.showMessageDialog(this,
                "Booking này đã nhận phòng rồi.",
                "Đã nhận phòng", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (isOverdue(arrival)) {
            JOptionPane.showMessageDialog(this,
                "<html>Booking <b>" + arrival.getMaDatPhong() + "</b> đã quá ngày trả dự kiến.<br>" +
                "Vui lòng kiểm tra lại lịch đặt phòng, hủy booking hoặc tạo booking mới.</html>",
                "Booking quá hạn",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
            "<html>Xác nhận nhận phòng cho booking <b>" + arrival.getMaDatPhong() + "</b>?<br>" +
            "Khách: <b>" + arrival.getTenKH() + "</b><br>" +
            "Số phòng: <b>" + arrival.getRoomCount() + "</b><br>" +
            "Phòng: <b>" + String.join(", ", arrival.getRoomCodes()) + "</b></html>",
            "Xác nhận nhận phòng",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;

        CheckInResult result = checkInService.confirmCheckIn(arrival.getMaDatPhong());
        if (!result.isSuccess()) {
            JOptionPane.showMessageDialog(this,
                result.getMessage(), "Không thể nhận phòng",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this,
            result.getMessage() + "\nMã hóa đơn: " + result.getMaHD(),
            "Thành công", JOptionPane.INFORMATION_MESSAGE);
        
        // Refresh RoomManagementPanel data
        java.awt.Window win = javax.swing.SwingUtilities.getWindowAncestor(this);
        if (win instanceof kqlhotel.gui.AppFrame) {
            ((kqlhotel.gui.AppFrame) win).refreshRoomManagementData();
        }
        
        reload();
    }

    private static String fmtDate(java.time.LocalDateTime dt) {
        return dt == null ? "" : dt.format(DATE_FMT);
    }

    private static String fmtMoney(long v) {
        return MONEY_FMT.format(v) + "đ";
    }

    // -----------------------------------------------------------
    // Table model
    // -----------------------------------------------------------
    private class ArrivalsTableModel extends AbstractTableModel {
        private final String[] cols = {
            "Mã đặt phòng", "Khách hàng", "SĐT", "Phòng",
            "Số phòng", "Số đêm", "Ngày nhận DK", "Tiền cọc", "Hành động"
        };
        private List<ArrivalDto> data = java.util.Collections.emptyList();

        void setData(List<ArrivalDto> rows) {
            this.data = rows == null ? java.util.Collections.emptyList() : rows;
            fireTableDataChanged();
        }

        ArrivalDto rowAt(int idx) { return data.get(idx); }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length + 1; }
        @Override public String getColumnName(int c) {
            if (c == 8) return "Trạng thái";
            if (c == 9) return cols[8];
            return cols[c];
        }

        @Override public boolean isCellEditable(int r, int c) {
            return c == getColumnCount() - 1; // action column
        }

        @Override
        public Object getValueAt(int r, int c) {
            ArrivalDto a = data.get(r);
            if (c == 8) return resolveStatus(a);
            if (c == 9) return a.isCheckedIn() ? "Đã nhận" : (isOverdue(a) ? "Kiểm tra" : "Nhận phòng");
            switch (c) {
                case 0: return a.getMaDatPhong();
                case 1: return a.getTenKH();
                case 2: return a.getSdtKH();
                case 3: return String.join(", ", a.getRoomCodes());
                case 4: return a.getRoomCount();
                case 5: return a.getNights();
                case 6: return fmtDate(a.getNgayNhanDuKien());
                case 7: return fmtMoney(a.getTienCoc());
                case 8: return a.isCheckedIn() ? "Đã nhận" : "Nhận phòng";
                default: return "";
            }
        }
    }

    // -----------------------------------------------------------
    // Table rendering
    // -----------------------------------------------------------
    private class ArrivalCellRenderer extends DefaultTableCellRenderer {
        ArrivalCellRenderer(int alignment) {
            setHorizontalAlignment(alignment);
            setBorder(new EmptyBorder(0, 10, 0, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            ArrivalDto arrival = tableModel.rowAt(row);
            setBackground(rowBackground(arrival, isSelected));
            setForeground(ThemeColors.TEXT_PRIMARY);
            setBorder(new EmptyBorder(0, 10, 0, 10));
            return this;
        }
    }

    private class StatusBadgeRenderer extends DefaultTableCellRenderer {
        StatusBadgeRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(getFont().deriveFont(Font.BOLD, 12f));
            setBorder(new EmptyBorder(7, 8, 7, 8));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = String.valueOf(value == null ? "" : value);
            setText(status);
            setOpaque(true);
            setForeground(statusForeground(status));
            setBackground(isSelected ? rowBackground(tableModel.rowAt(row), true) : statusBackground(status));
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(statusForeground(status), 1, true),
                new EmptyBorder(3, 8, 3, 8)));
            return this;
        }
    }

    // -----------------------------------------------------------
    // Action button (renderer + editor)
    // -----------------------------------------------------------
    private class ActionButtonRenderer extends JButton implements TableCellRenderer {
        ActionButtonRenderer() {
            setOpaque(true);
            setFocusPainted(false);
            setFont(getFont().deriveFont(Font.BOLD, 12f));
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            ArrivalDto a = tableModel.rowAt(row);
            if (a.isCheckedIn()) {
                setText("Đã nhận");
                setBackground(ThemeColors.SUCCESS_SOFT);
                setForeground(ThemeColors.SUCCESS);
                setBorder(BorderFactory.createLineBorder(ThemeColors.SUCCESS, 1, true));
            } else if (isOverdue(a)) {
                setText("Kiểm tra");
                setBackground(new Color(254, 226, 226));
                setForeground(new Color(185, 28, 28));
                setBorder(BorderFactory.createLineBorder(new Color(220, 38, 38), 1, true));
            } else {
                setText("Nhận phòng");
                setBackground(ThemeColors.ACCENT);
                setForeground(Color.WHITE);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            }
            return this;
        }
    }

    private class ActionButtonEditor extends DefaultCellEditor {
        private final JButton button = new JButton();
        private ArrivalDto current;

        ActionButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button.setFocusPainted(false);
            button.setFont(button.getFont().deriveFont(Font.BOLD, 12f));
            button.addActionListener(e -> {
                fireEditingStopped();
                if (current != null) {
                    SwingUtilities.invokeLater(() -> confirm(current));
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            current = tableModel.rowAt(row);
            if (current.isCheckedIn()) {
                button.setText("Đã nhận");
                button.setBackground(ThemeColors.SUCCESS_SOFT);
                button.setForeground(ThemeColors.SUCCESS);
                button.setEnabled(false);
            } else if (isOverdue(current)) {
                button.setText("Kiểm tra");
                button.setBackground(new Color(254, 226, 226));
                button.setForeground(new Color(185, 28, 28));
                button.setEnabled(true);
            } else {
                button.setText("Nhận phòng");
                button.setBackground(ThemeColors.ACCENT);
                button.setForeground(Color.WHITE);
                button.setEnabled(true);
            }
            return button;
        }

        @Override public Object getCellEditorValue() {
            return current == null ? "" : (current.isCheckedIn() ? "Đã nhận" : "Nhận phòng");
        }
    }

    @SuppressWarnings("unused")
    private static TableCellEditor unused() { return null; }
}
