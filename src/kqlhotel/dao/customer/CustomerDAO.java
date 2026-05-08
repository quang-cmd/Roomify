package kqlhotel.dao.customer;

import kqlhotel.dao.ConnectDB;
import kqlhotel.dao.DAO_Interface;
import kqlhotel.entity.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO implements DAO_Interface<Customer> {
    
    @Override
    public List<Customer> getAll() {
        List<Customer> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT * FROM KhachHang";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Customer getById(String id) {
        Customer customer = null;
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT * FROM KhachHang WHERE maKH = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                customer = mapResultSetToCustomer(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customer;
    }

    @Override
    public boolean create(Customer customer) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "INSERT INTO KhachHang (maKH, hoTenKH, gioiTinh, ngaySinh, email, sdt, CCCD, quocTich, diaChi, hangKH, diemTichLuy) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, customer.getMaKH());
            pstmt.setString(2, customer.getHoTenKH());
            pstmt.setBoolean(3, customer.isGioiTinh());
            pstmt.setTimestamp(4, customer.getNgaySinh() != null ? Timestamp.valueOf(customer.getNgaySinh()) : null);
            pstmt.setString(5, customer.getEmail());
            pstmt.setString(6, customer.getSdt());
            pstmt.setString(7, customer.getCCCD());
            pstmt.setString(8, customer.getQuocTich());
            pstmt.setString(9, customer.getDiaChi());
            pstmt.setString(10, customer.getHangKH());
            pstmt.setInt(11, customer.getDiemTichLuy());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(Customer customer) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "UPDATE KhachHang SET hoTenKH = ?, gioiTinh = ?, ngaySinh = ?, email = ?, sdt = ?, " +
                         "CCCD = ?, quocTich = ?, diaChi = ?, hangKH = ?, diemTichLuy = ? WHERE maKH = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, customer.getHoTenKH());
            pstmt.setBoolean(2, customer.isGioiTinh());
            pstmt.setTimestamp(3, customer.getNgaySinh() != null ? Timestamp.valueOf(customer.getNgaySinh()) : null);
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
            return false;
        }
    }

    @Override
    public boolean delete(String id) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "DELETE FROM KhachHang WHERE maKH = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setMaKH(rs.getString("maKH") != null ? rs.getString("maKH").trim() : null);
        customer.setHoTenKH(rs.getString("hoTenKH"));
        customer.setGioiTinh(rs.getBoolean("gioiTinh"));
        Timestamp ns = rs.getTimestamp("ngaySinh");
        if (ns != null) {
            customer.setNgaySinh(ns.toLocalDateTime());
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

    // --- Premium UI Support Methods ---

    // --- Premium UI Support Methods ---

    public List<Customer> getAllWithStats() {
        List<Customer> list = new ArrayList<>();
        String sql =
            "SELECT kh.*, " +
            "ISNULL(stats.tongDatPhong, 0) AS tongDatPhong, " +
            "ISNULL(stats.tongChiTieu, 0) AS tongChiTieu, " +
            "stats.ngayDatGanNhat, " +
            "CASE WHEN ISNULL(stats.tongDatPhong, 0) > 0 THEN 1 ELSE 0 END AS dangHoatDong " +
            "FROM KhachHang kh " +
            "LEFT JOIN ( " +
            "  SELECT maKH, COUNT(maHD) AS tongDatPhong, SUM(tongTienThanhToan) AS tongChiTieu, MAX(ngayLapHD) AS ngayDatGanNhat " +
            "  FROM HoaDon GROUP BY maKH " +
            ") stats ON stats.maKH = kh.maKH " +
            "ORDER BY kh.hoTenKH";

        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Customer kh = mapResultSetToCustomer(rs);
                // Stats
                kh.setTongDatPhong(rs.getInt("tongDatPhong"));
                kh.setTongChiTieu(rs.getDouble("tongChiTieu"));
                Timestamp ndgn = rs.getTimestamp("ngayDatGanNhat");
                if (ndgn != null) kh.setNgayDatGanNhatDate(new java.util.Date(ndgn.getTime()));
                kh.setDangHoatDong(rs.getInt("dangHoatDong") == 1);
                list.add(kh);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<kqlhotel.entity.CustomerBookingHistory> getBookingHistory(String maKH) {
        List<kqlhotel.entity.CustomerBookingHistory> list = new ArrayList<>();
        String sql = "SELECT maHD, ngayLapHD, tongTienThanhToan, trangThai FROM HoaDon WHERE maKH = ? ORDER BY ngayLapHD DESC";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKH);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    kqlhotel.entity.CustomerBookingHistory item = new kqlhotel.entity.CustomerBookingHistory();
                    item.setMaHD(rs.getString("maHD"));
                    Timestamp nlhd = rs.getTimestamp("ngayLapHD");
                    if (nlhd != null) item.setNgayLapHD(nlhd.toLocalDateTime());
                    item.setTongTien(rs.getDouble("tongTienThanhToan"));
                    item.setTinhTrang(rs.getString("trangThai"));
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public String generateNextCustomerId(Connection con) throws SQLException {
        String sql = "SELECT MAX(maKH) FROM KhachHang";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String lastId = rs.getString(1);
                if (lastId != null && lastId.startsWith("KH")) {
                    int num = Integer.parseInt(lastId.substring(2)) + 1;
                    return String.format("KH%03d", num);
                }
            }
            return "KH001";
        }
    }
}
