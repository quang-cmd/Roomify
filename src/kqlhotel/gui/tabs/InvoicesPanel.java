package kqlhotel.gui.tabs;

import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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
    private final RoundedPanel detailContainer = new RoundedPanel(
            24,
            new Color(255, 255, 255, 225),
            new Color(255, 255, 255, 140),
            1.2f
    );
    private final JPanel detailContent = new JPanel(
            new MigLayout("wrap 1,insets 18 16 18 16, gap 14, fillx", "[grow,fill]", "[]")
    );
    private final JLabel summaryLabel = new JLabel();
    private final List<PrimaryButton> filterButtons = new ArrayList<>();

    private List<Invoice> currentList;

    public InvoicesPanel() {
        setOpaque(false);
        setBackground(PAGE_BG);
        setLayout(new BorderLayout());

        //JPanel header = createHeader();

        BackgroundImagePanel content = new BackgroundImagePanel(
                "/kqlhotel/resources/icons/invoice_bg.png",
                new Color(10, 20, 35, 65)
        );
        content.setLayout(new MigLayout(
                "insets 0 14 14 14, gap 10, fill",
                "[270!,fill][grow,fill]",
                "[grow,fill]"
        ));

        JPanel leftSide = createLeftSide();
        createRightSide();

        content.add(leftSide, "growy, w 270!");

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
            return new Color(120, 120, 120);
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
        RoundedPanel left = new RoundedPanel(
                18,
                new Color(255, 255, 255, 225),
                new Color(255, 255, 255, 120),
                1.2f
        );
        left.setLayout(new MigLayout(
                "wrap 1,insets 14,gap 10,fillx",
                "[grow,fill]",
                "[][][grow,fill][120!]"
        ));
        left.setOpaque(false);

        JPanel filters1 = new JPanel(new MigLayout(
                "insets 0,gap 8,fillx",
                "[grow,fill][grow,fill][grow,fill]",
                "[]"
        ));
        filters1.setOpaque(false);
        filters1.add(createFilterBtn("Tất cả", true, e -> filterData("ALL", "Tất cả")), "h 40!");
        filters1.add(createFilterBtn("Đã thanh toán", false, e -> filterData("DaThanhToan", "Đã thanh toán")), "h 40!");
        filters1.add(createFilterBtn("Đã hủy", false, e -> filterData("DaHuy", "Đã hủy")), "h 40!");

        JPanel filters2 = new JPanel(new MigLayout(
                "insets 0,gap 8,fillx",
                "[grow,fill][grow,fill]",
                "[]"
        ));
        filters2.setOpaque(false);
        filters2.add(createFilterBtn("Chưa thanh toán", false, e -> filterData("ChuaThanhToan", "Chưa thanh toán")), "h 40!");
        filters2.add(createFilterBtn("Đang thanh toán", false, e -> filterData("DangThanhToan", "Đang thanh toán")), "h 40!");

        listPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        scroll.setPreferredSize(new Dimension(255, 0));

        JPanel brandPanel = new JPanel(new MigLayout("wrap 1,insets 8,align center center", "[center]", "[]"));
        brandPanel.setOpaque(false);

        JLabel brand = new JLabel("KQL HOTEL", SwingConstants.CENTER);
        brand.setForeground(new Color(255, 215, 120));
        brand.setFont(brand.getFont().deriveFont(Font.BOLD, 22f));

        JLabel slogan = new JLabel("Luxury Stay, Perfect Experience", SwingConstants.CENTER);
        slogan.setForeground(Color.WHITE);
        slogan.setFont(slogan.getFont().deriveFont(Font.BOLD, 12f));

        brandPanel.add(brand, "growx");
        brandPanel.add(slogan, "growx");

        left.add(filters1, "growx");
        left.add(filters2, "growx");
        left.add(scroll, "grow");
        left.add(brandPanel, "growx, aligny bottom");

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
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));

        if ("DaThanhToan".equals(computedStatus)) {
            icon.setText("✅");
        } else if ("DangThanhToan".equals(computedStatus)) {
            icon.setText("⏳");
        } else if ("DaHuy".equals(computedStatus)) {
            icon.setText("❌");
        } else {
            icon.setText("❗");
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

        double soTienHienThi = Math.max(0, hd.getTongTienThanhToan());

        if ("DaHuy".equals(computedStatus)
                && hd.getMaDatPhong() != null
                && !hd.getMaDatPhong().isBlank()) {
            soTienHienThi = Math.max(0, invoicesBUS.getDepositAmount(hd.getMaDatPhong()));
        }

        JLabel pLabel = new JLabel(CurrencyUtils.formatVND(soTienHienThi));
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
        detailContainer.setOpaque(false);
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

    private boolean isCancelledRoom(InvoiceDetail ct) {
        return ct != null && ct.getThanhTien() <= 0;
    }

    private String getRoomStatus(InvoiceDetail ct) {
        if (isCancelledRoom(ct)) {
            return "Đã hủy";
        }

        return ct.getNgayTraThucTe() != null ? "Đã trả" : "Chưa trả";
    }

    private int tinhSoDemHienThi(InvoiceDetail ct) {
        if (ct == null || ct.getNgayNhanPhong() == null) {
            return 1;
        }

        LocalDate ngayNhan = ct.getNgayNhanPhong().toLocalDate();

        LocalDate ngayKetThuc;
        if (ct.getNgayTraThucTe() != null) {
            ngayKetThuc = ct.getNgayTraThucTe().toLocalDate();
        } else {
            ngayKetThuc = LocalDate.now();
        }

        long soDem = ChronoUnit.DAYS.between(ngayNhan, ngayKetThuc);

        if (soDem < 1) {
            return 1;
        }

        return (int) soDem;
    }

    private void showDetail(Invoice hd) {
        detailContent.removeAll();

        Customer kh = invoicesBUS.getCustomerInfo(hd.getMaKhachHang());
        List<InvoiceDetail> roomDetails = invoicesBUS.getRoomDetails(hd.getMaHD());
        List<ServiceDetail> serviceDetails = invoicesBUS.getServiceDetails(hd.getMaHD());

        double tienCoc = hd.getTienCoc() > 0 ? hd.getTienCoc() : 0;
        if (tienCoc <= 0 && hd.getMaDatPhong() != null && !hd.getMaDatPhong().isBlank()) {
            tienCoc = invoicesBUS.getDepositAmount(hd.getMaDatPhong());
        }

        String computedStatus = invoicesBUS.getComputedStatus(hd);
        boolean laHoaDonHuy = "DaHuy".equals(computedStatus);

        double tongTienPhongThuan;
        double tongPhuThu;
        double tongPhiPhat;
        double tienDichVuHienThi;
        double tienThueHienThi;

        double tienKhuyenMaiHienThi;
        double tienKhuyenMaiMaHienThi;
        double tienKhuyenMaiHangHienThi;
        double tyLeGiamHangThanhVien;
        String tenHangThanhVien;

        double tongTruocGiam;
        double tongSauKhuyenMai;
        double conPhaiThanhToan;
        double tienHoanTra = 0;

// Tổng tiền khách đã trả thành công: gồm tiền cọc + các lần thanh toán trả phòng.
        double tongDaThanhToan = invoicesBUS.getTotalPaidAmount(hd.getMaHD());

// Dự phòng cho dữ liệu cũ chưa có dòng ThanhToan tiền cọc.
        if (tongDaThanhToan <= 0 && tienCoc > 0) {
            tongDaThanhToan = tienCoc;
        }

        double tienThanhToanThem = 0;

        if (laHoaDonHuy) {
            // Hủy phòng: khách mất cọc, không thu thêm, không hoàn cọc.
            tongTienPhongThuan = 0;
            tongPhuThu = 0;
            tongPhiPhat = 0;
            tienDichVuHienThi = 0;
            tienThueHienThi = 0;

            tienKhuyenMaiHienThi = 0;
            tienKhuyenMaiMaHienThi = 0;
            tienKhuyenMaiHangHienThi = 0;
            tyLeGiamHangThanhVien = 0;
            tenHangThanhVien = "Đồng";

            tongTruocGiam = Math.max(0, tienCoc);
            tongSauKhuyenMai = Math.max(0, tienCoc);

            conPhaiThanhToan = 0;
            tienHoanTra = hd.getTienHoanTra() > 0 ? hd.getTienHoanTra() : invoicesBUS.getRefundAmount(hd.getMaHD());
        } else {
            tongTienPhongThuan = Math.max(0, hd.getTienPhong());
            tongPhuThu = calculateTotalSurcharge(roomDetails);
            tongPhiPhat = calculateTotalPenalty(roomDetails);
            tienDichVuHienThi = Math.max(0, hd.getTienDichVu());
            tienThueHienThi = Math.max(0, hd.getTienThue());

            tienKhuyenMaiHienThi = Math.max(0, hd.getTienKhuyenMai());

            tongTruocGiam = Math.max(
                    0,
                    tongTienPhongThuan
                            + tongPhuThu
                            + tongPhiPhat
                            + tienDichVuHienThi
                            + tienThueHienThi
            );

            tyLeGiamHangThanhVien = getMembershipDiscountRate(kh);
            tenHangThanhVien = getMembershipRankName(kh);

            tienKhuyenMaiMaHienThi = calculatePromotionOnlyDiscount(
                    tongTruocGiam,
                    tienKhuyenMaiHienThi,
                    tyLeGiamHangThanhVien
            );

            tienKhuyenMaiHangHienThi = Math.max(
                    0,
                    tienKhuyenMaiHienThi - tienKhuyenMaiMaHienThi
            );

            tongSauKhuyenMai = Math.max(
                    0,
                    tongTruocGiam - tienKhuyenMaiMaHienThi - tienKhuyenMaiHangHienThi
            );

            if ("DaThanhToan".equals(computedStatus)) {
                tienThanhToanThem = Math.max(0, tongDaThanhToan - tienCoc);
                conPhaiThanhToan = 0;
                tienHoanTra = hd.getTienHoanTra() > 0 ? hd.getTienHoanTra() : invoicesBUS.getRefundAmount(hd.getMaHD());
            } else {
                tienThanhToanThem = Math.max(0, tongDaThanhToan - tienCoc);
                conPhaiThanhToan = Math.max(0, tongSauKhuyenMai - tongDaThanhToan);

                tienHoanTra = hd.getTienHoanTra() > 0 ? hd.getTienHoanTra() : 0;
                if (tienHoanTra <= 0) {
                    if (tongDaThanhToan > tongSauKhuyenMai) {
                        tienHoanTra = tongDaThanhToan - tongSauKhuyenMai;
                    } else {
                        tienHoanTra = invoicesBUS.getRefundAmount(hd.getMaHD());
                    }
                }
            }
        }

        JPanel topRow = new JPanel(new MigLayout("insets 0,fillx", "[][grow,fill][][][]", "[]"));
        topRow.setOpaque(false);

        JPanel idBox = new JPanel(new MigLayout("wrap 1,insets 0", "[]", "[]"));
        idBox.setOpaque(false);

        JPanel titleRow = new JPanel(new MigLayout("insets 0,gap 10", "[][]", "[]"));
        titleRow.setOpaque(false);

        JLabel lId = new JLabel(hd.getMaHD());
        lId.setFont(lId.getFont().deriveFont(Font.BOLD, 22f));
        lId.setForeground(new Color(24, 40, 66));

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
        bConfirm.setVisible(
                !"DaThanhToan".equals(computedStatus)
                        && !"DaHuy".equals(computedStatus)
        );
        bConfirm.addActionListener(e -> {
            if (invoicesBUS.confirmPayment(hd.getMaHD())) {
                refreshData();
                JOptionPane.showMessageDialog(this, "Đã xác nhận thanh toán thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Xác nhận thanh toán thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        String tmpPaymentStaffName = invoicesBUS.getPaymentStaffName(hd.getMaHD());
        String tmpPaymentStaffId = invoicesBUS.getPaymentStaffId(hd.getMaHD());

        if (tmpPaymentStaffName == null || tmpPaymentStaffName.isBlank()) {
            tmpPaymentStaffName = invoicesBUS.getStaffName(hd.getMaNhanVien());
        }

        if (tmpPaymentStaffId == null || tmpPaymentStaffId.isBlank()) {
            tmpPaymentStaffId = hd.getMaNhanVien();
        }

        final String paymentStaffName = tmpPaymentStaffName;
        final String paymentStaffId = tmpPaymentStaffId;

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
                    paymentStaffName,
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

        JPanel infoRow = new JPanel(new MigLayout("insets 0,gap 12,fillx", "[25%,fill][35%,fill][20%,fill][20%,fill]"));
        infoRow.setOpaque(false);

        infoRow.add(createBox(
                "KHÁCH HÀNG",
                kh != null ? kh.getHoTenKH() : "Unknown",
                kh != null ? "SĐT: " + kh.getSdt() : ""
        ));

        infoRow.add(createBox(
                "THÔNG TIN PHÒNG",
                roomText,
                "Số phòng: " + roomDetails.size() + " · Đã trả: " + countPaidRooms(roomDetails)
        ));

        infoRow.add(createBox(
                "TIỀN CỌC",
                CurrencyUtils.formatVND(tienCoc),
                hd.getMaDatPhong() != null ? "Mã đặt: " + hd.getMaDatPhong() : "Không có mã đặt"
        ));

        infoRow.add(createBox(
                "NHÂN VIÊN",
                paymentStaffName != null ? paymentStaffName : "Không xác định",
                paymentStaffId != null ? "Mã NV: " + paymentStaffId : "Chưa có nhân viên"
        ));

        JLabel tTitle = new JLabel("CHI TIẾT HÓA ĐƠN");
        tTitle.setForeground(new Color(130, 145, 165));
        tTitle.setFont(tTitle.getFont().deriveFont(Font.BOLD, 12f));

        RoundedPanel tablePanel = new RoundedPanel(12, Color.WHITE, new Color(225, 231, 245), 1f);
        tablePanel.setLayout(new MigLayout("wrap 1,insets 0,gap 0,fillx", "[grow,fill]", "[]"));

        JPanel tHeader = new JPanel(new MigLayout(
                "insets 12 8 12 8, fillx",
                "[grow,fill][52::65,right][78::92,right][80::92,right][70::82,center][92::108,center][85::98,right]",
                "[]"
        ));
        tHeader.setBackground(new Color(250, 252, 255));
        tHeader.add(makeTText("Hạng mục", false));
        tHeader.add(makeTText("Số đêm", true));
        tHeader.add(makeTText("Đêm thực tế", true));
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
            double penalty = Math.max(0, ct.getPhiPhat());
            double baseRoom = Math.max(0, ct.getThanhTien() - surcharge - penalty);

            int soDemTinhTien = ct.getSoDem() > 0 ? ct.getSoDem() : 1;
            double donGiaTheoDem = baseRoom / soDemTinhTien;

            boolean phongDaHuy = isCancelledRoom(ct);
            int soDemHienThi = tinhSoDemHienThi(ct);

            tablePanel.add(createTRow(
                    "Tiền phòng " + ct.getMaPhong(),
                    getBookedNightsText(ct),
                    getActualNightsText(ct),
                    CurrencyUtils.formatVND(donGiaTheoDem),
                    getRoomStatus(ct),
                    ngayTraText,
                    CurrencyUtils.formatVND(baseRoom)
            ), "growx");

            if (surcharge > 0) {
                tablePanel.add(createTRow(
                        "Phụ thu phòng " + ct.getMaPhong(),
                        "",
                        "",
                        "",
                        "",
                        "",
                        CurrencyUtils.formatVND(surcharge)
                ), "growx");
            }

            if (penalty > 0) {
                tablePanel.add(createTRow(
                        getCheckoutFeeLabel(ct) + " " + ct.getMaPhong(),
                        "",
                        "",
                        "",
                        "",
                        "",
                        CurrencyUtils.formatVND(penalty)
                ), "growx");
            }
        }

        for (ServiceDetail ct : serviceDetails) {
            String serviceRoomText = getServiceRoomText(ct);
            String serviceName = getServiceDisplayName(ct);

            tablePanel.add(createTRow(
                    "Dịch vụ " + serviceRoomText + ": " + serviceName,
                    "SL: " + ct.getSoLuong(),
                    "",
                    CurrencyUtils.formatVND(ct.getDonGia()),
                    "",
                    "",
                    CurrencyUtils.formatVND(ct.getThanhTien())
            ), "growx");
        }

        JPanel bottomCards = new JPanel(new MigLayout(
                "insets 18 16 18 16, gap 16, fillx",
                "[grow 75,fill][grow 25,fill]",
                "[]"
        ));
        bottomCards.setOpaque(false);
        bottomCards.setOpaque(false);

        boolean coTienHoanTra = tienHoanTra > 0;

        JPanel summaryCard = createImageSummaryCard(
                "/kqlhotel/resources/icons/invoice_summary_card.png",
                tongTienPhongThuan,
                tongPhuThu,
                tienDichVuHienThi,
                tienThueHienThi,
                tongTruocGiam,
                tienKhuyenMaiMaHienThi,
                tienKhuyenMaiHangHienThi,
                tenHangThanhVien,
                tongSauKhuyenMai,
                tienCoc,
                tongDaThanhToan
        );

        JPanel dueCard = createImageDueCard(
                "/kqlhotel/resources/icons/payment_due_card.png",
                coTienHoanTra,
                tienHoanTra,
                conPhaiThanhToan
        );

        bottomCards.add(summaryCard, "growx, h 250!");
        bottomCards.add(dueCard, "growx, h 250!");

        tablePanel.add(bottomCards, "growx");

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

    private JPanel createTRow(String name,
                              String bookedNights,
                              String actualNights,
                              String price,
                              String roomStatus,
                              String actualCheckout,
                              String total) {
        JPanel row = new JPanel(new MigLayout(
                "insets 10 8 10 8, fillx",
                "[grow,fill][52::65,right][78::92,right][80::92,right][70::82,center][92::108,center][85::98,right]",
                "[]"
        ));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 245, 250)));

        JLabel ln = new JLabel(name);
        ln.setForeground(new Color(50, 65, 80));

        JLabel lb = new JLabel(bookedNights);
        lb.setForeground(new Color(50, 65, 80));
        lb.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel lan = new JLabel(actualNights);
        lan.setForeground(new Color(50, 65, 80));
        lan.setHorizontalAlignment(SwingConstants.RIGHT);

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

        Font rowFont = ln.getFont().deriveFont(12f);

        ln.setFont(rowFont);
        lb.setFont(rowFont);
        lan.setFont(rowFont);
        lp.setFont(rowFont);
        ls.setFont(rowFont);
        la.setFont(rowFont);
        lt.setFont(lt.getFont().deriveFont(Font.BOLD, 12f));

        lt.setToolTipText(total);
        lp.setToolTipText(price);
        la.setToolTipText(actualCheckout);

        row.add(ln, "growx");
        row.add(lb, "alignx right");
        row.add(lan, "alignx right");
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

    private double calculateTotalBaseRoom(List<InvoiceDetail> roomDetails) {
        double total = 0;

        if (roomDetails == null) {
            return 0;
        }

        for (InvoiceDetail ct : roomDetails) {
            double surcharge = Math.max(0, ct.getPhuThu());
            double penalty = Math.max(0, ct.getPhiPhat());
            double baseRoom = Math.max(0, ct.getThanhTien() - surcharge - penalty);
            total += baseRoom;
        }

        return total;
    }

    private String getBookedNightsText(InvoiceDetail ct) {
        if (ct == null || ct.getNgayNhanPhong() == null || ct.getNgayTraPhong() == null) {
            return "--";
        }

        long nights = java.time.temporal.ChronoUnit.DAYS.between(
                ct.getNgayNhanPhong().toLocalDate(),
                ct.getNgayTraPhong().toLocalDate()
        );

        if (nights <= 0) nights = 1;
        return nights + " đêm";
    }

    private String getActualNightsText(InvoiceDetail ct) {
        if (ct == null || ct.getNgayTraThucTe() == null) {
            return "--";
        }

        return ct.getSoDem() + " đêm";
    }

    private double calculateTotalPenalty(List<InvoiceDetail> roomDetails) {
        double total = 0;

        if (roomDetails == null) {
            return 0;
        }

        for (InvoiceDetail ct : roomDetails) {
            total += Math.max(0, ct.getPhiPhat());
        }

        return total;
    }

    private double calculateTotalService(List<ServiceDetail> serviceDetails) {
        double total = 0;

        if (serviceDetails == null) {
            return 0;
        }

        for (ServiceDetail ct : serviceDetails) {
            total += Math.max(0, ct.getThanhTien());
        }

        return total;
    }

    private double getMembershipDiscountRate(Customer kh) {
        if (kh == null || kh.getHangKH() == null) {
            return 0;
        }

        String hang = kh.getHangKH().trim();

        return switch (hang) {
            case "Bac" -> 0.05;
            case "Vang" -> 0.10;
            case "KimCuong" -> 0.15;
            default -> 0.0;
        };
    }

    private String getMembershipRankName(Customer kh) {
        if (kh == null || kh.getHangKH() == null) {
            return "Đồng";
        }

        String hang = kh.getHangKH().trim();

        return switch (hang) {
            case "Bac" -> "Bạc";
            case "Vang" -> "Vàng";
            case "KimCuong" -> "Kim cương";
            default -> "Đồng";
        };
    }

    private String formatPercent(double rate) {
        double percent = rate * 100;

        if (percent == (long) percent) {
            return ((long) percent) + "%";
        }

        return percent + "%";
    }

    /**
     * Vì HoaDon hiện chỉ lưu tổng khuyến mãi trong tienKhuyenMai,
     * hàm này tách ngược ra phần khuyến mãi theo mã.
     *
     * Công thức lúc tính:
     * totalDiscount = promotionDiscount + (amountBeforeDiscount - promotionDiscount) * membershipRate
     *
     * Suy ra:
     * promotionDiscount = (totalDiscount - amountBeforeDiscount * membershipRate) / (1 - membershipRate)
     */
    private double calculatePromotionOnlyDiscount(double amountBeforeDiscount,
                                                  double totalDiscount,
                                                  double membershipRate) {
        totalDiscount = Math.max(0, totalDiscount);
        amountBeforeDiscount = Math.max(0, amountBeforeDiscount);
        membershipRate = Math.max(0, membershipRate);

        if (totalDiscount <= 0) {
            return 0;
        }

        if (membershipRate <= 0) {
            return totalDiscount;
        }

        if (membershipRate >= 1) {
            return 0;
        }

        double promotionDiscount =
                (totalDiscount - amountBeforeDiscount * membershipRate) / (1 - membershipRate);

        if (Double.isNaN(promotionDiscount) || Double.isInfinite(promotionDiscount)) {
            return 0;
        }

        return Math.max(0, Math.min(promotionDiscount, totalDiscount));
    }

    private String getServiceRoomText(ServiceDetail sd) {
        if (sd == null || sd.getGhiChu() == null) {
            return "chung";
        }

        String ghiChu = sd.getGhiChu();

        if (!ghiChu.startsWith("ROOM:")) {
            return "chung";
        }

        int pipeIndex = ghiChu.indexOf("|");

        if (pipeIndex <= 5) {
            return "chung";
        }

        String room = ghiChu.substring(5, pipeIndex).trim();

        return "phòng " + room;
    }

    private String getServiceDisplayName(ServiceDetail sd) {
        if (sd == null) {
            return "";
        }

        String ghiChu = sd.getGhiChu();

        if (ghiChu != null && ghiChu.startsWith("ROOM:")) {
            int pipeIndex = ghiChu.indexOf("|");

            if (pipeIndex >= 0 && pipeIndex < ghiChu.length() - 1) {
                return ghiChu.substring(pipeIndex + 1);
            }
        }

        String tenDV = invoicesBUS.getServiceName(sd.getMaDV());

        if (tenDV != null && !tenDV.isBlank()) {
            return sd.getMaDV() + " - " + tenDV;
        }

        if (ghiChu != null && !ghiChu.isBlank()) {
            return ghiChu;
        }

        return sd.getMaDV();
    }

    private String getCheckoutFeeLabel(InvoiceDetail ct) {
        if (ct == null || ct.getNgayTraThucTe() == null || ct.getNgayTraPhong() == null) {
            return "Phí/phạt trả phòng";
        }

        if (ct.getNgayTraThucTe().toLocalDate().isBefore(ct.getNgayTraPhong().toLocalDate())) {
            return "Phí trả phòng sớm";
        }

        return "Phạt trả trễ";
    }
    private ImageIcon loadImage(String path, int w, int h) {
        try {
            URL url = getClass().getResource(path);
            if (url == null) {
                java.io.File file = new java.io.File("src" + path);
                if (file.exists()) {
                    url = file.toURI().toURL();
                }
            }

            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                return new ImageIcon(icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static class BackgroundImagePanel extends JPanel {
        private Image bg;
        private final Color overlayColor;

        public BackgroundImagePanel(String path) {
            this(path, new Color(10, 20, 35, 90));
        }

        public BackgroundImagePanel(String path, Color overlayColor) {
            this.overlayColor = overlayColor;
            try {
                URL url = BackgroundImagePanel.class.getResource(path);
                if (url == null) {
                    java.io.File file = new java.io.File("src" + path);
                    if (file.exists()) {
                        url = file.toURI().toURL();
                    }
                }
                if (url != null) {
                    bg = new ImageIcon(url).getImage();
                }
            } catch (Exception ignored) {
            }
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            if (bg != null) {
                g2.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            } else {
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(235, 242, 252),
                        getWidth(), getHeight(), new Color(205, 220, 240)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }

            g2.setColor(overlayColor);
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(new Color(255, 255, 255, 80));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.dispose();
        }
    }

    private JPanel createImageSummaryCard(
            String imagePath,
            double tienPhong,
            double phuThu,
            double tienDV,
            double thue,
            double tongTruocGiam,
            double kmMa,
            double kmHang,
            String tenHang,
            double tongHoaDon,
            double tienCoc,
            double tongDaThanhToan
    ) {
        ImageCardPanel pane = new ImageCardPanel(imagePath);
        pane.setLayout(new MigLayout(
                "insets 35 35 25 15, fillx",
                "[160::200][180!][150!,right]",
                "[]2[]2[]2[]2[]2[]2[]2[]2[]2[]"
        ));

        pane.add(new JLabel(), "cell 0 0");
        pane.add(makeSummaryText("Tiền phòng", false), "cell 1 0");
        pane.add(makeSummaryText(CurrencyUtils.formatVND(tienPhong), true), "cell 2 0");

        pane.add(new JLabel(), "cell 0 1");
        pane.add(makeSummaryText("Phụ thu", false), "cell 1 1");
        pane.add(makeSummaryText(CurrencyUtils.formatVND(phuThu), true), "cell 2 1");

        pane.add(new JLabel(), "cell 0 2");
        pane.add(makeSummaryText("Tiền dịch vụ", false), "cell 1 2");
        pane.add(makeSummaryText(CurrencyUtils.formatVND(tienDV), true), "cell 2 2");

        pane.add(new JLabel(), "cell 0 3");
        pane.add(makeSummaryText("Thuế VAT (10%)", false), "cell 1 3");
        pane.add(makeSummaryText(CurrencyUtils.formatVND(thue), true), "cell 2 3");

        pane.add(new JLabel(), "cell 0 4");
        pane.add(makeSummaryText("Tổng trước giảm", false), "cell 1 4");
        pane.add(makeSummaryText(CurrencyUtils.formatVND(tongTruocGiam), true), "cell 2 4");

        pane.add(new JLabel(), "cell 0 5");
        pane.add(makeSummaryText("Khuyến mãi", false), "cell 1 5");
        pane.add(makeSummaryText("-" + CurrencyUtils.formatVND(kmMa), true), "cell 2 5");

        pane.add(new JLabel(), "cell 0 6");
        pane.add(makeSummaryText("Khuyến mãi hạng " + tenHang, false), "cell 1 6");
        pane.add(makeSummaryText("-" + CurrencyUtils.formatVND(kmHang), true), "cell 2 6");

        JLabel totalTitle = makeSummaryText("Tổng hóa đơn", false);
        totalTitle.setForeground(new Color(180, 120, 20));
        totalTitle.setFont(totalTitle.getFont().deriveFont(Font.BOLD, 13f));

        JLabel totalValue = makeSummaryText(CurrencyUtils.formatVND(tongHoaDon), true);
        totalValue.setForeground(new Color(180, 120, 20));
        totalValue.setFont(totalValue.getFont().deriveFont(Font.BOLD, 13f));

        pane.add(new JLabel(), "cell 0 7");
        pane.add(totalTitle, "cell 1 7");
        pane.add(totalValue, "cell 2 7");

        pane.add(new JLabel(), "cell 0 8");
        pane.add(makeSummaryText("Tiền cọc đặt phòng", false), "cell 1 8");
        pane.add(makeSummaryText("-" + CurrencyUtils.formatVND(tienCoc), true), "cell 2 8");

        pane.add(new JLabel(), "cell 0 9");
        pane.add(makeSummaryText("Tiền đã trả", false), "cell 1 9");
        pane.add(makeSummaryText("-" + CurrencyUtils.formatVND(tongDaThanhToan), true), "cell 2 9");

        return pane;
    }

    private JPanel createImageDueCard(
            String imagePath,
            boolean isRefund,
            double refund,
            double remain
    ) {
        ImageCardPanel pane = new ImageCardPanel(imagePath);
        pane.setLayout(new MigLayout(
                "wrap 1, insets 72 20 20 20, gap 12",
                "[grow,center]",
                "[]"
        ));

        JLabel title = new JLabel(isRefund ? "TIỀN HOÀN TRẢ" : "CÒN PHẢI THANH TOÁN");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        title.setForeground(isRefund ? new Color(30, 160, 100) : new Color(220, 38, 38));

        JLabel amount = new JLabel(isRefund ? "+" + CurrencyUtils.formatVND(refund) : CurrencyUtils.formatVND(remain));
        amount.setFont(amount.getFont().deriveFont(Font.BOLD, 26f));
        amount.setForeground(isRefund ? new Color(30, 160, 100) : new Color(220, 38, 38));

        pane.add(title, "gapy 18 20");
        pane.add(amount);

        return pane;
    }

    private static class ImageCardPanel extends JPanel {
        private Image bgImage;

        public ImageCardPanel(String path) {
            try {
                URL url = getClass().getResource(path);
                if (url == null) {
                    java.io.File file = new java.io.File("src" + path);
                    if (file.exists()) {
                        url = file.toURI().toURL();
                    }
                }
                if (url != null) {
                    bgImage = new ImageIcon(url).getImage();
                }
            } catch (Exception ignored) {
            }
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bgImage != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                g2.dispose();
            }
        }
    }

    private JLabel makeSummaryText(String text, boolean right) {
        JLabel label = new JLabel(text);
        label.setForeground(new Color(20, 35, 55));
        label.setFont(label.getFont().deriveFont(12f));

        if (right) {
            label.setHorizontalAlignment(SwingConstants.RIGHT);
        }

        return label;
    }
}
