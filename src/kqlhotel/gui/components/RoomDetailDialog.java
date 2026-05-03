package kqlhotel.gui.components;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;
import kqlhotel.gui.theme.ThemeColors;
import kqlhotel.entity.Service;
import kqlhotel.entity.ServiceDetail;
import kqlhotel.entity.Room;
import kqlhotel.entity.Invoice;
import kqlhotel.entity.Customer;
import kqlhotel.dao.service.ServiceDAO;
import kqlhotel.dao.invoice.ServiceDetailDAO;

public class RoomDetailDialog extends JDialog {

    private final JPanel contentCardPanel = new JPanel(new CardLayout());
    private final CardLayout cardLayout = (CardLayout) contentCardPanel.getLayout();
    private String currentTab = "GUEST";
    private String roomId;
    private String roomType;
    private String roomPrice;
    private Invoice invoice;
    private Customer customer;
    private JPanel serviceListPanel;
    private JLabel lblTotalServices;
    private JLabel lblVat;
    private JLabel lblGrandTotal;
    private JPanel addBtnRow;
    private JPanel addFormPanel;
    private Runnable onCheckout;

    private JPanel tabGuestCont;
    private JPanel tabInvoiceCont;
    private JButton btnFooterLeft;

    public RoomDetailDialog(Window owner, Room room, Invoice invoice, Customer customer) {
        this(owner, room, invoice, customer, null);
    }

