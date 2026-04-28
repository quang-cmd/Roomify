package kqlhotel.bus.Invoice;

import kqlhotel.dao.invoice.InvoiceDAO;
import kqlhotel.entity.Invoice;
import java.util.List;

public class InvoiceBUS {
    private InvoiceDAO invoiceDAO;

    public InvoiceBUS() {
        invoiceDAO = new InvoiceDAO();
    }

    public List<Invoice> getAll() {
        return invoiceDAO.getAll();
    }

    public Invoice getById(String invoiceId) {
        return invoiceDAO.getById(invoiceId);
    }

    public boolean cancelInvoice(String invoiceId) {
        return invoiceDAO.updateStatus(invoiceId, "DaHuy");
    }
}
