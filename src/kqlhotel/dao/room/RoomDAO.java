package kqlhotel.dao.room;

import kqlhotel.dao.ConnectDB;
import kqlhotel.entity.RoomType;
import kqlhotel.entity.Room;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {
    public List<Room> getAll() {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM Phong p JOIN LoaiPhong lp ON p.loaiPhong = lp.maLoaiPhong";
        try (Connection con = ConnectDB.getInstance().getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                RoomType rt = new RoomType(
                    rs.getString("maLoaiPhong"),
                    rs.getString("tenLoaiPhong"),
                    rs.getInt("soLuongPhong"),
                    rs.getDouble("giaPhong"),
                    rs.getInt("sucChuaToiDa"),
                    rs.getDouble("dienTich"),
                    rs.getString("moTa"),
                    rs.getString("tienNghi")
                );
                Room r = new Room(
                    rs.getString("maPhong"),
                    rs.getDouble("tienCoc"),
                    rt,
                    rs.getInt("tang"),
                    rs.getString("trangThaiPhong")
                );
                list.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateStatus(String roomId, String status) {
        String sql = "UPDATE Phong SET trangThaiPhong = ? WHERE maPhong = ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setString(2, roomId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
