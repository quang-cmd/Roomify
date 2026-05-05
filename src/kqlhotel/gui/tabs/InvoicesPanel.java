package kqlhotel.gui.tabs;

import java.awt.*;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;

import kqlhotel.bus.invoice.InvoicesBUS;
import kqlhotel.entity.Customer;
import kqlhotel.entity.Invoice;
import kqlhotel.entity.InvoiceDetail;
import kqlhotel.entity.ServiceDetail;
import kqlhotel.gui.components.PrimaryButton;
import kqlhotel.gui.components.RoundedPanel;
import kqlhotel.gui.theme.ThemeColors;
import kqlhotel.utils.CurrencyUtils;
import kqlhotel.utils.DateUtils;
import net.miginfocom.swing.MigLayout;

public class InvoicesPanel extends JPanel {
    private static final Color PAGE_BG = new Color(245, 248, 252);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final InvoicesBUS invoicesBUS = new InvoicesBUS();
    private final JPanel listPanel = new JPanel(new MigLayout("wrap 1,insets 0,gap 8", "[grow,fill]", "[]"));
    private final RoundedPanel detailContainer = new RoundedPanel(20, Color.WHITE, new Color(225, 231, 245), 1.5f);
    private final JPanel detailContent = new JPanel(
            new MigLayout("wrap 1,insets 20 20 20 20, gap 16, fillx", "[grow,fill]", "[]")
    );
    private final JLabel summaryLabel = new JLabel();
    private final List<PrimaryButton> filterButtons = new ArrayList<>();

    private List<Invoice> currentList;

    public InvoicesPanel() {
        setOpaque(false);
        setBackground(PAGE_BG);
        setLayout(new BorderLayout());

        //JPanel header = createHeader();

        JPanel content = new JPanel(new MigLayout(
                "insets 0 14 14 14, gap 10, fill",
                "[245::260,fill][grow,fill]",
                "[grow,fill]"
        ));
        content.setOpaque(false);

        JPanel leftSide = createLeftSide();
        createRightSide();

        content.add(leftSide, "growy, wmin 245, wmax 260");

        JScrollPane detailScroll = new JScrollPane(detailContainer);
        detailScroll.setBorder(BorderFactory.createEmptyBorder());
        detailScroll.setOpaque(false);
        detailScroll.getViewport().setOpaque(false);
        detailScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        detailScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        detailScroll.getVerticalScrollBar().setUnitIncrement(16);

        content.add(detailScroll, "grow, push");

        //add(header, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);

        refreshData();
    }

    private void refreshData() {
        this.currentList = invoicesBUS.getAllInvoices();
        summaryLabel.setText(invoicesBUS.getInvoiceSummary());
        updateFilterButtonStyles("Tất cả");
        renderList(currentList);
        showFirstInvoiceIfAny();
    }

    private void filterData(String status, String btnText) {
        List<Invoice> all = invoicesBUS.getAllInvoices();
        List<Invoice> filtered = new ArrayList<>();

        for (Invoice hd : all) {
            String computed = invoicesBUS.getComputedStatus(hd);
            if ("ALL".equals(status) || status.equals(computed)) {
                filtered.add(hd);
            }
        }

        this.currentList = filtered;
        summaryLabel.setText(invoicesBUS.getInvoiceSummary());
        updateFilterButtonStyles(btnText);
        renderList(currentList);
        showFirstInvoiceIfAny();
    }

    private void showFirstInvoiceIfAny() {
        if (!currentList.isEmpty()) {
            showDetail(currentList.get(0));
        } else {
            detailContent.removeAll();
            detailContent.revalidate();
            detailContent.repaint();
        }
    }

    private String getDisplayStatus(String status) {
        if ("DaThanhToan".equals(status)) {
            return "Đã thanh toán";
        }
        if ("DangThanhToan".equals(status)) {
            return "Đang thanh toán";
        }
        if ("DaHuy".equals(status)) {
            return "Đã hủy";
        }
        return "Chưa thanh toán";
    }

