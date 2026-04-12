package kqlhotel.gui.components;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;
import kqlhotel.gui.theme.ThemeColors;

public class RoomDetailDialog extends JDialog {

    private final JPanel contentCardPanel = new JPanel(new CardLayout());
    private final CardLayout cardLayout = (CardLayout) contentCardPanel.getLayout();
    private String currentTab = "KHACH";
    private String roomNo;
    private String roomType;
    private String roomPrice;

    // Tab buttons
    private JPanel tabKhachCont;
    private JPanel tabHoaDonCont;
    
    // Footer buttons
    private JButton btnFooterLeft;

    public RoomDetailDialog(Window owner, String roomNo, String roomType, String floor, String price) {
        super(owner, "Chi tiết phòng " + roomNo, ModalityType.APPLICATION_MODAL);
        this.roomNo = roomNo;
        this.roomType = roomType;
        this.roomPrice = price;
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        RoundedPanel rootPanel = new RoundedPanel(16, Color.WHITE, ThemeColors.BORDER, 1);
        rootPanel.setLayout(new MigLayout("insets 0, wrap 1, gap 0", "[fill, 550!]", "[]"));
        rootPanel.setOpaque(false);
        
        // Header
        rootPanel.add(createHeader(roomNo, roomType, floor), "growx");

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
        JLabel iconLbl = new JLabel("🛏", SwingConstants.CENTER);
        iconLbl.setForeground(Color.WHITE);
        iconLbl.setFont(iconLbl.getFont().deriveFont(20f));
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

        JButton btnClose = new JButton("×");
        btnClose.setFont(btnClose.getFont().deriveFont(Font.BOLD, 20f));
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
        JLabel lblKhach = new JLabel("👤 Thông tin khách");
        lblKhach.setFont(lblKhach.getFont().deriveFont(Font.BOLD, 13f));
        tabKhachCont.add(lblKhach, "aligny center");
        tabKhachCont.setCursor(new Cursor(Cursor.HAND_CURSOR));

        tabHoaDonCont = new JPanel(new MigLayout("insets 0 10 0 10, gap 8", "[][]", "[grow]"));
        tabHoaDonCont.setBackground(Color.WHITE);
        JLabel lblHoaDon = new JLabel("📄 Hóa đơn");
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
                btnFooterLeft.setText("📄 Xem hóa đơn");
            }
        } else {
            tabKhachCont.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
            ((JLabel)tabKhachCont.getComponent(0)).setForeground(ThemeColors.TEXT_MUTED);
            
            tabHoaDonCont.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ThemeColors.PRIMARY));
            ((JLabel)tabHoaDonCont.getComponent(0)).setForeground(ThemeColors.PRIMARY);

            if(btnFooterLeft != null) {
                btnFooterLeft.setText("👤 Xem thông tin khách");
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
        
        JLabel lblCode = new JLabel("Mã đặt: BK001");
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

        pnlPersonal.add(createIconLabelData("👤 Họ và tên", "Lê Văn Dũng"));
        pnlPersonal.add(createIconLabelData("📞 Điện thoại", "0901 234 567"));
        pnlPersonal.add(createIconLabelData("✉ Email", "dung.le@yahoo.com"));
        pnlPersonal.add(createIconLabelData("💳 CMND/CCCD", "048085023456"));
        pnlPersonal.add(createIconLabelData("📍 Địa chỉ", "78 Nguyễn Văn Linh, Đà Nẵng"), "span 2");

        RoundedPanel pnlBooking = new RoundedPanel(12, new Color(230, 255, 240), new Color(180, 240, 200), 1);
        pnlBooking.setLayout(new MigLayout("insets 16 20 16 20, wrap 2", "[grow][grow]", "[]12[]"));
        JLabel title2 = new JLabel("THÔNG TIN ĐẶT PHÒNG");
        title2.setFont(title2.getFont().deriveFont(Font.BOLD, 10f));
        title2.setForeground(new Color(40, 120, 70));
        pnlBooking.add(title2, "span 2, wrap");

        pnlBooking.add(createIconLabelDataTextOnly("Nhận phòng", "1/4/2026", new Color(40, 160, 80), new Color(40, 120, 70)));
        pnlBooking.add(createIconLabelDataTextOnly("Trả phòng", "5/4/2026", new Color(40, 160, 80), new Color(40, 120, 70)));
        pnlBooking.add(createIconLabelDataTextOnly("Số khách", "2 người", new Color(40, 160, 80), new Color(40, 120, 70)));
        long pricePerNightK = parseMoney(roomPrice);
        long totalRoomK = pricePerNightK * 4;
        pnlBooking.add(createIconLabelDataTextOnly("Tiền phòng", formatMoney(totalRoomK), new Color(40, 160, 80), new Color(40, 120, 70)));

        JPanel pnlStats = new JPanel(new MigLayout("insets 0, gap 12", "[grow,fill][grow,fill][grow,fill]", "[]"));
        pnlStats.setOpaque(false);
        pnlStats.add(createStatBox("Tổng lần đặt", "8", new Color(255, 245, 210), new Color(250, 220, 120), new Color(160, 80, 0)));
        pnlStats.add(createStatBox("Tổng chi tiêu", "45.800.000đ", new Color(250, 245, 255), new Color(230, 210, 255), new Color(120, 60, 160)));
        pnlStats.add(createStatBox("Thành viên từ", "thg 6 2023", new Color(240, 248, 255), new Color(200, 220, 255), new Color(40, 80, 160)));

        PrimaryButton btnBigInvoice = new PrimaryButton("📄 Xem hóa đơn");
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

    private JPanel createIconLabelData(String label, String data) {
        JPanel pnl = new JPanel(new MigLayout("insets 0, wrap 1, gap 2", "[]", "[]"));
        pnl.setOpaque(false);
        JLabel lblTitle = new JLabel(label);
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
        JLabel lbl2 = new JLabel("KQL-2026-0125");
        lbl2.setForeground(ThemeColors.TEXT_PRIMARY);
        lbl2.setFont(lbl2.getFont().deriveFont(Font.BOLD, 16f));
        JLabel lbl3 = new JLabel("Tạo ngày 1/4/2026");
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
        
        JPanel titleRow = new JPanel(new MigLayout("insets 16 20 12 20", "[]", "[]"));
        titleRow.setOpaque(false);
        JLabel mainTitle = new JLabel("CHI TIẾT HÓA ĐƠN");
        mainTitle.setFont(mainTitle.getFont().deriveFont(Font.BOLD, 11f));
        mainTitle.setForeground(ThemeColors.TEXT_MUTED);
        titleRow.add(mainTitle);
        mainPnl.add(titleRow);

        mainPnl.add(createSeparator());
        long pricePerNight = parseMoney(roomPrice);
        long totalRoom = pricePerNight * 4;
        mainPnl.add(createInvoiceItem("🛏", "Tiền phòng " + roomType + " – 4 đêm", "1/4/2026 → 5/4/2026", formatMoney(totalRoom), false));
        mainPnl.add(createSeparator());
        mainPnl.add(createInvoiceItem("🍽", "Bữa sáng buffet", "8 × 220.000đ", "1.760.000đ", true));
        mainPnl.add(createSeparator());
        mainPnl.add(createInvoiceItem("💆", "Spa & Massage", "2 × 800.000đ", "1.600.000đ", true));
        mainPnl.add(createSeparator());
        mainPnl.add(createInvoiceItem("👕", "Giặt ủi", "3 × 50.000đ", "150.000đ", true));
        mainPnl.add(createSeparator());

        JPanel addRow = new JPanel(new MigLayout("insets 16 20 16 20", "[grow,fill]", "[]"));
        addRow.setOpaque(false);
        JButton btnAddDichVu = new JButton("+ Thêm dịch vụ");
        btnAddDichVu.setFont(btnAddDichVu.getFont().deriveFont(13f));
        btnAddDichVu.setForeground(ThemeColors.PRIMARY);
        btnAddDichVu.setBackground(new Color(245, 250, 255));
        btnAddDichVu.setFocusPainted(false);
        btnAddDichVu.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAddDichVu.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        btnAddDichVu.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                super.paint(g, c);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(150, 180, 240));
                Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{6}, 0);
                g2.setStroke(dashed);
                g2.drawRoundRect(0, 0, c.getWidth()-1, c.getHeight()-1, 8, 8);
                g2.dispose();
            }
        });
        addRow.add(btnAddDichVu);
        mainPnl.add(addRow);
        mainPnl.add(createSeparator());

        JPanel sumRow = new JPanel(new MigLayout("insets 16 20 20 20, wrap 2, gapy 8", "[grow][right]", "[]"));
        sumRow.setOpaque(false);
        long totalServices = 3510000;
        long vat = (long)((totalRoom + totalServices) * 0.1);
        long grandTotal = totalRoom + totalServices + vat;
        sumRow.add(createMutedLabel("Tạm tính")); sumRow.add(createMutedLabelDark(formatMoney(totalRoom)));
        sumRow.add(createMutedLabel("Tiền dịch vụ")); sumRow.add(createMutedLabelDark(formatMoney(totalServices)));
        sumRow.add(createMutedLabel("Thuế VAT (10%)")); sumRow.add(createMutedLabelDark(formatMoney(vat)));
        
        sumRow.add(createSeparator(), "span 2, growx, gapy 8 8");

        JLabel lblTongT = new JLabel("Còn phải thanh toán");
        lblTongT.setFont(lblTongT.getFont().deriveFont(Font.BOLD, 14f));
        lblTongT.setForeground(ThemeColors.TEXT_PRIMARY);
        JLabel lblTongV = new JLabel(formatMoney(grandTotal));
        lblTongV.setFont(lblTongV.getFont().deriveFont(Font.BOLD, 16f));
        lblTongV.setForeground(ThemeColors.PRIMARY);
        sumRow.add(lblTongT); sumRow.add(lblTongV);

        mainPnl.add(sumRow);

        pnl.add(hdr);
        pnl.add(mainPnl);

        return pnl;
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

    private JPanel createInvoiceItem(String iconStr, String title, String sub, String price, boolean deleteable) {
        JPanel pnl = new JPanel(new MigLayout("insets 12 20 12 20", "[][grow][][]", "[]"));
        pnl.setOpaque(false);
        JLabel icon = new JLabel(iconStr);
        icon.setFont(icon.getFont().deriveFont(18f));
        icon.setForeground(ThemeColors.TEXT_MUTED);
        
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

        if (deleteable) {
            JButton btnDel = new JButton("🗑");
            btnDel.setForeground(new Color(240, 80, 80));
            btnDel.setFont(btnDel.getFont().deriveFont(14f));
            btnDel.setContentAreaFilled(false);
            btnDel.setBorderPainted(false);
            btnDel.setMargin(new Insets(0, 0, 0, 0));
            btnDel.setCursor(new Cursor(Cursor.HAND_CURSOR));
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

        btnFooterLeft = new JButton("📄 Xem hóa đơn");
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
}
