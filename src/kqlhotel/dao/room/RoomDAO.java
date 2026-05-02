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
            Connection con = ConnectDB.getConnection();
            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(mapResultSetToRoom(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Room getById(String id) {
        String sql = "SELECT p.*, lp.tenLoaiPhong, lp.soLuongPhong, lp.giaPhong, lp.sucChuaToiDa, lp.dienTich, lp.moTa, lp.tienNghi " +
                     "FROM Phong p JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong WHERE p.maPhong = ?";
        try {
            Connection con = ConnectDB.getConnection();
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setString(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToRoom(rs);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Room> search(String roomId, String roomTypeName, String status) {
        List<Room> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT p.*, lp.tenLoaiPhong, lp.soLuongPhong, lp.giaPhong, lp.sucChuaToiDa, lp.dienTich, lp.moTa, lp.tienNghi " +
            "FROM Phong p JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong WHERE 1=1"
        );

        if (roomId != null && !roomId.trim().isEmpty()) {
            sql.append(" AND p.maPhong LIKE ?");
        }
        if (roomTypeName != null && !roomTypeName.trim().isEmpty() && !roomTypeName.equals("All room types")) {
            sql.append(" AND lp.tenLoaiPhong = ?");
        }
        if (status != null && !status.trim().isEmpty() && !status.equals("All statuses")) {
            sql.append(" AND p.trangThaiPhong = ?");
        }

        try {
            Connection con = ConnectDB.getConnection();
            try (PreparedStatement pstmt = con.prepareStatement(sql.toString())) {
                int index = 1;
                if (roomId != null && !roomId.trim().isEmpty()) {
                    pstmt.setString(index++, "%" + roomId.trim() + "%");
                }
                if (roomTypeName != null && !roomTypeName.trim().isEmpty() && !roomTypeName.equals("All room types")) {
                    pstmt.setString(index++, roomTypeName);
                }
                if (status != null && !status.trim().isEmpty() && !status.equals("All statuses")) {
                    pstmt.setString(index++, status);
                }

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToRoom(rs));
                    }
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
            Connection con = ConnectDB.getConnection();
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setString(1, p.getRoomId());
                pstmt.setString(2, p.getRoomType().getRoomTypeId());
                pstmt.setInt(3, p.getFloor());
                pstmt.setString(4, p.getStatus());
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Room p) {
        String sql = "UPDATE Phong SET maLoaiPhong = ?, tang = ?, trangThaiPhong = ? WHERE maPhong = ?";
        try {
            Connection con = ConnectDB.getConnection();
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setString(1, p.getRoomType().getRoomTypeId());
                pstmt.setInt(2, p.getFloor());
                pstmt.setString(3, p.getStatus());
                pstmt.setString(4, p.getRoomId());
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateStatus(String roomId, String status) {
        String sql = "UPDATE Phong SET trangThaiPhong = ? WHERE maPhong = ?";
        try {
            Connection con = ConnectDB.getConnection();
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setString(1, status);
                pstmt.setString(2, roomId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Room mapResultSetToRoom(ResultSet rs) throws SQLException {
        RoomType rt = new RoomType();
        rt.setRoomTypeId(rs.getString("maLoaiPhong"));
        rt.setRoomTypeName(rs.getString("tenLoaiPhong"));
        rt.setRoomCount(rs.getInt("soLuongPhong"));
        rt.setPrice(rs.getDouble("giaPhong"));
        rt.setMaxCapacity(rs.getInt("sucChuaToiDa"));
        rt.setArea(rs.getDouble("dienTich"));
        rt.setDescription(rs.getString("moTa"));
        rt.setAmenities(rs.getString("tienNghi"));

        Room r = new Room();
        r.setRoomId(rs.getString("maPhong"));
        r.setFloor(rs.getInt("tang"));
        r.setStatus(rs.getString("trangThaiPhong"));
        r.setRoomType(rt);
        r.setDeposit(0.0); // Default or fetch from DB if column exists
        return r;
    }
}
