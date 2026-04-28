package kqlhotel.bus.staff;

import kqlhotel.dao.Staff.StaffDAO;
import kqlhotel.dao.account.AccountDAO;
import kqlhotel.entity.Staff;
import java.util.List;

public class StaffBUS {
    private StaffDAO staffDAO;
    private AccountDAO accountDAO;

    public StaffBUS() {
        staffDAO = new StaffDAO();
        accountDAO = new AccountDAO();
    }

    public List<Staff> getAll() {
        return staffDAO.getAll();
    }

    // Alias for compatibility
    public void them(Staff s) {
        addStaff(s);
    }

    // Alias for compatibility
    public void xoa(String maNV) {
        staffDAO.delete(maNV);
    }

    /**
     * Cập nhật nhân viên: update TaiKhoan trước, sau đó update NhanVien.
     */
    public boolean updateStaff(Staff staff) {
        boolean accountOk = accountDAO.update(staff.getAccount());
        if (!accountOk) {
            return false;
        }
        return staffDAO.update(staff);
    }

    /**
     * Thêm nhân viên mới: insert TaiKhoan trước, sau đó insert NhanVien.
     */
    public boolean addStaff(Staff staff) {
        boolean accountOk = accountDAO.insert(staff.getAccount());
        if (!accountOk) {
            return false;
        }
        return staffDAO.insert(staff);
    }
}
