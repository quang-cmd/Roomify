package kqlhotel.bus;

import kqlhotel.dao.DoiPhongDao;
import java.util.List;

public class DoiPhongBus {

    private DoiPhongDao dao;

    public DoiPhongBus() {
        dao = new DoiPhongDao();
    }

    public String getCurrentRoom(String maDatPhong) {
        return dao.getCurrentRoom(maDatPhong);
    }

    public List<String> getAvailableRooms() {
        return dao.getAvailableRooms();
    }

    public boolean changeRoom(String maDatPhong, String newRoom) {
        return dao.changeRoom(maDatPhong, newRoom);
    }
}