package kqlhotel.bus;

import kqlhotel.dao.DichVuDao;
import kqlhotel.entity.DichVuEntity;

import java.util.List;

public class DichVuBus {

    private DichVuDao dichVuDao = new DichVuDao();

    public List<DichVuEntity> getAll() {
        return dichVuDao.getAll();
    }
}