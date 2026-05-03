package kqlhotel.gui.dialog;

import kqlhotel.bus.Invoice.InvoicesBUS;
import kqlhotel.entity.Customer;
import kqlhotel.entity.Invoice;
import kqlhotel.entity.InvoiceDetail;
import kqlhotel.entity.ServiceDetail;
import kqlhotel.utils.CurrencyUtils;
import kqlhotel.utils.DateUtils;

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

        setSize(1000, 740);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(16, 24, 12, 24));
        header.setBackground(Color.WHITE);

        JLabel title = new JLabel("Xem trước hóa đơn chi tiết từng phòng");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setForeground(new Color(24, 40, 66));

        header.add(title, BorderLayout.WEST);
        return header;
    }

    private JScrollPane createBody() {
        invoiceListPanel.setLayout(new GridLayout(0, 2, 18, 18));
        invoiceListPanel.setBackground(new Color(245, 248, 252));
        invoiceListPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        for (InvoiceDetail room : rooms) {
            boolean isPaid = room.getNgayTraThucTe() != null;

            JCheckBox cb = new JCheckBox(isPaid
                    ? "Chọn hóa đơn phòng " + room.getMaPhong()
                    : "Phòng " + room.getMaPhong() + " chưa thanh toán");

            cb.setSelected(isPaid);
            cb.setEnabled(isPaid);
            cb.setFont(cb.getFont().deriveFont(Font.BOLD, 13f));
            cb.setOpaque(false);
            cb.setForeground(isPaid ? new Color(30, 160, 90) : new Color(220, 38, 38));

            JPanel invoicePanel = createInvoicePanel(room);

            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setBackground(Color.WHITE);
            wrapper.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(
                            isPaid ? new Color(30, 180, 120) : new Color(220, 38, 38),
                            2
                    ),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));

            wrapper.add(cb, BorderLayout.NORTH);
            wrapper.add(invoicePanel, BorderLayout.CENTER);

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
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JButton selectAllBtn = new JButton("Chọn tất cả");
        selectAllBtn.setPreferredSize(new Dimension(130, 36));
        selectAllBtn.addActionListener(e -> {
            for (JCheckBox cb : checkBoxes) {
                if (cb.isEnabled()) {
                    cb.setSelected(true);
                }
            }
        });

        JButton unselectAllBtn = new JButton("Bỏ chọn tất cả");
        unselectAllBtn.setPreferredSize(new Dimension(140, 36));
        unselectAllBtn.addActionListener(e -> checkBoxes.forEach(cb -> cb.setSelected(false)));

        JButton printBtn = new JButton("In hóa đơn đã chọn");
        printBtn.setPreferredSize(new Dimension(170, 36));
        printBtn.setBackground(new Color(40, 167, 69));
        printBtn.setForeground(Color.WHITE);
        printBtn.addActionListener(e -> printSelectedInvoices());

        left.add(selectAllBtn);
        left.add(unselectAllBtn);
        left.add(printBtn);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);

        JButton closeBtn = new JButton("Đóng");
        closeBtn.setPreferredSize(new Dimension(120, 36));
        closeBtn.addActionListener(e -> dispose());
        right.add(closeBtn);

        footer.add(left, BorderLayout.WEST);
        footer.add(right, BorderLayout.EAST);

        return footer;
    }

    private JPanel createInvoicePanel(InvoiceDetail room) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        panel.add(centerLabel("KQL HOTEL", 24, true));
        panel.add(centerLabel("HÓA ĐƠN THANH TOÁN PHÒNG " + room.getMaPhong(), 16, true));
        panel.add(Box.createVerticalStrut(10));

        panel.add(line("Mã hóa đơn", hd.getMaHD()));
        panel.add(line("Ngày lập", DateUtils.format(hd.getNgayLapHD())));
        panel.add(line("Ngày thanh toán", hd.getNgayThanhToan() == null ? "Chưa thanh toán" : DateUtils.format(hd.getNgayThanhToan())));
        panel.add(line("Khách hàng", kh != null ? kh.getHoTenKH() : hd.getMaKhachHang()));
        panel.add(line("Số điện thoại", kh != null ? kh.getSdt() : ""));
        panel.add(line("Nhân viên", staffName != null ? staffName : hd.getMaNhanVien()));
        panel.add(line("Mã đặt phòng", hd.getMaDatPhong() == null ? "" : hd.getMaDatPhong()));
        panel.add(Box.createVerticalStrut(8));

        panel.add(sectionTitle("THÔNG TIN PHÒNG"));
        panel.add(line("Mã phòng", room.getMaPhong()));
        panel.add(line("Ngày nhận", room.getNgayNhanPhong() == null ? "" : room.getNgayNhanPhong().format(DTF)));
        panel.add(line("Ngày trả dự kiến", room.getNgayTraPhong() == null ? "" : room.getNgayTraPhong().format(DTF)));
        panel.add(line("Ngày trả thực tế", room.getNgayTraThucTe() == null ? "Chưa trả" : room.getNgayTraThucTe().format(DTF)));
        panel.add(line("Số đêm", room.getSoDem() + " đêm"));

        double donGiaPhong = room.getSoDem() > 0 ? room.getThanhTien() / room.getSoDem() : room.getThanhTien();
        panel.add(line("Đơn giá phòng", CurrencyUtils.formatVND(donGiaPhong)));
        panel.add(line("Tiền phòng", CurrencyUtils.formatVND(room.getThanhTien())));

        panel.add(sectionTitle("DỊCH VỤ PHÁT SINH"));
        if (services.isEmpty()) {
            panel.add(line("Dịch vụ", "Không có"));
        } else {
            for (ServiceDetail dv : services) {
                String tenDV = invoicesBUS.getServiceName(dv.getMaDV());
                String name = dv.getMaDV() + (tenDV == null || tenDV.isBlank() ? "" : " - " + tenDV);
                panel.add(line(name, dv.getSoLuong() + " x " + CurrencyUtils.formatVND(dv.getDonGia()) + " = " + CurrencyUtils.formatVND(dv.getThanhTien())));
            }
        }

        panel.add(sectionTitle("TỔNG TIỀN HÓA ĐƠN"));
        panel.add(line("Tổng tiền phòng", CurrencyUtils.formatVND(hd.getTienPhong())));
        panel.add(line("Tổng tiền dịch vụ", CurrencyUtils.formatVND(hd.getTienDichVu())));
        panel.add(line("Thuế VAT", CurrencyUtils.formatVND(hd.getTienThue())));
        panel.add(line("Khuyến mãi", "-" + CurrencyUtils.formatVND(hd.getTienKhuyenMai())));
        panel.add(line("Tổng thanh toán", CurrencyUtils.formatVND(hd.getTongTienThanhToan())));

        panel.add(Box.createVerticalStrut(12));
        panel.add(centerLabel("Cảm ơn quý khách đã sử dụng dịch vụ!", 13, false));

        return panel;
    }

    private JLabel centerLabel(String text, int size, boolean bold) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setFont(label.getFont().deriveFont(bold ? Font.BOLD : Font.PLAIN, (float) size));
        label.setForeground(new Color(24, 40, 66));
        return label;
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        label.setForeground(new Color(49, 106, 210));
        label.setBorder(BorderFactory.createEmptyBorder(14, 0, 8, 0));
        return label;
    }

    private JPanel line(String left, String right) {
        JPanel row = new JPanel(new GridLayout(1, 2));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));

        JLabel l = new JLabel(left);
        l.setForeground(new Color(80, 95, 115));
        l.setFont(l.getFont().deriveFont(13f));

        JLabel r = new JLabel(right == null ? "" : right);
        r.setForeground(new Color(24, 40, 66));
        r.setFont(r.getFont().deriveFont(Font.BOLD, 13f));
        r.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(l);
        row.add(r);

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
}
