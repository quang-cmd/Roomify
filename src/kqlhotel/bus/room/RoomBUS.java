package kqlhotel.bus.room;

import kqlhotel.dao.room.PhongDAO;
import kqlhotel.entity.Phong;
import kqlhotel.entity.Invoice;
import kqlhotel.entity.Customer;
import kqlhotel.dao.invoice.InvoiceDAO;
import kqlhotel.dao.customer.CustomerDAO;
import java.util.List;

public class RoomBUS {
    private PhongDAO roomDAO;
    private InvoiceDAO invoiceDAO = new InvoiceDAO();
    private CustomerDAO customerDAO = new CustomerDAO();

    public RoomBUS() {
        roomDAO = new PhongDAO();
    }

    public List<Phong> getAll() {
        return roomDAO.getAll();
    }

    public List<Phong> getAllRooms() {
        return roomDAO.getAll();
    }

    public List<Phong> getAllDetailed() {
        return roomDAO.getAll();
    }

    public List<Phong> search(String query) {
        return roomDAO.search(query);
    }

    public boolean addRoom(Phong r) {
        return roomDAO.create(r);
    }

    public boolean updateRoom(Phong r) {
        return roomDAO.update(r);
    }

    public boolean deleteRoom(String id) {
        return roomDAO.delete(id);
    }

    public boolean updateStatus(String roomId, String status) {
        return roomDAO.updateStatus(roomId, status);
    }

    public long countByStatus(List<Phong> list, String status) {
        return list.stream().filter(r -> status.equalsIgnoreCase(r.getStatus())).count();
    }

    public Invoice getActiveInvoiceForRoom(String roomId) {
        List<Invoice> all = invoiceDAO.getAll();
        if (all == null) return null;
        for (Invoice inv : all) {
            if ("ChuaThanhToan".equalsIgnoreCase(inv.getTrangThai())) {
                return inv;
            }
        }
        return null;
    }

    public Customer getCustomerByMaKH(String maKH) {
        return customerDAO.getById(maKH);
    }

    public String mapDbStatusToGuiStatus(String dbStatus) {
        if (dbStatus == null) return "Trống";
        switch (dbStatus) {
            case "Trong": return "Trống";
            case "DangSuDung": return "Đang thuê";
            case "DaDat": return "Đã đặt";
            case "BaoTri": return "Bảo trì";
            default: return dbStatus;
        }
    }

    public String mapGuiStatusToDbStatus(String guiStatus) {
        if (guiStatus == null) return "Trong";
        switch (guiStatus) {
            case "Trống": return "Trong";
            case "Đang thuê": return "DangSuDung";
            case "Đã đặt": return "DaDat";
            case "Bảo trì": return "BaoTri";
            default: return guiStatus;
        }
    }
}