package kqlhotel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import kqlhotel.entity.Service;

public class DichVuDao {
    public List<Service> getAll() {
        List<Service> services = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM DichVu ORDER BY tenDV";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                services.add(mapResultSetToService(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return services;
    }

    public Service getById(String id) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM DichVu WHERE maDV = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToService(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean create(Service service) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "INSERT INTO DichVu (maDV, tenDV, donGia, moTaDV, trangThaiDV) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, service.getMaDV());
            pstmt.setString(2, service.getTenDV());
            pstmt.setDouble(3, service.getDonGia());
            pstmt.setString(4, service.getMoTaDV());
            pstmt.setString(5, service.getTrangThaiDV());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Service service) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "UPDATE DichVu SET tenDV = ?, donGia = ?, moTaDV = ?, trangThaiDV = ? WHERE maDV = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, service.getTenDV());
            pstmt.setDouble(2, service.getDonGia());
            pstmt.setString(3, service.getMoTaDV());
            pstmt.setString(4, service.getTrangThaiDV());
            pstmt.setString(5, service.getMaDV());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(String id) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "DELETE FROM DichVu WHERE maDV = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Service> search(String keyword) {
        List<Service> services = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM DichVu WHERE maDV LIKE ? OR tenDV LIKE ? OR moTaDV LIKE ? ORDER BY tenDV";
            PreparedStatement pstmt = con.prepareStatement(sql);
            String searchValue = "%" + keyword + "%";
            pstmt.setString(1, searchValue);
            pstmt.setString(2, searchValue);
            pstmt.setString(3, searchValue);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                services.add(mapResultSetToService(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return services;
    }

    public List<Service> filterByStatus(String status) {
        List<Service> services = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM DichVu WHERE trangThaiDV = ? ORDER BY tenDV";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                services.add(mapResultSetToService(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return services;
    }

    private Service mapResultSetToService(ResultSet rs) throws SQLException {
        Service service = new Service();
        service.setMaDV(rs.getString("maDV"));
        service.setTenDV(rs.getString("tenDV"));
        service.setDonGia(rs.getDouble("donGia"));
        service.setMoTaDV(rs.getString("moTaDV"));
        service.setTrangThaiDV(rs.getString("trangThaiDV"));
        return service;
    }
}
