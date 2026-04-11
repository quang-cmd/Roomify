package kqlhotel.dao;

import kqlhotel.entity.Staff;
import kqlhotel.entity.Account;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {
    public List<Staff> getAll() {
        List<Staff> list = new ArrayList<>();
        String sql = "SELECT * FROM NhanVien nv JOIN TaiKhoan tk ON nv.taiKhoan = tk.tenDangNhap";
        try {
            ConnectDB.getInstance().connect();
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Lỗi kết nối database: " + e.getMessage());
            return list;
        }

        try (Connection con = ConnectDB.getInstance().getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Account acc = new Account(
                    rs.getString("tenDangNhap"),
                    rs.getString("matKhau"),
                    rs.getString("vaiTro"),
                    rs.getString("TrangThaiTK")
                );
                Staff staff = new Staff(
                    rs.getString("maNV"),
                    rs.getString("hoTenNV"),
                    rs.getString("sdt"),
                    rs.getBoolean("gioiTinh"),
                    acc
                );
                list.add(staff);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(Staff staff) {
        String sql = "INSERT INTO NhanVien(maNV, hoTenNV, sdt, gioiTinh, taiKhoan) VALUES(?, ?, ?, ?, ?)";
        try {
            ConnectDB.getInstance().connect();
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Lỗi kết nối database: " + e.getMessage());
            return false;
        }

        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, staff.getStaffId());
            pstmt.setString(2, staff.getFullName());
            pstmt.setString(3, staff.getPhone());
            pstmt.setBoolean(4, staff.getGender());
            pstmt.setString(5, staff.getAccount().getUsername());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi thêm nhân viên: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Staff staff) {
        String sql = "UPDATE NhanVien SET hoTenNV = ?, sdt = ?, gioiTinh = ?, taiKhoan = ? WHERE maNV = ?";
        try {
            ConnectDB.getInstance().connect();
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Lỗi kết nối database: " + e.getMessage());
            return false;
        }

        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, staff.getFullName());
            pstmt.setString(2, staff.getPhone());
            pstmt.setBoolean(3, staff.getGender());
            pstmt.setString(4, staff.getAccount().getUsername());
            pstmt.setString(5, staff.getStaffId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật nhân viên: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
