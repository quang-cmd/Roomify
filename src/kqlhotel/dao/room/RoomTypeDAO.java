package kqlhotel.dao.room;

import kqlhotel.dao.ConnectDB;
import kqlhotel.dao.DAO_Interface;
import kqlhotel.entity.RoomType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomTypeDAO implements DAO_Interface<RoomType> {

    @Override
    public List<RoomType> getAll() {
        List<RoomType> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM LoaiPhong")) {
            while (rs.next()) {
                list.add(mapResultSetToRoomType(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public RoomType getById(String id) {
        RoomType roomType = null;
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT * FROM LoaiPhong WHERE maLoaiPhong = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                roomType = mapResultSetToRoomType(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roomType;
    }

    @Override
    public boolean create(RoomType roomType) {
        String sql = "INSERT INTO LoaiPhong (maLoaiPhong, tenLoaiPhong, soLuongPhong, giaPhong, sucChuaToiDa, dienTich, moTa, tienNghi) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, roomType.getRoomTypeId());
            pstmt.setString(2, roomType.getRoomTypeName());
            pstmt.setInt(3, roomType.getRoomCount());
            pstmt.setDouble(4, roomType.getPrice());
            pstmt.setInt(5, roomType.getMaxCapacity());
            pstmt.setDouble(6, roomType.getArea());
            pstmt.setString(7, roomType.getDescription());
            pstmt.setString(8, roomType.getAmenities());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(RoomType roomType) {
        String sql = "UPDATE LoaiPhong SET tenLoaiPhong = ?, soLuongPhong = ?, giaPhong = ?, sucChuaToiDa = ?, dienTich = ?, moTa = ?, tienNghi = ? WHERE maLoaiPhong = ?";
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, roomType.getRoomTypeName());
            pstmt.setInt(2, roomType.getRoomCount());
            pstmt.setDouble(3, roomType.getPrice());
            pstmt.setInt(4, roomType.getMaxCapacity());
            pstmt.setDouble(5, roomType.getArea());
            pstmt.setString(6, roomType.getDescription());
            pstmt.setString(7, roomType.getAmenities());
            pstmt.setString(8, roomType.getRoomTypeId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM LoaiPhong WHERE maLoaiPhong = ?";
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private RoomType mapResultSetToRoomType(ResultSet rs) throws SQLException {
        RoomType lp = new RoomType();
        lp.setRoomTypeId(rs.getString("maLoaiPhong"));
        lp.setRoomTypeName(rs.getString("tenLoaiPhong"));
        lp.setRoomCount(rs.getInt("soLuongPhong"));
        lp.setPrice(rs.getDouble("giaPhong"));
        lp.setMaxCapacity(rs.getInt("sucChuaToiDa"));
        lp.setArea(rs.getDouble("dienTich"));
        lp.setDescription(rs.getString("moTa"));
        lp.setAmenities(rs.getString("tienNghi"));
        return lp;
    }
}