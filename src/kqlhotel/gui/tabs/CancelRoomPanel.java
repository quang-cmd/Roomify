package kqlhotel.gui.tabs;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.awt.Image;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import kqlhotel.dao.ConnectDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import kqlhotel.gui.components.RoundedPanel;
import kqlhotel.gui.theme.ThemeColors;
import net.miginfocom.swing.MigLayout;

public class CancelRoomPanel extends JPanel {
    private static final Color PAGE_BG = new Color(245, 248, 252);
    
    private final JPanel leftCardPanel = new JPanel(new CardLayout());
    private final JPanel rightCardPanel = new JPanel(new CardLayout());
    private JLabel step1;
    private JLabel step2;
    private JLabel arrow;

    private final JTextField txtMaDatPhong = new JTextField();
    private final JTextField txtTenKhach = new JTextField();
    private final JTextField txtSdt = new JTextField();
    private final JTextField txtNgayNhan = new JTextField();

    private class BookingDTO {
        String maDatPhong;
        String maPhong;
        String tenLoaiPhong;
        int tang;
        String tenKhach;
        String sdt;
        LocalDateTime ngayNhanDuKien;
        double tienCoc;
        String maHD;
        boolean isFullyPaid;
    }

    private BookingDTO selectedBooking;
    private String selectedRoomType = "Phòng 201 - Deluxe";
    private String selectedFloor = "Tầng 2";

    // Dynamic Labels for policy calculating
    private JLabel lblTienCoc = new JLabel("0đ");
    private JLabel lblTienTru = new JLabel("0đ");
    private JLabel lblTienHoan = new JLabel("0đ");
    private JLabel lblTrangThai = new JLabel("Trống");
    private JLabel polSub = new JLabel("");
    private JLabel polEnd = new JLabel("");
    private double computedPenalty = 0;

