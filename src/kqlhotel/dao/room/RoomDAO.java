package kqlhotel.dao.room;

import kqlhotel.dao.ConnectDB;
import kqlhotel.entity.RoomType;
import kqlhotel.entity.Room;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {
    public List<Room> getAll() {
        List<Room> list = new ArrayList<>();
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
                RoomType lp = new RoomType(
                    rs.getString("maLoaiPhong"), // maLoaiPhong
                    rs.getString("tenLoaiPhong"),
                    rs.getInt("soLuongPhong"),
                    rs.getDouble("giaPhong"),
                    rs.getInt("sucChuaToiDa"),
                    rs.getDouble("dienTich"),
                    rs.getString("moTa"),
                    rs.getString("tienNghi")
                );

                Room p = new Room(
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

    public Room getById(String id) {
        String sql = "SELECT p.*, lp.tenLoaiPhong, lp.soLuongPhong, lp.giaPhong, lp.sucChuaToiDa, lp.dienTich, lp.moTa, lp.tienNghi " +
                     "FROM Phong p JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong WHERE p.maPhong = ?";
        try {
            ConnectDB.getInstance().connect();
        } catch (SQLException | ClassNotFoundException e) {
            return null;
        }

        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    RoomType lp = new RoomType(
                        rs.getString("maLoaiPhong"),
                        rs.getString("tenLoaiPhong"),
                        rs.getInt("soLuongPhong"),
                        rs.getDouble("giaPhong"),
                        rs.getInt("sucChuaToiDa"),
                        rs.getDouble("dienTich"),
                        rs.getString("moTa"),
                        rs.getString("tienNghi")
                    );

                    return new Room(
                        rs.getString("maPhong"),
                        0.0,
                        lp,
                        rs.getInt("tang"),
                        rs.getString("trangThaiPhong")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Room> search(String maPhong, String tenLoaiPhong, String trangThaiPhong) {
        List<Room> list = new ArrayList<>();
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
                    RoomType lp = new RoomType(
                        rs.getString("maLoaiPhong"),
                        rs.getString("tenLoaiPhong"),
                        rs.getInt("soLuongPhong"),
                        rs.getDouble("giaPhong"),
                        rs.getInt("sucChuaToiDa"),
                        rs.getDouble("dienTich"),
                        rs.getString("moTa"),
                        rs.getString("tienNghi")
                    );

                    Room p = new Room(
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
    public boolean create(Room p) {
        String sql = "INSERT INTO Phong (maPhong, maLoaiPhong, tang, trangThaiPhong) VALUES (?, ?, ?, ?)";
        try {
            ConnectDB.getInstance().connect();
            Connection con = ConnectDB.getInstance().getConnection();
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setString(1, p.getRoomId());
                pstmt.setString(2, p.getRoomType().getRoomTypeId());
                pstmt.setInt(3, p.getFloor());
                pstmt.setString(4, p.getStatus());
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Room p) {
        String sql = "UPDATE Phong SET maLoaiPhong = ?, tang = ?, trangThaiPhong = ? WHERE maPhong = ?";
        try {
            ConnectDB.getInstance().connect();
            Connection con = ConnectDB.getInstance().getConnection();
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setString(1, p.getRoomType().getRoomTypeId());
                pstmt.setInt(2, p.getFloor());
                pstmt.setString(3, p.getStatus());
                pstmt.setString(4, p.getRoomId());
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateStatus(String roomId, String status) {
        String sql = "UPDATE Phong SET trangThaiPhong = ? WHERE maPhong = ?";
        try {
            ConnectDB.getInstance().connect();
            Connection con = ConnectDB.getInstance().getConnection();
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setString(1, status);
                pstmt.setString(2, roomId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
}
