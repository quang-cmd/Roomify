package kqlhotel.bus;

import kqlhotel.dao.KhachHangDao;
import kqlhotel.entity.KhachHangEntity;

import java.util.List;

public class KhachHangBus {

    private KhachHangDao dao;

    public KhachHangBus() {
        dao = new KhachHangDao();
    }

    public List<KhachHangEntity> getAll() {
        return dao.getAll();
    }

    public boolean add(KhachHangEntity kh) {
        return dao.add(kh);
    }

    public boolean update(KhachHangEntity kh) {
        return dao.update(kh);
    }

    public boolean delete(String maKH) {
        return dao.delete(maKH);
    }

    public KhachHangEntity getById(String maKH) {
        return dao.getById(maKH);
    }
}