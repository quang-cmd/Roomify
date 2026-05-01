package kqlhotel.dao.Staff;

import kqlhotel.dao.ConnectDB;
import kqlhotel.entity.Staff;
import kqlhotel.entity.Account;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {

    public List<Staff> getAll() {
        List<Staff> list = new ArrayList<>();
        String sql = "SELECT * FROM NhanVien nv JOIN TaiKhoan tk ON nv.tenDangNhap = tk.tenDangNhap";
        
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Account acc = new Account(
                    rs.getString("tenDangNhap"),
                    rs.getString("matKhau"),
                    rs.getString("vaiTro"),
                    rs.getString("trangThaiTK")
                );
                Staff staff = new Staff(
                    rs.getString("maNV"),
                    rs.getString("hoTenNV"),
                    rs.getString("sdt"),
                    rs.getBoolean("gioiTinh"),
                    acc
                );
                Date ngayVaoDate = rs.getDate("ngayVao");
                if (ngayVaoDate != null) {
                    staff.setNgayVao(ngayVaoDate.toLocalDate());
                }
                double luong = rs.getDouble("luong");
                if (!rs.wasNull()) {
                    staff.setLuong(luong);
                }
                list.add(staff);
            }
        } catch (Exception e) {
            System.err.println("Lỗi truy vấn: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(Staff staff) {
        String sql = "INSERT INTO NhanVien(maNV, hoTenNV, sdt, gioiTinh, tenDangNhap, ngayVao, luong) VALUES(?, ?, ?, ?, ?, ?, ?)";
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, staff.getStaffId());
            pstmt.setString(2, staff.getFullName());
            pstmt.setString(3, staff.getPhone());
            pstmt.setBoolean(4, staff.getGender() != null ? staff.getGender() : true);
            pstmt.setString(5, staff.getAccount().getUsername());
            pstmt.setDate(6, staff.getNgayVao() != null
                ? Date.valueOf(staff.getNgayVao())
                : Date.valueOf(LocalDate.now()));
            if (staff.getLuong() != null) {
                pstmt.setDouble(7, staff.getLuong());
            } else {
                pstmt.setDouble(7, 0.0);
            }
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi thêm nhân viên: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Staff staff) {
        String sql = "UPDATE NhanVien SET hoTenNV = ?, sdt = ?, gioiTinh = ?, ngayVao = ?, luong = ?, tenDangNhap = ? WHERE maNV = ?";
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, staff.getFullName());
            pstmt.setString(2, staff.getPhone());
            pstmt.setBoolean(3, staff.getGender() != null ? staff.getGender() : true);
            pstmt.setDate(4, staff.getNgayVao() != null
                ? Date.valueOf(staff.getNgayVao())
                : Date.valueOf(LocalDate.now()));
            if (staff.getLuong() != null) {
                pstmt.setDouble(5, staff.getLuong());
            } else {
                pstmt.setDouble(5, 0.0);
            }
            pstmt.setString(6, staff.getAccount().getUsername());
            pstmt.setString(7, staff.getStaffId());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi cập nhật nhân viên: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(String maNV) {
        String sql = "DELETE FROM NhanVien WHERE maNV = ?";
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, maNV);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
