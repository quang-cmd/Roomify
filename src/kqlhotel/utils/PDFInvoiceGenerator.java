package kqlhotel.utils;

import kqlhotel.entity.Invoice;
import kqlhotel.entity.InvoiceDetail;
import kqlhotel.entity.ServiceDetail;
import java.awt.*;
import java.awt.print.*;
import java.util.List;

public class PDFInvoiceGenerator {
    public static void exportInvoice(Invoice hd, List<InvoiceDetail> rooms, List<ServiceDetail> services) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                if (pageIndex > 0) return NO_SUCH_PAGE;

                Graphics2D g2 = (Graphics2D) graphics;
                g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                g2.setFont(new Font("Serif", Font.BOLD, 18));
                
                int y = 50;
                g2.drawString("HÓA ĐƠN THANH TOÁN - KQL HOTEL", 100, y);
                
                g2.setFont(new Font("Serif", Font.PLAIN, 12));
                y += 30;
                g2.drawString("Mã hóa đơn: " + hd.getMaHD(), 50, y);
                g2.drawString("Ngày lập: " + DateUtils.format(hd.getNgayLapHD()), 300, y);
                
                y += 20;
                g2.drawString("Khách hàng: " + hd.getMaKhachHang(), 50, y);
                
                y += 30;
                g2.setFont(new Font("Serif", Font.BOLD, 12));
                g2.drawString("Chi tiết tiền phòng:", 50, y);
                g2.setFont(new Font("Serif", Font.PLAIN, 12));
                for (InvoiceDetail ct : rooms) {
                    y += 20;
                    g2.drawString(ct.getMaPhong() + " - " + ct.getSoDem() + " đêm", 70, y);
                    g2.drawString(CurrencyUtils.formatVND(ct.getThanhTien()), 400, y);
                }

                y += 30;
                g2.setFont(new Font("Serif", Font.BOLD, 12));
                g2.drawString("Chi tiết dịch vụ:", 50, y);
                g2.setFont(new Font("Serif", Font.PLAIN, 12));
                for (ServiceDetail ct : services) {
                    y += 20;
                    g2.drawString(ct.getMaDV() + " x" + ct.getSoLuong(), 70, y);
                    g2.drawString(CurrencyUtils.formatVND(ct.getThanhTien()), 400, y);
                }

                y += 40;
                g2.drawLine(50, y, 500, y);
                y += 20;
                g2.setFont(new Font("Serif", Font.BOLD, 14));
                g2.drawString("TỔNG CỘNG:", 50, y);
                g2.drawString(CurrencyUtils.formatVND(hd.getTongTienThanhToan()), 400, y);

                return PAGE_EXISTS;
            }
        });

        // This will open the system print dialog. 
        // User can select "Microsoft Print to PDF" to save it.
        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException e) {
                e.printStackTrace();
            }
        }
    }
}
