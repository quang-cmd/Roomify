package kqlhotel.gui.components;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.io.File;
import java.util.List;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;
import kqlhotel.gui.theme.ThemeColors;
import kqlhotel.entity.Service;
import kqlhotel.entity.ServiceDetail;
import kqlhotel.dao.service.ServiceDAO;
import kqlhotel.dao.invoice.ServiceDetailDAO;

public class RoomDetailDialog extends JDialog {

    private final JPanel contentCardPanel = new JPanel(new CardLayout());
    private final CardLayout cardLayout = (CardLayout) contentCardPanel.getLayout();
    private String currentTab = "KHACH";
    private String roomNo;
    private String roomType;
    private String roomPrice;
    private kqlhotel.entity.Invoice invoice;
    private kqlhotel.entity.Customer customer;
    // Panel chứa danh sách dịch vụ (để refresh khi thêm/xóa)
    private JPanel serviceListPanel;
    private JLabel lblTotalServices;
    private JLabel lblVat;
    private JLabel lblGrandTotal;
    private long totalRoom;
    private JPanel addBtnRow;
    private JPanel addFormPanel;
    private Runnable onCheckout;

    // Tab buttons
    private JPanel tabKhachCont;
    private JPanel tabHoaDonCont;

    // Footer buttons
    private JButton btnFooterLeft;

    public RoomDetailDialog(Window owner, kqlhotel.entity.Phong phong, kqlhotel.entity.Invoice invoice, kqlhotel.entity.Customer customer) {
        this(owner, phong, invoice, customer, null);
    }

