package kqlhotel.dao.service;

import kqlhotel.dao.ConnectDB;
import kqlhotel.entity.Service;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDAO {

    public List<Service> getAll() {
        List<Service> list = new ArrayList<>();
        String sql = "SELECT maDV, tenDV, donGia, loaiDV, moTaDV, trangThaiDV FROM DichVu";
        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToService(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Service> getAllActive() {
        List<Service> list = new ArrayList<>();
        String sql = "SELECT maDV, tenDV, donGia, loaiDV, moTaDV, trangThaiDV FROM DichVu WHERE trangThaiDV = 'DangHoatDong'";

        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToService(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(Service s) {
        String sql = "INSERT INTO DichVu (maDV, tenDV, donGia, loaiDV, moTaDV, trangThaiDV) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConnectDB.getConnection()) {
            if (s.getMaDV() == null || s.getMaDV().isBlank()) {
                s.setMaDV(generateNextId(con));
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, s.getMaDV());
                ps.setString(2, s.getTenDV());
                ps.setDouble(3, s.getGia());
                ps.setString(4, s.getLoaiDV());
                ps.setString(5, s.getMoTa());
                ps.setString(6, s.getTrangThai());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String generateNextId(Connection con) {
        String sql = "SELECT MAX(CAST(SUBSTRING(maDV, 3, LEN(maDV) - 2) AS INT)) FROM DichVu";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                int nextId = rs.getInt(1) + 1;
                return String.format("DV%03d", nextId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "DV001";
    }


    public boolean update(Service s) {
        String sql = "UPDATE DichVu SET tenDV = ?, donGia = ?, loaiDV = ?, moTaDV = ?, trangThaiDV = ? WHERE maDV = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getTenDV());
            ps.setDouble(2, s.getGia());
            ps.setString(3, s.getLoaiDV());
            ps.setString(4, s.getMoTa());
            ps.setString(5, s.getTrangThai());
            ps.setString(6, s.getMaDV());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateStatus(String maDV, String trangThai) {
        String sql = "UPDATE DichVu SET trangThaiDV = ? WHERE maDV = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            ps.setString(2, maDV);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Service mapResultSetToService(ResultSet rs) throws SQLException {
        return new Service(
            rs.getString("maDV"),
            rs.getString("tenDV"),
            rs.getDouble("donGia"),
            rs.getString("loaiDV"),
            rs.getString("moTaDV"),
            rs.getString("trangThaiDV")
        );
    }

}
