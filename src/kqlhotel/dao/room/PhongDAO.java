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
                     "FROM Phong p JOIN LoaiPhong lp ON p.loaiPhong = lp.maLoaiPhong";

        try {
            ConnectDB.getInstance().connect();
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Lỗi kết nối database: " + e.getMessage());
            return list;
        }

        try (Connection con = ConnectDB.getInstance().getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                LoaiPhong lp = new LoaiPhong(
                    rs.getString("loaiPhong"), // maLoaiPhong
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
                    rs.getDouble("tienCoc"),
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
            "FROM Phong p JOIN LoaiPhong lp ON p.loaiPhong = lp.maLoaiPhong WHERE 1=1"
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

        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql.toString())) {
             
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
                        rs.getString("loaiPhong"),
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
                        rs.getDouble("tienCoc"),
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
}