    public RoomDetailDialog(Window owner, kqlhotel.entity.Phong phong, kqlhotel.entity.Invoice invoice, kqlhotel.entity.Customer customer, Runnable onCheckout) {
        super(owner, "Chi tiết phòng " + phong.getMaPhong(), ModalityType.APPLICATION_MODAL);
        this.roomNo = phong.getMaPhong();
        this.roomType = phong.getLoaiPhong().getTenLoaiPhong();
        this.roomPrice = String.valueOf(phong.getLoaiPhong().getGiaPhong());
        this.invoice = invoice;
        this.customer = customer;
        this.onCheckout = onCheckout;
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        RoundedPanel rootPanel = new RoundedPanel(16, Color.WHITE, ThemeColors.BORDER, 1);
        rootPanel.setLayout(new MigLayout("insets 0, wrap 1, gap 0", "[fill, 550!]", "[]"));
        rootPanel.setOpaque(false);

        // Header
        rootPanel.add(createHeader(roomNo, roomType, "Tầng " + phong.getTang()), "growx");

        // Tabs
        rootPanel.add(createTabsRow(), "growx");

        // Content
        contentCardPanel.setOpaque(false);
        // ScrollPane for content
        JScrollPane scrollPane = new JScrollPane(contentCardPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        contentCardPanel.add(createKhachTab(), "KHACH");
        contentCardPanel.add(createHoaDonTab(), "HOADON");

        rootPanel.add(scrollPane, "grow, h 550!");

        // Footer
        rootPanel.add(createFooter(), "growx");

        setContentPane(rootPanel);
        pack();
        setLocationRelativeTo(owner);

        switchTab("KHACH");
    }

    private JPanel createHeader(String roomNo, String roomType, String floor) {
        JPanel header = new JPanel(new MigLayout("insets 16 20 16 20", "[][grow][]", "[]")) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(230, 240, 255));
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 20, 16, 16);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.BORDER));

        JPanel iconPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeColors.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconPanel.setOpaque(false);
        JLabel iconLbl = new JLabel();
        ImageIcon bedIcon = loadIcon("bed.png", 22, 22);
        if (bedIcon != null) {
            iconLbl.setIcon(bedIcon);
        } else {
            iconLbl.setText("🛏");
            iconLbl.setFont(iconLbl.getFont().deriveFont(20f));
        }
        iconLbl.setHorizontalAlignment(SwingConstants.CENTER);
        iconLbl.setForeground(Color.WHITE);
        iconPanel.add(iconLbl);

        JPanel textPanel = new JPanel(new MigLayout("insets 0, wrap 1, gap 2", "[]", "[]"));
        textPanel.setOpaque(false);
        JLabel title = new JLabel("Phòng " + roomNo + " — Khách & Hóa đơn");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        title.setForeground(ThemeColors.TEXT_PRIMARY);
        JLabel subtitle = new JLabel(roomType + " - " + floor);
        subtitle.setFont(subtitle.getFont().deriveFont(12f));
        subtitle.setForeground(ThemeColors.PRIMARY);
        textPanel.add(title);
        textPanel.add(subtitle);

        JButton btnClose = new JButton();
        ImageIcon closeIcon = loadIcon("close.png", 16, 16);
        if (closeIcon != null) {
            btnClose.setIcon(closeIcon);
        } else {
            btnClose.setText("×");
            btnClose.setFont(btnClose.getFont().deriveFont(Font.BOLD, 20f));
        }
        btnClose.setForeground(ThemeColors.TEXT_MUTED);
        btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());
        btnClose.setMargin(new Insets(0, 0, 0, 0));

        header.add(iconPanel, "w 40!, h 40!");
        header.add(textPanel, "growx, gapx 10");
        header.add(btnClose, "top");

        return header;
    }

    private JPanel createTabsRow() {
        JPanel tabs = new JPanel(new MigLayout("insets 0 20 0 20, gap 10", "[][][]", "[45!]"));
        tabs.setBackground(Color.WHITE);
        tabs.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.BORDER));

        tabKhachCont = new JPanel(new MigLayout("insets 0 10 0 10", "[]", "[grow]"));
        tabKhachCont.setBackground(Color.WHITE);
        JLabel lblKhach = new JLabel("Thông tin khách");
        ImageIcon clientIcon = loadIcon("client.png", 14, 14);
        if (clientIcon != null) {
            lblKhach.setIcon(clientIcon);
            lblKhach.setIconTextGap(6);
        }
        lblKhach.setFont(lblKhach.getFont().deriveFont(Font.BOLD, 13f));
        tabKhachCont.add(lblKhach, "aligny center");
        tabKhachCont.setCursor(new Cursor(Cursor.HAND_CURSOR));

        tabHoaDonCont = new JPanel(new MigLayout("insets 0 10 0 10, gap 8", "[][]", "[grow]"));
        tabHoaDonCont.setBackground(Color.WHITE);
        JLabel lblHoaDon = new JLabel("Hóa đơn");
        ImageIcon invoiceIcon = loadIcon("invoice.png", 14, 14);
        if (invoiceIcon != null) {
            lblHoaDon.setIcon(invoiceIcon);
            lblHoaDon.setIconTextGap(6);
        }
        lblHoaDon.setFont(lblHoaDon.getFont().deriveFont(Font.BOLD, 13f));

        JPanel badge = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 245, 235));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        JLabel badgeLbl = new JLabel("Chờ thanh toán");
        badgeLbl.setFont(badgeLbl.getFont().deriveFont(Font.BOLD, 10f));
        badgeLbl.setForeground(ThemeColors.ACCENT);
        badge.add(badgeLbl);

        tabHoaDonCont.add(lblHoaDon, "aligny center");
        tabHoaDonCont.add(badge, "aligny center");
        tabHoaDonCont.setCursor(new Cursor(Cursor.HAND_CURSOR));

        tabKhachCont.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { switchTab("KHACH"); }
        });
        tabHoaDonCont.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { switchTab("HOADON"); }
        });

        tabs.add(tabKhachCont, "growy");
        tabs.add(tabHoaDonCont, "growy");

        return tabs;
    }

    private void switchTab(String tab) {
        currentTab = tab;
        cardLayout.show(contentCardPanel, tab);

        if (tab.equals("KHACH")) {
            tabKhachCont.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ThemeColors.PRIMARY));
            ((JLabel)tabKhachCont.getComponent(0)).setForeground(ThemeColors.PRIMARY);

            tabHoaDonCont.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
            ((JLabel)tabHoaDonCont.getComponent(0)).setForeground(ThemeColors.TEXT_MUTED);

            if(btnFooterLeft != null) {
                btnFooterLeft.setText("Xem hóa đơn");
                ImageIcon icon = loadIcon("invoice.png", 14, 14);
                if (icon != null) btnFooterLeft.setIcon(icon);
            }
        } else {
            tabKhachCont.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
            ((JLabel)tabKhachCont.getComponent(0)).setForeground(ThemeColors.TEXT_MUTED);

            tabHoaDonCont.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ThemeColors.PRIMARY));
            ((JLabel)tabHoaDonCont.getComponent(0)).setForeground(ThemeColors.PRIMARY);

            if(btnFooterLeft != null) {
                btnFooterLeft.setText("Xem thông tin khách");
                ImageIcon icon = loadIcon("client.png", 14, 14);
                if (icon != null) btnFooterLeft.setIcon(icon);
            }
        }
    }

    private JPanel createKhachTab() {
        JPanel pnl = new JPanel(new MigLayout("insets 20, wrap 1, gap 16", "[grow,fill]", "[]"));
        pnl.setBackground(Color.WHITE);

        JPanel hdr = new JPanel(new MigLayout("insets 0", "[][]", "[]"));
        hdr.setOpaque(false);
        JPanel badgeStatus = new RoundedPanel(12, new Color(225, 250, 230), null, 0);
        badgeStatus.setLayout(new BorderLayout());
        badgeStatus.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        JLabel lblStatus = new JLabel("• Đã nhận phòng");
        lblStatus.setFont(lblStatus.getFont().deriveFont(Font.BOLD, 11f));
        lblStatus.setForeground(new Color(40, 160, 80));
        badgeStatus.add(lblStatus);

        String maHD = (invoice != null) ? invoice.getMaHD() : "N/A";
        JLabel lblCode = new JLabel("Mã Hóa Đơn: " + maHD);
        lblCode.setForeground(ThemeColors.TEXT_MUTED);
        lblCode.setFont(lblCode.getFont().deriveFont(12f));

        hdr.add(badgeStatus);
        hdr.add(lblCode, "gapx 10");

        RoundedPanel pnlPersonal = new RoundedPanel(12, Color.WHITE, ThemeColors.BORDER, 1);
        pnlPersonal.setLayout(new MigLayout("insets 16 20 16 20, wrap 2", "[grow][grow]", "[]12[]"));
        JLabel title1 = new JLabel("THÔNG TIN CÁ NHÂN");
        title1.setFont(title1.getFont().deriveFont(Font.BOLD, 10f));
        title1.setForeground(ThemeColors.TEXT_MUTED);
        pnlPersonal.add(title1, "span 2, wrap");

        String ten = (customer != null) ? customer.getHoTenKH() : "Khách vãng lai";
        String sdt = (customer != null) ? customer.getSdt() : "N/A";
        String email = (customer != null && customer.getEmail() != null && !customer.getEmail().isEmpty()) ? customer.getEmail() : "N/A";
        String cccd = (customer != null) ? customer.getCCCD() : "N/A";
        String diaChi = (customer != null && customer.getDiaChi() != null && !customer.getDiaChi().isEmpty()) ? customer.getDiaChi() : "N/A";

        pnlPersonal.add(createIconLabelData("client.png", "Họ và tên", ten));
        pnlPersonal.add(createIconLabelData("telephone.png", "Điện thoại", sdt));
        pnlPersonal.add(createIconLabelData("email.png", "Email", email));
        pnlPersonal.add(createIconLabelData("credit-card.png", "CMND/CCCD", cccd));
        pnlPersonal.add(createIconLabelData("location.png", "Địa chỉ", diaChi), "span 2");

        RoundedPanel pnlBooking = new RoundedPanel(12, new Color(230, 255, 240), new Color(180, 240, 200), 1);
        pnlBooking.setLayout(new MigLayout("insets 16 20 16 20, wrap 2", "[grow][grow]", "[]12[]"));
        JLabel title2 = new JLabel("THÔNG TIN ĐẶT PHÒNG");
        title2.setFont(title2.getFont().deriveFont(Font.BOLD, 10f));
        title2.setForeground(new Color(40, 120, 70));
        pnlBooking.add(title2, "span 2, wrap");

        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String checkIn = (invoice != null && invoice.getNgayLapHD() != null) ? invoice.getNgayLapHD().format(dtf) : "N/A";
        String soKhach = (invoice != null) ? (invoice.getSoLuongNguoi() + " người") : "N/A";
        long totalRoomK = (invoice != null) ? (long)invoice.getTienPhong() : parseMoney(roomPrice);

        // Lấy ngày trả phòng dự kiến từ DB
        String expectedCheckout = getExpectedCheckoutDate(invoice, roomNo);

        pnlBooking.add(createIconLabelDataTextOnly("Nhận phòng", checkIn, new Color(40, 160, 80), new Color(40, 120, 70)));
        pnlBooking.add(createIconLabelDataTextOnly("Trả phòng", expectedCheckout, new Color(40, 160, 80), new Color(40, 120, 70)));
        pnlBooking.add(createIconLabelDataTextOnly("Số khách", soKhach, new Color(40, 160, 80), new Color(40, 120, 70)));
        pnlBooking.add(createIconLabelDataTextOnly("Tiền phòng", formatMoney(totalRoomK), new Color(40, 160, 80), new Color(40, 120, 70)));

        JPanel pnlStats = new JPanel(new MigLayout("insets 0, gap 12", "[grow,fill][grow,fill][grow,fill]", "[]"));
        pnlStats.setOpaque(false);
        pnlStats.add(createStatBox("Hạng thẻ", (customer != null) ? customer.getHangKH() : "N/A", new Color(255, 245, 210), new Color(250, 220, 120), new Color(160, 80, 0)));
        pnlStats.add(createStatBox("Điểm tích lũy", (customer != null) ? String.valueOf(customer.getDiemTichLuy()) : "0", new Color(250, 245, 255), new Color(230, 210, 255), new Color(120, 60, 160)));
        pnlStats.add(createStatBox("Quốc tịch", (customer != null) ? customer.getQuocTich() : "N/A", new Color(240, 248, 255), new Color(200, 220, 255), new Color(40, 80, 160)));

        PrimaryButton btnBigInvoice = new PrimaryButton("Xem hóa đơn");
        ImageIcon bigInvoiceIcon = loadIcon("invoice.png", 16, 16);
        if (bigInvoiceIcon != null) {
            btnBigInvoice.setIcon(bigInvoiceIcon);
            btnBigInvoice.setIconTextGap(8);
        }
        btnBigInvoice.setBackground(ThemeColors.PRIMARY);
        btnBigInvoice.setForeground(Color.WHITE);
        btnBigInvoice.addActionListener(e -> switchTab("HOADON"));

        pnl.add(hdr);
        pnl.add(pnlPersonal);
        pnl.add(pnlBooking);
        pnl.add(pnlStats);
        pnl.add(btnBigInvoice, "h 44!");

        return pnl;
    }

    private JPanel createIconLabelData(String iconName, String label, String data) {
        JPanel pnl = new JPanel(new MigLayout("insets 0, wrap 1, gap 2", "[]", "[]"));
        pnl.setOpaque(false);
        JLabel lblTitle = new JLabel(label);
        ImageIcon icon = loadIcon(iconName, 14, 14);
        if (icon != null) {
            lblTitle.setIcon(icon);
            lblTitle.setIconTextGap(6);
        }
        lblTitle.setForeground(ThemeColors.TEXT_MUTED);
        lblTitle.setFont(lblTitle.getFont().deriveFont(11f));
        JLabel lblData = new JLabel(data);
        lblData.setForeground(ThemeColors.TEXT_PRIMARY);
        lblData.setFont(lblData.getFont().deriveFont(Font.BOLD, 13f));
        pnl.add(lblTitle);
        pnl.add(lblData);
        return pnl;
    }

    private JPanel createIconLabelDataTextOnly(String label, String data, Color titleColor, Color dataColor) {
        JPanel pnl = new JPanel(new MigLayout("insets 0, wrap 1, gap 4", "[]", "[]"));
        pnl.setOpaque(false);
        JLabel lblTitle = new JLabel(label);
        lblTitle.setForeground(titleColor);
        lblTitle.setFont(lblTitle.getFont().deriveFont(11f));
        JLabel lblData = new JLabel(data);
        lblData.setForeground(dataColor);
        lblData.setFont(lblData.getFont().deriveFont(Font.BOLD, 14f));
        pnl.add(lblTitle);
        pnl.add(lblData);
        return pnl;
    }

    private JPanel createStatBox(String title, String val, Color bg, Color border, Color textCol) {
        RoundedPanel pnl = new RoundedPanel(12, bg, border, 1);
        pnl.setLayout(new MigLayout("insets 16 8 16 8, wrap 1, gap 8", "[grow,center]", "[]"));
        JLabel lblT = new JLabel(title);
        lblT.setForeground(textCol);
        lblT.setFont(lblT.getFont().deriveFont(10f));
        JLabel lblV = new JLabel(val);
        lblV.setForeground(textCol);
        lblV.setFont(lblV.getFont().deriveFont(Font.BOLD, 16f));
        pnl.add(lblT);
        pnl.add(lblV);
        return pnl;
    }

    private JPanel createHoaDonTab() {
        JPanel pnl = new JPanel(new MigLayout("insets 20, wrap 1, gap 16", "[grow,fill]", "[]"));
        pnl.setBackground(Color.WHITE);

        RoundedPanel hdr = new RoundedPanel(12, Color.WHITE, ThemeColors.BORDER, 1);
        hdr.setLayout(new MigLayout("insets 16 20 16 20", "[grow][]", "[]"));

        JPanel pnlLeft = new JPanel(new MigLayout("insets 0, wrap 1, gap 2", "[]", "[]"));
        pnlLeft.setOpaque(false);
        JLabel lbl1 = new JLabel("Số hóa đơn");
        lbl1.setForeground(ThemeColors.TEXT_MUTED);
        lbl1.setFont(lbl1.getFont().deriveFont(11f));
        String maHD = (invoice != null) ? invoice.getMaHD() : "N/A";
        JLabel lbl2 = new JLabel(maHD);
        lbl2.setForeground(ThemeColors.TEXT_PRIMARY);
        lbl2.setFont(lbl2.getFont().deriveFont(Font.BOLD, 16f));
        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String ngayLap = (invoice != null && invoice.getNgayLapHD() != null) ? invoice.getNgayLapHD().format(dtf) : "N/A";
        JLabel lbl3 = new JLabel("Tạo ngày " + ngayLap);
        lbl3.setForeground(ThemeColors.TEXT_MUTED);
        lbl3.setFont(lbl3.getFont().deriveFont(11f));
        pnlLeft.add(lbl1);
        pnlLeft.add(lbl2);
        pnlLeft.add(lbl3);

        JPanel pnlRight = new JPanel(new MigLayout("insets 0, wrap 1, gap 8", "[right]", "[]"));
        pnlRight.setOpaque(false);

        JPanel badgeStatus = new RoundedPanel(20, Color.WHITE, new Color(250, 200, 150), 1);
        badgeStatus.setLayout(new BorderLayout());
        badgeStatus.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        JLabel lblStat = new JLabel("• Chờ thanh toán");
        lblStat.setFont(lblStat.getFont().deriveFont(Font.BOLD, 11f));
        lblStat.setForeground(ThemeColors.ACCENT);
        badgeStatus.add(lblStat);

        JLabel lblSub = new JLabel("Có thể bổ sung dịch vụ");
        lblSub.setForeground(ThemeColors.TEXT_MUTED);
        lblSub.setFont(lblSub.getFont().deriveFont(11f));

        pnlRight.add(badgeStatus, "right");
        pnlRight.add(lblSub, "right");

        hdr.add(pnlLeft);
        hdr.add(pnlRight, "aligny center");

        RoundedPanel mainPnl = new RoundedPanel(12, Color.WHITE, ThemeColors.BORDER, 1);
        mainPnl.setLayout(new MigLayout("insets 0, wrap 1, gap 0", "[grow,fill]", "[]"));

        // Title row với star icon
        JPanel titleRow = new JPanel(new MigLayout("insets 16 20 12 20", "[][]", "[]"));
        titleRow.setOpaque(false);
        JLabel starIcon = new JLabel();
        ImageIcon starIco = loadIcon("star.png", 14, 14);
        if (starIco != null) starIcon.setIcon(starIco); else starIcon.setText("★");
        JLabel mainTitle = new JLabel("CHI TIẾT HÓA ĐƠN");
        mainTitle.setFont(mainTitle.getFont().deriveFont(Font.BOLD, 11f));
        mainTitle.setForeground(ThemeColors.TEXT_MUTED);
        titleRow.add(starIcon);
        titleRow.add(mainTitle, "gapx 4");
        mainPnl.add(titleRow);

        // Tiền phòng (fixed)
        mainPnl.add(createSeparator());
        long totalRoom = (invoice != null) ? (long)invoice.getTienPhong() : (parseMoney(roomPrice) * 4);
        mainPnl.add(createInvoiceItem("bed.png", "🛏", "Tiền phòng " + roomType, "Tính đến hiện tại", formatMoney(totalRoom), null, null));
        mainPnl.add(createSeparator());

        // Danh sách dịch vụ từ DB
        serviceListPanel = new JPanel(new MigLayout("insets 0, wrap 1, gap 0", "[grow,fill]", "[]"));
        serviceListPanel.setOpaque(false);
        mainPnl.add(serviceListPanel, "growx");

        // Nút + inline form thêm dịch vụ
        addBtnRow = createAddBtnRow();
        addFormPanel = createInlineAddForm();
        addFormPanel.setVisible(false);
        mainPnl.add(addBtnRow, "growx");
        mainPnl.add(addFormPanel, "growx");
        mainPnl.add(createSeparator());

        // Tổng tiền
        JPanel sumRow = new JPanel(new MigLayout("insets 16 20 20 20, wrap 2, gapy 8", "[grow][right]", "[]"));
        sumRow.setOpaque(false);
        long totalServices = (invoice != null) ? (long)invoice.getTienDichVu() : 0;
        long vat = (invoice != null) ? (long)invoice.getTienThue() : (long)((totalRoom + totalServices) * 0.1);
        long grandTotal = (invoice != null) ? (long)invoice.getTongTienThanhToan() : (totalRoom + totalServices + vat);

        lblTotalServices = new JLabel(formatMoney(totalServices));
        lblVat = new JLabel(formatMoney(vat));
        lblGrandTotal = new JLabel(formatMoney(grandTotal));
        lblTotalServices.setForeground(ThemeColors.TEXT_PRIMARY);
        lblTotalServices.setFont(lblTotalServices.getFont().deriveFont(12f));
        lblVat.setForeground(ThemeColors.TEXT_PRIMARY);
        lblVat.setFont(lblVat.getFont().deriveFont(12f));
        lblGrandTotal.setFont(lblGrandTotal.getFont().deriveFont(Font.BOLD, 16f));
        lblGrandTotal.setForeground(ThemeColors.PRIMARY);

        sumRow.add(createMutedLabel("Tạm tính")); sumRow.add(createMutedLabelDark(formatMoney(totalRoom)));
        sumRow.add(createMutedLabel("Tiền dịch vụ")); sumRow.add(lblTotalServices);
        sumRow.add(createMutedLabel("Thuế VAT (10%)")); sumRow.add(lblVat);
        sumRow.add(createSeparator(), "span 2, growx, gapy 8 8");
        JLabel lblTongT = new JLabel("Còn phải thanh toán");
        lblTongT.setFont(lblTongT.getFont().deriveFont(Font.BOLD, 14f));
        lblTongT.setForeground(ThemeColors.TEXT_PRIMARY);
        sumRow.add(lblTongT); sumRow.add(lblGrandTotal);
        mainPnl.add(sumRow);

        // Nút thanh toán
        JPanel payRow = new JPanel(new MigLayout("insets 0 20 16 20", "[grow,fill]", "[]"));
        payRow.setOpaque(false);
        JButton btnThanhToan = new JButton("Thanh toán");
        btnThanhToan.setFont(btnThanhToan.getFont().deriveFont(Font.BOLD, 14f));
        btnThanhToan.setBackground(new Color(34, 197, 94));
        btnThanhToan.setForeground(Color.WHITE);
        btnThanhToan.setFocusPainted(false);
        btnThanhToan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnThanhToan.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(22, 163, 74), 1, true),
                BorderFactory.createEmptyBorder(10, 0, 10, 0)
        ));
        btnThanhToan.addActionListener(e -> {
            dispose();
            if (onCheckout != null) onCheckout.run();
        });
        payRow.add(btnThanhToan);
        mainPnl.add(payRow);

        pnl.add(hdr);
        pnl.add(mainPnl);

        // Load dịch vụ từ DB sau khi UI đã được dựng
        javax.swing.SwingUtilities.invokeLater(() -> refreshServiceList());

        return pnl;
    }

    private void refreshServiceList() {
        if (serviceListPanel == null) return;
        serviceListPanel.removeAll();
        if (invoice != null) {
            ServiceDetailDAO dao = new ServiceDetailDAO();
            List<ServiceDetail> list = dao.getByInvoice(invoice.getMaHD());
            for (ServiceDetail sd : list) {
                String tenDV = (sd.getGhiChu() != null && !sd.getGhiChu().isEmpty()) ? sd.getGhiChu() : sd.getMaDV();
                String sub = sd.getSoLuong() + " × " + formatMoney((long)sd.getDonGia());
                serviceListPanel.add(createInvoiceItem("star.png", "🔧", tenDV, sub, formatMoney((long)sd.getThanhTien()), sd.getMaCTDV(), serviceListPanel));
                serviceListPanel.add(createSeparator());
            }
            // Cập nhật label tổng
            double svcTotal = list.stream().mapToDouble(ServiceDetail::getThanhTien).sum();
            long roomAmt = (long)invoice.getTienPhong();
            long vatAmt = (long)((roomAmt + svcTotal) * 0.1);
            long grandAmt = roomAmt + (long)svcTotal + vatAmt;
            if (lblTotalServices != null) lblTotalServices.setText(formatMoney((long)svcTotal));
            if (lblVat != null) lblVat.setText(formatMoney(vatAmt));
            if (lblGrandTotal != null) lblGrandTotal.setText(formatMoney(grandAmt));
        }
        serviceListPanel.revalidate();
        serviceListPanel.repaint();
    }

    private JPanel createAddBtnRow() {
        JPanel row = new JPanel(new MigLayout("insets 12 20 12 20", "[grow,fill]", "[]"));
        row.setOpaque(false);
        JButton btn = new JButton("+ Thêm dịch vụ");
        btn.setFont(btn.getFont().deriveFont(13f));
        btn.setForeground(ThemeColors.PRIMARY);
        btn.setBackground(new Color(245, 250, 255));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override public void paint(Graphics g, JComponent c) {
                super.paint(g, c);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(150, 180, 240));
                g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{6}, 0));
                g2.drawRoundRect(0, 0, c.getWidth()-1, c.getHeight()-1, 8, 8);
                g2.dispose();
            }
        });
        btn.addActionListener(e -> {
            addBtnRow.setVisible(false);
            addFormPanel.setVisible(true);
            Container p = addBtnRow.getParent();
            if (p != null) { p.revalidate(); p.repaint(); }
        });
        row.add(btn);
        return row;
    }

    private JPanel createInlineAddForm() {
        List<Service> services = new ServiceDAO().getAllActive();
        RoundedPanel form = new RoundedPanel(12, new Color(240, 245, 255), new Color(180, 210, 255), 1);
        form.setLayout(new MigLayout("insets 16 20 16 20, wrap 1, gap 10", "[grow,fill]", "[]"));

        JLabel title = new JLabel("Thêm dịch vụ mới");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 13f));
        title.setForeground(ThemeColors.PRIMARY);
        form.add(title);

        JComboBox<Object> combo = new JComboBox<>();
        combo.addItem("-- Chọn dịch vụ --");
        for (Service s : services) combo.addItem(s);
        combo.setBackground(Color.WHITE);
        combo.setFont(combo.getFont().deriveFont(13f));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int idx, boolean sel, boolean focus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, idx, sel, focus);
                if (value instanceof Service) {
                    Service s = (Service) value;
                    String p = s.getDonGia() == 0 ? "Miễn phí" : formatMoney((long)s.getDonGia());
                    lbl.setText(s.getTenDV() + " — " + p);
                }
                return lbl;
            }
        });
        form.add(combo, "growx, h 34!");

        JPanel detailPnl = new JPanel(new MigLayout("insets 0, wrap 1, gap 6", "[grow,fill]", "[]"));
        detailPnl.setOpaque(false);
        detailPnl.setVisible(false);

        JPanel donGiaRow = new JPanel(new MigLayout("insets 4 8 4 8", "[grow][28!][36!][28!]", "[]"));
        donGiaRow.setBackground(new Color(230, 238, 255));
        donGiaRow.setBorder(BorderFactory.createLineBorder(new Color(180, 210, 255), 1));
        JLabel lblDonGia = new JLabel("Đơn giá: 0đ");
        lblDonGia.setFont(lblDonGia.getFont().deriveFont(12f));
        lblDonGia.setForeground(ThemeColors.TEXT_PRIMARY);
        int[] qty = {1};
        JButton btnMinus = new JButton("−");
        JLabel lblQtyLbl = new JLabel("1", SwingConstants.CENTER);
        lblQtyLbl.setFont(lblQtyLbl.getFont().deriveFont(Font.BOLD, 13f));
        JButton btnPlus = new JButton("+");
        for (JButton b : new JButton[]{btnMinus, btnPlus}) {
            b.setFont(b.getFont().deriveFont(Font.BOLD, 14f));
            b.setBackground(Color.WHITE);
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER, 1, true));
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        donGiaRow.add(lblDonGia, "grow");
        donGiaRow.add(btnMinus);
        donGiaRow.add(lblQtyLbl);
        donGiaRow.add(btnPlus);
        detailPnl.add(donGiaRow, "growx");

        JPanel thanhTienRow = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));
        thanhTienRow.setOpaque(false);
        JLabel lblTTLabel = new JLabel("Thành tiền (1 × 0đ)");
        lblTTLabel.setFont(lblTTLabel.getFont().deriveFont(12f));
        lblTTLabel.setForeground(ThemeColors.TEXT_MUTED);
        JLabel lblTTVal = new JLabel("0đ");
        lblTTVal.setFont(lblTTVal.getFont().deriveFont(Font.BOLD, 13f));
        lblTTVal.setForeground(ThemeColors.PRIMARY);
        thanhTienRow.add(lblTTLabel, "grow");
        thanhTienRow.add(lblTTVal);
        detailPnl.add(thanhTienRow, "growx");
        form.add(detailPnl, "growx");

        Runnable updateDetail = () -> {
            if (combo.getSelectedItem() instanceof Service) {
                Service s = (Service) combo.getSelectedItem();
                long price = (long) s.getDonGia();
                lblDonGia.setText("Đơn giá: " + formatMoney(price));
                lblQtyLbl.setText(String.valueOf(qty[0]));
                lblTTLabel.setText("Thành tiền (" + qty[0] + " × " + formatMoney(price) + ")");
                lblTTVal.setText(formatMoney(price * qty[0]));
            }
        };
        combo.addActionListener(e -> {
            if (combo.getSelectedItem() instanceof Service) {
                qty[0] = 1; updateDetail.run(); detailPnl.setVisible(true);
            } else { detailPnl.setVisible(false); }
            form.revalidate(); form.repaint();
        });
        btnMinus.addActionListener(e -> { if (qty[0] > 1) { qty[0]--; updateDetail.run(); } });
        btnPlus.addActionListener(e -> { qty[0]++; updateDetail.run(); });

        JPanel btnRow = new JPanel(new MigLayout("insets 0", "[grow][][]", "[]"));
        btnRow.setOpaque(false);
        JButton btnOk = new JButton("Xác nhận thêm");
        btnOk.setBackground(ThemeColors.PRIMARY);
        btnOk.setForeground(Color.WHITE);
        btnOk.setFocusPainted(false);
        btnOk.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btnOk.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFocusPainted(false);
        btnCancel.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> {
            combo.setSelectedIndex(0); qty[0] = 1;
            detailPnl.setVisible(false);
            addFormPanel.setVisible(false);
            addBtnRow.setVisible(true);
            Container p = addFormPanel.getParent();
            if (p != null) { p.revalidate(); p.repaint(); }
        });
        btnOk.addActionListener(e -> {
            if (!(combo.getSelectedItem() instanceof Service)) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn dịch vụ!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Service s = (Service) combo.getSelectedItem();
            ServiceDetail sd = new ServiceDetail();
            sd.setMaHD(invoice != null ? invoice.getMaHD() : "");
            sd.setMaDV(s.getMaDV());
            sd.setSoLuong(qty[0]);
            sd.setDonGia(s.getDonGia());
            sd.setThanhTien(s.getDonGia() * qty[0]);
            sd.setGhiChu(s.getTenDV());
            if (new ServiceDetailDAO().insert(sd)) {
                combo.setSelectedIndex(0); qty[0] = 1;
                detailPnl.setVisible(false);
                addFormPanel.setVisible(false);
                addBtnRow.setVisible(true);
                Container p = addFormPanel.getParent();
                if (p != null) { p.revalidate(); p.repaint(); }
                refreshServiceList();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm dịch vụ thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnRow.add(btnOk, "grow, h 36!");
        btnRow.add(btnCancel, "w 80!, h 36!");
        form.add(btnRow, "growx");
        return form;
    }

    private JLabel createMutedLabel(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(ThemeColors.TEXT_MUTED);
        l.setFont(l.getFont().deriveFont(12f));
        return l;
    }

    private JLabel createMutedLabelDark(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(ThemeColors.TEXT_PRIMARY);
        l.setFont(l.getFont().deriveFont(12f));
        return l;
    }

    private JPanel createSeparator() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(ThemeColors.BORDER_SOFT);
                g.drawLine(20, 0, getWidth() - 20, 0);
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(100, 1));
        return p;
    }

    private JPanel createInvoiceItem(String iconFile, String iconStr, String title, String sub, String price, String maCTDV, JPanel parentPanel) {
        JPanel pnl = new JPanel(new MigLayout("insets 12 20 12 20", "[][grow][][]", "[]"));
        pnl.setOpaque(false);
        JLabel icon = new JLabel();
        ImageIcon imgIcon = (iconFile != null && !iconFile.isEmpty()) ? loadIcon(iconFile, 18, 18) : null;
        if (imgIcon != null) { icon.setIcon(imgIcon); }
        else { icon.setText(iconStr); icon.setFont(icon.getFont().deriveFont(18f)); icon.setForeground(ThemeColors.TEXT_MUTED); }

        JPanel pnlText = new JPanel(new MigLayout("insets 0, wrap 1, gap 2", "[]", "[]"));
        pnlText.setOpaque(false);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(lblTitle.getFont().deriveFont(13f));
        lblTitle.setForeground(ThemeColors.TEXT_PRIMARY);
        JLabel lblSub = new JLabel(sub);
        lblSub.setFont(lblSub.getFont().deriveFont(11f));
        lblSub.setForeground(ThemeColors.TEXT_PLACEHOLDER);
        pnlText.add(lblTitle);
        pnlText.add(lblSub);

        JLabel lblPrice = new JLabel(price);
        lblPrice.setFont(lblPrice.getFont().deriveFont(Font.BOLD, 13f));
        lblPrice.setForeground(ThemeColors.TEXT_PRIMARY);

        pnl.add(icon, "w 24!");
        pnl.add(pnlText, "growx");
        pnl.add(lblPrice);

        if (maCTDV != null) {
            JButton btnDel = new JButton();
            ImageIcon delIco = loadIcon("delete.png", 16, 16);
            if (delIco != null) { btnDel.setIcon(delIco); }
            else { btnDel.setText("🗑"); btnDel.setFont(btnDel.getFont().deriveFont(14f)); }
            btnDel.setForeground(new Color(240, 80, 80));
            btnDel.setContentAreaFilled(false);
            btnDel.setBorderPainted(false);
            btnDel.setMargin(new Insets(0, 0, 0, 0));
            btnDel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnDel.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this, "Xóa dịch vụ này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    new ServiceDetailDAO().delete(maCTDV);
                    refreshServiceList();
                }
            });
            pnl.add(btnDel, "w 24!");
        } else {
            pnl.add(new JLabel(" "), "w 24!");
        }
        return pnl;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new MigLayout("insets 16 20 16 20", "[][grow][]", "[]"));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeColors.BORDER_SOFT));

        btnFooterLeft = new JButton("Xem hóa đơn");
        ImageIcon footerInvoiceIcon = loadIcon("invoice.png", 14, 14);
        if (footerInvoiceIcon != null) {
            btnFooterLeft.setIcon(footerInvoiceIcon);
            btnFooterLeft.setIconTextGap(6);
        }
        btnFooterLeft.setFont(btnFooterLeft.getFont().deriveFont(13f));
        btnFooterLeft.setForeground(ThemeColors.TEXT_MUTED);
        btnFooterLeft.setBackground(Color.WHITE);
        btnFooterLeft.setFocusPainted(false);
        btnFooterLeft.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeColors.BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        btnFooterLeft.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnFooterLeft.addActionListener(e -> {
            if (currentTab.equals("KHACH")) switchTab("HOADON");
            else switchTab("KHACH");
        });

        PrimaryButton btnFooterRight = new PrimaryButton("Đóng");
        btnFooterRight.setBackground(new Color(20, 30, 50));
        btnFooterRight.setForeground(Color.WHITE);
        btnFooterRight.addActionListener(e -> dispose());

        footer.add(btnFooterLeft);
        footer.add(btnFooterRight, "right, w 100!, h 36!");

        return footer;
    }

    /**
     * Lấy ngày trả phòng dự kiến từ bảng ChiTietDatPhong.
     * Ưu tiên ngayTraDuKien từ booking, fallback về ngayTraPhong từ ChiTietHoaDon.
     */
    private String getExpectedCheckoutDate(kqlhotel.entity.Invoice invoice, String maPhong) {
        if (invoice == null) return "N/A";

        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Thử lấy từ ChiTietDatPhong qua maDatPhong
        if (invoice.getMaDatPhong() != null && !invoice.getMaDatPhong().isBlank()) {
            try {
                java.sql.Connection con = kqlhotel.dao.ConnectDB.getInstance().getConnection();
                String sql = "SELECT ngayTraDuKien FROM ChiTietDatPhong WHERE maDatPhong = ? AND maPhong = ?";
                java.sql.PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, invoice.getMaDatPhong());
                ps.setString(2, maPhong);
                java.sql.ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getTimestamp("ngayTraDuKien") != null) {
                    return rs.getTimestamp("ngayTraDuKien").toLocalDateTime().format(dtf);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Fallback: lấy từ ChiTietHoaDon (ngayTraPhong)
        try {
            java.sql.Connection con = kqlhotel.dao.ConnectDB.getInstance().getConnection();
            String sql = "SELECT ngayTraPhong FROM ChiTietHoaDon WHERE maHD = ? AND maPhong = ?";
            java.sql.PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, invoice.getMaHD());
            ps.setString(2, maPhong);
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getTimestamp("ngayTraPhong") != null) {
                return rs.getTimestamp("ngayTraPhong").toLocalDateTime().format(dtf);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Chưa xác định";
    }

    private long parseMoney(String moneyStr) {
        try {
            return Long.parseLong(moneyStr.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private String formatMoney(long money) {
        return String.format("%,d", money).replace(',', '.') + "đ";
    }

    private ImageIcon loadIcon(String filename, int width, int height) {
        try {
            URL resource = getClass().getResource("/kqlhotel/resources/icons/" + filename);
            if (resource == null) {
                String srcPath = "src/kqlhotel/resources/icons/" + filename;
                File file = new File(srcPath);
                if (file.exists()) {
                    resource = file.toURI().toURL();
                }
            }
            if (resource != null) {
                ImageIcon icon = new ImageIcon(resource);
                Image scaledImage = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }
        } catch (Exception e) {}
        return null;
    }
}
