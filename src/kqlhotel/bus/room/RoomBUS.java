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

    public boolean updateStatus(String roomId, String status) {
        return roomDAO.updateStatus(roomId, status);
    }
}
