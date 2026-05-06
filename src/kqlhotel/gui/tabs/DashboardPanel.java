package kqlhotel.gui.tabs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;
import kqlhotel.bus.shift.ShiftBUS;
import kqlhotel.dao.shift.ShiftDAO.ShiftInfo;
import kqlhotel.gui.theme.ThemeColors;

public class DashboardPanel extends JPanel {

    private final ShiftBUS shiftBUS = new ShiftBUS();

    private final JLabel lblCaHienTai   = kpiValue("--");
    private final JLabel lblGioCa       = kpiValue("--");
    private final JLabel lblNhanVien    = kpiValue("--");
    private final JLabel lblTienMoCa    = kpiValue("--");
    private final JLabel lblDoanhThu    = kpiValue("--");
    private final JLabel lblGiaoDich    = kpiValue("--");
    private final JLabel lblTrangThai   = new JLabel("Đang tải...");

    public DashboardPanel() {
        setLayout(new MigLayout("insets 24, gap 16, wrap 1", "[grow,fill]", "[]16[]"));
        setBackground(ThemeColors.PREMIUM_BG);

        add(buildHeader());
        add(buildShiftSection());

        refresh();
    }

    private JPanel buildHeader() {
        JPanel row = new JPanel(new MigLayout("insets 0, gap 12", "[grow,fill][]", "[]"));
        row.setOpaque(false);

        lblTrangThai.setFont(lblTrangThai.getFont().deriveFont(12f));
        lblTrangThai.setForeground(ThemeColors.PREMIUM_TEXT_MUTED);

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.setFont(btnRefresh.getFont().deriveFont(Font.BOLD, 12f));
        btnRefresh.setBackground(ThemeColors.PREMIUM_PRIMARY);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
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
        row2.add(kpiCard("Số giao dịch", lblGiaoDich, new Color(0xFFEDD5), new Color(0x9A3412)));
        section.add(row2);

        return section;
    }

    private JPanel kpiCard(String label, JLabel valueLabel, Color bg, Color accent) {
        JPanel card = new JPanel(new MigLayout("insets 16, wrap 1, gap 4", "[grow,fill]", "[]4[]"));
        card.setBackground(bg);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accent.brighter(), 1, true),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        card.setPreferredSize(new Dimension(0, 90));

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

    public void refresh() {
        lblTrangThai.setText("Đang tải...");
        new Thread(() -> {
            ShiftInfo info = shiftBUS.getCurrentShift();
            SwingUtilities.invokeLater(() -> applyShiftInfo(info));
        }).start();
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

    private static String formatVND(double amount) {
        return String.format("%,.0f đ", amount).replace(',', '.');
    }
}
