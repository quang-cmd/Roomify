package kqlhotel.dao.invoice;

import kqlhotel.dao.ConnectDB;
import kqlhotel.entity.Customer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class ChiTietKhachODAO {

    public List<Customer> getByInvoice(String maHD) {
        List<Customer> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT kh.* FROM KhachHang kh " +
                         "JOIN ChiTietKhachO ct ON kh.maKH = ct.maKH " +
                         "WHERE ct.maHD = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, maHD);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Customer kh = new Customer();
                kh.setMaKH(rs.getString("maKH"));
                kh.setHoTenKH(rs.getString("hoTenKH"));
                kh.setSdt(rs.getString("sdt"));
                kh.setCCCD(rs.getString("CCCD"));
                kh.setEmail(rs.getString("email"));
                kh.setQuocTich(rs.getString("quocTich"));
                kh.setDiaChi(rs.getString("diaChi"));
                kh.setHangKH(rs.getString("hangKH"));
                kh.setDiemTichLuy(rs.getInt("diemTichLuy"));
                list.add(kh);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(String maHD, String maKH, String ghiChu) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "INSERT INTO ChiTietKhachO (maHD, maKH, ghiChu) VALUES (?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maHD);
            ps.setString(2, maKH);
            ps.setString(3, ghiChu);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(String maHD, String maKH) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "DELETE FROM ChiTietKhachO WHERE maHD = ? AND maKH = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maHD);
            ps.setString(2, maKH);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
