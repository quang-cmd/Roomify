package kqlhotel.bus.room;

import kqlhotel.dao.room.RoomDAO;
import kqlhotel.entity.Room;
import java.util.List;

public class RoomBUS {
    private RoomDAO roomDAO;

    public RoomBUS() {
        roomDAO = new RoomDAO();
    }

    public List<Room> getAll() {
        return roomDAO.getAll();
    }
    
    public List<Room> getAllRooms() {
        return roomDAO.getAll();
    }

    public List<Room> searchRooms(String roomId, String typeName, String guiStatus) {
        String dbStatus = mapGuiStatusToDbStatus(guiStatus);
        return roomDAO.search(roomId, typeName, dbStatus);
    }

    public boolean updateStatus(String roomId, String status) {
        return roomDAO.updateStatus(roomId, status);
    }

    public boolean insertRoom(Room r) {
        return roomDAO.create(r);
    }

    public boolean addRoom(Room r) {
        return roomDAO.create(r);
    }

    public boolean updateRoom(Room r) {
        return roomDAO.update(r);
    }

    public boolean deleteRoom(String id) {
        return roomDAO.delete(id);
    }

    public String mapGuiStatusToDbStatus(String guiStatus) {
        if (guiStatus == null || guiStatus.equals("Tất cả trạng thái")) return null;
        switch (guiStatus) {
            case "Trống": return "Trong";
            case "Đang sử dụng": return "DangSuDung";
            case "Đang sửa chữa": return "DangSuaChua";
            case "Đã đặt": return "DaDat";
            default: return null;
        }
    }

    public String mapDbStatusToGuiStatus(String dbStatus) {
        if (dbStatus == null) return "Trống";
        switch (dbStatus) {
            case "Trong": return "Trống";
            case "DangSuDung": return "Đang sử dụng";
            case "DangSuaChua": return "Đang sửa chữa";
            case "DaDat": return "Đã đặt";
            default: return "Trống";
        }
    }

    public long countByStatus(List<Room> rooms, String status) {
        if (rooms == null) return 0;
        return rooms.stream()
                .filter(r -> status.equalsIgnoreCase(r.getTrangThaiPhong()))
                .count();
    }
}