    public RoomDetailDialog(Window owner, Room room, Invoice invoice, Customer customer, Runnable onCheckout) {
        super(owner, "Chi tiết phòng - " + room.getRoomId(), ModalityType.APPLICATION_MODAL);
        this.roomId = room.getRoomId();
        this.roomType = room.getRoomType().getRoomTypeName();
        this.roomPrice = String.valueOf(room.getRoomType().getGiaPhong());
        this.invoice = invoice;
        this.customer = customer;
        this.onCheckout = onCheckout;
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        RoundedPanel rootPanel = new RoundedPanel(16, Color.WHITE, new Color(226, 232, 240), 1);
        rootPanel.setLayout(new MigLayout("insets 0, wrap 1, gap 0", "[fill, 550!]", "[]"));
        rootPanel.setOpaque(false);
        
        rootPanel.add(createHeader(roomId, roomType, "Tầng " + room.getFloor()), "growx");
        rootPanel.add(createTabsRow(), "growx");

        contentCardPanel.setOpaque(false);
        JScrollPane scrollPane = new JScrollPane(contentCardPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        contentCardPanel.add(createGuestTab(), "GUEST");
        contentCardPanel.add(createInvoiceTab(), "INVOICE");
        
        rootPanel.add(scrollPane, "grow, h 550!");
        rootPanel.add(createFooter(), "growx");

        setContentPane(rootPanel);
        pack();
        setLocationRelativeTo(owner);
        
        switchTab("GUEST");
    }

    private JPanel createHeader(String roomId, String roomType, String floor) {
        JPanel header = new JPanel(new MigLayout("insets 16 20 16 20", "[][grow][]", "[]")) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(248, 250, 252));
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 20, 16, 16);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        JLabel title = new JLabel("Phòng " + roomId + " — Khách & Hóa đơn");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(15, 23, 42));
        
        JLabel subtitle = new JLabel(roomType + " - " + floor);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(ThemeColors.PRIMARY);

        JPanel textPanel = new JPanel(new MigLayout("insets 0, wrap 1, gap 2"));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(subtitle);

        JButton btnClose = new JButton("×");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 24));
        btnClose.setForeground(new Color(100, 116, 139));
        btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());

        header.add(new JLabel("🛏"), "w 32!, h 32!");
        header.add(textPanel, "growx, gapx 10");
        header.add(btnClose, "top");

        return header;
    }

    private JPanel createTabsRow() {
        JPanel tabs = new JPanel(new MigLayout("insets 0 20 0 20, gap 20", "[][]", "[45!]"));
        tabs.setBackground(Color.WHITE);
        tabs.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        tabGuestCont = createTabBtn("Thông tin khách");
        tabInvoiceCont = createTabBtn("Hóa đơn");

        tabGuestCont.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { switchTab("GUEST"); } });
        tabInvoiceCont.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { switchTab("INVOICE"); } });

        tabs.add(tabGuestCont, "growy");
        tabs.add(tabInvoiceCont, "growy");

        return tabs;
    }

    private JPanel createTabBtn(String label) {
        JPanel p = new JPanel(new MigLayout("insets 0 10 0 10", "[]", "[grow]"));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        p.add(lbl, "aligny center");
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return p;
    }

    private void switchTab(String tab) {
        currentTab = tab;
        cardLayout.show(contentCardPanel, tab);
        
        if (tab.equals("GUEST")) {
            tabGuestCont.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ThemeColors.PRIMARY));
            tabInvoiceCont.setBorder(null);
            if(btnFooterLeft != null) btnFooterLeft.setText("Xem hóa đơn");
        } else {
            tabInvoiceCont.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ThemeColors.PRIMARY));
            tabGuestCont.setBorder(null);
            if(btnFooterLeft != null) btnFooterLeft.setText("Xem thông tin khách");
        }
    }

    private JPanel createGuestTab() {
        JPanel pnl = new JPanel(new MigLayout("insets 20, wrap 1, gap 16", "[grow,fill]", "[]"));
        pnl.setBackground(Color.WHITE);

        String name = (customer != null) ? customer.getHoTenKH() : "Khách vãng lai";
        String phone = (customer != null) ? customer.getSdt() : "N/A";
        String email = (customer != null) ? customer.getEmail() : "N/A";

        pnl.add(new JLabel("THÔNG TIN CÁ NHÂN") {{ setFont(new Font("Segoe UI", Font.BOLD, 10)); setForeground(new Color(100, 116, 139)); }});
        pnl.add(createIconLabelData("client.png", "Họ và tên", name));
        pnl.add(createIconLabelData("telephone.png", "Số điện thoại", phone));
        pnl.add(createIconLabelData("email.png", "Email", email));

        pnl.add(new JLabel("THÔNG TIN ĐẶT PHÒNG") {{ setFont(new Font("Segoe UI", Font.BOLD, 10)); setForeground(new Color(100, 116, 139)); }}, "gapy 10 0");
        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        pnl.add(createIconLabelData("calendar.png", "Ngày nhận phòng", (invoice != null && invoice.getNgayLapHD() != null) ? invoice.getNgayLapHD().format(dtf) : "N/A"));
        pnl.add(createIconLabelData("guest.png", "Số lượng người", (invoice != null) ? String.valueOf(invoice.getSoLuongNguoi()) : "1"));

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

    private JPanel createInvoiceTab() {
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

        JPanel titleRow = new JPanel(new MigLayout("insets 16 20 12 20", "[][]", "[]"));
        titleRow.setOpaque(false);
        JLabel starIcon = new JLabel("★");
        starIcon.setForeground(ThemeColors.TEXT_MUTED);
        JLabel mainTitle = new JLabel("CHI TIẾT HÓA ĐƠN");
        mainTitle.setFont(mainTitle.getFont().deriveFont(Font.BOLD, 11f));
        mainTitle.setForeground(ThemeColors.TEXT_MUTED);
        titleRow.add(starIcon);
        titleRow.add(mainTitle, "gapx 4");
        mainPnl.add(titleRow);

        mainPnl.add(createSeparator());
        long totalRoom = (invoice != null) ? (long)invoice.getTienPhong() : 0;
        mainPnl.add(createInvoiceItem("bed.png", "🛏", "Tiền phòng " + roomType, "Tính đến hiện tại", formatMoney(totalRoom), null, null));
        mainPnl.add(createSeparator());

        serviceListPanel = new JPanel(new MigLayout("insets 0, wrap 1, gap 0", "[grow,fill]", "[]"));
        serviceListPanel.setOpaque(false);
        mainPnl.add(serviceListPanel, "growx");

        addBtnRow = createAddBtnRow();
        addFormPanel = createInlineAddForm();
        addFormPanel.setVisible(false);
        mainPnl.add(addBtnRow, "growx");
        mainPnl.add(addFormPanel, "growx");
        mainPnl.add(createSeparator());

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

        sumRow.add(createMutedLabel("Tạm tính")); sumRow.add(new JLabel(formatMoney(totalRoom)));
        sumRow.add(createMutedLabel("Tiền dịch vụ")); sumRow.add(lblTotalServices);
        sumRow.add(createMutedLabel("Thuế VAT (10%)")); sumRow.add(lblVat);
        sumRow.add(createSeparator(), "span 2, growx, gapy 8 8");
        JLabel lblTongT = new JLabel("Còn phải thanh toán");
        lblTongT.setFont(lblTongT.getFont().deriveFont(Font.BOLD, 14f));
        lblTongT.setForeground(ThemeColors.TEXT_PRIMARY);
        sumRow.add(lblTongT); sumRow.add(lblGrandTotal);
        mainPnl.add(sumRow);

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

    private JPanel createSeparator() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(ThemeColors.BORDER_SOFT);
                g.drawLine(20, 0, getWidth() - 20, 0);
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(1, 1));
        return p;
    }

    private JPanel createInvoiceItem(String iconName, String fallback, String title, String sub, String val, String maCTDV, JPanel parent) {
        JPanel pnl = new JPanel(new MigLayout("insets 12 20 12 20", "[][grow][right]", "[]"));
        pnl.setOpaque(false);
        JLabel ico = new JLabel();
        ImageIcon img = loadIcon(iconName, 18, 18);
        if (img != null) ico.setIcon(img); else ico.setText(fallback);
        
        JPanel textPnl = new JPanel(new MigLayout("insets 0, wrap 1, gap 2", "[]", "[]"));
        textPnl.setOpaque(false);
        JLabel lblT = new JLabel(title);
        lblT.setFont(lblT.getFont().deriveFont(Font.BOLD, 13f));
        lblT.setForeground(ThemeColors.TEXT_PRIMARY);
        JLabel lblS = new JLabel(sub);
        lblS.setFont(lblS.getFont().deriveFont(11f));
        lblS.setForeground(ThemeColors.TEXT_MUTED);
        textPnl.add(lblT);
        textPnl.add(lblS);

        JLabel lblV = new JLabel(val);
        lblV.setFont(lblV.getFont().deriveFont(Font.BOLD, 14f));
        lblV.setForeground(ThemeColors.TEXT_PRIMARY);

        pnl.add(ico, "w 24!");
        pnl.add(textPnl, "gapx 10");
        pnl.add(lblV);

        return pnl;
    }

    private String formatMoney(long val) {
        return String.format("%,dđ", val);
    }

    private ImageIcon loadIcon(String filename, int w, int h) {
        try {
            java.net.URL url = getClass().getResource("/kqlhotel/resources/icons/" + filename);
            if (url != null) return new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
        } catch (Exception e) {}
        return null;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new MigLayout("insets 16 20 16 20", "[][grow][]"));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        btnFooterLeft = new JButton("Xem hóa đơn");
        btnFooterLeft.addActionListener(e -> {
            if (currentTab.equals("GUEST")) switchTab("INVOICE");
            else switchTab("GUEST");
        });

        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dispose());

        footer.add(btnFooterLeft);
        footer.add(new JLabel(""), "growx");
        footer.add(btnClose);

        return footer;
    }
}
