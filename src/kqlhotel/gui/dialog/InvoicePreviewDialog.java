package kqlhotel.gui.dialog;

import kqlhotel.bus.Invoice.InvoicesBUS;
import kqlhotel.entity.Customer;
import kqlhotel.entity.Invoice;
import kqlhotel.entity.InvoiceDetail;
import kqlhotel.entity.ServiceDetail;
import kqlhotel.utils.CurrencyUtils;
import kqlhotel.utils.DateUtils;
import kqlhotel.gui.components.PrimaryButton;

import javax.swing.*;
import java.awt.*;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class InvoicePreviewDialog extends JDialog {
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Invoice hd;
    private final Customer kh;
    private final String staffName;
    private final List<InvoiceDetail> rooms;
    private final List<ServiceDetail> services;
    private final InvoicesBUS invoicesBUS;

    private final JPanel invoiceListPanel = new JPanel();
    private final List<JCheckBox> checkBoxes = new ArrayList<>();
    private final List<JPanel> invoicePanels = new ArrayList<>();

    public InvoicePreviewDialog(
            Window parent,
            Invoice hd,
            Customer kh,
            String staffName,
            List<InvoiceDetail> rooms,
            List<ServiceDetail> services,
            InvoicesBUS invoicesBUS
    ) {
        super(parent, "Xem trước hóa đơn", ModalityType.APPLICATION_MODAL);

        this.hd = hd;
        this.kh = kh;
        this.staffName = staffName;
        this.rooms = rooms == null ? new ArrayList<>() : rooms;
        this.services = services == null ? new ArrayList<>() : services;
        this.invoicesBUS = invoicesBUS;

        setSize(1200, 820);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(250, 248, 244));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(225, 220, 210)),
                BorderFactory.createEmptyBorder(18, 28, 18, 28)
        ));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel hotel = new JLabel("KQL HOTEL");
        hotel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        hotel.setForeground(new Color(196, 142, 45));

        JLabel subtitle = new JLabel("Luxury Stay • Perfect Experience");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(120, 120, 120));

        JLabel title = new JLabel("XEM TRƯỚC HÓA ĐƠN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(24, 40, 66));

        left.add(hotel);
        left.add(Box.createVerticalStrut(4));
        left.add(subtitle);
        left.add(Box.createVerticalStrut(14));
        left.add(title);

        header.add(left, BorderLayout.WEST);

        return header;
    }

    private JScrollPane createBody() {
        invoiceListPanel.setLayout(new GridLayout(0, 2, 22, 22));
        invoiceListPanel.setBackground(new Color(245, 248, 252));
        invoiceListPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        String computedStatus = invoicesBUS.getComputedStatus(hd);
        boolean laHoaDonHuy = "DaHuy".equals(computedStatus);

        for (InvoiceDetail room : rooms) {
            boolean isPaid = room.getNgayTraThucTe() != null;
            boolean canPrint = isPaid || laHoaDonHuy;

            String checkBoxText;
            if (laHoaDonHuy) {
                checkBoxText = "Chọn hóa đơn hủy phòng " + room.getMaPhong();
            } else if (isPaid) {
                checkBoxText = "Chọn hóa đơn phòng " + room.getMaPhong();
            } else {
                checkBoxText = "Phòng " + room.getMaPhong() + " chưa thanh toán";
            }

            JCheckBox cb = new JCheckBox(checkBoxText);
            cb.setSelected(canPrint);
            cb.setEnabled(canPrint);
            cb.setFont(cb.getFont().deriveFont(Font.BOLD, 13f));
            cb.setOpaque(false);

            Color statusColor;
            if (laHoaDonHuy) {
                statusColor = new Color(120, 120, 120);
            } else if (isPaid) {
                statusColor = new Color(30, 160, 90);
            } else {
                statusColor = new Color(220, 38, 38);
            }

            cb.setForeground(statusColor);

            JPanel invoicePanel = createInvoicePanel(room);

            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setBackground(new Color(255,255,255));
            wrapper.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(
                            new Color(statusColor.getRed(),
                                    statusColor.getGreen(),
                                    statusColor.getBlue(), 120), 2),
                    BorderFactory.createEmptyBorder(16,16,16,16)
            ));

            wrapper.add(cb, BorderLayout.NORTH);
            wrapper.add(invoicePanel, BorderLayout.CENTER);
            wrapper.setPreferredSize(new Dimension(420, 760));

            checkBoxes.add(cb);
            invoicePanels.add(wrapper);
            invoiceListPanel.add(wrapper);
        }

        JScrollPane scroll = new JScrollPane(invoiceListPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        return scroll;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(250,248,244));
        footer.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JButton selectAllBtn = new JButton("Chọn tất cả");
        selectAllBtn.setPreferredSize(new Dimension(130, 36));
        selectAllBtn.setFocusPainted(false);
        selectAllBtn.addActionListener(e -> {
            for (JCheckBox cb : checkBoxes) {
                if (cb.isEnabled()) {
                    cb.setSelected(true);
                }
            }
        });

        JButton unselectAllBtn = new JButton("Bỏ chọn tất cả");
        unselectAllBtn.setPreferredSize(new Dimension(140, 36));
        unselectAllBtn.setFocusPainted(false);
        unselectAllBtn.addActionListener(e -> checkBoxes.forEach(cb -> cb.setSelected(false)));

        PrimaryButton printBtn = new PrimaryButton("In hóa đơn đã chọn");
        printBtn.setPreferredSize(new Dimension(170, 36));
        printBtn.setMinimumSize(new Dimension(170, 36));
        printBtn.setMaximumSize(new Dimension(170, 36));
        printBtn.setBackground(new Color(40, 167, 69));
        printBtn.setForeground(Color.WHITE);
        printBtn.setArc(14);
        printBtn.setFocusPainted(false);
        printBtn.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        printBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        printBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                printBtn.setBackground(new Color(25, 135, 84));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                printBtn.setBackground(new Color(40, 167, 69));
            }
        });

        printBtn.addActionListener(e -> printSelectedInvoices());

        left.add(selectAllBtn);
        left.add(unselectAllBtn);
        left.add(printBtn);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);

        JButton closeBtn = new JButton("Đóng");
        closeBtn.setBackground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.setPreferredSize(new Dimension(120, 36));
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> dispose());

        right.add(closeBtn);

        footer.add(left, BorderLayout.WEST);
        footer.add(right, BorderLayout.EAST);

        return footer;
    }

    private JPanel createInvoicePanel(InvoiceDetail room) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(252,250,247));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(235,225,210)),
                BorderFactory.createEmptyBorder(22,22,22,22)
        ));

        String computedStatus = invoicesBUS.getComputedStatus(hd);
        boolean laHoaDonHuy = "DaHuy".equals(computedStatus);

        double tienCoc = 0;
        if (hd.getMaDatPhong() != null && !hd.getMaDatPhong().isBlank()) {
            tienCoc = invoicesBUS.getDepositAmount(hd.getMaDatPhong());
        }

        String title = laHoaDonHuy
                ? "HÓA ĐƠN HỦY PHÒNG " + room.getMaPhong()
                : "HÓA ĐƠN THANH TOÁN PHÒNG " + room.getMaPhong();

        String ngayThanhToanText = laHoaDonHuy
                ? "Đã hủy"
                : room.getNgayTraThucTe() == null
                  ? "Chưa thanh toán"
                  : room.getNgayTraThucTe().format(DTF);

        String ngayTraThucTeText = laHoaDonHuy
                ? "Đã hủy"
                : room.getNgayTraThucTe() == null
                  ? "Chưa trả"
                  : room.getNgayTraThucTe().format(DTF);

        String soDemText = laHoaDonHuy
                ? "Đã hủy"
                : room.getSoDem() + " đêm";

        double donGiaPhong = 0;
        double tienPhong = 0;
        double phuThuHienThi = 0;
        double phiPhatHienThi = 0;

        if (!laHoaDonHuy) {
            phuThuHienThi = Math.max(0, room.getPhuThu());
            phiPhatHienThi = Math.max(0, room.getPhiPhat());

            tienPhong = Math.max(
                    0,
                    room.getThanhTien() - phuThuHienThi - phiPhatHienThi
            );

            donGiaPhong = room.getSoDem() > 0 ? tienPhong / room.getSoDem() : tienPhong;
        }

        double tongTienPhongHienThi = laHoaDonHuy ? 0 : Math.max(0, tienPhong);

        List<ServiceDetail> roomServices = getServicesForRoom(room.getMaPhong());

        double tongTienDichVuHienThi = laHoaDonHuy
                ? 0
                : roomServices.stream()
                  .mapToDouble(ServiceDetail::getThanhTien)
                  .sum();

        double tongTinhThueHienThi = tongTienPhongHienThi
                + phuThuHienThi
                + phiPhatHienThi
                + tongTienDichVuHienThi;

        double tienThueHienThi = laHoaDonHuy
                ? 0
                : tongTinhThueHienThi * 0.10;

        double tongTruocGiamHienThi = tongTinhThueHienThi + tienThueHienThi;

        double tyLeGiamHangThanhVien = laHoaDonHuy ? 0 : getMembershipDiscountRate(kh);
        String tenHangThanhVien = laHoaDonHuy ? "Đồng" : getMembershipRankName(kh);

