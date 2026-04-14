package kqlhotel.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoiPhongDao {

    public String getCurrentRoom(String maDatPhong) {
        try {
            Connection conn = ConnectDB.getInstance().getConnection();
            String sql = "SELECT MaPhong FROM DatPhong WHERE MaDatPhong=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, maDatPhong);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("MaPhong");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public List<String> getAvailableRooms() {
        List<String> list = new ArrayList<>();

        try {
            Connection conn = ConnectDB.getInstance().getConnection();
            String sql = "SELECT MaPhong FROM Phong WHERE TrangThai='Trong'";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(rs.getString("MaPhong"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean changeRoom(String maDatPhong, String newRoom) {
        try {
            Connection conn = ConnectDB.getInstance().getConnection();

            // Cập nhật phòng mới vào đặt phòng
            String sql1 = "UPDATE DatPhong SET MaPhong=? WHERE MaDatPhong=?";
            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ps1.setString(1, newRoom);
            ps1.setString(2, maDatPhong);

            // Cập nhật trạng thái phòng
            String sql2 = "UPDATE Phong SET TrangThai='Trong' WHERE MaPhong=(SELECT MaPhong FROM DatPhong WHERE MaDatPhong=?)";
            PreparedStatement ps2 = conn.prepareStatement(sql2);
            ps2.setString(1, maDatPhong);

            String sql3 = "UPDATE Phong SET TrangThai='Dang o' WHERE MaPhong=?";
            PreparedStatement ps3 = conn.prepareStatement(sql3);
            ps3.setString(1, newRoom);

            ps1.executeUpdate();
            ps2.executeUpdate();
            ps3.executeUpdate();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}