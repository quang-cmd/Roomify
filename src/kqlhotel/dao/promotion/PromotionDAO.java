package kqlhotel.dao.promotion;

import kqlhotel.dao.ConnectDB;
import kqlhotel.dao.DAO_Interface;

import kqlhotel.entity.Promotion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PromotionDAO implements DAO_Interface<Promotion> {
    
    @Override
    public List<Promotion> getAll() {
        List<Promotion> dsPromotion = new ArrayList<>();
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT * FROM KhuyenMai ORDER BY ngayBatDau DESC";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Promotion km = mapResultSetToPromotion(rs);
                dsPromotion.add(km);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsPromotion;
    }

    public List<Promotion> searchByStatus(String status) {
        List<Promotion> dsPromotion = new ArrayList<>();
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT * FROM KhuyenMai WHERE trangThaiKM = ? ORDER BY ngayBatDau DESC";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Promotion km = mapResultSetToPromotion(rs);
                dsPromotion.add(km);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsPromotion;
    }

    @Override
    public Promotion getById(String id) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT * FROM KhuyenMai WHERE maKM = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToPromotion(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean create(Promotion km) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "INSERT INTO KhuyenMai (maKM, tenKM, dieuKienApDung, loaiKM, giaTriToiDa, tienKhuyenMai, ngayBatDau, ngayKetThuc, trangThaiKM) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, km.getMaKM());
            pstmt.setString(2, km.getTenKM());
            pstmt.setDouble(3, km.getDieuKienApDung());
            pstmt.setString(4, km.getLoaiKM());
            pstmt.setDouble(5, Math.max(0, km.getGiaTriToiDa()));
            pstmt.setDouble(6, Math.max(0, km.getTienKhuyenMai()));
            pstmt.setTimestamp(7, Timestamp.valueOf(km.getNgayBatDau()));
            pstmt.setTimestamp(8, Timestamp.valueOf(km.getNgayKetThuc()));
            pstmt.setString(9, km.getTrangThaiKM());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Promotion km) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "UPDATE KhuyenMai SET tenKM=?, dieuKienApDung=?, loaiKM=?, giaTriToiDa=?, tienKhuyenMai=?, ngayBatDau=?, ngayKetThuc=?, trangThaiKM=? " +
                         "WHERE maKM=?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, km.getTenKM());
            pstmt.setDouble(2, km.getDieuKienApDung());
            pstmt.setString(3, km.getLoaiKM());
            pstmt.setDouble(4, Math.max(0, km.getGiaTriToiDa()));
            pstmt.setDouble(5, Math.max(0, km.getTienKhuyenMai()));
            pstmt.setTimestamp(6, Timestamp.valueOf(km.getNgayBatDau()));
            pstmt.setTimestamp(7, Timestamp.valueOf(km.getNgayKetThuc()));
            pstmt.setString(8, km.getTrangThaiKM());
            pstmt.setString(9, km.getMaKM());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(String id) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "DELETE FROM KhuyenMai WHERE maKM = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Promotion mapResultSetToPromotion(ResultSet rs) throws SQLException {
        Promotion km = new Promotion();

        km.setMaKM(rs.getString("maKM"));
        km.setTenKM(rs.getString("tenKM"));
        km.setDieuKienApDung(rs.getDouble("dieuKienApDung"));
        km.setLoaiKM(rs.getString("loaiKM"));

        double giaTriToiDa = rs.getDouble("giaTriToiDa");
        if (rs.wasNull()) {
            giaTriToiDa = 0;
        }
        km.setGiaTriToiDa(giaTriToiDa);

        double tienKhuyenMai = rs.getDouble("tienKhuyenMai");
        if (rs.wasNull()) {
            tienKhuyenMai = 0;
        }
        km.setTienKhuyenMai(tienKhuyenMai);

        Timestamp ngayBatDau = rs.getTimestamp("ngayBatDau");
        if (ngayBatDau != null) {
            km.setNgayBatDau(ngayBatDau.toLocalDateTime());
        }

        Timestamp ngayKetThuc = rs.getTimestamp("ngayKetThuc");
        if (ngayKetThuc != null) {
            km.setNgayKetThuc(ngayKetThuc.toLocalDateTime());
        }

        km.setTrangThaiKM(rs.getString("trangThaiKM"));

        return km;
    }
}
