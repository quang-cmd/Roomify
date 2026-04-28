package kqlhotel.gui.tabs;

import java.awt.*;
import java.net.URL;
import java.util.List;
import javax.swing.*;
import kqlhotel.bus.Invoice.InvoicesBUS;
import kqlhotel.entity.Invoice;
import kqlhotel.entity.InvoiceDetail;
import kqlhotel.entity.ServiceDetail;
import kqlhotel.entity.Customer;
import kqlhotel.gui.components.PrimaryButton;
import kqlhotel.gui.components.RoundedPanel;
import kqlhotel.gui.theme.ThemeColors;
import kqlhotel.utils.CurrencyUtils;
import kqlhotel.utils.DateUtils;
import kqlhotel.utils.PDFInvoiceGenerator;
import net.miginfocom.swing.MigLayout;

public class InvoicesPanel extends JPanel {
    private static final Color PAGE_BG = new Color(245, 248, 252);
    private final InvoicesBUS invoicesBUS = new InvoicesBUS();
    private final JPanel listPanel = new JPanel(new MigLayout("wrap 1,insets 0,gap 8", "[grow,fill]", "[]"));
    private final RoundedPanel detailContainer = new RoundedPanel(20, Color.WHITE, new Color(225, 231, 245), 1.5f);
    private final JPanel detailContent = new JPanel(
            new MigLayout("wrap 1,insets 24 24 24 24, gap 18, fillx", "[grow,fill]", "[]")
    );
    private final JLabel summaryLabel = new JLabel();
    private final List<PrimaryButton> filterButtons = new java.util.ArrayList<>();

    private List<Invoice> currentList;
    private String currentStatusFilter = "Tất cả";

    public InvoicesPanel() {
        setOpaque(false);
        setBackground(PAGE_BG);
        setLayout(new BorderLayout());

        JPanel header = createHeader();

        JPanel content = new JPanel(new MigLayout(
                "insets 10 20 20 20, gap 16",
                "[300::340,fill][grow,fill]",
                "[grow,fill]"
        ));
        content.setOpaque(false);
        
        JPanel leftSide = createLeftSide();
        createRightSide(); // Initializes detailPanel
        
        content.add(leftSide, "growy");
        
        JScrollPane detailScroll = new JScrollPane(detailContainer);
        detailScroll.setBorder(BorderFactory.createEmptyBorder());
        detailScroll.setOpaque(false);
        detailScroll.getViewport().setOpaque(false);
        detailScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        detailScroll.getVerticalScrollBar().setUnitIncrement(16);
        
        content.add(detailScroll, "grow");

        add(header, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);

        refreshData();
    }

    private void refreshData() {
        this.currentList = invoicesBUS.getAllInvoices();
        summaryLabel.setText(invoicesBUS.getInvoiceSummary());
        updateFilterButtonStyles("Tất cả");
        renderList(currentList);
        if (!currentList.isEmpty()) {
            showDetail(currentList.get(0));
        }
    }

    private void filterData(String status, String btnText) {
        this.currentList = invoicesBUS.filterInvoices(null, null, null, status);
        updateFilterButtonStyles(btnText);
        renderList(currentList);
        if (!currentList.isEmpty()) {
            showDetail(currentList.get(0));
        }
    }

    private void updateFilterButtonStyles(String activeText) {
        for (PrimaryButton btn : filterButtons) {
            if (btn.getText().equals(activeText)) {
                btn.setBackground(new Color(24, 34, 52));
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(Color.WHITE);
                btn.setForeground(new Color(100, 115, 135));
                btn.setBorder(BorderFactory.createLineBorder(new Color(225, 231, 245), 2)); // Increased from 1
            }
        }
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new MigLayout("insets 20 24 0 24,gap 0", "[grow]", "[]"));
        panel.setOpaque(false);
        
        JPanel titleBox = new JPanel(new MigLayout("insets 0, wrap 1", "[]", "[]"));
        titleBox.setOpaque(false);
        JLabel title = new JLabel("Hóa đơn");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setForeground(new Color(24, 40, 66));
        summaryLabel.setForeground(new Color(119, 137, 168));
        summaryLabel.setFont(summaryLabel.getFont().deriveFont(13f));
        titleBox.add(title);
        titleBox.add(summaryLabel);