    private Color getDisplayStatusColor(String status) {
        if ("DaThanhToan".equals(status)) {
            return new Color(30, 180, 120);
        }
        if ("DangThanhToan".equals(status)) {
            return new Color(255, 153, 0);
        }
        if ("DaHuy".equals(status)) {
            return new Color(150, 165, 185);
        }
        return new Color(220, 38, 38);
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new MigLayout("insets 10 24 0 24,gap 0", "[grow]", "[]"));
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
        JPanel left = new JPanel(new MigLayout("wrap 1,insets 0,gap 12,fillx", "[grow,fill]", "[][][grow,fill]"));
        left.setOpaque(false);

        JPanel filters1 = new JPanel(new MigLayout("insets 0,gap 8,fillx", "[grow,fill][grow,fill]", "[]"));
        filters1.setOpaque(false);
        filters1.add(createFilterBtn("Tất cả", true, e -> filterData("ALL", "Tất cả")), "h 34!");
        filters1.add(createFilterBtn("Đã thanh toán", false, e -> filterData("DaThanhToan", "Đã thanh toán")), "h 34!");

        JPanel filters2 = new JPanel(new MigLayout("insets 0,gap 8,fillx", "[grow,fill][grow,fill][grow,fill]", "[]"));
        filters2.setOpaque(false);
        filters2.add(createFilterBtn("Chưa thanh toán", false, e -> filterData("ChuaThanhToan", "Chưa thanh toán")), "h 34!");
        filters2.add(createFilterBtn("Đang thanh toán", false, e -> filterData("DangThanhToan", "Đang thanh toán")), "h 34!");
        filters2.add(createFilterBtn("Đã hủy", false, e -> filterData("DaHuy", "Đã hủy")), "h 34!");

        listPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        scroll.setPreferredSize(new Dimension(255, 0));

        left.add(filters1, "growx");
        left.add(filters2, "growx");
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

    private void updateFilterButtonStyles(String activeText) {
        for (PrimaryButton btn : filterButtons) {
            if (btn.getText().equals(activeText)) {
                btn.setBackground(new Color(24, 34, 52));
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(Color.WHITE);
                btn.setForeground(new Color(100, 115, 135));
                btn.setBorder(BorderFactory.createLineBorder(new Color(225, 231, 245), 2));
            }
        }
    }

