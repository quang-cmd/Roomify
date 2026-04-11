package kqlhotel.bus;

import kqlhotel.dao.*;
import kqlhotel.entity.*;
import java.time.LocalDateTime;
import java.util.List;

public class InvoicesBUS {
    private InvoiceDAO invoiceDAO = new InvoiceDAO();
    private CustomerDAO customerDAO = new CustomerDAO();
    private InvoiceDetailDAO invoiceDetailDAO = new InvoiceDetailDAO();
    private ServiceDetailDAO serviceDetailDAO = new ServiceDetailDAO();

    public List<Invoice> getAllInvoices() {
        return invoiceDAO.getAll();
    }

    public List<Invoice> filterInvoices(LocalDateTime start, LocalDateTime end, String customer, String status) {
        return invoiceDAO.searchInvoices(start, end, customer, status);
    }

    public String getInvoiceSummary() {
        double[] stats = invoiceDAO.getRevenueStats();
        int total = (int)(stats[1] + stats[2] + stats[3]);
        return String.format("%d hóa đơn · %d đã thanh toán · %d chưa thanh toán", 
            total, (int)stats[1], (int)stats[2]);
    }

    public Customer getCustomerInfo(String maKH) {
        return customerDAO.getById(maKH);
    }

    public List<InvoiceDetail> getRoomDetails(String maHD) {
        return invoiceDetailDAO.getByInvoice(maHD);
    }

    public List<ServiceDetail> getServiceDetails(String maHD) {
        return serviceDetailDAO.getByInvoice(maHD);
    }

    public boolean confirmPayment(String maHD) {
        return invoiceDAO.updateStatus(maHD, "DaThanhToan");
    }
}
