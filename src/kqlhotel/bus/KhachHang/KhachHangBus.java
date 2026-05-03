package kqlhotel.bus.khachhang;

import java.util.List;
import kqlhotel.dao.KhachHangDao;
import kqlhotel.entity.KhachHangBookingHistory;
import kqlhotel.entity.KhachHangEntity;

public class KhachHangBus {

    private final KhachHangDao dao = new KhachHangDao();

    public List<KhachHangEntity> getAll() {
        return dao.getAll();
    }

    public List<KhachHangBookingHistory> getBookingHistory(String maKH) {
        return dao.getBookingHistory(maKH);
    }

    public boolean insert(KhachHangEntity kh) {
        return dao.insert(kh);
    }

    public boolean update(KhachHangEntity kh) {
        return dao.update(kh);
    }
}
