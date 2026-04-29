package kqlhotel.bus.invoice;

import kqlhotel.dao.customer.CustomerDAO;
import kqlhotel.dao.invoice.InvoiceDAO;
import kqlhotel.dao.invoice.InvoiceDetailDAO;
import kqlhotel.dao.invoice.ServiceDetailDAO;
import kqlhotel.entity.Customer;
import kqlhotel.entity.Invoice;
import kqlhotel.entity.InvoiceDetail;
import kqlhotel.entity.ServiceDetail;

import java.time.LocalDateTime;
import java.util.List;

public class InvoicesBUS {
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final InvoiceDetailDAO invoiceDetailDAO = new InvoiceDetailDAO();
    private final ServiceDetailDAO serviceDetailDAO = new ServiceDetailDAO();

    public List<Invoice> getAllInvoices() {
        return invoiceDAO.getAll();
    }

    public List<Invoice> filterInvoices(LocalDateTime start, LocalDateTime end, String customer, String status) {
        return invoiceDAO.searchInvoices(start, end, customer, status);
    }

    public Customer getCustomerInfo(String maKH) {
        return customerDAO.getById(maKH);
    }

    public List<InvoiceDetail> getRoomDetails(String maHD) {
        List<InvoiceDetail> details = invoiceDetailDAO.getByInvoice(maHD);

        if (details != null && !details.isEmpty()) {
            return details;
        }

        Invoice hd = invoiceDAO.getById(maHD);
        if (hd == null || hd.getMaDatPhong() == null || hd.getMaDatPhong().isBlank()) {
            return details;
        }

        return invoiceDetailDAO.getByBooking(hd.getMaHD(), hd.getMaDatPhong());
    }

    public List<ServiceDetail> getServiceDetails(String maHD) {
        return serviceDetailDAO.getByInvoice(maHD);
    }

    public double getDepositAmount(String maDatPhong) {
        return invoiceDAO.getDepositAmount(maDatPhong);
    }

    public String getComputedStatus(Invoice hd) {
        List<InvoiceDetail> roomDetails = getRoomDetails(hd.getMaHD());
        if (roomDetails == null || roomDetails.isEmpty()) {
            return "ChuaThanhToan";
        }

        int paid = 0;
        for (InvoiceDetail ct : roomDetails) {
            if (ct.getNgayTraThucTe() != null) {
                paid++;
            }
        }

        if (paid == 0) {
            return "ChuaThanhToan";
        }
        if (paid == roomDetails.size()) {
            return "DaThanhToan";
        }
        return "DangThanhToan";
    }

    public String getInvoiceSummary() {
        List<Invoice> allInvoices = getAllInvoices();
        int total = allInvoices.size();
        int fullyPaid = 0;
        int partialPaid = 0;
        int unpaid = 0;

        for (Invoice hd : allInvoices) {
            String status = getComputedStatus(hd);
            if ("DaThanhToan".equals(status)) {
                fullyPaid++;
            } else if ("DangThanhToan".equals(status)) {
                partialPaid++;
            } else {
                unpaid++;
            }
        }

        return String.format("%d hóa đơn · %d đã thanh toán · %d đang thanh toán · %d chưa thanh toán",
                total, fullyPaid, partialPaid, unpaid);
    }

    public boolean confirmPayment(String maHD) {
        boolean updatedDetails = invoiceDetailDAO.markAllRemainingRoomsCheckedOut(maHD, LocalDateTime.now());
        boolean updatedInvoice = invoiceDAO.updateStatus(maHD, "DaThanhToan");
        return updatedDetails && updatedInvoice;
    }
    public String getStaffName(String maNV) {
        return invoiceDAO.getStaffName(maNV);
    }
    public String getServiceName(String maDV) {
        return invoiceDAO.getServiceName(maDV);
    }
}