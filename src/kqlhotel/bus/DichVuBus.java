package kqlhotel.bus;

import java.util.List;
import kqlhotel.dao.DichVuDao;
import kqlhotel.entity.DichVuEntity;

public class DichVuBus {

    private final DichVuDao dichVuDao = new DichVuDao();

    public List<DichVuEntity> getAll() {
        return dichVuDao.getAll();
    }

    public boolean insert(DichVuEntity dichVu) {
        return dichVuDao.insert(dichVu);
    }

    public boolean update(DichVuEntity dichVu) {
        return dichVuDao.update(dichVu);
    }

    public boolean updateStatus(String maDV, String trangThai) {
        return dichVuDao.updateStatus(maDV, trangThai);
    }
}