// Tổng trước giảm của cả hóa đơn
        double tongTruocGiamHoaDon = Math.max(
                0,
                hd.getTienPhong() + hd.getTienDichVu() + hd.getTienThue()
        );

// Tổng khuyến mãi của cả hóa đơn
        double tongKhuyenMaiHoaDon = laHoaDonHuy
                ? 0
                : Math.max(0, hd.getTienKhuyenMai());

// Tách phần khuyến mãi mã ra khỏi tổng khuyến mãi hóa đơn.
// Lý do: hd.getTienKhuyenMai() đang là tổng = khuyến mãi mã + khuyến mãi hạng.
        double tongKhuyenMaiMaHoaDon = calculatePromotionOnlyDiscount(
                tongTruocGiamHoaDon,
                tongKhuyenMaiHoaDon,
                tyLeGiamHangThanhVien
        );

// Áp dụng khuyến mãi mã trực tiếp cho phòng đang in,
// giống màn Trả phòng, không chia tỷ lệ.
        double tienKhuyenMaiMaHienThi = Math.min(
                tongKhuyenMaiMaHoaDon,
                tongTruocGiamHienThi
        );

// Khuyến mãi hạng khách hàng tính sau khi trừ khuyến mãi mã.
        double tienKhuyenMaiHangHienThi = Math.max(
                0,
                (tongTruocGiamHienThi - tienKhuyenMaiMaHienThi) * tyLeGiamHangThanhVien
        );

