package kqlhotel.gui.tabs;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;

import kqlhotel.bus.shift.ShiftBUS;
import kqlhotel.bus.dashboard.EmployeeDashboardBUS;
import kqlhotel.dao.shift.ShiftDAO.ShiftInfo;
import kqlhotel.dto.dashboard.RoomScheduleDTO;
import kqlhotel.gui.theme.ThemeColors;

public class EmployeeDashboardPanel extends JPanel {

    private static final Color NAVY = new Color(24, 34, 52);
    private static final Color SURFACE = new Color(255, 255, 255, 230);
    private static final Color BORDER = new Color(220, 225, 235);
    private static final Color MUTED = new Color(100, 115, 135);

    private final ShiftBUS shiftBUS = new ShiftBUS();
    private final EmployeeDashboardBUS employeeDashboardBUS = new EmployeeDashboardBUS();

    private final JLabel lblStatus = smallText("Đang tải...");
    private final JLabel lblGreeting = new JLabel("Xin chào nhân viên");
    private final JLabel lblToday = smallText("--");

    private final JLabel lblShiftName = valueLabel("--");
    private final JLabel lblShiftTime = valueLabel("--");
    private final JLabel lblStaff = valueLabel("--");
    private final JLabel lblShiftRevenue = valueLabel("--");

    private final JLabel lblCheckInRooms = bigValue("--");
    private final JLabel lblCheckInBookings = bigValue("--");
    private final JLabel lblCheckOutRooms = bigValue("--");
    private final JLabel lblNeedPayment = bigValue("--");

    private final JPanel notificationList = new JPanel(new MigLayout("wrap 1,insets 0,gap 8", "[grow,fill]", "[]"));
    private final JPanel checkInList = new JPanel(new MigLayout("wrap 1,insets 0,gap 8", "[grow,fill]", "[]"));
    private final JPanel checkOutList = new JPanel(new MigLayout("wrap 1,insets 0,gap 8", "[grow,fill]", "[]"));

    private boolean loadedOnce;

    public EmployeeDashboardPanel() {
        setLayout(new MigLayout(
                "insets 24, gap 16, fill",
                "[grow 62,fill][grow 38,fill]",
                "[]16[]16[grow,fill]"
        ));
        setBackground(ThemeColors.PREMIUM_BG);

        add(buildHeader(), "span 2,growx,wrap");
        add(buildKpiSection(), "span 2,growx,wrap");
        add(buildMainWorkSection(), "grow");
        add(buildRightSection(), "grow");
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (!loadedOnce) {
            loadedOnce = true;
            SwingUtilities.invokeLater(this::refresh);
        }
    }

