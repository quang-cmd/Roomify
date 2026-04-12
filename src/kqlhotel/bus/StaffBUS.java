package kqlhotel.bus;

import kqlhotel.dao.Staff.StaffDAO;
import kqlhotel.entity.Staff;
import java.util.List;

public class StaffBUS {
    private StaffDAO staffDAO;

    public StaffBUS() {
        staffDAO = new StaffDAO();
    }

    public List<Staff> getAll() {
        return staffDAO.getAll();
    }

    public boolean insert(Staff staff) {
        return staffDAO.insert(staff);
    }

    public boolean update(Staff staff) {
        return staffDAO.update(staff);
    }
}
