package kqlhotel.dao.service;

import kqlhotel.dao.ConnectDB;
import kqlhotel.entity.Service;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDAO {

    public List<Service> getAll() {
        return getAllActive();
    }

    public List<Service> getAllActive() {
        List<Service> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT * FROM DichVu WHERE trangThaiDV = 'DangHoatDong' ORDER BY tenDV";
            PreparedStatement pstmt = con.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToService(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // --- Premium UI Support Methods ---

    public List<Service> getAllDetailed() {
        List<Service> list = new ArrayList<>();
        String sql = "SELECT * FROM DichVu ORDER BY tenDV";
        try (Connection con = ConnectDB.getInstance().getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToService(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Service> search(String query) {
        List<Service> list = new ArrayList<>();
        String sql = "SELECT * FROM DichVu WHERE tenDV LIKE ? OR maDV LIKE ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + query + "%");
            ps.setString(2, "%" + query + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToService(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean create(Service s) {
        String sql = "INSERT INTO DichVu (maDV, tenDV, donGia, loaiDV, moTaDV, trangThaiDV) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConnectDB.getInstance().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getMaDV());
            ps.setString(2, s.getTenDV());
            ps.setDouble(3, s.getDonGia());
            ps.setString(4, s.getLoaiDV());
            ps.setString(5, s.getMoTaDV());
            ps.setString(6, s.getTrangThaiDV());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Service s) {
        String sql = "UPDATE DichVu SET tenDV = ?, donGia = ?, loaiDV = ?, moTaDV = ?, trangThaiDV = ? WHERE maDV = ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getTenDV());
            ps.setDouble(2, s.getDonGia());
            ps.setString(3, s.getLoaiDV());
            ps.setString(4, s.getMoTaDV());
            ps.setString(5, s.getTrangThaiDV());
            ps.setString(6, s.getMaDV());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(String id) {
        String sql = "DELETE FROM DichVu WHERE maDV = ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStatus(String maDV, String trangThai) {
        String sql = "UPDATE DichVu SET trangThaiDV = ? WHERE maDV = ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            ps.setString(2, maDV);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getNextId() {
        String sql = "SELECT MAX(maDV) FROM DichVu";
        try (Connection con = ConnectDB.getInstance().getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String lastId = rs.getString(1);
                if (lastId != null && lastId.startsWith("DV")) {
                    int num = Integer.parseInt(lastId.substring(2));
                    return String.format("DV%03d", num + 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "DV001";
    }

    private Service mapResultSetToService(ResultSet rs) throws SQLException {
        Service s = new Service();
        s.setMaDV(rs.getString("maDV"));
        s.setTenDV(rs.getString("tenDV"));
        s.setDonGia(rs.getDouble("donGia"));
        s.setMoTaDV(rs.getString("moTaDV"));
        s.setTrangThaiDV(rs.getString("trangThaiDV"));
        try { s.setLoaiDV(rs.getString("loaiDV")); } catch (Exception e) {}
        return s;
    }
}
