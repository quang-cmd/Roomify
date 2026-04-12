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
            pstmt.setString(1, roomType.getMaLoaiPhong());
            pstmt.setString(2, roomType.getTenLoaiPhong());
            pstmt.setInt(3, roomType.getSoLuongPhong());
            pstmt.setDouble(4, roomType.getGiaPhong());
            pstmt.setInt(5, roomType.getSucChuaToiDa());
            pstmt.setDouble(6, roomType.getDienTich());
            pstmt.setString(7, roomType.getMoTa());
            pstmt.setString(8, roomType.getTienNghi());
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
            pstmt.setString(1, roomType.getTenLoaiPhong());
            pstmt.setInt(2, roomType.getSoLuongPhong());
            pstmt.setDouble(3, roomType.getGiaPhong());
            pstmt.setInt(4, roomType.getSucChuaToiDa());
            pstmt.setDouble(5, roomType.getDienTich());
            pstmt.setString(6, roomType.getMoTa());
            pstmt.setString(7, roomType.getTienNghi());
            pstmt.setString(8, roomType.getMaLoaiPhong());
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
        lp.setMaLoaiPhong(rs.getString("maLoaiPhong"));
        lp.setTenLoaiPhong(rs.getString("tenLoaiPhong"));
        lp.setSoLuongPhong(rs.getInt("soLuongPhong"));
        lp.setGiaPhong(rs.getDouble("giaPhong"));
        lp.setSucChuaToiDa(rs.getInt("sucChuaToiDa"));
        lp.setDienTich(rs.getDouble("dienTich"));
        lp.setMoTa(rs.getString("moTa"));
        lp.setTienNghi(rs.getString("tienNghi"));
        return lp;
    }
}
