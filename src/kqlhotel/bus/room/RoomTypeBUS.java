package kqlhotel.bus.room;

import kqlhotel.dao.room.RoomTypeDAO;
import kqlhotel.entity.RoomType;
import java.util.List;

public class RoomTypeBUS {
    private RoomTypeDAO roomTypeDAO;

    public RoomTypeBUS() {
        roomTypeDAO = new RoomTypeDAO();
    }

    public List<RoomType> getAll() {
        return roomTypeDAO.getAll();
    }
}
