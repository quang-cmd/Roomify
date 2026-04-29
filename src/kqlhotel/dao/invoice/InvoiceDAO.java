package kqlhotel.dao.invoice;

import kqlhotel.dao.ConnectDB;
import kqlhotel.dao.DAO_Interface;
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
            Connection con = ConnectDB.getInstance().getConnection();
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
            Connection con = ConnectDB.getInstance().getConnection();
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
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT hd.* " +
                    "FROM HoaDon hd " +
                    "JOIN ChiTietHoaDon cthd ON hd.maHD = cthd.maHD " +
                    "WHERE cthd.maPhong = ? AND hd.trangThai = ? " +
                    "ORDER BY hd.ngayLapHD DESC";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, maPhong);
            pstmt.setString(2, "ChuaThanhToan");

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
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "UPDATE HoaDon SET " +
                    "ngayThanhToan = ?, tienPhong = ?, tienDichVu = ?, " +
                    "tienKhuyenMai = ?, tienThue = ?, tongTienThanhToan = ?, " +
                    "phiDoiPhong = ?, maKM = ?, phuongThucTT = ?, trangThai = ?, ghiChu = ? " +
                    "WHERE maHD = ?";

            PreparedStatement pstmt = con.prepareStatement(sql);
            if (invoice.getNgayThanhToan() != null) {
                pstmt.setTimestamp(1, Timestamp.valueOf(invoice.getNgayThanhToan()));
            } else {
                pstmt.setNull(1, Types.TIMESTAMP);
            }
            pstmt.setDouble(2, invoice.getTienPhong());
            pstmt.setDouble(3, invoice.getTienDichVu());
            pstmt.setDouble(4, invoice.getTienKhuyenMai());
            pstmt.setDouble(5, invoice.getTienThue());
            pstmt.setDouble(6, invoice.getTongTienThanhToan());
            pstmt.setDouble(7, invoice.getPhiDoiPhong());

            if (invoice.getMaKhuyenMai() != null && !invoice.getMaKhuyenMai().isBlank()) {
                pstmt.setString(8, invoice.getMaKhuyenMai());
            } else {
                pstmt.setNull(8, Types.CHAR);
            }

            pstmt.setString(9, invoice.getPhuongThucTT());
            pstmt.setString(10, invoice.getTrangThai());
            pstmt.setString(11, invoice.getGhiChu());
            pstmt.setString(12, invoice.getMaHD());

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

        Timestamp tsThanhToan = rs.getTimestamp("ngayThanhToan");
        if (tsThanhToan != null) {
            invoice.setNgayThanhToan(tsThanhToan.toLocalDateTime());
        }

        invoice.setGhiChu(rs.getString("ghiChu"));
        invoice.setSoLuongNguoi(rs.getInt("soLuongNguoiO"));
        invoice.setTienPhong(rs.getDouble("tienPhong"));
        invoice.setTienDichVu(rs.getDouble("tienDichVu"));
        invoice.setTienKhuyenMai(rs.getDouble("tienKhuyenMai"));
        invoice.setTienThue(rs.getDouble("tienThue"));
        invoice.setTongTienThanhToan(rs.getDouble("tongTienThanhToan"));
        invoice.setPhiDoiPhong(rs.getDouble("phiDoiPhong"));
        invoice.setMaKhuyenMai(rs.getString("maKM"));
        invoice.setMaKhachHang(rs.getString("maKH"));
        invoice.setMaNhanVien(rs.getString("maNV"));
        invoice.setPhuongThucTT(rs.getString("phuongThucTT"));
        invoice.setTrangThai(rs.getString("trangThai"));
        invoice.setMaDatPhong(rs.getString("maDatPhong"));
        return invoice;
    }

    public List<Invoice> searchInvoices(LocalDateTime start, LocalDateTime end, String customer, String status) {
        List<Invoice> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            StringBuilder sql = new StringBuilder("SELECT hd.* FROM HoaDon hd ");

            if (customer != null && !customer.isEmpty()) {
                sql.append("JOIN KhachHang kh ON hd.maKH = kh.maKH ");
            }

            sql.append("WHERE 1=1 ");

            if (start != null) {
                sql.append("AND hd.ngayLapHD >= ? ");
            }
            if (end != null) {
                sql.append("AND hd.ngayLapHD <= ? ");
            }
            if (customer != null && !customer.isEmpty()) {
                sql.append("AND (kh.hoTenKH LIKE ? OR kh.maKH LIKE ?) ");
            }
            if (status != null && !status.isEmpty() && !"Tất cả".equals(status)) {
                sql.append("AND hd.trangThai = ? ");
            }

            sql.append("ORDER BY hd.ngayLapHD DESC");

            PreparedStatement pstmt = con.prepareStatement(sql.toString());
            int idx = 1;

            if (start != null) {
                pstmt.setTimestamp(idx++, Timestamp.valueOf(start));
            }
            if (end != null) {
                pstmt.setTimestamp(idx++, Timestamp.valueOf(end));
            }
            if (customer != null && !customer.isEmpty()) {
                pstmt.setString(idx++, "%" + customer + "%");
                pstmt.setString(idx++, "%" + customer + "%");
            }
            if (status != null && !status.isEmpty() && !"Tất cả".equals(status)) {
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
        double[] stats = new double[4];
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT " +
                    "SUM(CASE WHEN trangThai = ? THEN tongTienThanhToan ELSE 0 END) AS Total, " +
                    "COUNT(CASE WHEN trangThai = ? THEN 1 END) AS Paid, " +
                    "COUNT(CASE WHEN trangThai = ? THEN 1 END) AS Unpaid, " +
                    "COUNT(CASE WHEN trangThai = ? THEN 1 END) AS Deposit " +
                    "FROM HoaDon";

            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, "DaThanhToan");
            pstmt.setString(2, "DaThanhToan");
            pstmt.setString(3, "ChuaThanhToan");
            pstmt.setString(4, "DatCoc");

            ResultSet rs = pstmt.executeQuery();
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
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "UPDATE HoaDon SET trangThai = ?, ngayThanhToan = ? WHERE maHD = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);

            pstmt.setString(1, status);
            if ("DaThanhToan".equals(status)) {
                pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            } else {
                pstmt.setNull(2, Types.TIMESTAMP);
            }
            pstmt.setString(3, maHD);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public double getDepositAmount(String maDatPhong) {
        if (maDatPhong == null || maDatPhong.isBlank()) {
            return 0;
        }

        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT tienCoc FROM DatPhong WHERE maDatPhong = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, maDatPhong);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("tienCoc");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String getStaffName(String maNV) {
        if (maNV == null || maNV.isBlank()) {
            return null;
        }

        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT hoTenNV FROM NhanVien WHERE maNV = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, maNV);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("hoTenNV");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
    public String getServiceName(String maDV) {
        if (maDV == null || maDV.isBlank()) {
            return "";
        }

        String sql = "SELECT tenDV FROM DichVu WHERE maDV = ?";

        try (
                java.sql.Connection con = kqlhotel.dao.ConnectDB.getInstance().getConnection();
                java.sql.PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, maDV);

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("tenDV");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    @Override
    public boolean create(Invoice t) {
        return false;
    }

    @Override
    public boolean delete(String id) {
        return false;
    }
}