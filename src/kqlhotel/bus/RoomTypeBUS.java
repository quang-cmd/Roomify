package kqlhotel.bus;

import kqlhotel.dao.RoomTypeDAO;
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
