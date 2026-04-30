package kqlhotel.bus;

import java.util.List;
import kqlhotel.dao.DoiPhongDao;
import kqlhotel.entity.DoiPhongRoomOption;
import kqlhotel.entity.DoiPhongSearchResult;

public class DoiPhongBus {

    private final DoiPhongDao dao;

    public DoiPhongBus() {
        dao = new DoiPhongDao();
    }

    public List<DoiPhongSearchResult> searchBookings(String maDatPhong, String tenKhach, String soDienThoai, String maPhong) {
        return dao.searchBookings(maDatPhong, tenKhach, soDienThoai, maPhong);
    }

    public List<DoiPhongRoomOption> getAvailableRooms(DoiPhongSearchResult booking) {
        return dao.getAvailableRooms(booking);
    }

    public boolean changeRoom(String maChiTietDatPhong, String newRoom) {
        return dao.changeRoom(maChiTietDatPhong, newRoom);
    }
}
