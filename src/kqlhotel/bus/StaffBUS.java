package kqlhotel.bus;

import kqlhotel.dao.Staff.StaffDAO;
import kqlhotel.entity.Staff;
import kqlhotel.entity.Account;
import java.util.ArrayList;
import java.util.List;

public class StaffBUS {
    private StaffDAO staffDAO;

    public StaffBUS() {
        staffDAO = new StaffDAO();
    }

    public List<Staff> getAll() {
        List<Staff> list = staffDAO.getAll();

        // Nếu database trả về rỗng, dùng mock data tạm thời
        if (list.isEmpty()) {
            list = getMockData();
        }

        return list;
    }

    public boolean insert(Staff staff) {
        return staffDAO.insert(staff);
    }

    public boolean update(Staff staff) {
        return staffDAO.update(staff);
    }

    // Mock data cho phát triển giao diện
    private List<Staff> getMockData() {
        List<Staff> mockList = new ArrayList<>();
        mockList.add(new Staff(
            "NV001",
            "Nguyễn Khả Luân",
            "0912345678",
            true,
            new Account("admin", "admin123", "QuanLy", "DangHoatDong")
        ));

        mockList.add(new Staff(
            "NV002",
            "Trần Thị Hương",
            "0912345679",
            false,
            new Account("user1", "pass123", "NhanVien", "DangHoatDong")
        ));

        mockList.add(new Staff(
            "NV003",
            "Lê Văn Minh",
            "0912345680",
            true,
            new Account("user2", "pass123", "NhanVien", "DangHoatDong")
        ));

        mockList.add(new Staff(
            "NV004",
            "Phạm Thúy Hạnh",
            "0912345681",
            false,
            new Account("user3", "pass123", "NhanVien", "NghiPhep")
        ));

        mockList.add(new Staff(
            "NV005",
            "Vũ Đức Hòa",
            "0912345682",
            true,
            new Account("user4", "pass123", "NhanVien", "NghiViec")
        ));

        return mockList;
    }
}
