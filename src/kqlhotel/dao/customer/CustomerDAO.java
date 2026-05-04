package kqlhotel.dao.customer;

import kqlhotel.dao.ConnectDB;
import kqlhotel.dao.DAO_Interface;
import kqlhotel.entity.Customer;
import kqlhotel.entity.CustomerBookingHistory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO implements DAO_Interface<Customer> {

    @Override
    public List<Customer> getAll() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM KhachHang ORDER BY hoTenKH";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapCustomerSimple(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

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
                list.add(mapCustomer(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public Customer getById(String id) {
        String sql = "SELECT * FROM KhachHang WHERE maKH = ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapCustomerSimple(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean create(Customer kh) {
        String sql =
            "INSERT INTO KhachHang(maKH, hoTenKH, gioiTinh, ngaySinh, email, sdt, CCCD, quocTich, diaChi, hangKH, diemTichLuy) " +
            "VALUES(?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String nextId = generateNextCustomerId(con);
            kh.setMaKH(nextId);
            ps.setString(1, nextId);
            ps.setString(2, kh.getHoTenKH());
            ps.setBoolean(3, "Nam".equalsIgnoreCase(kh.getGioiTinh()));
            if (kh.getNgaySinh() != null) {
                ps.setTimestamp(4, new Timestamp(kh.getNgaySinh().getTime()));
            } else {
                ps.setTimestamp(4, Timestamp.valueOf("1990-01-01 00:00:00"));
            }
            ps.setString(5, (kh.getEmail() == null || kh.getEmail().isBlank()) ? null : kh.getEmail());
            ps.setString(6, kh.getSdt());
            ps.setString(7, kh.getCCCD());
            ps.setString(8, (kh.getQuocTich() == null || kh.getQuocTich().isBlank()) ? "Viet Nam" : kh.getQuocTich());
            ps.setString(9, (kh.getDiaChi() == null || kh.getDiaChi().isBlank()) ? null : kh.getDiaChi());
            ps.setString(10, (kh.getHangKH() == null || kh.getHangKH().isBlank()) ? "Dong" : kh.getHangKH());
            ps.setInt(11, kh.getDiemTichLuy());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(Customer kh) {
        String sql =
            "UPDATE KhachHang SET hoTenKH=?, gioiTinh=?, ngaySinh=?, email=?, sdt=?, CCCD=?, quocTich=?, diaChi=?, hangKH=?, diemTichLuy=? " +
            "WHERE maKH=?";

        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, kh.getHoTenKH());
            ps.setBoolean(2, "Nam".equalsIgnoreCase(kh.getGioiTinh()));
            if (kh.getNgaySinh() != null) {
                ps.setTimestamp(3, new Timestamp(kh.getNgaySinh().getTime()));
            } else {
                ps.setTimestamp(3, Timestamp.valueOf("1990-01-01 00:00:00"));
            }
            ps.setString(4, (kh.getEmail() == null || kh.getEmail().isBlank()) ? null : kh.getEmail());
            ps.setString(5, kh.getSdt());
            ps.setString(6, kh.getCCCD());
            ps.setString(7, (kh.getQuocTich() == null || kh.getQuocTich().isBlank()) ? "Viet Nam" : kh.getQuocTich());
            ps.setString(8, (kh.getDiaChi() == null || kh.getDiaChi().isBlank()) ? null : kh.getDiaChi());
            ps.setString(9, (kh.getHangKH() == null || kh.getHangKH().isBlank()) ? "Dong" : kh.getHangKH());
            ps.setInt(10, kh.getDiemTichLuy());
            ps.setString(11, kh.getMaKH());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM KhachHang WHERE maKH = ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<CustomerBookingHistory> getBookingHistory(String maKH) {
        List<CustomerBookingHistory> list = new ArrayList<>();
        String sql =
            "SELECT maHD, maDatPhong, ngayLapHD, tongTienThanhToan, trangThai " +
            "FROM HoaDon WHERE maKH = ? ORDER BY ngayLapHD DESC";

        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKH);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CustomerBookingHistory item = new CustomerBookingHistory();
                    item.setMaHoaDon(rs.getString("maHD"));
                    item.setMaDatPhong(rs.getString("maDatPhong"));
                    item.setNgayLap(rs.getTimestamp("ngayLapHD"));
                    item.setTongTien(rs.getDouble("tongTienThanhToan"));
                    item.setTrangThai(rs.getString("trangThai"));
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    private Customer mapCustomer(ResultSet rs) throws SQLException {
        Customer kh = mapCustomerSimple(rs);
        // Stats
        kh.setTongDatPhong(rs.getInt("tongDatPhong"));
        kh.setTongChiTieu(rs.getDouble("tongChiTieu"));
        kh.setNgayDatGanNhat(rs.getTimestamp("ngayDatGanNhat"));
        kh.setDangHoatDong(rs.getBoolean("dangHoatDong"));
        return kh;
    }

    private Customer mapCustomerSimple(ResultSet rs) throws SQLException {
        Customer kh = new Customer();
        kh.setMaKH(rs.getString("maKH"));
        kh.setHoTenKH(rs.getString("hoTenKH"));
        kh.setQuocTich(rs.getString("quocTich"));
        kh.setDiaChi(rs.getString("diaChi"));
        kh.setHangKH(rs.getString("hangKH"));
        kh.setGioiTinh(rs.getBoolean("gioiTinh") ? "Nam" : "Nu");
        kh.setNgaySinh(rs.getTimestamp("ngaySinh"));
        kh.setEmail(rs.getString("email"));
        kh.setSdt(rs.getString("sdt"));
        kh.setCCCD(rs.getString("CCCD"));
        kh.setDiemTichLuy(rs.getInt("diemTichLuy"));
        return kh;
    }

    private String generateNextCustomerId(Connection con) throws Exception {
        String sql = "SELECT MAX(CAST(SUBSTRING(maKH, 3, LEN(maKH) - 2) AS INT)) AS maxId FROM KhachHang";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int next = 1;
            if (rs.next()) {
                next = rs.getInt("maxId") + 1;
            }
            return String.format("KH%03d", next);
        }
    }
}
