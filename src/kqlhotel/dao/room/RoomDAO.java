package kqlhotel.dao.room;

import kqlhotel.dao.ConnectDB;
import kqlhotel.dao.DAO_Interface;
import kqlhotel.entity.Room;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO implements DAO_Interface<Room> {

    @Override
    public List<Room> getAll() {
        List<Room> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT maPhong, maLoaiPhong, tang, trangThaiPhong FROM Phong";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Room p = mapRoom(rs);
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
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT maPhong, maLoaiPhong, tang, trangThaiPhong FROM Phong WHERE maPhong = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                p = mapRoom(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return p;
    }

    private Room mapRoom(ResultSet rs) throws SQLException {
        Room p = new Room();
        p.setMaPhong(rs.getString("maPhong"));
        p.setLoaiPhong(rs.getString("maLoaiPhong"));
        p.setTang(rs.getInt("tang"));
        p.setTrangThaiPhong(rs.getString("trangThaiPhong"));
        return p;
    }

    public boolean updateStatus(String maPhong, String status) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
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
    public boolean create(Room p) {
        return false;
    }

    @Override
    public boolean update(Room p) {
        return false;
    }

    @Override
    public boolean delete(String id) {
        return false;
    }
}