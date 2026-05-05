package kqlhotel.dao.swaproom;

import kqlhotel.dao.ConnectDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import kqlhotel.entity.SwapRoomOption;
import kqlhotel.entity.SwapRoomSearchResult;

public class SwapRoomDAO {

    public List<SwapRoomSearchResult> searchBookings(String bookingId, String guestName, String phoneNumber, String roomId) {
        List<SwapRoomSearchResult> results = new ArrayList<>();
        String sql =
            "SELECT dp.maDatPhong, kh.maKH, kh.hoTenKH, kh.sdt, kh.CCCD, " +
            "ctdp.maPhong, p.maLoaiPhong, lp.tenLoaiPhong, lp.sucChuaToiDa, " +
            "ctdp.ngayNhanDuKien, ctdp.ngayTraDuKien, ctdp.soLuongNguoiO " +
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

        try (Connection conn = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookingId);
            ps.setString(2, "%" + bookingId + "%");
            ps.setString(3, guestName);
            ps.setString(4, "%" + guestName + "%");
            ps.setString(5, phoneNumber);
            ps.setString(6, "%" + phoneNumber + "%");
            ps.setString(7, roomId);
            ps.setString(8, "%" + roomId + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SwapRoomSearchResult item = new SwapRoomSearchResult();
                    item.setBookingDetailId(rs.getString("maDatPhong") + ":" + rs.getString("maPhong"));
                    item.setBookingId(rs.getString("maDatPhong"));
                    item.setCustomerId(rs.getString("maKH"));
                    item.setCustomerName(rs.getString("hoTenKH"));
                    item.setPhoneNumber(rs.getString("sdt"));
                    item.setIdCard(rs.getString("CCCD"));
                    item.setCurrentRoomId(rs.getString("maPhong"));
                    item.setCurrentRoomTypeId(rs.getString("maLoaiPhong"));
                    item.setCurrentRoomTypeName(rs.getString("tenLoaiPhong"));
                    Timestamp ngayNhan = rs.getTimestamp("ngayNhanDuKien");
                    Timestamp ngayTra = rs.getTimestamp("ngayTraDuKien");
                    item.setCheckInDate(ngayNhan != null ? ngayNhan.toLocalDateTime() : null);
                    item.setCheckOutDate(ngayTra != null ? ngayTra.toLocalDateTime() : null);
                    item.setOccupantCount(rs.getInt("soLuongNguoiO"));
                    item.setCurrentRoomMaxCapacity(rs.getInt("sucChuaToiDa"));
                    results.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    public List<SwapRoomOption> getAvailableRooms(SwapRoomSearchResult booking) {
        List<SwapRoomOption> rooms = new ArrayList<>();
        String sql =
            "SELECT p.maPhong, p.maLoaiPhong, lp.tenLoaiPhong, lp.sucChuaToiDa, p.tang, p.trangThaiPhong " +
            "FROM Phong p " +
            "JOIN LoaiPhong lp ON lp.maLoaiPhong = p.maLoaiPhong " +
            "WHERE p.trangThaiPhong = 'Trong' " +
            "AND p.maPhong <> ? " +
            "AND lp.sucChuaToiDa >= ? " +
            "ORDER BY CASE WHEN p.maLoaiPhong = ? THEN 0 ELSE 1 END, " +
            "lp.sucChuaToiDa ASC, p.tang ASC, p.maPhong ASC";

        try (Connection conn = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, booking == null ? "" : booking.getCurrentRoomId());
            ps.setInt(2, booking == null ? 1 : Math.max(1, booking.getOccupantCount()));
            ps.setString(3, booking == null ? "" : booking.getCurrentRoomTypeId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SwapRoomOption room = new SwapRoomOption();
                    room.setRoomId(rs.getString("maPhong"));
                    room.setRoomTypeId(rs.getString("maLoaiPhong"));
                    room.setRoomTypeName(rs.getString("tenLoaiPhong"));
                    room.setMaxCapacity(rs.getInt("sucChuaToiDa"));
                    room.setFloor(rs.getInt("tang"));
                    room.setStatus(rs.getString("trangThaiPhong"));
                    rooms.add(room);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rooms;
    }

    public boolean changeRoom(String maDatPhong, String oldRoom, String newRoom) {
        String updateDetailSql = "UPDATE ChiTietDatPhong SET maPhong = ? WHERE maDatPhong = ? AND maPhong = ?";
        String updateInvoiceSql = "UPDATE ChiTietHoaDon SET maPhong = ? WHERE maPhong = ? AND maHD IN (SELECT maHD FROM HoaDon WHERE maDatPhong = ?)";
        
        String updateOldRoomSql = "UPDATE Phong SET trangThaiPhong = 'Trong' WHERE maPhong = ?";
        String updateNewRoomSql = "UPDATE Phong SET trangThaiPhong = 'DangSuDung' WHERE maPhong = ?";

        try (Connection conn = ConnectDB.getInstance().getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement updateDetailPs = conn.prepareStatement(updateDetailSql);
                     PreparedStatement updateInvoicePs = conn.prepareStatement(updateInvoiceSql);
                     PreparedStatement updateOldRoomPs = conn.prepareStatement(updateOldRoomSql);
                     PreparedStatement updateNewRoomPs = conn.prepareStatement(updateNewRoomSql)) {
                    
                    updateDetailPs.setString(1, newRoom);
                    updateDetailPs.setString(2, maDatPhong);
                    updateDetailPs.setString(3, oldRoom);
                    int affected = updateDetailPs.executeUpdate();
                    
                    if (affected == 0) {
                        conn.rollback();
                        return false;
                    }

                    updateInvoicePs.setString(1, newRoom);
                    updateInvoicePs.setString(2, oldRoom);
                    updateInvoicePs.setString(3, maDatPhong);
                    updateInvoicePs.executeUpdate();

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
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
