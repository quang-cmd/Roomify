package kqlhotel.dao.room;

import kqlhotel.dao.ConnectDB;
import kqlhotel.dao.DAO_Interface;
import kqlhotel.entity.Room;
import kqlhotel.entity.RoomType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO implements DAO_Interface<Room> {
    private RoomTypeDAO roomTypeDAO = new RoomTypeDAO();

    private Room mapResultSetToRoom(ResultSet rs) throws SQLException {
        Room p = new Room();
        p.setMaPhong(rs.getString("maPhong"));
        RoomType rt = new RoomType();
        rt.setMaLoaiPhong(rs.getString("maLoaiPhong"));
        p.setLoaiPhong(rt);
        p.setTang(rs.getInt("tang"));
        p.setTrangThaiPhong(rs.getString("trangThaiPhong"));
        return p;
    }

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
        RoomType rt = new RoomType();
        rt.setMaLoaiPhong(rs.getString("maLoaiPhong"));
        p.setLoaiPhong(rt);
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

    public boolean create(Room r) {
        String sql = "INSERT INTO Phong (maPhong, maLoaiPhong, tang, trangThaiPhong) VALUES (?, ?, ?, ?)";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, r.getMaPhong());
            ps.setString(2, r.getLoaiPhong().getMaLoaiPhong());
            ps.setInt(3, r.getTang());
            ps.setString(4, r.getTrangThaiPhong());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Room r) {
        String sql = "UPDATE Phong SET maLoaiPhong = ?, tang = ?, trangThaiPhong = ? WHERE maPhong = ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, r.getLoaiPhong().getMaLoaiPhong());
            ps.setInt(2, r.getTang());
            ps.setString(3, r.getTrangThaiPhong());
            ps.setString(4, r.getMaPhong());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM Phong WHERE maPhong = ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- Premium UI Support Methods ---

    private Room mapRoomWithDetails(ResultSet rs) throws SQLException {
        Room p = mapResultSetToRoom(rs);
        try {
            RoomType rt = new RoomType();
            rt.setMaLoaiPhong(rs.getString("maLoaiPhong"));
            rt.setTenLoaiPhong(rs.getString("tenLoaiPhong"));
            rt.setGiaPhong(rs.getDouble("giaPhong"));
            p.setLoaiPhong(rt);
        } catch (SQLException e) {
            // Field might be missing in simple queries
        }
        return p;
    }

    public List<Room> getAllDetailed() {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT p.*, lp.tenLoaiPhong, lp.giaPhong FROM Phong p JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRoomWithDetails(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Room> search(String query) {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT p.*, lp.tenLoaiPhong, lp.giaPhong FROM Phong p JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong WHERE p.maPhong LIKE ? OR lp.tenLoaiPhong LIKE ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + query + "%");
            ps.setString(2, "%" + query + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRoomWithDetails(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}