package kqlhotel.bus.staff;

import kqlhotel.dao.Staff.StaffDAO;
import kqlhotel.dao.Account.AccountDAO;
import kqlhotel.entity.Staff;
import kqlhotel.entity.Account;
import java.util.ArrayList;
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

    public boolean insert(Staff staff) {
        return staffDAO.insert(staff);
    }

    public boolean update(Staff staff) {
        return staffDAO.update(staff);
    }

    /**
     * Thêm nhân viên mới: insert TaiKhoan trước, sau đó insert NhanVien.
     */
    public boolean addStaff(Staff staff) {
        // 1. Insert tài khoản vào bảng TaiKhoan
        boolean accountOk = accountDAO.insert(staff.getAccount());
        if (!accountOk) {
            System.err.println("[StaffBUS] Thêm tài khoản thất bại: " + staff.getAccount().getUsername());
            return false;
        }
        System.out.println("[StaffBUS] Đã thêm tài khoản: " + staff.getAccount().getUsername());

        // 2. Insert nhân viên vào bảng NhanVien
        boolean staffOk = staffDAO.insert(staff);
        if (!staffOk) {
            System.err.println("[StaffBUS] Thêm nhân viên thất bại: " + staff.getStaffId());
            return false;
        }
        System.out.println("[StaffBUS] Đã thêm nhân viên: " + staff.getStaffId());
        return true;
    }
}