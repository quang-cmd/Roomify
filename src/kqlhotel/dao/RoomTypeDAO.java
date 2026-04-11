package kqlhotel.dao;

import kqlhotel.entity.RoomType;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomTypeDAO {
    public List<RoomType> getAll() {
        List<RoomType> list = new ArrayList<>();
        String sql = "SELECT * FROM LoaiPhong";
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
                list.add(rt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
