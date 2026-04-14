package kqlhotel.bus.staff;

import kqlhotel.dao.Staff.StaffDAO;
import kqlhotel.entity.Staff;
import java.util.List;

public class StaffBUS {

    private StaffDAO dao = new StaffDAO();

    public List<Staff> getAll() {
        return dao.getAll();
    }

    public void them(Staff s) {
        dao.insert(s);
    }

    public void xoa(String maNV) {
        dao.delete(maNV);
    }
}