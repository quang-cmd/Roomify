package kqlhotel.dao;

import kqlhotel.entity.Invoice;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDAO implements DAO_Interface<Invoice> {
    @Override
    public List<Invoice> getAll() {
        List<Invoice> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM HoaDon ORDER BY ngayLapHD DESC";
            PreparedStatement pstmt = con.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToInvoice(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Invoice getById(String id) {
        Invoice invoice = null;
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM HoaDon WHERE maHD = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                invoice = mapResultSetToInvoice(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoice;
    }

    public Invoice getActiveByRoom(String maPhong) {
        Invoice invoice = null;
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT hd.* FROM HoaDon hd " +
                         "JOIN ChiTietHoaDon cthd ON hd.maHD = cthd.maHD " +
                         "WHERE cthd.phong = ? AND hd.trangThai = N'ChuaThanhToan'";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, maPhong);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                invoice = mapResultSetToInvoice(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoice;
    }

    @Override
    public boolean update(Invoice invoice) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "UPDATE HoaDon SET ngayThanhToan = ?, tienPhong = ?, tienDichVu = ?, " +
                         "tienKhuyenMai = ?, tienThue = ?, tongTienThanhToan = ?, trangThai = ? " +
                         "WHERE maHD = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setTimestamp(1, Timestamp.valueOf(invoice.getNgayThanhToan()));
            pstmt.setDouble(2, invoice.getTienPhong());
            pstmt.setDouble(3, invoice.getTienDichVu());
            pstmt.setDouble(4, invoice.getTienKhuyenMai());
            pstmt.setDouble(5, invoice.getTienThue());
            pstmt.setDouble(6, invoice.getTongTienThanhToan());
            pstmt.setString(7, invoice.getTrangThai());
            pstmt.setString(8, invoice.getMaHD());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Invoice mapResultSetToInvoice(ResultSet rs) throws SQLException {
        Invoice invoice = new Invoice();
        invoice.setMaHD(rs.getString("maHD"));
        invoice.setNgayLapHD(rs.getTimestamp("ngayLapHD").toLocalDateTime());
        Timestamp tsTT = rs.getTimestamp("ngayThanhToan");
        if (tsTT != null) invoice.setNgayThanhToan(tsTT.toLocalDateTime());
        invoice.setGhiChu(rs.getString("ghiChu"));
        invoice.setSoLuongNguoi(rs.getInt("soLuongNguoi"));
        invoice.setTienPhong(rs.getDouble("tienPhong"));
        invoice.setTienDichVu(rs.getDouble("tienDichVu"));
        invoice.setTienKhuyenMai(rs.getDouble("tienKhuyenMai"));
        invoice.setTienThue(rs.getDouble("tienThue"));
        invoice.setTongTienThanhToan(rs.getDouble("tongTienThanhToan"));
        invoice.setPhiDoiPhong(rs.getDouble("phiDoiPhong"));
        invoice.setMaKhuyenMai(rs.getString("khuyenMai"));
        invoice.setMaKhachHang(rs.getString("khachHang"));
        invoice.setMaNhanVien(rs.getString("nhanVien"));
        invoice.setPhuongThucTT(rs.getString("phuongThucTT"));
        invoice.setTrangThai(rs.getString("trangThai"));
        return invoice;
    }

    public List<Invoice> searchInvoices(LocalDateTime start, LocalDateTime end, String customer, String status) {
        List<Invoice> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            StringBuilder sql = new StringBuilder("SELECT hd.* FROM HoaDon hd ");
            if (customer != null && !customer.isEmpty()) {
                sql.append("JOIN KhachHang kh ON hd.khachHang = kh.maKH ");
            }
            sql.append("WHERE 1=1 ");

            if (start != null) sql.append("AND hd.ngayLapHD >= ? ");
            if (end != null) sql.append("AND hd.ngayLapHD <= ? ");
            if (customer != null && !customer.isEmpty()) {
                sql.append("AND (kh.hoTenKH LIKE ? OR kh.maKH LIKE ?) ");
            }
            if (status != null && !status.isEmpty() && !status.equals("Tất cả")) {
                sql.append("AND hd.trangThai = ? ");
            }
            sql.append("ORDER BY hd.ngayLapHD DESC");

            PreparedStatement pstmt = con.prepareStatement(sql.toString());
            int idx = 1;
            if (start != null) pstmt.setTimestamp(idx++, Timestamp.valueOf(start));
            if (end != null) pstmt.setTimestamp(idx++, Timestamp.valueOf(end));
            if (customer != null && !customer.isEmpty()) {
                pstmt.setString(idx++, "%" + customer + "%");
                pstmt.setString(idx++, "%" + customer + "%");
            }
            if (status != null && !status.isEmpty() && !status.equals("Tất cả")) {
                pstmt.setString(idx++, status);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToInvoice(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public double[] getRevenueStats() {
        double[] stats = new double[4]; // [Total, PaidCount, UnpaidCount, DepositCount]
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT " +
                         "SUM(CASE WHEN trangThai = N'DaThanhToan' THEN tongTienThanhToan ELSE 0 END) as Total, " +
                         "COUNT(CASE WHEN trangThai = N'DaThanhToan' THEN 1 END) as Paid, " +
                         "COUNT(CASE WHEN trangThai = N'ChuaThanhToan' THEN 1 END) as Unpaid, " +
                         "COUNT(CASE WHEN trangThai = N'DatCoc' THEN 1 END) as Deposit " +
                         "FROM HoaDon";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                stats[0] = rs.getDouble("Total");
                stats[1] = rs.getDouble("Paid");
                stats[2] = rs.getDouble("Unpaid");
                stats[3] = rs.getDouble("Deposit");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    public boolean updateStatus(String maHD, String status) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "UPDATE HoaDon SET trangThai = ? WHERE maHD = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setString(2, maHD);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean create(Invoice t) { return false; }
    @Override
    public boolean delete(String id) { return false; }
}
