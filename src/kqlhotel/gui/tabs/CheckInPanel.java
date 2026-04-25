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
    private final JTextField fromField = new JTextField();
    private final JTextField toField = new JTextField();
    private final JLabel summaryLabel = new JLabel();

    public CheckInPanel() {
        setLayout(new MigLayout("insets 24,wrap 1,gap 16", "[grow,fill]", "[][grow,fill]"));
        add(buildFilterCard(), "growx");
        add(buildTableCard(), "grow,push");
        // Default: arrivals expected today
        LocalDate today = LocalDate.now();
        fromField.setText(today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        toField.setText(today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        reload();
    }

    // -----------------------------------------------------------
    // Filter card
    // -----------------------------------------------------------
    private RoundedPanel buildFilterCard() {
        RoundedPanel card = new RoundedPanel(14, ThemeColors.SURFACE, ThemeColors.BORDER_SOFT, 1f);
        card.setLayout(new MigLayout("insets 18 22,gap 14",
            "[grow,fill][140!][140!][120!][120!]", "[]"));

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

        decorateDateField(fromField, "Từ ngày (dd/MM/yyyy)");
        decorateDateField(toField, "Đến ngày (dd/MM/yyyy)");

        JButton searchBtn = new JButton("Tìm");
        styleSecondaryButton(searchBtn);
        searchBtn.addActionListener(this::onSearch);

        PrimaryButton refreshBtn = new PrimaryButton("Hôm nay");
        refreshBtn.addActionListener(e -> {
            LocalDate today = LocalDate.now();
            fromField.setText(today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            toField.setText(today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            keywordField.setText("");
            reload();
        });

        card.add(titleWrap, "spanx 5, wrap, gapbottom 6");
        card.add(keywordField, "h 36!");
        card.add(fromField, "h 36!");
        card.add(toField, "h 36!");
        card.add(searchBtn, "h 36!");
        card.add(refreshBtn, "h 36!");
        return card;
    }

    private void decorateDateField(JTextField field, String placeholder) {
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.setPreferredSize(new Dimension(0, 36));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.BORDER, 1, true),
            new EmptyBorder(0, 12, 0, 12)));
        field.setHorizontalAlignment(SwingConstants.CENTER);
        field.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) reload();
            }
        });
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
        card.setLayout(new MigLayout("insets 18 22,wrap 1,gap 12", "[grow,fill]", "[][grow,fill][]"));

        summaryLabel.setForeground(ThemeColors.TEXT_MUTED);
        summaryLabel.setFont(summaryLabel.getFont().deriveFont(12f));

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
        int[] widths = {110, 200, 130, 150, 80, 90, 130, 100, 130};
        for (int i = 0; i < widths.length && i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int col : new int[]{0, 4, 5, 6, 7}) {
            if (col < table.getColumnModel().getColumnCount()) {
                table.getColumnModel().getColumn(col).setCellRenderer(center);
            }
        }

        // Action button column
        int actionCol = tableModel.getColumnCount() - 1;
        table.getColumnModel().getColumn(actionCol).setCellRenderer(new ActionButtonRenderer());
        table.getColumnModel().getColumn(actionCol).setCellEditor(new ActionButtonEditor(new JCheckBox()));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER_SOFT, 1, true));
        scroll.getViewport().setBackground(ThemeColors.SURFACE);

        card.add(summaryLabel);
        card.add(scroll, "grow,push");
        return card;
    }

    // -----------------------------------------------------------
    // Actions
    // -----------------------------------------------------------
    private void onSearch(ActionEvent e) { reload(); }

    private void reload() {
        LocalDate from = parseDate(fromField.getText());
        LocalDate to = parseDate(toField.getText());
        if (from == null || to == null) {
            JOptionPane.showMessageDialog(this,
                "Ngày không hợp lệ. Định dạng: dd/MM/yyyy",
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
        summaryLabel.setText(String.format(
            "Tìm thấy %d booking trong khoảng %s — %s · %d chưa nhận phòng",
            arrivals.size(),
            from.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            to.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            pending));
    }

    private void confirm(ArrivalDto arrival) {
        if (arrival.isCheckedIn()) {
            JOptionPane.showMessageDialog(this,
                "Booking này đã nhận phòng rồi.",
                "Đã nhận phòng", JOptionPane.INFORMATION_MESSAGE);
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
        reload();
    }

    private static LocalDate parseDate(String text) {
        if (text == null) return null;
        String s = text.trim();
        if (s.isEmpty()) return null;
        try {
            return LocalDate.parse(s, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception ex) {
            return null;
        }
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
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override public boolean isCellEditable(int r, int c) {
            return c == cols.length - 1; // action column
        }

        @Override
        public Object getValueAt(int r, int c) {
            ArrivalDto a = data.get(r);
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
