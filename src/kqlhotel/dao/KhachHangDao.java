package kqlhotel.dao;

import kqlhotel.entity.KhachHangEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KhachHangDao {

    // ================== LẤY TẤT CẢ ==================
    public List<KhachHangEntity> getAll() {
        List<KhachHangEntity> list = new ArrayList<>();

        try {
            Connection conn = ConnectDB.getInstance().getConnection();
            String sql = "SELECT * FROM KhachHang";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                KhachHangEntity kh = new KhachHangEntity(
                        rs.getString("MaKH"),
                        rs.getString("TenKH"),
                        rs.getString("SDT"),
                        rs.getString("CCCD"),
                        rs.getString("DiaChi"),
                        rs.getString("TrangThai")
                );
                list.add(kh);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ================== THÊM ==================
    public boolean add(KhachHangEntity kh) {
        try {
            Connection conn = ConnectDB.getInstance().getConnection();
            String sql = "INSERT INTO KhachHang VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, kh.getMaKH());
            ps.setString(2, kh.getTenKH());
            ps.setString(3, kh.getSdt());
            ps.setString(4, kh.getCccd());
            ps.setString(5, kh.getDiaChi());
            ps.setString(6, kh.getTrangThai());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ================== UPDATE ==================
    public boolean update(KhachHangEntity kh) {
        try {
            Connection conn = ConnectDB.getInstance().getConnection();
            String sql = "UPDATE KhachHang SET TenKH=?, SDT=?, CCCD=?, DiaChi=?, TrangThai=? WHERE MaKH=?";
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, kh.getTenKH());
            ps.setString(2, kh.getSdt());
            ps.setString(3, kh.getCccd());
            ps.setString(4, kh.getDiaChi());
            ps.setString(5, kh.getTrangThai());
            ps.setString(6, kh.getMaKH());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ================== DELETE ==================
    public boolean delete(String maKH) {
        try {
            Connection conn = ConnectDB.getInstance().getConnection();
            String sql = "DELETE FROM KhachHang WHERE MaKH=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, maKH);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ================== GET BY ID ==================
    public KhachHangEntity getById(String maKH) {
        try {
            Connection conn = ConnectDB.getInstance().getConnection();
            String sql = "SELECT * FROM KhachHang WHERE MaKH=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, maKH);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new KhachHangEntity(
                        rs.getString("MaKH"),
                        rs.getString("TenKH"),
                        rs.getString("SDT"),
                        rs.getString("CCCD"),
                        rs.getString("DiaChi"),
                        rs.getString("TrangThai")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}