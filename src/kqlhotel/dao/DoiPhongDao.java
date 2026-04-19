package kqlhotel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import kqlhotel.entity.DoiPhongRoomOption;
import kqlhotel.entity.DoiPhongSearchResult;

public class DoiPhongDao {

    public List<DoiPhongSearchResult> searchBookings(String maDatPhong, String tenKhach, String soDienThoai, String maPhong) {
        List<DoiPhongSearchResult> results = new ArrayList<>();
        String sql =
            "SELECT ctdp.maCTDP, dp.maDatPhong, kh.maKH, kh.hoTenKH, kh.sdt, kh.CCCD, " +
            "ctdp.maPhong, lp.tenLoaiPhong, ctdp.ngayNhanDuKien, ctdp.ngayTraDuKien, ctdp.soLuongNguoiO " +
            "FROM ChiTietDatPhong ctdp " +
            "JOIN DatPhong dp ON dp.maDatPhong = ctdp.maDatPhong " +
            "JOIN KhachHang kh ON kh.maKH = dp.maKH " +
            "JOIN Phong p ON p.maPhong = ctdp.maPhong " +
            "JOIN LoaiPhong lp ON lp.maLoaiPhong = p.maLoaiPhong " +
            "WHERE (? = '' OR dp.maDatPhong LIKE ?) " +
            "AND (? = '' OR kh.hoTenKH LIKE ?) " +
            "AND (? = '' OR kh.sdt LIKE ?) " +
            "AND (? = '' OR ctdp.maPhong LIKE ?) " +
            "ORDER BY dp.ngayNhanDuKien DESC, dp.maDatPhong DESC";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maDatPhong);
            ps.setString(2, "%" + maDatPhong + "%");
            ps.setString(3, tenKhach);
            ps.setString(4, "%" + tenKhach + "%");
            ps.setString(5, soDienThoai);
            ps.setString(6, "%" + soDienThoai + "%");
            ps.setString(7, maPhong);
            ps.setString(8, "%" + maPhong + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DoiPhongSearchResult item = new DoiPhongSearchResult();
                    item.setMaChiTietDatPhong(rs.getString("maCTDP"));
                    item.setMaDatPhong(rs.getString("maDatPhong"));
                    item.setMaKhachHang(rs.getString("maKH"));
                    item.setTenKhachHang(rs.getString("hoTenKH"));
                    item.setSoDienThoai(rs.getString("sdt"));
                    item.setCccd(rs.getString("CCCD"));
                    item.setMaPhongHienTai(rs.getString("maPhong"));
                    item.setLoaiPhongHienTai(rs.getString("tenLoaiPhong"));
                    Timestamp ngayNhan = rs.getTimestamp("ngayNhanDuKien");
                    Timestamp ngayTra = rs.getTimestamp("ngayTraDuKien");
                    item.setNgayNhan(ngayNhan != null ? ngayNhan.toLocalDateTime() : null);
                    item.setNgayTra(ngayTra != null ? ngayTra.toLocalDateTime() : null);
                    item.setSoLuongNguoiO(rs.getInt("soLuongNguoiO"));
                    results.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    public List<DoiPhongRoomOption> getAvailableRooms(String currentRoom) {
        List<DoiPhongRoomOption> rooms = new ArrayList<>();
        String sql =
            "SELECT p.maPhong, lp.tenLoaiPhong, p.tang, p.trangThaiPhong " +
            "FROM Phong p " +
            "JOIN LoaiPhong lp ON lp.maLoaiPhong = p.maLoaiPhong " +
            "WHERE p.trangThaiPhong = 'Trong' AND p.maPhong <> ? " +
            "ORDER BY p.tang, p.maPhong";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, currentRoom == null ? "" : currentRoom);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DoiPhongRoomOption room = new DoiPhongRoomOption();
                    room.setMaPhong(rs.getString("maPhong"));
                    room.setTenLoaiPhong(rs.getString("tenLoaiPhong"));
                    room.setTang(rs.getInt("tang"));
                    room.setTrangThaiPhong(rs.getString("trangThaiPhong"));
                    rooms.add(room);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rooms;
    }

    public boolean changeRoom(String maChiTietDatPhong, String newRoom) {
        String selectSql = "SELECT maPhong FROM ChiTietDatPhong WHERE maCTDP = ?";
        String updateDetailSql = "UPDATE ChiTietDatPhong SET maPhong = ? WHERE maCTDP = ?";
        String updateOldRoomSql = "UPDATE Phong SET trangThaiPhong = 'Trong' WHERE maPhong = ?";
        String updateNewRoomSql = "UPDATE Phong SET trangThaiPhong = 'DaDat' WHERE maPhong = ?";

        try (Connection conn = ConnectDB.getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                String oldRoom;
                try (PreparedStatement selectPs = conn.prepareStatement(selectSql)) {
                    selectPs.setString(1, maChiTietDatPhong);
                    try (ResultSet rs = selectPs.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }
                        oldRoom = rs.getString("maPhong");
                    }
                }

                try (PreparedStatement updateDetailPs = conn.prepareStatement(updateDetailSql);
                     PreparedStatement updateOldRoomPs = conn.prepareStatement(updateOldRoomSql);
                     PreparedStatement updateNewRoomPs = conn.prepareStatement(updateNewRoomSql)) {
                    updateDetailPs.setString(1, newRoom);
                    updateDetailPs.setString(2, maChiTietDatPhong);
                    updateDetailPs.executeUpdate();

                    updateOldRoomPs.setString(1, oldRoom);
                    updateOldRoomPs.executeUpdate();

                    updateNewRoomPs.setString(1, newRoom);
                    updateNewRoomPs.executeUpdate();
                }

                conn.commit();
                conn.setAutoCommit(autoCommit);
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                conn.setAutoCommit(autoCommit);
                throw ex;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
