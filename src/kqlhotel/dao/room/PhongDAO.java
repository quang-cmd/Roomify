package kqlhotel.dao.room;

import kqlhotel.dao.ConnectDB;
import kqlhotel.entity.LoaiPhong;
import kqlhotel.entity.Phong;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PhongDAO {
    public List<Phong> getAll() {
        List<Phong> list = new ArrayList<>();
        String sql = "SELECT p.*, lp.tenLoaiPhong, lp.soLuongPhong, lp.giaPhong, lp.sucChuaToiDa, lp.dienTich, lp.moTa, lp.tienNghi " +
                     "FROM Phong p JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong";

        try {
            ConnectDB.getInstance().connect();
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Lỗi kết nối database: " + e.getMessage());
            return list;
        }

        Connection con = ConnectDB.getInstance().getConnection();
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                LoaiPhong lp = new LoaiPhong(
                    rs.getString("maLoaiPhong"), // maLoaiPhong
                    rs.getString("tenLoaiPhong"),
                    rs.getInt("soLuongPhong"),
                    rs.getDouble("giaPhong"),
                    rs.getInt("sucChuaToiDa"),
                    rs.getDouble("dienTich"),
                    rs.getString("moTa"),
                    rs.getString("tienNghi")
                );

                Phong p = new Phong(
                    rs.getString("maPhong"),
                    0.0, // Phong table doesn't have tienCoc
                    lp,
                    rs.getInt("tang"),
                    rs.getString("trangThaiPhong")
                );
                list.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn Phong: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public List<Phong> search(String maPhong, String tenLoaiPhong, String trangThaiPhong) {
        List<Phong> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT p.*, lp.tenLoaiPhong, lp.soLuongPhong, lp.giaPhong, lp.sucChuaToiDa, lp.dienTich, lp.moTa, lp.tienNghi " +
            "FROM Phong p JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong WHERE 1=1"
        );

        if (maPhong != null && !maPhong.trim().isEmpty()) {
            sql.append(" AND p.maPhong LIKE ?");
        }
        if (tenLoaiPhong != null && !tenLoaiPhong.trim().isEmpty() && !tenLoaiPhong.equals("Tất cả loại phòng")) {
            sql.append(" AND lp.tenLoaiPhong = ?");
        }
        if (trangThaiPhong != null && !trangThaiPhong.trim().isEmpty() && !trangThaiPhong.equals("Tất cả trạng thái")) {
            sql.append(" AND p.trangThaiPhong = ?");
        }

        try {
            ConnectDB.getInstance().connect();
        } catch (SQLException | ClassNotFoundException e) {
            return list;
        }

        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pstmt = con.prepareStatement(sql.toString())) {
             
            int index = 1;
            if (maPhong != null && !maPhong.trim().isEmpty()) {
                pstmt.setString(index++, "%" + maPhong.trim() + "%");
            }
            if (tenLoaiPhong != null && !tenLoaiPhong.trim().isEmpty() && !tenLoaiPhong.equals("Tất cả loại phòng")) {
                pstmt.setString(index++, tenLoaiPhong);
            }
            if (trangThaiPhong != null && !trangThaiPhong.trim().isEmpty() && !trangThaiPhong.equals("Tất cả trạng thái")) {
                pstmt.setString(index++, trangThaiPhong);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    LoaiPhong lp = new LoaiPhong(
                        rs.getString("maLoaiPhong"),
                        rs.getString("tenLoaiPhong"),
                        rs.getInt("soLuongPhong"),
                        rs.getDouble("giaPhong"),
                        rs.getInt("sucChuaToiDa"),
                        rs.getDouble("dienTich"),
                        rs.getString("moTa"),
                        rs.getString("tienNghi")
                    );

                    Phong p = new Phong(
                        rs.getString("maPhong"),
                        0.0, // Phong table doesn't have tienCoc
                        lp,
                        rs.getInt("tang"),
                        rs.getString("trangThaiPhong")
                    );
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    public boolean create(Phong p) {
        String sql = "INSERT INTO Phong (maPhong, maLoaiPhong, tang, trangThaiPhong) VALUES (?, ?, ?, ?)";
        try {
            ConnectDB.getInstance().connect();
            Connection con = ConnectDB.getInstance().getConnection();
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setString(1, p.getMaPhong());
                pstmt.setString(2, p.getLoaiPhong().getMaLoaiPhong());
                pstmt.setInt(3, p.getTang());
                pstmt.setString(4, p.getTrangThaiPhong());
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Phong p) {
        String sql = "UPDATE Phong SET maLoaiPhong = ?, tang = ?, trangThaiPhong = ? WHERE maPhong = ?";
        try {
            ConnectDB.getInstance().connect();
            Connection con = ConnectDB.getInstance().getConnection();
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setString(1, p.getLoaiPhong().getMaLoaiPhong());
                pstmt.setInt(2, p.getTang());
                pstmt.setString(3, p.getTrangThaiPhong());
                pstmt.setString(4, p.getMaPhong());
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(String maPhong) {
        String sql = "DELETE FROM Phong WHERE maPhong = ?";
        try {
            ConnectDB.getInstance().connect();
            Connection con = ConnectDB.getInstance().getConnection();
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setString(1, maPhong);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateStatus(String maPhong, String trangThai) {
        String sql = "UPDATE Phong SET trangThaiPhong = ? WHERE maPhong = ?";
        try {
            ConnectDB.getInstance().connect();
            Connection con = ConnectDB.getInstance().getConnection();
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setString(1, trangThai);
                pstmt.setString(2, maPhong);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Phong> search(String query) {
        return search(query, null, null);
    }
}
