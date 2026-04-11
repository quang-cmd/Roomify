package kqlhotel.dao.room;

import kqlhotel.dao.connectDB.*;

import kqlhotel.entity.Room;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO implements DAO_Interface<Room> {
    @Override
    public List<Room> getAll() {
        List<Room> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM Phong";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Room p = new Room();
                p.setMaPhong(rs.getString("maPhong"));
                p.setTienCoc(rs.getDouble("tienCoc"));
                p.setLoaiPhong(rs.getString("loaiPhong"));
                p.setTang(rs.getInt("tang"));
                p.setTrangThaiPhong(rs.getString("trangThaiPhong"));
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Room getById(String id) {
        Room p = null;
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM Phong WHERE maPhong = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                p = new Room();
                p.setMaPhong(rs.getString("maPhong"));
                p.setTienCoc(rs.getDouble("tienCoc"));
                p.setLoaiPhong(rs.getString("loaiPhong"));
                p.setTang(rs.getInt("tang"));
                p.setTrangThaiPhong(rs.getString("trangThaiPhong"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return p;
    }

    public boolean updateStatus(String maPhong, String status) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "UPDATE Phong SET trangThaiPhong = ? WHERE maPhong = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setString(2, maPhong);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean create(Room p) { return false; } // Not needed for checkout
    @Override
    public boolean update(Room p) { return false; } // Handled by updateStatus
    @Override
    public boolean delete(String id) { return false; }
}
