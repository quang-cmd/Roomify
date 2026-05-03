package kqlhotel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import kqlhotel.entity.DichVuEntity;

public class DichVuDao {

    public List<DichVuEntity> getAll() {
        List<DichVuEntity> list = new ArrayList<>();
        String sql = "SELECT * FROM DichVu ORDER BY tenDV";

        try (Connection conn = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                DichVuEntity dv = new DichVuEntity();
                dv.setMaDV(rs.getString("maDV"));
                dv.setTenDV(rs.getString("tenDV"));
                dv.setGia(rs.getDouble("donGia"));
                dv.setTrangThai(rs.getString("trangThaiDV"));
                String rawDescription = rs.getString("moTaDV");
                dv.setLoaiDV(extractCategory(rawDescription, dv.getTenDV()));
                dv.setMoTa(cleanDescription(rawDescription));
                list.add(dv);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(DichVuEntity dichVu) {
        String sql = "INSERT INTO DichVu(maDV, tenDV, donGia, moTaDV, trangThaiDV) VALUES(?,?,?,?,?)";
        try (Connection conn = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, generateNextServiceId(conn));
            ps.setString(2, dichVu.getTenDV());
            ps.setDouble(3, dichVu.getGia());
            ps.setString(4, encodeDescription(dichVu.getLoaiDV(), dichVu.getMoTa()));
            ps.setString(5, normalizeStatus(dichVu.getTrangThai()));
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(DichVuEntity dichVu) {
        String sql = "UPDATE DichVu SET tenDV=?, donGia=?, moTaDV=?, trangThaiDV=? WHERE maDV=?";
        try (Connection conn = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dichVu.getTenDV());
            ps.setDouble(2, dichVu.getGia());
            ps.setString(3, encodeDescription(dichVu.getLoaiDV(), dichVu.getMoTa()));
            ps.setString(4, normalizeStatus(dichVu.getTrangThai()));
            ps.setString(5, dichVu.getMaDV());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStatus(String maDV, String trangThai) {
        String sql = "UPDATE DichVu SET trangThaiDV=? WHERE maDV=?";
        try (Connection conn = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizeStatus(trangThai));
            ps.setString(2, maDV);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String generateNextServiceId(Connection conn) throws Exception {
        String sql = "SELECT MAX(CAST(SUBSTRING(maDV, 3, LEN(maDV) - 2) AS INT)) AS maxId FROM DichVu";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int next = 1;
            if (rs.next()) {
                next = rs.getInt("maxId") + 1;
            }
            return String.format("DV%03d", next);
        }
    }

    private String encodeDescription(String category, String description) {
        String normalizedCategory = category == null || category.isBlank() ? "Tiện ích" : category;
        String normalizedDescription = description == null ? "" : description.trim();
        return "[CAT:" + normalizedCategory + "] " + normalizedDescription;
    }

    private String extractCategory(String description, String tenDV) {
        if (description != null && description.startsWith("[CAT:")) {
            int end = description.indexOf(']');
            if (end > 5) {
                return description.substring(5, end);
            }
        }

        String lowerName = tenDV == null ? "" : tenDV.toLowerCase();
        if (lowerName.contains("giặt") || lowerName.contains("dọn phòng")) {
            return "Buồng phòng";
        }
        if (lowerName.contains("nước") || lowerName.contains("buffet") || lowerName.contains("mì") || lowerName.contains("ăn")) {
            return "Ăn uống";
        }
        if (lowerName.contains("spa") || lowerName.contains("massage") || lowerName.contains("gym")) {
            return "Thư giãn";
        }
        if (lowerName.contains("đưa đón") || lowerName.contains("xe")) {
            return "Vận chuyển";
        }
        return "Tiện ích";
    }

    private String cleanDescription(String description) {
        if (description == null) {
            return "";
        }
        if (description.startsWith("[CAT:")) {
            int end = description.indexOf(']');
            if (end >= 0 && end + 1 < description.length()) {
                return description.substring(end + 1).trim();
            }
        }
        return description;
    }

    private String normalizeStatus(String status) {
        return "NgungHoatDong".equalsIgnoreCase(status) || "Tam dung".equalsIgnoreCase(status) ? "NgungHoatDong" : "DangHoatDong";
    }
}