    private void renderList(List<Invoice> list) {
        listPanel.removeAll();
        for (Invoice data : list) {
            listPanel.add(createListItem(data, false), "growx");
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel createListItem(Invoice hd, boolean selected) {
        RoundedPanel item = new RoundedPanel(
                12,
                selected ? Color.WHITE : new Color(250, 252, 255),
                selected ? new Color(49, 106, 210) : new Color(230, 235, 245),
                selected ? 2.5f : 1.5f
        );
        item.setLayout(new MigLayout("insets 14 12 14 12", "[][grow,fill][][]", "[]"));

        String computedStatus = invoicesBUS.getComputedStatus(hd);

        JLabel icon = new JLabel();
        if ("DaThanhToan".equals(computedStatus)) {
            icon.setIcon(loadIcon("check.png", 18, 18));
        } else if ("DangThanhToan".equals(computedStatus)) {
            icon.setIcon(loadIcon("clock-circle.png", 18, 18)); // Fallback to text if missing
        } else if ("DaHuy".equals(computedStatus)) {
            icon.setIcon(loadIcon("cancel-room.png", 18, 18));
        } else {
            icon.setIcon(loadIcon("alert-circle.png", 18, 18));
        }

        JPanel info = new JPanel(new MigLayout("wrap 1,insets 0", "[grow,fill]", "[][]"));
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

        JPanel pricePane = new JPanel(new MigLayout("wrap 1,insets 0", "[right]", "[][]"));
        pricePane.setOpaque(false);

        JLabel pLabel = new JLabel(CurrencyUtils.formatVND(hd.getTongTienThanhToan()));
        pLabel.setFont(pLabel.getFont().deriveFont(Font.BOLD, 14f));
        pLabel.setForeground(new Color(24, 40, 66));
        pLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel sLabel = new JLabel(getDisplayStatus(computedStatus));
        sLabel.setForeground(getDisplayStatusColor(computedStatus));
        sLabel.setFont(sLabel.getFont().deriveFont(11f));
        sLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        pricePane.add(pLabel, "alignx right");
        pricePane.add(sLabel, "alignx right");

        JLabel arrow = new JLabel("›");
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

    private double calculatePaidRoomAmount(List<InvoiceDetail> roomDetails) {
        double total = 0;
        for (InvoiceDetail ct : roomDetails) {
            if (ct.getNgayTraThucTe() != null) {
                total += ct.getThanhTien();
            }
        }
        return total;
    }

    private int countPaidRooms(List<InvoiceDetail> roomDetails) {
        int count = 0;
        for (InvoiceDetail ct : roomDetails) {
            if (ct.getNgayTraThucTe() != null) {
                count++;
            }
        }
        return count;
    }

    private String getRoomStatus(InvoiceDetail ct, String computedStatus) {
        if ("DaHuy".equals(computedStatus)) {
            return "Đã hủy";
        }
        return ct.getNgayTraThucTe() != null ? "Đã trả" : "Chưa trả";
    }

    private void showDetail(Invoice hd) {
        detailContent.removeAll();

        Customer kh = invoicesBUS.getCustomerInfo(hd.getMaKhachHang());
        List<InvoiceDetail> roomDetails = invoicesBUS.getRoomDetails(hd.getMaHD());
        List<ServiceDetail> serviceDetails = invoicesBUS.getServiceDetails(hd.getMaHD());

        double tienCoc = 0;
        if (hd.getMaDatPhong() != null && !hd.getMaDatPhong().isBlank()) {
            tienCoc = invoicesBUS.getDepositAmount(hd.getMaDatPhong());
        }

        double tongPhuThu = calculateTotalSurcharge(roomDetails);
        double tongTruocGiam = hd.getTienPhong() + tongPhuThu + hd.getTienDichVu() + hd.getTienThue();
        double tongSauKhuyenMai = hd.getTongTienThanhToan();
        double conPhaiThanhToan = Math.max(0, tongSauKhuyenMai - tienCoc);

        JPanel topRow = new JPanel(new MigLayout("insets 0,fillx", "[][grow,fill][][][]", "[]"));
        topRow.setOpaque(false);

        JPanel idBox = new JPanel(new MigLayout("wrap 1,insets 0", "[]", "[]"));
        idBox.setOpaque(false);

        JPanel titleRow = new JPanel(new MigLayout("insets 0,gap 10", "[][]", "[]"));
        titleRow.setOpaque(false);

        JLabel lId = new JLabel(hd.getMaHD());
        lId.setFont(lId.getFont().deriveFont(Font.BOLD, 22f));
        lId.setForeground(new Color(24, 40, 66));

        String computedStatus = invoicesBUS.getComputedStatus(hd);
        JLabel lStatus = new JLabel(" • " + getDisplayStatus(computedStatus));
        lStatus.setForeground(getDisplayStatusColor(computedStatus));
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
        bConfirm.setVisible(!"DaThanhToan".equals(computedStatus));
        bConfirm.addActionListener(e -> {
            if (invoicesBUS.confirmPayment(hd.getMaHD())) {
                refreshData();
                JOptionPane.showMessageDialog(this, "Đã xác nhận thanh toán thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Xác nhận thanh toán thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        PrimaryButton bPdf = new PrimaryButton("Xuất PDF");
        bPdf.setIcon(loadIcon("print.png", 18, 18));
        bPdf.setBackground(new Color(255, 193, 7));
        bPdf.setForeground(Color.WHITE);
        bPdf.setFocusPainted(false);
        bPdf.setArc(12);
        bPdf.addActionListener(e -> {
            new kqlhotel.gui.dialog.InvoicePreviewDialog(
                    SwingUtilities.getWindowAncestor(this),
                    hd,
                    invoicesBUS.getCustomerInfo(hd.getMaKhachHang()),
                    invoicesBUS.getStaffName(hd.getMaNhanVien()),
                    invoicesBUS.getRoomDetails(hd.getMaHD()),
                    invoicesBUS.getServiceDetails(hd.getMaHD()),
                    invoicesBUS
            ).setVisible(true);
        });

        topRow.add(idBox);
        topRow.add(new JPanel() {{ setOpaque(false); }}, "growx");
        topRow.add(bConfirm, "h 38!");
        topRow.add(bPdf, "h 38!");

        String roomText = roomDetails.isEmpty()
                ? "Không có phòng"
                : roomDetails.stream().map(InvoiceDetail::getMaPhong).collect(Collectors.joining(", "));

        JPanel infoRow = new JPanel(new MigLayout("insets 0,gap 12,fillx", "[grow,fill][grow,fill][grow,fill]", "[]"));
        infoRow.setOpaque(false);

        infoRow.add(createBox(
                "KHÁCH HÀNG",
                kh != null ? kh.getHoTenKH() : "Unknown",
                kh != null ? "SĐT: " + kh.getSdt() : ""
        ));

        infoRow.add(createBox(
                "THÔNG TIN PHÒNG",
                roomText,
                "DaHuy".equals(computedStatus) ? "Đã hủy toàn bộ" : ("Số phòng: " + roomDetails.size() + " · Đã trả: " + countPaidRooms(roomDetails))
        ));

        infoRow.add(createBox(
                "TIỀN CỌC",
                CurrencyUtils.formatVND(tienCoc),
                hd.getMaDatPhong() != null ? "Mã đặt: " + hd.getMaDatPhong() : "Không có mã đặt"
        ));

        String staffName = invoicesBUS.getStaffName(hd.getMaNhanVien());

        infoRow.add(createBox(
                "NHÂN VIÊN",
                staffName != null ? staffName : hd.getMaNhanVien(),
                hd.getMaNhanVien() != null ? "Mã NV: " + hd.getMaNhanVien() : "Chưa có nhân viên"
        ));

        JLabel tTitle = new JLabel("CHI TIẾT HÓA ĐƠN");
        tTitle.setForeground(new Color(130, 145, 165));
        tTitle.setFont(tTitle.getFont().deriveFont(Font.BOLD, 12f));

        RoundedPanel tablePanel = new RoundedPanel(12, Color.WHITE, new Color(225, 231, 245), 1f);
        tablePanel.setLayout(new MigLayout("wrap 1,insets 0,gap 0,fillx", "[grow,fill]", "[]"));

        JPanel tHeader = new JPanel(new MigLayout(
                "insets 14 16 14 16, fillx",
                "[grow,fill][65::85,right][95::120,right][90::110,center][120::140,center][95::120,right]",
                "[]"
        ));
        tHeader.setBackground(new Color(250, 252, 255));
        tHeader.add(makeTText("Hạng mục", false));
        tHeader.add(makeTText("Số đêm/SL", true));
        tHeader.add(makeTText("Đơn giá", true));
        tHeader.add(makeTText("Tình trạng", true));
        tHeader.add(makeTText("Trả thực tế", true));
        tHeader.add(makeTText("Thành tiền", true));

        tablePanel.add(tHeader, "growx");

        for (InvoiceDetail ct : roomDetails) {
            String ngayTraText = ct.getNgayTraThucTe() != null
                    ? ct.getNgayTraThucTe().format(DATE_TIME_FORMATTER)
                    : "--";

            double surcharge = Math.max(0, ct.getPhuThu());
            double baseRoom = Math.max(0, ct.getThanhTien() - surcharge);
            double donGiaTheoDem = ct.getSoDem() > 0 ? baseRoom / ct.getSoDem() : 0;

            tablePanel.add(createTRow(
                    "Tiền phòng " + ct.getMaPhong(),
                    ct.getSoDem() + " đêm",
                    CurrencyUtils.formatVND(donGiaTheoDem),
                    getRoomStatus(ct, computedStatus),
                    ngayTraText,
                    CurrencyUtils.formatVND(baseRoom)
            ), "growx");

            if (ct.getPhiPhat() > 0) {
                tablePanel.add(createTRow(
                        "Phí phạt phòng " + ct.getMaPhong(),
                        "",
                        "",
                        "",
                        "",
                        CurrencyUtils.formatVND(ct.getPhiPhat())
                ), "growx");
            }

            if (surcharge > 0) {
                tablePanel.add(createTRow(
                        "Phụ thu phòng " + ct.getMaPhong(),
                        "",
                        "",
                        "",
                        "",
                        CurrencyUtils.formatVND(surcharge)
                ), "growx");
            }
        }

        for (ServiceDetail ct : serviceDetails) {
            tablePanel.add(createTRow(
                    "Dịch vụ: " + ct.getMaDV()
                            + (invoicesBUS.getServiceName(ct.getMaDV()).isBlank()
                            ? ""
                            : " - " + invoicesBUS.getServiceName(ct.getMaDV())),
                    String.valueOf(ct.getSoLuong()),
                    CurrencyUtils.formatVND(ct.getDonGia()),
                    "",
                    "",
                    CurrencyUtils.formatVND(ct.getThanhTien())
            ), "growx");
        }

        JPanel tFooter = new JPanel(new MigLayout(
                "wrap 2,insets 18 16 18 16, fillx",
                "[grow,fill][180::240,right]",
                "[]"
        ));
        tFooter.setBackground(Color.WHITE);

        if ("DaHuy".equals(computedStatus)) {
            tFooter.removeAll();
            tFooter.setLayout(new MigLayout("wrap 2,insets 18 16 18 16, fillx", "[grow,fill][180::240,right]", "[]"));

            double penalty = hd.getTienDichVu();
            double refund = invoicesBUS.getRefundAmount(hd.getMaHD());

            tFooter.add(makeTText("Phí phạt hủy phòng", false), "alignx left");
            tFooter.add(makeTText(CurrencyUtils.formatVND(penalty), true), "alignx right");

            tFooter.add(makeTText("Tiền cọc đã thu", false), "alignx left");
            tFooter.add(makeTText(CurrencyUtils.formatVND(tienCoc), true), "alignx right");

            tFooter.add(makeTText("Tiền hoàn trả cho khách", false), "alignx left");
            tFooter.add(makeTText(CurrencyUtils.formatVND(refund), true), "alignx right");

            JPanel divider2 = new JPanel();
            divider2.setBackground(new Color(230, 235, 245));
            tFooter.add(divider2, "span 2, growx, h 1!, gapy 12 12");

            JLabel lConLai2 = new JLabel("Còn phải thanh toán");
            lConLai2.setFont(lConLai2.getFont().deriveFont(Font.BOLD, 16f));
            lConLai2.setForeground(new Color(24, 40, 66));

            JLabel valConLai2 = new JLabel(CurrencyUtils.formatVND(conPhaiThanhToan));
            valConLai2.setFont(valConLai2.getFont().deriveFont(Font.BOLD, 22f));
            valConLai2.setForeground(new Color(220, 38, 38));
            valConLai2.setHorizontalAlignment(SwingConstants.RIGHT);

            tFooter.add(lConLai2, "alignx left");
            tFooter.add(valConLai2, "alignx right");
        } else {
            tFooter.add(makeTText("Tiền phòng", false), "alignx left");
            tFooter.add(makeTText(CurrencyUtils.formatVND(hd.getTienPhong()), true), "alignx right");

            tFooter.add(makeTText("Phụ thu", false), "alignx left");
            tFooter.add(makeTText(CurrencyUtils.formatVND(tongPhuThu), true), "alignx right");

            tFooter.add(makeTText("Tiền dịch vụ", false), "alignx left");
            tFooter.add(makeTText(CurrencyUtils.formatVND(hd.getTienDichVu()), true), "alignx right");

            tFooter.add(makeTText("Thuế VAT (10%)", false), "alignx left");
            tFooter.add(makeTText(CurrencyUtils.formatVND(hd.getTienThue()), true), "alignx right");

            tFooter.add(makeTText("Tổng trước giảm", false), "alignx left");
            tFooter.add(makeTText(CurrencyUtils.formatVND(tongTruocGiam), true), "alignx right");

            tFooter.add(makeTText("Khuyến mãi", false), "alignx left");
            tFooter.add(makeTText("-" + CurrencyUtils.formatVND(hd.getTienKhuyenMai()), true), "alignx right");

            tFooter.add(makeTText("Tổng hóa đơn", false), "alignx left");
            tFooter.add(makeTText(CurrencyUtils.formatVND(tongSauKhuyenMai), true), "alignx right");

            tFooter.add(makeTText("Tiền cọc đã cọc", false), "alignx left");
            tFooter.add(makeTText("-" + CurrencyUtils.formatVND(tienCoc), true), "alignx right");

            JPanel divider = new JPanel();
            divider.setBackground(new Color(230, 235, 245));
            tFooter.add(divider, "span 2, growx, h 1!, gapy 12 12");

            JLabel lConLai = new JLabel("Còn phải thanh toán");
            lConLai.setFont(lConLai.getFont().deriveFont(Font.BOLD, 16f));
            lConLai.setForeground(new Color(24, 40, 66));

            JLabel valConLai = new JLabel(CurrencyUtils.formatVND(conPhaiThanhToan));
            valConLai.setFont(valConLai.getFont().deriveFont(Font.BOLD, 22f));
            valConLai.setForeground(new Color(220, 38, 38));
            valConLai.setHorizontalAlignment(SwingConstants.RIGHT);

            tFooter.add(lConLai, "alignx left");
            tFooter.add(valConLai, "alignx right");
        }

        tablePanel.add(tFooter, "growx");

        JLabel lGen = new JLabel("KQL HOTEL - Hóa đơn được tạo bởi hệ thống quản lý tự động", SwingConstants.CENTER);
        lGen.setForeground(new Color(150, 165, 185));
        lGen.setFont(lGen.getFont().deriveFont(11f));

        detailContent.add(topRow, "growx");
        detailContent.add(infoRow, "growx");
        detailContent.add(tTitle, "gapy 8 0");
        detailContent.add(tablePanel, "growx");
        detailContent.add(lGen, "growx, gapy 16 0");

        detailContent.revalidate();
        detailContent.repaint();
    }

    private JPanel createBox(String title, String val1, String val2) {
        RoundedPanel p = new RoundedPanel(12, new Color(250, 252, 255), new Color(230, 235, 245), 1f);
        p.setLayout(new MigLayout("wrap 1,insets 14,gap 4,fillx", "[grow,fill]", "[]"));

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
        if (right) {
            l.setHorizontalAlignment(SwingConstants.RIGHT);
        }
        return l;
    }

    private JPanel createTRow(String name, String qty, String price, String roomStatus, String actualCheckout, String total) {
        JPanel row = new JPanel(new MigLayout(
                "insets 12 16 12 16, fillx",
                "[grow,fill][65::85,right][95::120,right][90::110,center][120::140,center][95::120,right]",
                "[]"
        ));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 245, 250)));

        JLabel ln = new JLabel(name);
        ln.setForeground(new Color(50, 65, 80));

        JLabel lq = new JLabel(qty);
        lq.setForeground(new Color(50, 65, 80));
        lq.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel lp = new JLabel(price);
        lp.setForeground(new Color(50, 65, 80));
        lp.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel ls = new JLabel(roomStatus);
        ls.setHorizontalAlignment(SwingConstants.CENTER);
        if ("Đã trả".equals(roomStatus)) {
            ls.setForeground(new Color(30, 180, 120));
            ls.setFont(ls.getFont().deriveFont(Font.BOLD));
        } else if ("Chưa trả".equals(roomStatus)) {
            ls.setForeground(new Color(220, 38, 38));
            ls.setFont(ls.getFont().deriveFont(Font.BOLD));
        } else {
            ls.setForeground(new Color(100, 115, 135));
        }

        JLabel la = new JLabel(actualCheckout);
        la.setForeground(new Color(50, 65, 80));
        la.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lt = new JLabel(total);
        lt.setForeground(new Color(24, 40, 66));
        lt.setFont(lt.getFont().deriveFont(Font.BOLD));
        lt.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(ln, "growx");
        row.add(lq, "alignx right");
        row.add(lp, "alignx right");
        row.add(ls, "alignx center");
        row.add(la, "alignx center");
        row.add(lt, "alignx right");

        return row;
    }

    private ImageIcon loadIcon(String filename, int w, int h) {
        try {
            URL resource = getClass().getResource("/kqlhotel/resources/icons/" + filename);
            if (resource == null) {
                java.io.File file = new java.io.File("src/kqlhotel/resources/icons/" + filename);
                if (file.exists()) {
                    resource = file.toURI().toURL();
                }
            }
            if (resource != null) {
                ImageIcon icon = new ImageIcon(resource);
                return new ImageIcon(icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
            }
        } catch (Exception ignored) {
        }
        return null;
    }
    private double calculateTotalSurcharge(List<InvoiceDetail> roomDetails) {
        double total = 0;

        if (roomDetails == null) {
            return 0;
        }

        for (InvoiceDetail ct : roomDetails) {
            total += Math.max(0, ct.getPhuThu());
        }

        return total;
    }
}
