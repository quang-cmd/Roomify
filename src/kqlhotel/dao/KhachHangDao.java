package kqlhotel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import kqlhotel.entity.Customer;

public class KhachHangDao {
    public List<Customer> getAll() {
        List<Customer> customers = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM KhachHang ORDER BY hoTenKH";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    public Customer getById(String id) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM KhachHang WHERE maKH = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToCustomer(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean create(Customer customer) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "INSERT INTO KhachHang (maKH, hoTenKH, gioiTinh, ngaySinh, email, sdt, CCCD, quocTich, diaChi, hangKH, diemTichLuy) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = con.prepareStatement(sql);
            bindCustomer(pstmt, customer);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Customer customer) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "UPDATE KhachHang SET hoTenKH = ?, gioiTinh = ?, ngaySinh = ?, email = ?, sdt = ?, CCCD = ?, "
                + "quocTich = ?, diaChi = ?, hangKH = ?, diemTichLuy = ? WHERE maKH = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, customer.getHoTenKH());
            pstmt.setBoolean(2, customer.isGioiTinh());
            if (customer.getNgaySinh() != null) {
                pstmt.setTimestamp(3, Timestamp.valueOf(customer.getNgaySinh()));
            } else {
                pstmt.setTimestamp(3, null);
            }
            pstmt.setString(4, customer.getEmail());
            pstmt.setString(5, customer.getSdt());
            pstmt.setString(6, customer.getCCCD());
            pstmt.setString(7, customer.getQuocTich());
            pstmt.setString(8, customer.getDiaChi());
            pstmt.setString(9, customer.getHangKH());
            pstmt.setInt(10, customer.getDiemTichLuy());
            pstmt.setString(11, customer.getMaKH());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(String id) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "DELETE FROM KhachHang WHERE maKH = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Customer> search(String keyword) {
        List<Customer> customers = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM KhachHang WHERE maKH LIKE ? OR hoTenKH LIKE ? OR sdt LIKE ? ORDER BY hoTenKH";
            PreparedStatement pstmt = con.prepareStatement(sql);
            String searchValue = "%" + keyword + "%";
            pstmt.setString(1, searchValue);
            pstmt.setString(2, searchValue);
            pstmt.setString(3, searchValue);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    public List<Customer> filterByRank(String rank) {
        List<Customer> customers = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM KhachHang WHERE hangKH = ? ORDER BY hoTenKH";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, rank);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    private void bindCustomer(PreparedStatement pstmt, Customer customer) throws SQLException {
        pstmt.setString(1, customer.getMaKH());
        pstmt.setString(2, customer.getHoTenKH());
        pstmt.setBoolean(3, customer.isGioiTinh());
        if (customer.getNgaySinh() != null) {
            pstmt.setTimestamp(4, Timestamp.valueOf(customer.getNgaySinh()));
        } else {
            pstmt.setTimestamp(4, null);
        }
        pstmt.setString(5, customer.getEmail());
        pstmt.setString(6, customer.getSdt());
        pstmt.setString(7, customer.getCCCD());
        pstmt.setString(8, customer.getQuocTich());
        pstmt.setString(9, customer.getDiaChi());
        pstmt.setString(10, customer.getHangKH());
        pstmt.setInt(11, customer.getDiemTichLuy());
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setMaKH(rs.getString("maKH"));
        customer.setHoTenKH(rs.getString("hoTenKH"));
        customer.setGioiTinh(rs.getBoolean("gioiTinh"));
        Timestamp ngaySinh = rs.getTimestamp("ngaySinh");
        if (ngaySinh != null) {
            customer.setNgaySinh(ngaySinh.toLocalDateTime());
        }
        customer.setEmail(rs.getString("email"));
        customer.setSdt(rs.getString("sdt"));
        customer.setCCCD(rs.getString("CCCD"));
        customer.setQuocTich(rs.getString("quocTich"));
        customer.setDiaChi(rs.getString("diaChi"));
        customer.setHangKH(rs.getString("hangKH"));
        customer.setDiemTichLuy(rs.getInt("diemTichLuy"));
        return customer;
    }
}
