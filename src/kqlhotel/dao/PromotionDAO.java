package kqlhotel.dao;

import kqlhotel.entity.Promotion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PromotionDAO implements DAO_Interface<Promotion> {
    
    @Override
    public List<Promotion> getAll() {
        List<Promotion> dsPromotion = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
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
            Connection con = ConnectDB.getConnection();
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
            Connection con = ConnectDB.getConnection();
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
            Connection con = ConnectDB.getConnection();
            String sql = "INSERT INTO KhuyenMai (maKM, tenKM, dieuKienApDung, loaiKM, giaTriToiDa, tienKhuyenMai, ngayBatDau, ngayKetThuc, trangThaiKM) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, km.getMaKM());
            pstmt.setString(2, km.getTenKM());
            pstmt.setString(3, km.getDieuKienApDung());
            pstmt.setString(4, km.getLoaiKM());
            if (km.getGiaTriToiDa() > 0) pstmt.setDouble(5, km.getGiaTriToiDa()); else pstmt.setNull(5, Types.FLOAT);
            if (km.getTienKhuyenMai() > 0) pstmt.setDouble(6, km.getTienKhuyenMai()); else pstmt.setNull(6, Types.FLOAT);
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
            Connection con = ConnectDB.getConnection();
            String sql = "UPDATE KhuyenMai SET tenKM=?, dieuKienApDung=?, loaiKM=?, giaTriToiDa=?, tienKhuyenMai=?, ngayBatDau=?, ngayKetThuc=?, trangThaiKM=? " +
                         "WHERE maKM=?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, km.getTenKM());
            pstmt.setString(2, km.getDieuKienApDung());
            pstmt.setString(3, km.getLoaiKM());
            if (km.getGiaTriToiDa() > 0) pstmt.setDouble(4, km.getGiaTriToiDa()); else pstmt.setNull(4, Types.FLOAT);
            if (km.getTienKhuyenMai() > 0) pstmt.setDouble(5, km.getTienKhuyenMai()); else pstmt.setNull(5, Types.FLOAT);
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
            Connection con = ConnectDB.getConnection();
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
        km.setDieuKienApDung(rs.getString("dieuKienApDung"));
        km.setLoaiKM(rs.getString("loaiKM"));
        km.setGiaTriToiDa(rs.getDouble("giaTriToiDa"));
        km.setTienKhuyenMai(rs.getDouble("tienKhuyenMai"));
        km.setNgayBatDau(rs.getTimestamp("ngayBatDau").toLocalDateTime());
        km.setNgayKetThuc(rs.getTimestamp("ngayKetThuc").toLocalDateTime());
        km.setTrangThaiKM(rs.getString("trangThaiKM"));
        return km;
    }
}