    private JPanel buildHeader() {
        JPanel panel = cardPanel(new MigLayout("insets 18 20, gap 12", "[grow,fill][]", "[]"));

        JPanel left = new JPanel(new MigLayout("wrap 1,insets 0,gap 4", "[grow,fill]", "[]"));
        left.setOpaque(false);

        lblGreeting.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblGreeting.setForeground(NAVY);

        lblToday.setText("Hôm nay: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        left.add(lblGreeting);
        left.add(lblToday);
        left.add(lblStatus);

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.setBackground(NAVY);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        btnRefresh.addActionListener(e -> refresh());

        panel.add(left, "growx");
        panel.add(btnRefresh, "h 38!, aligny center");

        return panel;
    }

    private JPanel buildKpiSection() {
        JPanel panel = new JPanel(new MigLayout(
                "insets 0, gap 14, fillx",
                "[grow,fill][grow,fill][grow,fill][grow,fill]",
                "[]"
        ));
        panel.setOpaque(false);

        panel.add(statCard("Phòng sắp nhận", lblCheckInRooms, "Khách dự kiến check-in hôm nay", new Color(0xE0F2FE), new Color(0x0369A1)));
        panel.add(statCard("Booking sắp nhận", lblCheckInBookings, "Đơn đặt phòng cần xử lý", new Color(0xDCFCE7), new Color(0x166534)));
        panel.add(statCard("Phòng sắp trả", lblCheckOutRooms, "Khách dự kiến check-out hôm nay", new Color(0xFEF3C7), new Color(0x92400E)));
        panel.add(statCard("Cần thanh toán", lblNeedPayment, "Hóa đơn / phòng cần thu tiền", new Color(0xFEE2E2), new Color(0x991B1B)));

        return panel;
    }

    private JPanel buildMainWorkSection() {
        JPanel panel = cardPanel(new MigLayout(
                "wrap 1,insets 18,gap 12,fill",
                "[grow,fill]",
                "[]8[grow,fill]12[]8[grow,fill]"
        ));

        panel.add(sectionTitle("Lịch nhận phòng hôm nay"));
        panel.add(wrapScroll(checkInList), "grow");
        panel.add(sectionTitle("Lịch trả phòng hôm nay"));
        panel.add(wrapScroll(checkOutList), "grow");

        return panel;
    }

    private JPanel buildRightSection() {
        JPanel panel = new JPanel(new MigLayout(
                "wrap 1,insets 0,gap 16,fill",
                "[grow,fill]",
                "[220!,fill][grow,fill]"
        ));
        panel.setOpaque(false);

        panel.add(buildShiftCard(), "grow");
        panel.add(buildNotificationCard(), "grow");

        return panel;
    }

    private JPanel buildShiftCard() {
        JPanel panel = cardPanel(new MigLayout("wrap 2,insets 18,gap 10", "[grow,fill][grow,fill]", "[]"));
        panel.add(sectionTitle("Ca làm việc"), "span 2");
        panel.add(miniInfo("Ca hiện tại", lblShiftName));
        panel.add(miniInfo("Giờ làm việc", lblShiftTime));
        panel.add(miniInfo("Nhân viên", lblStaff));
        panel.add(miniInfo("Doanh thu ca", lblShiftRevenue));
        return panel;
    }

    private JPanel buildNotificationCard() {
        JPanel panel = cardPanel(new MigLayout("wrap 1,insets 18,gap 12,fill", "[grow,fill]", "[]8[grow,fill]"));
        panel.add(sectionTitle("Thông báo nghiệp vụ"));
        notificationList.setOpaque(false);
        panel.add(wrapScroll(notificationList), "grow");
        return panel;
    }

    public void refresh() {
        lblStatus.setText("Đang tải dữ liệu...");

        new Thread(() -> {
            ShiftInfo shiftInfo = shiftBUS.getCurrentShift();

            List<RoomScheduleDTO> checkInRooms = safeList(() -> employeeDashboardBUS.getTodayCheckInRooms());
            List<RoomScheduleDTO> checkOutRooms = safeList(() -> employeeDashboardBUS.getTodayCheckOutRooms());

            int checkInBookingCount = safe(() -> employeeDashboardBUS.countCheckInBookingsToday());
            int needPayment = safe(() -> employeeDashboardBUS.countNeedPayment());

            EmployeeDashboardData data = new EmployeeDashboardData(
                    shiftInfo,
                    checkInRooms,
                    checkInBookingCount,
                    checkOutRooms,
                    needPayment
            );

            SwingUtilities.invokeLater(() -> applyData(data));
        }).start();
    }

    private void applyData(EmployeeDashboardData data) {
        applyShift(data.shiftInfo);

        lblCheckInRooms.setText(data.checkInRooms.size() + " phòng");
        lblCheckInBookings.setText(data.checkInBookings + " booking");
        lblCheckOutRooms.setText(data.checkOutRooms.size() + " phòng");
        lblNeedPayment.setText(data.needPayment + " mục");

        renderNotifications(data);
        renderCheckInList(data.checkInRooms);
        renderCheckOutList(data.checkOutRooms);

        lblStatus.setText("Cập nhật lúc " + java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    private void applyShift(ShiftInfo info) {
        if (info == null) {
            lblGreeting.setText("Xin chào nhân viên");
            lblShiftName.setText("Không có");
            lblShiftTime.setText("--");
            lblStaff.setText("--");
            lblShiftRevenue.setText("--");
            return;
        }

        String tenCa = switch (info.loaiCa) {
            case "CaSang" -> "Ca sáng";
            case "CaChieu" -> "Ca chiều";
            case "CaToi" -> "Ca tối";
            default -> info.loaiCa;
        };

        lblGreeting.setText("Xin chào, " + info.hoTenNV);
        lblShiftName.setText(tenCa);
        lblShiftTime.setText(info.gioBatDau + " – " + info.gioKetThuc);
        lblStaff.setText(info.hoTenNV);
        lblShiftRevenue.setText(formatVND(info.doanhThu));
    }

    private void renderNotifications(EmployeeDashboardData data) {
        notificationList.removeAll();

        if (data.checkInBookings > 0) {
            notificationList.add(notificationItem("Có " + data.checkInBookings + " booking dự kiến nhận phòng hôm nay", new Color(0x0369A1)));
        }
        if (!data.checkInRooms.isEmpty()) {
            notificationList.add(notificationItem(data.checkInRooms.size() + " phòng cần chuẩn bị check-in", new Color(0x166534)));
        }
        if (!data.checkOutRooms.isEmpty()) {
            notificationList.add(notificationItem(data.checkOutRooms.size() + " phòng dự kiến trả hôm nay", new Color(0x92400E)));
        }
        if (data.needPayment > 0) {
            notificationList.add(notificationItem(data.needPayment + " hóa đơn/phòng cần thanh toán", new Color(0x991B1B)));
        }

        if (notificationList.getComponentCount() == 0) {
            notificationList.add(notificationItem("Hiện chưa có thông báo cần xử lý", new Color(0x64748B)));
        }

        notificationList.revalidate();
        notificationList.repaint();
    }

    private void renderCheckInList(List<RoomScheduleDTO> list) {
        checkInList.removeAll();

        if (list == null || list.isEmpty()) {
            checkInList.add(emptyRow("Không có lịch nhận phòng hôm nay"));
        } else {
            for (RoomScheduleDTO item : list) {
                checkInList.add(scheduleRow(item, "Nhận phòng", new Color(0x0369A1)));
            }
        }

        checkInList.revalidate();
        checkInList.repaint();
    }

    private void renderCheckOutList(List<RoomScheduleDTO> list) {
        checkOutList.removeAll();

        if (list == null || list.isEmpty()) {
            checkOutList.add(emptyRow("Không có lịch trả phòng hôm nay"));
        } else {
            for (RoomScheduleDTO item : list) {
                checkOutList.add(scheduleRow(item, "Trả phòng", new Color(0x92400E)));
            }
        }

        checkOutList.revalidate();
        checkOutList.repaint();
    }

    private JPanel scheduleRow(RoomScheduleDTO item, String type, Color color) {
        JPanel row = new JPanel(new MigLayout(
                "insets 12,gap 10",
                "[70!][grow,fill][90!,right]",
                "[][]"
        ));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createLineBorder(new Color(color.getRed(), color.getGreen(), color.getBlue(), 70), 1));

        JLabel room = new JLabel(item.getRoomCode(), SwingConstants.CENTER);
        room.setOpaque(true);
        room.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 25));
        room.setForeground(color);
        room.setFont(new Font("Segoe UI", Font.BOLD, 14));
        room.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        JLabel customer = new JLabel(item.getCustomerName());
        customer.setForeground(NAVY);
        customer.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JLabel time = new JLabel(item.getTime());
        time.setForeground(color);
        time.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JLabel note = new JLabel(type + " · " + item.getNote());
        note.setForeground(MUTED);
        note.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        row.add(room, "spany 2,growy");
        row.add(customer);
        row.add(time, "wrap");
        row.add(note, "span 2");

        return row;
    }

