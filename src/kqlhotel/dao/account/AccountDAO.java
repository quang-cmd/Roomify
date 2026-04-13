package kqlhotel.dao.Account;

import kqlhotel.dao.ConnectDB;
import kqlhotel.entity.Account;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {
    public List<Account> getAll() {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM TaiKhoan";
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
                list.add(acc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(Account acc) {
        String sql = "INSERT INTO TaiKhoan(tenDangNhap, matKhau, vaiTro, TrangThaiTK) VALUES(?, ?, ?, ?)";
        try {
            ConnectDB.getInstance().connect();
        } catch (Exception e) {
            System.err.println("Lỗi kết nối database: " + e.getMessage());
            return false;
        }
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, acc.getUsername());
            pstmt.setString(2, acc.getPassword());
            pstmt.setString(3, acc.getRole());
            pstmt.setString(4, acc.getStatus());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Account acc) {
        String sql = "UPDATE TaiKhoan SET matKhau = ?, vaiTro = ?, TrangThaiTK = ? WHERE tenDangNhap = ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, acc.getPassword());
            pstmt.setString(2, acc.getRole());
            pstmt.setString(3, acc.getStatus());
            pstmt.setString(4, acc.getUsername());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}