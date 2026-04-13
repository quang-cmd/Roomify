package kqlhotel.bus;

import kqlhotel.dao.DoiPhongDao;
import kqlhotel.entity.PhongEntity;

import java.sql.Connection;
import java.util.List;

public class DoiPhongBus {

    private DoiPhongDao dao;

    public DoiPhongBus(Connection conn) {
        dao = new DoiPhongDao(conn);
    }

    public List<PhongEntity> layDanhSachPhong() {
        return dao.getDanhSachPhong();
    }

    public void doiPhong(String phongCu, String phongMoi) {
        if (phongCu.equals(phongMoi)) {
            throw new IllegalArgumentException("Không thể đổi cùng phòng");
        }

        dao.doiPhong(phongCu, phongMoi);
    }
}