// Tổng thanh toán riêng phòng, chưa trừ cọc.
        double tongThanhToanHienThi = laHoaDonHuy
                ? Math.max(0, tienCoc)
                : Math.max(
                0,
                tongTruocGiamHienThi
                - tienKhuyenMaiMaHienThi
                - tienKhuyenMaiHangHienThi
        );

        JLabel logo = centerLabel("KQL HOTEL", 28, true);
        logo.setForeground(new Color(196,142,45));
        panel.add(logo);

        JLabel slogan = centerLabel("Luxury Stay • Perfect Experience", 12, false);
        slogan.setForeground(new Color(120,120,120));
        panel.add(slogan);

        panel.add(Box.createVerticalStrut(10));

        JLabel invoiceTitle = centerLabel(title, 18, true);
        invoiceTitle.setForeground(new Color(24,40,66));
        panel.add(invoiceTitle);

        panel.add(Box.createVerticalStrut(10));

        panel.add(line("Mã hóa đơn", hd.getMaHD()));
        panel.add(line("Ngày lập", DateUtils.format(hd.getNgayLapHD())));
        panel.add(line("Ngày thanh toán", ngayThanhToanText));
        panel.add(line("Khách hàng", kh != null ? kh.getHoTenKH() : hd.getMaKhachHang()));
        panel.add(line("Số điện thoại", kh != null ? kh.getSdt() : ""));
        panel.add(line("Nhân viên", staffName != null ? staffName : hd.getMaNhanVien()));
        panel.add(line("Mã đặt phòng", hd.getMaDatPhong() == null ? "" : hd.getMaDatPhong()));
        panel.add(Box.createVerticalStrut(8));

        panel.add(sectionTitle("THÔNG TIN PHÒNG"));
        panel.add(line("Mã phòng", room.getMaPhong()));
        panel.add(line("Ngày nhận", room.getNgayNhanPhong() == null ? "" : room.getNgayNhanPhong().format(DTF)));
        panel.add(line("Ngày trả dự kiến", room.getNgayTraPhong() == null ? "" : room.getNgayTraPhong().format(DTF)));
        panel.add(line("Ngày trả thực tế", ngayTraThucTeText));
        panel.add(line("Số đêm", soDemText));
        panel.add(line("Đơn giá phòng", CurrencyUtils.formatVND(donGiaPhong)));
        if (phuThuHienThi > 0) {
            panel.add(line("Phụ thu", CurrencyUtils.formatVND(phuThuHienThi)));
        }

        if (phiPhatHienThi > 0) {
            panel.add(line(getCheckoutFeeLabel(room), CurrencyUtils.formatVND(phiPhatHienThi)));
        }

        panel.add(sectionTitle("DỊCH VỤ PHÁT SINH"));

        if (laHoaDonHuy) {
            panel.add(line("Dịch vụ", "Đã hủy"));
        } else if (roomServices.isEmpty()) {
            panel.add(line("Dịch vụ", "Không có"));
        } else {
            for (ServiceDetail sd : roomServices) {
                panel.add(line(
                        getServiceDisplayName(sd) + " x" + sd.getSoLuong(),
                        CurrencyUtils.formatVND(sd.getThanhTien())
                ));
            }
        }

        panel.add(sectionTitle("TỔNG TIỀN HÓA ĐƠN"));
        panel.add(line("Tổng tiền phòng", CurrencyUtils.formatVND(tongTienPhongHienThi)));
        panel.add(line("Tổng tiền dịch vụ", CurrencyUtils.formatVND(tongTienDichVuHienThi)));
        panel.add(line("Thuế VAT", CurrencyUtils.formatVND(tienThueHienThi)));

        if (tienKhuyenMaiMaHienThi > 0) {
            panel.add(line(
                    "Khuyến mãi",
                    "-" + CurrencyUtils.formatVND(tienKhuyenMaiMaHienThi)
            ));
        }

        if (tienKhuyenMaiHangHienThi > 0) {
            panel.add(line(
                    "Khuyến mãi hạng " + tenHangThanhVien + " (" + formatPercent(tyLeGiamHangThanhVien) + ")",
                    "-" + CurrencyUtils.formatVND(tienKhuyenMaiHangHienThi)
            ));
        }

        if (laHoaDonHuy) {
            panel.add(line("Phí hủy (giữ cọc)", CurrencyUtils.formatVND(tienCoc)));
        }

        panel.add(line("Tổng thanh toán (Phạt)", CurrencyUtils.formatVND(tongThanhToanHienThi)));

        if (!laHoaDonHuy) {
            double refund = invoicesBUS.getRefundAmount(hd.getMaHD());
            if (refund > 0) {
                panel.add(line("Tiền hoàn trả cho khách", "+" + CurrencyUtils.formatVND(refund)));
            }
        }

        panel.add(Box.createVerticalStrut(12));
        panel.add(centerLabel("Cảm ơn quý khách đã sử dụng dịch vụ!", 13, false));

        return panel;
    }

    private JLabel centerLabel(String text, int size, boolean bold) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setFont(label.getFont().deriveFont(bold ? Font.BOLD : Font.PLAIN, (float) size));
        label.setForeground(new Color(24,40,66));
        label.setFont(new Font("Segoe UI",
                bold ? Font.BOLD : Font.PLAIN,
                size));
        return label;
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setHorizontalAlignment(SwingConstants.CENTER);

        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(196, 142, 45));

        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(
                        0, 0, 2, 0,
                        new Color(196, 142, 45)
                ),
                BorderFactory.createEmptyBorder(12, 20, 6, 20)
        ));

        label.setMaximumSize(new Dimension(220, 40));
        label.setPreferredSize(new Dimension(220, 40));

        return label;
    }

    private JPanel line(String left, String right) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(6, 2, 6, 2));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        JLabel l = new JLabel(left);
        l.setForeground(new Color(90, 100, 120));
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JLabel r = new JLabel(right == null ? "" : right);
        r.setForeground(new Color(24, 40, 66));
        r.setFont(new Font("Segoe UI", Font.BOLD, 13));

        row.add(l, BorderLayout.WEST);
        row.add(r, BorderLayout.EAST);

        return row;
    }

    private void printSelectedInvoices() {
        List<JPanel> selected = new ArrayList<>();

        for (int i = 0; i < checkBoxes.size(); i++) {
            if (checkBoxes.get(i).isSelected()) {
                selected.add(invoicePanels.get(i));
            }
        }

        if (selected.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất 1 hóa đơn để in!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("In hóa đơn " + hd.getMaHD());

        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex >= selected.size()) {
                return Printable.NO_SUCH_PAGE;
            }

            Graphics2D g2 = (Graphics2D) graphics;
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            JPanel panel = selected.get(pageIndex);

            double scaleX = pageFormat.getImageableWidth() / panel.getWidth();
            double scaleY = pageFormat.getImageableHeight() / panel.getHeight();
            double scale = Math.min(scaleX, scaleY);

            if (scale > 1) {
                scale = 1;
            }

            g2.scale(scale, scale);
            panel.printAll(g2);

            return Printable.PAGE_EXISTS;
        });

        if (job.printDialog()) {
            try {
                job.print();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "In hóa đơn thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
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

    private List<ServiceDetail> getServicesForRoom(String maPhong) {
        List<ServiceDetail> result = new ArrayList<>();

        if (maPhong == null || maPhong.isBlank()) {
            return result;
        }

        for (ServiceDetail sd : services) {
            if (isServiceForRoom(sd, maPhong)) {
                result.add(sd);
            }
        }

        return result;
    }

    private boolean isServiceForRoom(ServiceDetail sd, String maPhong) {
        if (sd == null || maPhong == null || maPhong.isBlank()) {
            return false;
        }

        String ghiChu = sd.getGhiChu();

        if (ghiChu == null || ghiChu.isBlank()) {
            return false;
        }

        if (!ghiChu.startsWith("ROOM:")) {
            return false;
        }

        int pipeIndex = ghiChu.indexOf("|");

        if (pipeIndex <= 5) {
            return false;
        }

        String serviceRoom = ghiChu.substring(5, pipeIndex).trim();

        return maPhong.equals(serviceRoom);
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

        if (ghiChu != null && !ghiChu.isBlank()) {
            return ghiChu;
        }

        return sd.getMaDV();
    }

    private String getCheckoutFeeLabel(InvoiceDetail room) {
        if (room == null || room.getNgayTraThucTe() == null || room.getNgayTraPhong() == null) {
            return "Phí/phạt trả phòng";
        }

        if (room.getNgayTraThucTe().toLocalDate().isBefore(room.getNgayTraPhong().toLocalDate())) {
            return "Phí trả phòng sớm";
        }

        return "Phạt trả trễ";
    }
}