    public CancelRoomPanel() {
        setOpaque(false);
        setBackground(PAGE_BG);
        setLayout(new MigLayout("insets 24,gap 20,wrap 1", "[grow,fill]", "[][grow,fill]"));

        // ===== 1. Header =====
        JPanel header = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));
        header.setOpaque(false);

        JPanel titlePanel = new JPanel(new MigLayout("insets 0,wrap 1,gap 2", "[]", "[]"));
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("Hủy đặt phòng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(15, 23, 42));
        titlePanel.add(title);

        // Stepper
        JPanel stepper = new JPanel(new MigLayout("insets 6 16,gap 10", "[][][]", "[]"));
        stepper.setOpaque(false);
        stepper.setBackground(Color.WHITE);
        stepper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 235, 245), 1),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
        
        step1 = new JLabel("1   Tìm đặt phòng");
        step1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        step1.setForeground(new Color(15, 23, 42));
        
        arrow = new JLabel(" > ");
        arrow.setForeground(new Color(200, 210, 230));

        step2 = new JLabel("2   Xác nhận hủy");
        step2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        step2.setForeground(new Color(100, 116, 139));

        stepper.add(step1);
        stepper.add(arrow);
        stepper.add(step2);

        header.add(titlePanel);
        header.add(stepper, "alignx right");

        // ===== 2. Body Area (Split 2 Columns) =====
        JPanel body = new JPanel(new MigLayout("insets 20 0,gap 20", "[310!][grow,fill]", "[grow,fill]"));
        body.setOpaque(false);

        leftCardPanel.setOpaque(false);
        rightCardPanel.setOpaque(false);

        leftCardPanel.add(createFormCard(), "SEARCH");
        leftCardPanel.add(createConfirmFormCard(), "CONFIRM");

        updateSearchResults();
        body.add(leftCardPanel, "aligny top");
        body.add(rightCardPanel, "aligny top, grow");

        add(header);
        add(body, "grow");
        
        setState("RESULT");
    }

    private void updateSearchResults() {
        rightCardPanel.removeAll();
        rightCardPanel.add(createEmptyResultCard(), "EMPTY");
        rightCardPanel.add(createSearchResultCard(false), "RESULT");
        rightCardPanel.add(createSearchResultCard(true), "CONFIRMING");
        rightCardPanel.revalidate();
        rightCardPanel.repaint();
    }

    private void setState(String state) {
        if (state.equals("SEARCH")) {
            ((CardLayout)leftCardPanel.getLayout()).show(leftCardPanel, "SEARCH");
            ((CardLayout)rightCardPanel.getLayout()).show(rightCardPanel, "EMPTY");
            step1.setForeground(new Color(15, 23, 42));
            step1.setFont(step1.getFont().deriveFont(Font.BOLD));
            step2.setForeground(new Color(100, 116, 139));
            step2.setFont(step2.getFont().deriveFont(Font.PLAIN));
            arrow.setForeground(new Color(200, 210, 230));
        } else if (state.equals("RESULT")) {
            ((CardLayout)leftCardPanel.getLayout()).show(leftCardPanel, "SEARCH");
            ((CardLayout)rightCardPanel.getLayout()).show(rightCardPanel, "RESULT");
            step1.setForeground(new Color(15, 23, 42));
            step1.setFont(step1.getFont().deriveFont(Font.BOLD));
            step2.setForeground(new Color(100, 116, 139));
            step2.setFont(step2.getFont().deriveFont(Font.PLAIN));
            arrow.setForeground(new Color(200, 210, 230));
        } else if (state.equals("CONFIRM")) {
            leftCardPanel.add(createConfirmFormCard(), "CONFIRM");
            ((CardLayout)leftCardPanel.getLayout()).show(leftCardPanel, "CONFIRM");
            rightCardPanel.add(createSearchResultCard(true), "CONFIRMING");
            ((CardLayout)rightCardPanel.getLayout()).show(rightCardPanel, "CONFIRMING");
            
            step1.setForeground(new Color(100, 116, 139));
            step1.setFont(step1.getFont().deriveFont(Font.PLAIN));
            step2.setForeground(new Color(220, 38, 38));
            step2.setFont(step2.getFont().deriveFont(Font.BOLD));
            arrow.setForeground(new Color(100, 116, 139));
        }
    }

    private RoundedPanel createFormCard() {
        RoundedPanel card = new RoundedPanel(16, Color.WHITE, new Color(226, 232, 240), 1f);
        card.setLayout(new MigLayout("wrap 1,insets 24,gap 12", "[grow,fill]", "[]"));

        JPanel hForm = new JPanel(new MigLayout("insets 0,gap 10", "[grow]", "[]"));
        hForm.setOpaque(false);
        
        JLabel fTitle = new JLabel("Tìm đặt phòng cần hủy");
        fTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        fTitle.setForeground(new Color(15, 23, 42));
        JLabel fSub = new JLabel("Nhập thông tin để tra cứu đặt phòng.");
        fSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fSub.setForeground(new Color(100, 116, 139));
        hForm.add(fTitle, "wrap");
        hForm.add(fSub);

        card.add(hForm, "gapy 0 16");

        card.add(createLabel("Mã đặt phòng"));
        txtMaDatPhong.putClientProperty("JTextField.placeholderText", "VD: BK001");
        card.add(createFieldEnclosure("", txtMaDatPhong), "h 42!");

        card.add(createLabel("Tên khách hàng"));
        txtTenKhach.putClientProperty("JTextField.placeholderText", "Nhập tên khách");
        card.add(createFieldEnclosure("", txtTenKhach), "h 42!");

        card.add(createLabel("Số điện thoại"));
        txtSdt.putClientProperty("JTextField.placeholderText", "09xx xxx xxx");
        card.add(createFieldEnclosure("", txtSdt), "h 42!");

        card.add(createLabel("Ngày nhận phòng"));
        txtNgayNhan.putClientProperty("JTextField.placeholderText", "dd/mm/yyyy");
        card.add(createFieldEnclosure("", txtNgayNhan), "h 42!");

        JButton btnSearch = new JButton("Tìm đặt phòng");
        btnSearch.setBackground(new Color(15, 23, 42));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSearch.setFocusPainted(false);
        btnSearch.addActionListener(e -> {
            updateSearchResults();
            setState("RESULT");
        });

        JButton btnReset = new JButton("Làm mới");
        btnReset.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnReset.setForeground(new Color(100, 116, 139));
        btnReset.setBackground(new Color(248, 250, 252));
        btnReset.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        btnReset.setFocusPainted(false);
        btnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReset.addActionListener(e -> {
            txtMaDatPhong.setText("");
            txtTenKhach.setText("");
            txtSdt.setText("");
            txtNgayNhan.setText("");
            updateSearchResults();
            setState("RESULT");
        });

        JPanel btnRow = new JPanel(new MigLayout("insets 0, gap 10", "[grow,fill][grow,fill]", "[]"));
        btnRow.setOpaque(false);
        btnRow.add(btnReset, "h 42!");
        btnRow.add(btnSearch, "h 42!");

        card.add(btnRow, "gapy 16 0");

        return card;
    }

    private JPanel createConfirmFormCard() {
        JPanel wrap = new JPanel(new MigLayout("insets 0, wrap 1, gap 8", "[grow,fill]", "top"));
        wrap.setOpaque(false);

        JButton btnBack = new JButton("← Quay lại danh sách");
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnBack.setForeground(new Color(100, 116, 139));
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setMargin(new java.awt.Insets(0,0,0,0));
        btnBack.setHorizontalAlignment(SwingConstants.LEFT);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> setState("RESULT"));
        wrap.add(btnBack, "gapy 0 4");

        JLabel title = new JLabel("Xác nhận hủy phòng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(220, 38, 38));
        wrap.add(title);

        RoundedPanel blueBox = new RoundedPanel(10, new Color(239, 246, 255), new Color(191, 219, 254), 1);
        blueBox.setLayout(new MigLayout("insets 10, gap 10", "[][grow]", "[]"));
        JPanel blueText = new JPanel(new MigLayout("insets 0, wrap 1", "[]", "[]"));
        blueText.setOpaque(false);
        JLabel dpRoom = new JLabel((selectedBooking != null ? selectedBooking.maDatPhong : "") + " - " + selectedRoomType);
        dpRoom.setFont(new Font("Segoe UI", Font.BOLD, 14));
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        JLabel dpCust = new JLabel((selectedBooking != null ? selectedBooking.tenKhach : "") + " - " + (selectedBooking != null ? selectedBooking.ngayNhanDuKien.format(dtf) : ""));
        dpCust.setForeground(new Color(100, 116, 139));
        blueText.add(dpRoom); blueText.add(dpCust);
        blueBox.add(new JLabel(""), "w 20!"); blueBox.add(blueText);
        wrap.add(blueBox);

        wrap.add(createLabel("Thời điểm yêu cầu hủy (dd/MM/yyyy HH:mm) *"));
        JTextField txtTime = new JTextField(LocalDateTime.now().format(dtf));
        wrap.add(createFieldEnclosure("", txtTime), "h 36!");

        wrap.add(createLabel("Lý do hủy (Tùy chọn)"));
        JTextArea txtReason = new JTextArea(2, 20);
        txtReason.setLineWrap(true);
        txtReason.setWrapStyleWord(true);
        txtReason.setForeground(new Color(15, 23, 42));
        
        RoundedPanel wrapReason = new RoundedPanel(8, Color.WHITE, new Color(226, 232, 240), 1);
        wrapReason.setLayout(new BorderLayout());
        wrapReason.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        txtReason.setBorder(BorderFactory.createEmptyBorder());
        wrapReason.add(txtReason, BorderLayout.CENTER);
        wrap.add(wrapReason, "h 50!");

        RoundedPanel policyBox = new RoundedPanel(10, new Color(254, 242, 242), new Color(254, 202, 202), 1);
        policyBox.setLayout(new MigLayout("insets 10, wrap 1, gap 6", "[grow,fill]", "[]"));
        JLabel polTitle = new JLabel("Chính sách áp dụng");
        polTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        polTitle.setForeground(new Color(220, 38, 38));
        
        polSub.setForeground(new Color(220, 38, 38));
        
        JPanel polGrid = new JPanel(new MigLayout("insets 0, gap 6", "[grow,fill][grow,fill]", "[]4[]"));
        polGrid.setOpaque(false);
        polGrid.add(createMoneyBox("Tiền cọc đã nhận", lblTienCoc));
        polGrid.add(createMoneyBox("Tiền bị trừ", lblTienTru), "wrap");
        polGrid.add(createMoneyBox("Tiền hoàn lại", lblTienHoan));
        polGrid.add(createMoneyBox("Trạng thái phòng", lblTrangThai));
        
        polEnd.setForeground(new Color(220, 100, 100));
        polEnd.setFont(new Font("Segoe UI", Font.PLAIN, 10));

        policyBox.add(polTitle);
        policyBox.add(polSub);
        policyBox.add(polGrid, "gapy 4 4");
        policyBox.add(polEnd);
        wrap.add(policyBox);

        JButton btnConfirm = new JButton("Xác nhận hủy phòng");
        btnConfirm.setBackground(new Color(220, 38, 38));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnConfirm.setFocusPainted(false);
        btnConfirm.addActionListener(e -> {
            if (selectedBooking != null) {
                double refund = Math.max(0, selectedBooking.tienCoc - computedPenalty);
                boolean success = cancelBookingInDB(selectedBooking.maDatPhong, selectedBooking.maHD, refund, selectedBooking.tienCoc);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Hủy phòng thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    
                    // Refresh RoomManagementPanel data
                    java.awt.Window win = javax.swing.SwingUtilities.getWindowAncestor(this);
                    if (win instanceof kqlhotel.gui.AppFrame) {
                        ((kqlhotel.gui.AppFrame) win).refreshRoomManagementData();
                    }

                    setState("SEARCH");
                    updateSearchResults();
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi hủy phòng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        wrap.add(btnConfirm, "h 40!, gapy 4 0");

        Runnable updatePolicy = () -> {
            if (selectedBooking == null) return;
            try {
                LocalDateTime cancelTime = LocalDateTime.parse(txtTime.getText().trim(), dtf);
                calculateCancellationFee(selectedBooking.ngayNhanDuKien, cancelTime, selectedBooking.tienCoc, selectedBooking.isFullyPaid);
            } catch (DateTimeParseException ex) {
                polSub.setText("Định dạng ngày không hợp lệ.");
            }
        };
        
        txtTime.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updatePolicy.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updatePolicy.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updatePolicy.run(); }
        });
        updatePolicy.run();

        return wrap;
    }

    private void calculateCancellationFee(LocalDateTime checkInTime, LocalDateTime cancelTime, double deposit, boolean isFullyPaid) {
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###đ");
        lblTienCoc.setText(df.format(deposit));
        lblTrangThai.setText("Trống");
        
        if (isFullyPaid) {
            lblTienTru.setText("0đ");
            lblTienHoan.setText("0đ");
            lblTrangThai.setText("Giữ nguyên");
            polSub.setText("Đã thanh toán toàn bộ: Phòng được giữ nguyên.");
            polEnd.setText("Khách thanh toán toàn bộ tiền phòng sẽ không bị hủy.");
            return;
        }

        long daysBefore = java.time.temporal.ChronoUnit.DAYS.between(cancelTime.toLocalDate(), checkInTime.toLocalDate());
        double penalty = 0;
        
        if (cancelTime.isAfter(checkInTime)) {
            penalty = deposit;
            polSub.setText("Hủy sau giờ nhận phòng: Phạt 100% cọc.");
        } else if (daysBefore <= 5) {
            penalty = deposit;
            polSub.setText("Hủy trong vòng 1-5 ngày: Phạt 100% cọc.");
        } else if (daysBefore <= 10) {
            penalty = deposit * 0.5;
            polSub.setText("Hủy trong vòng 6-10 ngày: Phạt 50% cọc.");
        } else {
            penalty = 0;
            polSub.setText("Hủy sớm: Không mất phí.");
        }
        
        this.computedPenalty = penalty;
        lblTienTru.setText(df.format(penalty));
        lblTienHoan.setText(df.format(deposit - penalty));
    }

    private JPanel createSearchResultCard(boolean isConfirming) {
        JPanel wrap = new JPanel(new MigLayout("wrap 1,insets 0 0 0 0", "[grow,fill]", "[][][grow,fill]"));
        wrap.setOpaque(false);
        
        JPanel titleRow = new JPanel(new MigLayout("insets 0", "[]", "[]"));
        titleRow.setOpaque(false);
        JLabel title = new JLabel("Danh sách đặt phòng phù hợp");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(15, 23, 42));
        titleRow.add(title);
        
        JLabel sub = new JLabel("Chọn một đặt phòng để thực hiện hủy.");
        sub.setForeground(new Color(100, 116, 139));
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        wrap.add(titleRow, "gapy 0 4");
        wrap.add(sub, "gapy 0 16");

        JPanel listPnl = new ScrollablePanel(new WrapLayout(java.awt.FlowLayout.LEFT, 20, 20));
        listPnl.setOpaque(false);

        if (isConfirming && selectedBooking != null) {
            listPnl.add(createSingleRoomCard(selectedBooking, selectedRoomType, selectedFloor, isConfirming));
        } else {
            List<BookingDTO> dbList = fetchBookingsFromDB();
            for (BookingDTO b : dbList) {
                if (checkMatch(b.maDatPhong, b.tenKhach, b.sdt)) {
                    String rType = "Phòng " + b.maPhong + " - " + b.tenLoaiPhong;
                    String fl = "Tầng " + b.tang;
                    listPnl.add(createSingleRoomCard(b, rType, fl, isConfirming));
                }
            }
        }
        
        JScrollPane sp = new JScrollPane(listPnl);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        wrap.add(sp, "grow");
        return wrap;
    }

    private boolean checkMatch(String code, String name, String phone) {
        String m = txtMaDatPhong.getText().trim().toLowerCase();
        String t = txtTenKhach.getText().trim().toLowerCase();
        String s = txtSdt.getText().trim().toLowerCase();
        if (m.isEmpty() && t.isEmpty() && s.isEmpty()) return true;
        if (!m.isEmpty() && code.toLowerCase().contains(m)) return true;
        if (!t.isEmpty() && name.toLowerCase().contains(t)) return true;
        if (!s.isEmpty() && phone.contains(s)) return true;
        return false;
    }

    private JPanel createSingleRoomCard(BookingDTO b, String type, String floor, boolean isConfirming) {
        RoundedPanel card = new RoundedPanel(16, Color.WHITE, new Color(226, 232, 240), 1.5f);
        card.setLayout(new MigLayout("insets 16 20, wrap 1, gap 8", "[grow,fill]", "[]"));
        card.setPreferredSize(new Dimension(330, 320));
        
        if (!isConfirming) {
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            card.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectedBooking = b;
                    selectedRoomType = type;
                    selectedFloor = floor;
                    setState("CONFIRM");
                }
            });
        }

        JLabel dpId = new JLabel(b.maDatPhong);
        dpId.setFont(new Font("Segoe UI", Font.BOLD, 18));
        dpId.setForeground(new Color(15, 23, 42));

        JLabel rType = new JLabel(type + " · " + floor);
        rType.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rType.setForeground(new Color(100, 116, 139));

        card.add(dpId);
        card.add(rType);
        
        RoundedPanel infoBox = new RoundedPanel(8, new Color(248, 250, 252), new Color(226, 232, 240), 1);
        infoBox.setLayout(new MigLayout("insets 12, wrap 1, gap 4", "[]", "[]"));
        infoBox.add(new JLabel("Khách: " + b.tenKhach));
        infoBox.add(new JLabel("SĐT: " + b.sdt));
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        infoBox.add(new JLabel("Ngày nhận: " + b.ngayNhanDuKien.format(dtf)));
        card.add(infoBox, "gapy 8 8");

        JPanel botRow = new JPanel(new BorderLayout());
        botRow.setOpaque(false);
        

        if (isConfirming) {
            JLabel lblConfirming = new JLabel("Đã chọn", SwingConstants.CENTER);
            lblConfirming.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblConfirming.setForeground(new Color(220, 38, 38));
            botRow.add(lblConfirming, BorderLayout.EAST);
        } else {
            JButton btnCancel = new JButton("Hủy đặt phòng");
            btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnCancel.setForeground(new Color(220, 38, 38));
            btnCancel.setBackground(new Color(254, 242, 242));
            btnCancel.setFocusPainted(false);
            btnCancel.addActionListener(e -> {
                selectedBooking = b;
                selectedRoomType = type;
                selectedFloor = floor;
                setState("CONFIRM");
            });
            botRow.add(btnCancel, BorderLayout.EAST);
        }

        card.add(botRow, "gapy 4 0");
        return card;
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(new Color(71, 85, 105));
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return lbl;
    }

    private JPanel createFieldEnclosure(String icon, JTextField field) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(Color.WHITE);
        wrap.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        field.setBorder(BorderFactory.createEmptyBorder());
        field.setOpaque(false);
        field.setForeground(new Color(15, 23, 42));
        wrap.add(field, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel createMoneyBox(String title, JLabel valLabel) {
        RoundedPanel p = new RoundedPanel(8, Color.WHITE, new Color(226, 232, 240), 1);
        p.setLayout(new MigLayout("insets 8, wrap 1, gap 2", "[]", "[]"));
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        t.setForeground(new Color(100, 116, 139));
        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        valLabel.setForeground(new Color(15, 23, 42));
        p.add(t); p.add(valLabel);
        return p;
    }

    private JPanel createEmptyResultCard() {
        JPanel wrap = new JPanel(new MigLayout("wrap 1,insets 40 20", "[fill]", "[]"));
        wrap.setOpaque(false);
        JLabel resTitle = new JLabel("Chưa có kết quả tra cứu", SwingConstants.CENTER);
        resTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        resTitle.setForeground(new Color(15, 23, 42));
        JLabel resSub = new JLabel("<html><center>Nhập thông tin rồi nhấn <b>Tìm đặt phòng</b></center></html>", SwingConstants.CENTER);
        resSub.setForeground(new Color(100, 116, 139));
        wrap.add(resTitle, "alignx center");
        wrap.add(resSub, "alignx center, gapy 10 0");
        return wrap;
    }

    private List<BookingDTO> fetchBookingsFromDB() {
        List<BookingDTO> list = new ArrayList<>();
        String sql = "SELECT dp.maDatPhong, ctdp.maPhong, lp.tenLoaiPhong, p.tang, kh.hoTenKH, kh.sdt, ctdp.ngayNhanDuKien, dp.tienCoc, hd.maHD, 0 as isFullyPaid FROM DatPhong dp JOIN ChiTietDatPhong ctdp ON dp.maDatPhong = ctdp.maDatPhong JOIN Phong p ON ctdp.maPhong = p.maPhong JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong JOIN KhachHang kh ON dp.maKH = kh.maKH JOIN HoaDon hd ON hd.maDatPhong = dp.maDatPhong WHERE hd.trangThai = 'ChuaThanhToan'";
        try (Connection con = ConnectDB.getInstance().getConnection(); PreparedStatement pst = con.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                BookingDTO b = new BookingDTO();
                b.maDatPhong = rs.getString("maDatPhong");
                b.maPhong = rs.getString("maPhong");
                b.tenLoaiPhong = rs.getString("tenLoaiPhong");
                b.tang = rs.getInt("tang");
                b.tenKhach = rs.getString("hoTenKH");
                b.sdt = rs.getString("sdt");
                b.ngayNhanDuKien = rs.getTimestamp("ngayNhanDuKien").toLocalDateTime();
                b.tienCoc = rs.getDouble("tienCoc");
                b.maHD = rs.getString("maHD");
                list.add(b);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    private boolean cancelBookingInDB(String maDatPhong, String maHD, double refund, double deposit) {
        try (Connection con = ConnectDB.getInstance().getConnection()) {
            con.setAutoCommit(false);
            String updHD = "UPDATE HoaDon SET trangThai = 'DaHuy' WHERE maHD = ?";
            try (PreparedStatement pst = con.prepareStatement(updHD)) { pst.setString(1, maHD); pst.executeUpdate(); }
            String updP = "UPDATE Phong SET trangThaiPhong = 'Trong' WHERE maPhong IN (SELECT maPhong FROM ChiTietDatPhong WHERE maDatPhong = ?)";
            try (PreparedStatement pst = con.prepareStatement(updP)) { pst.setString(1, maDatPhong); pst.executeUpdate(); }
            con.commit();
            return true;
        } catch (Exception e) { return false; }
    }

    private static class ScrollablePanel extends JPanel implements Scrollable {
        public ScrollablePanel(java.awt.LayoutManager layout) { super(layout); }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 64; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    private static class WrapLayout extends java.awt.FlowLayout {
        WrapLayout(int a, int h, int v) { super(a, h, v); }
        @Override public Dimension preferredLayoutSize(java.awt.Container t) { return layoutSize(t); }
        private Dimension layoutSize(java.awt.Container t) {
            int w = t.getWidth(); if (w == 0) w = 1000;
            int h = 0, rowH = 0, x = 0;
            for (java.awt.Component c : t.getComponents()) {
                Dimension d = c.getPreferredSize();
                if (x + d.width > w) { h += rowH + 20; x = 0; rowH = 0; }
                x += d.width + 20; rowH = Math.max(rowH, d.height);
            }
            return new Dimension(w, h + rowH + 40);
        }
    }
}
