package kqlhotel.dao.room;

import kqlhotel.dao.connectDB.*;

import kqlhotel.entity.RoomType;
import java.sql.*;

public class RoomTypeDAO {
    public RoomType getById(String id) {
        RoomType roomType = null;
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM LoaiPhong WHERE maLoaiPhong = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                roomType = new RoomType();
                roomType.setMaLoaiPhong(rs.getString("maLoaiPhong"));
                roomType.setTenLoaiPhong(rs.getString("tenLoaiPhong"));
                roomType.setGiaPhong(rs.getDouble("giaPhong"));
                // ... set others if needed
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roomType;
    }
}
