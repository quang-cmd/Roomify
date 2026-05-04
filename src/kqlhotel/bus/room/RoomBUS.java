package kqlhotel.bus.room;

import kqlhotel.dao.room.RoomDAO;
import kqlhotel.dao.invoice.InvoiceDAO;
import kqlhotel.dao.customer.CustomerDAO;
import kqlhotel.entity.Room;
import kqlhotel.entity.Invoice;
import kqlhotel.entity.Customer;

import java.util.List;

public class RoomBUS {
    private RoomDAO roomDAO;

    public RoomBUS() {
        roomDAO = new RoomDAO();
    }

    public List<Room> getAllRooms() {
        return roomDAO.getAll();
    }

    public List<Room> searchRooms(String roomId, String roomTypeName, String guiStatus) {
        String dbStatus = mapGuiStatusToDbStatus(guiStatus);
        return roomDAO.search(roomId, roomTypeName, dbStatus);
    }
    
    public String mapGuiStatusToDbStatus(String guiStatus) {
        if (guiStatus == null) return null;
        switch (guiStatus) {
            case "Vacant": return "Trong";
            case "Occupied": return "DangSuDung";
            case "Maintenance": return "BaoTri";
            default: return guiStatus;
        }
    }

    public String mapDbStatusToGuiStatus(String dbStatus) {
        if (dbStatus == null) return "Unknown";
        switch (dbStatus) {
            case "Trong":      return "Vacant";
            case "DangSuDung": return "Occupied";
            case "BaoTri":     return "Maintenance";
            default: return dbStatus;
        }
    }

    public long countByStatus(List<Room> list, String dbStatus) {
        if (list == null) return 0;
        return list.stream().filter(p -> dbStatus.equals(p.getStatus())).count();
    }

    public boolean addRoom(Room p) {
        if (p.getRoomId() == null || p.getRoomId().trim().isEmpty()) return false;
        if (p.getStatus() == null) p.setStatus("Trong");
        return roomDAO.create(p);
    }

    public boolean updateRoom(Room p) {
        if (p.getRoomId() == null || p.getRoomId().trim().isEmpty()) return false;
        return roomDAO.update(p);
    }
}