    private JPanel notificationItem(String text, Color color) {
        JPanel row = new JPanel(new MigLayout("insets 10 12,gap 8", "[][grow,fill]", "[]"));
        row.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 18));
        row.setBorder(BorderFactory.createLineBorder(new Color(color.getRed(), color.getGreen(), color.getBlue(), 70), 1));

        JLabel dot = new JLabel("●");
        dot.setForeground(color);

        JLabel label = new JLabel(text);
        label.setForeground(NAVY);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));

        row.add(dot);
        row.add(label);
        return row;
    }

    private JPanel emptyRow(String text) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(new Color(248, 250, 252));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(18, 12, 18, 12)
        ));

        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(MUTED);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        row.add(label, BorderLayout.CENTER);
        return row;
    }

    private JPanel statCard(String title, JLabel value, String desc, Color bg, Color accent) {
        JPanel card = new JPanel(new MigLayout("wrap 1,insets 16,gap 4", "[grow,fill]", "[]"));
        card.setBackground(bg);
        card.setBorder(BorderFactory.createLineBorder(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 90), 1));

        JLabel t = new JLabel(title);
        t.setForeground(accent);
        t.setFont(new Font("Segoe UI", Font.BOLD, 13));

        value.setForeground(accent);

        JLabel d = new JLabel(desc);
        d.setForeground(new Color(90, 105, 125));
        d.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        card.add(t);
        card.add(value);
        card.add(d);

        return card;
    }

    private JPanel miniInfo(String title, JLabel value) {
        JPanel p = new JPanel(new MigLayout("wrap 1,insets 10,gap 3", "[grow,fill]", "[]"));
        p.setBackground(new Color(248, 250, 252));
        p.setBorder(BorderFactory.createLineBorder(BORDER, 1));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setForeground(MUTED);

        value.setFont(new Font("Segoe UI", Font.BOLD, 13));
        value.setForeground(NAVY);

        p.add(t);
        p.add(value);

        return p;
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        label.setForeground(NAVY);
        return label;
    }

    private JPanel cardPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        return panel;
    }

    private JScrollPane wrapScroll(JPanel panel) {
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private static JLabel valueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return label;
    }

    private static JLabel bigValue(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        return label;
    }

    private static JLabel smallText(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(MUTED);
        return label;
    }

    private static int safe(IntSupplierEx supplier) {
        try {
            return supplier.getAsInt();
        } catch (Exception e) {
            return 0;
        }
    }

    private static List<RoomScheduleDTO> safeList(ListSupplierEx supplier) {
        try {
            List<RoomScheduleDTO> result = supplier.get();
            return result == null ? Collections.emptyList() : result;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private static String formatVND(double amount) {
        return String.format("%,.0f đ", amount).replace(',', '.');
    }

    private record EmployeeDashboardData(
            ShiftInfo shiftInfo,
            List<RoomScheduleDTO> checkInRooms,
            int checkInBookings,
            List<RoomScheduleDTO> checkOutRooms,
            int needPayment
    ) {}

    @FunctionalInterface
    private interface IntSupplierEx {
        int getAsInt() throws Exception;
    }

    @FunctionalInterface
    private interface ListSupplierEx {
        List<RoomScheduleDTO> get() throws Exception;
    }
}