        panel.add(titleBox, "aligny center");
        return panel;
    }

    private JPanel createLeftSide() {
        JPanel left = new JPanel(new MigLayout("wrap 1,insets 0,gap 16", "[grow,fill]", "[][grow,fill]"));
        left.setOpaque(false);

        // Filters
        JPanel filters1 = new JPanel(new MigLayout("insets 0,gap 10", "[grow,fill][grow,fill]", "[]"));
        filters1.setOpaque(false);
        filters1.add(createFilterBtn("Tất cả", true, e -> refreshData()), "h 36!");
        filters1.add(createFilterBtn("Đã thanh toán", false, e -> filterData("DaThanhToan", "Đã thanh toán")), "h 36!");
        
        JPanel filters2 = new JPanel(new MigLayout("insets 0,gap 10", "[grow,fill][grow,fill]", "[]"));
        filters2.setOpaque(false);
        filters2.add(createFilterBtn("Chưa thanh toán", false, e -> filterData("ChuaThanhToan", "Chưa thanh toán")), "h 36!");
        filters2.add(createFilterBtn("Đặt cọc", false, e -> filterData("DatCoc", "Đặt cọc")), "h 36!");

        listPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(24); // Tăng tốc độ cuộn

        left.add(filters1);
        left.add(filters2);
        left.add(scroll, "grow");
        return left;
    }

    private PrimaryButton createFilterBtn(String text, boolean active, java.awt.event.ActionListener al) {
        PrimaryButton btn = new PrimaryButton(text);
        if (active) {
            btn.setBackground(new Color(24, 34, 52));
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(100, 115, 135));
            btn.setBorder(BorderFactory.createLineBorder(new Color(225, 231, 245), 2));
        }
        btn.addActionListener(al);
        filterButtons.add(btn);
        return btn;
    }

    private void renderList(List<Invoice> list) {
        listPanel.removeAll();
        for (Invoice data : list) {
            listPanel.add(createListItem(data, false));
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel createListItem(Invoice hd, boolean selected) {
        RoundedPanel item = new RoundedPanel(12, selected ? Color.WHITE : new Color(250, 252, 255), selected ? new Color(49, 106, 210) : new Color(230, 235, 245), selected ? 2.5f : 1.5f);
        item.setLayout(new MigLayout("insets 16 12 16 12", "[][grow,fill][][]", "[]"));

        // Icon part
        JLabel icon = new JLabel();
        if ("DaThanhToan".equals(hd.getTrangThai())) {
            icon.setIcon(loadIcon("check-circle.png", 20, 20));
        } else if ("ChuaThanhToan".equals(hd.getTrangThai())) {
            icon.setIcon(loadIcon("alert-circle.png", 20, 20));
        } else {
            icon.setIcon(loadIcon("clock-circle.png", 20, 20));
        }

        // Info part
        JPanel info = new JPanel(new MigLayout("wrap 1,insets 0", "[]", "[][]"));
        info.setOpaque(false);
        JLabel idLabel = new JLabel(hd.getMaHD());
        idLabel.setFont(idLabel.getFont().deriveFont(Font.BOLD, 14f));
        idLabel.setForeground(new Color(24, 40, 66));
        
        Customer kh = invoicesBUS.getCustomerInfo(hd.getMaKhachHang());
        JLabel nameLabel = new JLabel(kh != null ? kh.getHoTenKH() : hd.getMaKhachHang());
        nameLabel.setForeground(new Color(110, 125, 145));
        nameLabel.setFont(nameLabel.getFont().deriveFont(12f));
        info.add(idLabel);
        info.add(nameLabel);

        // Price part
        JPanel pricePane = new JPanel(new MigLayout("wrap 1,insets 0", "[]", "[][]"));
        pricePane.setOpaque(false);
        JLabel pLabel = new JLabel(CurrencyUtils.formatVND(hd.getTongTienThanhToan()));
        pLabel.setFont(pLabel.getFont().deriveFont(Font.BOLD, 14f));
        pLabel.setForeground(new Color(24, 40, 66));
        pLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        JLabel sLabel = new JLabel(hd.getTrangThai());
        sLabel.setForeground(new Color(30, 180, 120)); // Green for all as default for clean look, or switch case later
        sLabel.setFont(sLabel.getFont().deriveFont(11f));
        sLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        pricePane.add(pLabel, "alignx right");
        pricePane.add(sLabel, "alignx right");

        JLabel arrow = new JLabel(" \u203A ");
        arrow.setForeground(new Color(180, 190, 210));

        item.add(icon, "aligny center");
        item.add(info, "aligny center");
        item.add(pricePane, "aligny center");
        item.add(arrow, "aligny center");

        item.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                showDetail(hd);
            }
        });

        return item;
    }

    private void createRightSide() {
        detailContainer.setLayout(new BorderLayout());
        detailContent.setOpaque(false);
        detailContainer.add(detailContent);
    }

    private void showDetail(Invoice hd) {
        detailContent.removeAll();

        // Top Action row
        JPanel topRow = new JPanel(new MigLayout("insets 0", "[][grow,fill][][][]", "[]"));
        topRow.setOpaque(false);

        JPanel idBox = new JPanel(new MigLayout("wrap 1,insets 0", "[]", "[]"));
        idBox.setOpaque(false);
        
        JPanel titleRow = new JPanel(new MigLayout("insets 0,gap 10", "[][]", "[]"));
        titleRow.setOpaque(false);
        JLabel lId = new JLabel(hd.getMaHD());
        lId.setFont(lId.getFont().deriveFont(Font.BOLD, 22f));
        lId.setForeground(new Color(24, 40, 66));
        
        JLabel lStatus = new JLabel(" \u2022 " + hd.getTrangThai());
        lStatus.setForeground(new Color(30, 180, 120));
        lStatus.setFont(lStatus.getFont().deriveFont(Font.BOLD, 12f));
        titleRow.add(lId, "aligny bottom");
        titleRow.add(lStatus, "aligny bottom");

        JLabel lDate = new JLabel("Ngày tạo: " + DateUtils.format(hd.getNgayLapHD()));
        lDate.setForeground(new Color(110, 125, 145));
        idBox.add(titleRow);
        idBox.add(lDate);

        PrimaryButton bConfirm = new PrimaryButton("Xác nhận thanh toán");
        bConfirm.setBackground(ThemeColors.SUCCESS);
        bConfirm.setForeground(Color.WHITE);
        bConfirm.setIcon(loadIcon("check-circle.png", 16, 16));
        bConfirm.setVisible("ChuaThanhToan".equals(hd.getTrangThai()) || "DatCoc".equals(hd.getTrangThai()));
        bConfirm.addActionListener(e -> {
            if (invoicesBUS.confirmPayment(hd.getMaHD())) {
                refreshData();
                JOptionPane.showMessageDialog(this, "Đã xác nhận thanh toán thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        PrimaryButton bPdf = new PrimaryButton("Xuất PDF");
        bPdf.setBackground(new Color(255, 193, 7)); // Yellow/Amber
        bPdf.setForeground(Color.WHITE);
        bPdf.setFocusPainted(false);
        bPdf.addActionListener(e -> {
            PDFInvoiceGenerator.exportInvoice(hd, invoicesBUS.getRoomDetails(hd.getMaHD()), invoicesBUS.getServiceDetails(hd.getMaHD()));
        });
        
        PrimaryButton bPrint = new PrimaryButton("");
        bPrint.setIcon(loadIcon("print.png", 24, 24));
        bPrint.setBackground(Color.WHITE);
        bPrint.setBorder(BorderFactory.createLineBorder(new Color(180, 190, 210), 2));
        bPrint.setToolTipText("In hóa đơn");
        bPrint.setPreferredSize(new Dimension(38, 38));
        bPrint.setArc(20);
        bPrint.addActionListener(e -> {
            PDFInvoiceGenerator.exportInvoice(hd, invoicesBUS.getRoomDetails(hd.getMaHD()), invoicesBUS.getServiceDetails(hd.getMaHD()));
        });

        topRow.add(idBox);
        topRow.add(new JPanel(){{setOpaque(false);}}, "growx"); // spacer
        topRow.add(bConfirm, "h 38!");
        topRow.add(bPdf, "h 38!");
        topRow.add(bPrint, "h 38!,w 38!");

        // Two boxes: KH, Phòng
        Customer kh = invoicesBUS.getCustomerInfo(hd.getMaKhachHang());
        List<InvoiceDetail> rooms = invoicesBUS.getRoomDetails(hd.getMaHD());
        String roomStr = rooms.isEmpty() ? "N/A" : rooms.get(0).getMaPhong();

        JPanel infoRow = new JPanel(new MigLayout("insets 0,gap 20", "[grow,fill][grow,fill]", "[]"));
        infoRow.setOpaque(false);
        infoRow.add(createBox("KHÁCH HÀNG", kh != null ? kh.getHoTenKH() : "Unknown", kh != null ? "SĐT: " + kh.getSdt() : ""));
        infoRow.add(createBox("THÔNG TIN PHÒNG", "Phòng " + roomStr, "Mã HD: " + hd.getMaHD()));

        // Table Title
        JLabel tTitle = new JLabel("CHI TIẾT DỊCH VỤ");
        tTitle.setForeground(new Color(130, 145, 165));
        tTitle.setFont(tTitle.getFont().deriveFont(Font.BOLD, 12f));

        // Table
        RoundedPanel tablePanel = new RoundedPanel(12, Color.WHITE, new Color(225, 231, 245), 1f);
        tablePanel.setLayout(new MigLayout("wrap 1,insets 0,gap 0", "[grow,fill]", "[]"));
        
        // Table header
        JPanel tHeader = new JPanel(new MigLayout(
    "insets 16 20 16 20, fillx",
    "[grow,fill][60::80,right][90::120,right][110::150,right]",
    "[]"
));
        tHeader.setBackground(new Color(250, 252, 255));
        tHeader.add(makeTText("Dịch vụ", false));
        tHeader.add(makeTText("Số lượng", true));
        tHeader.add(makeTText("Đơn giá", true));
        tHeader.add(makeTText("Thành tiền", true));
        
        tablePanel.add(tHeader, "growx");
        
        // Rows
        List<InvoiceDetail> ctRooms = invoicesBUS.getRoomDetails(hd.getMaHD());
        for (InvoiceDetail ct : ctRooms) {
            tablePanel.add(createTRow("Tiền phòng " + ct.getMaPhong(), String.valueOf(ct.getSoDem()), "", CurrencyUtils.formatVND(ct.getThanhTien())));
        }
        
        List<ServiceDetail> ctServices = invoicesBUS.getServiceDetails(hd.getMaHD());
        for (ServiceDetail ct : ctServices) {
            tablePanel.add(createTRow("Dịch vụ: " + ct.getMaDV(), String.valueOf(ct.getSoLuong()), CurrencyUtils.formatVND(ct.getDonGia()), CurrencyUtils.formatVND(ct.getThanhTien())));
        }

        // Footer in Table
        JPanel tFooter = new JPanel(new MigLayout(
    "wrap 2,insets 20 20 20 20, fillx",
    "[grow,fill][140::220,right]",
    "[]"
));
        tFooter.setBackground(Color.WHITE);
        
        tFooter.add(makeTText("Tiền phòng", false), "alignx left");
        tFooter.add(makeTText(CurrencyUtils.formatVND(hd.getTienPhong()), true), "alignx right");
        
        tFooter.add(makeTText("Tiền dịch vụ", false), "alignx left");
        tFooter.add(makeTText(CurrencyUtils.formatVND(hd.getTienDichVu()), true), "alignx right");

        tFooter.add(makeTText("Thuế VAT (10%)", false), "alignx left");
        tFooter.add(makeTText(CurrencyUtils.formatVND(hd.getTienThue()), true), "alignx right");

        tFooter.add(makeTText("Khuyến mãi", false), "alignx left");
        tFooter.add(makeTText("-" + CurrencyUtils.formatVND(hd.getTienKhuyenMai()), true), "alignx right");
        
        JPanel divider = new JPanel(); divider.setBackground(new Color(230, 235, 245));
        tFooter.add(divider, "span 2, growx, h 1!, gapy 12 12");
        
        JLabel lTotal = new JLabel("Tổng cộng");
        lTotal.setFont(lTotal.getFont().deriveFont(Font.BOLD, 16f));
        lTotal.setForeground(new Color(24, 40, 66));
        
        JLabel valTotal = new JLabel(CurrencyUtils.formatVND(hd.getTongTienThanhToan()));
        valTotal.setFont(valTotal.getFont().deriveFont(Font.BOLD, 20f));
        valTotal.setForeground(new Color(220, 38, 38));
        valTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        
        tFooter.add(lTotal, "alignx left");
        tFooter.add(valTotal, "alignx right");

        tablePanel.add(tFooter, "growx");

        // Generated Label
        JLabel lGen = new JLabel("KQL HOTEL - Hóa đơn được tạo bởi hệ thống quản lý tự động", SwingConstants.CENTER);
        lGen.setForeground(new Color(150, 165, 185));
        lGen.setFont(lGen.getFont().deriveFont(11f));

        detailContent.add(topRow, "growx");
        detailContent.add(infoRow, "growx");
        detailContent.add(tTitle, "gapy 10 0");
        detailContent.add(tablePanel, "growx");
        detailContent.add(lGen, "growx, gapy 20 0");

        detailContent.revalidate();
        detailContent.repaint();
    }

    private JPanel createBox(String title, String val1, String val2) {
        RoundedPanel p = new RoundedPanel(12, new Color(250, 252, 255), new Color(230, 235, 245), 1f);
        p.setLayout(new MigLayout("wrap 1,insets 16,gap 4", "[grow,fill]", "[]"));
        
        JLabel t = new JLabel(title);
        t.setForeground(new Color(130, 145, 165));
        t.setFont(t.getFont().deriveFont(Font.BOLD, 11f));
        
        JLabel v1 = new JLabel(val1);
        v1.setFont(v1.getFont().deriveFont(Font.BOLD, 14f));
        v1.setForeground(new Color(24, 40, 66));
        
        JLabel v2 = new JLabel(val2);
        v2.setForeground(new Color(110, 125, 145));
        
        p.add(t, "gapy 0 8");
        p.add(v1);
        p.add(v2);
        
        return p;
    }

    private JLabel makeTText(String t, boolean right) {
        JLabel l = new JLabel(t);
        l.setForeground(new Color(100, 115, 135));
        if (right) l.setHorizontalAlignment(SwingConstants.RIGHT);
        return l;
    }

    private JPanel createTRow(String n, String q, String p, String t) {
        JPanel row = new JPanel(new MigLayout("insets 14 20 14 20", "[grow,fill][100!][150!][150!]", "[]"));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 245, 250)));
        
        JLabel ln = new JLabel(n); ln.setForeground(new Color(50, 65, 80));
        JLabel lq = new JLabel(q); lq.setForeground(new Color(50, 65, 80)); lq.setHorizontalAlignment(SwingConstants.RIGHT);
        JLabel lp = new JLabel(p); lp.setForeground(new Color(50, 65, 80)); lp.setHorizontalAlignment(SwingConstants.RIGHT);
        JLabel lt = new JLabel(t); lt.setForeground(new Color(24, 40, 66)); lt.setFont(lt.getFont().deriveFont(Font.BOLD)); lt.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(ln);
        row.add(lq);
        row.add(lp);
        row.add(lt);
        return row;
    }

    private ImageIcon loadIcon(String filename, int w, int h) {
        try {
            URL resource = getClass().getResource("/kqlhotel/resources/icons/" + filename);
            if (resource == null) {
                java.io.File file = new java.io.File("src/kqlhotel/resources/icons/" + filename);
                if (file.exists()) resource = file.toURI().toURL();
            }
            if (resource != null) {
                ImageIcon icon = new ImageIcon(resource);
                return new ImageIcon(icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static class InvoiceData {
        String id, customer, price, status;
        Color statusColor;
        InvoiceData(String id, String c, String p, String s, Color c2) {
            this.id = id; customer = c; price = p; status = s; statusColor = c2; 
        }
    }
}
