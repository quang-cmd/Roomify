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
                list.add(mapRoomWithDetails(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Room getById(String id) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT maPhong, maLoaiPhong, tang, trangThaiPhong FROM Phong WHERE maPhong = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRoomWithDetails(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Room mapRoomWithDetails(ResultSet rs) throws SQLException {
        Room r = new Room();
        r.setMaPhong(rs.getString("maPhong"));
        r.setLoaiPhong(rs.getString("maLoaiPhong"));
        r.setTang(rs.getInt("tang"));
        r.setTrangThaiPhong(rs.getString("trangThaiPhong"));
        
        // Load RoomType object for Premium UI
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

    public boolean updateStatus(String maPhong, String status) {
        String sql = "UPDATE Phong SET trangThaiPhong = ? WHERE maPhong = ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, maPhong);
            return ps.executeUpdate() > 0;
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
            ps.setString(2, r.getLoaiPhong());
            ps.setInt(3, r.getTang());
            ps.setString(4, r.getTrangThaiPhong());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(Room r) {
        String sql = "UPDATE Phong SET maLoaiPhong = ?, tang = ?, trangThaiPhong = ? WHERE maPhong = ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, r.getLoaiPhong());
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
}
