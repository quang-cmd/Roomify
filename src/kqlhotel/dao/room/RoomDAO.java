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
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "INSERT INTO Phong (maPhong, maLoaiPhong, tang, trangThaiPhong) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, p.getMaPhong());
            pstmt.setString(2, p.getLoaiPhong());
            pstmt.setInt(3, p.getTang());
            pstmt.setString(4, p.getTrangThaiPhong());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(Room p) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "UPDATE Phong SET maLoaiPhong = ?, tang = ?, trangThaiPhong = ? WHERE maPhong = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, p.getLoaiPhong());
            pstmt.setInt(2, p.getTang());
            pstmt.setString(3, p.getTrangThaiPhong());
            pstmt.setString(4, p.getMaPhong());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(String id) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "DELETE FROM Phong WHERE maPhong = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Premium UI Support Methods
    public List<Room> getAllWithDetails() {
        List<Room> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT maPhong, maLoaiPhong, tang, trangThaiPhong FROM Phong";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(mapRoomWithDetails(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Room mapRoomWithDetails(ResultSet rs) throws SQLException {
        Room r = mapRoom(rs);
        RoomType rt = roomTypeDAO.getById(r.getLoaiPhong());
        r.setRoomType(rt);
        return r;
    }

    public List<Room> search(String roomId, String typeName, String status) {
        List<Room> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT p.* FROM Phong p JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong WHERE 1=1"
        );
        if (roomId != null && !roomId.isBlank()) sql.append(" AND p.maPhong LIKE ?");
        if (typeName != null && !typeName.isBlank()) sql.append(" AND lp.tenLoaiPhong LIKE ?");
        if (status != null && !status.isBlank()) sql.append(" AND p.trangThaiPhong = ?");

        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int idx = 1;
            if (roomId != null && !roomId.isBlank()) ps.setString(idx++, "%" + roomId + "%");
            if (typeName != null && !typeName.isBlank()) ps.setString(idx++, "%" + typeName + "%");
            if (status != null && !status.isBlank()) ps.setString(idx++, status);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRoomWithDetails(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
