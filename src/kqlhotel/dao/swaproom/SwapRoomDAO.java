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
            "SELECT TRIM(dp.maDatPhong) as maDatPhong, TRIM(kh.maKH) as maKH, kh.hoTenKH, kh.sdt, kh.CCCD, " +
            "TRIM(ctdp.maPhong) as maPhong, TRIM(p.maLoaiPhong) as maLoaiPhong, lp.tenLoaiPhong, lp.sucChuaToiDa, " +
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
            "ORDER BY ctdp.ngayNhanDuKien DESC, dp.maDatPhong DESC";

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
            "SELECT TRIM(p.maPhong) as maPhong, TRIM(p.maLoaiPhong) as maLoaiPhong, lp.tenLoaiPhong, lp.sucChuaToiDa, p.tang, p.trangThaiPhong " +
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

    public boolean changeRoom(String maDatPhong, String oldRoom, String newRoom, String reason) {
        try (Connection conn = ConnectDB.getInstance().getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                // 1. Get current stay details
                String maHD = "";
                Timestamp ngayNhanThucTe = null;
                Timestamp ngayTraDuKien = null;
                double giaPhongCu = 0;
                double giaPhongMoi = 0;
                int soLuongKhach = 1;

                // Query old room stay info and prices
                String infoSql = 
                    "SELECT cthd.maHD, cthd.ngayNhanPhong, cthd.ngayTraPhong, lp_old.giaPhong as giaCu, lp_new.giaPhong as giaMoi, cthd.soDem, cthd.thanhTien, dp.maKH, hd.soLuongNguoiO " +
                    "FROM ChiTietHoaDon cthd " +
                    "JOIN HoaDon hd ON hd.maHD = cthd.maHD " +
                    "JOIN DatPhong dp ON dp.maDatPhong = hd.maDatPhong " +
                    "JOIN Phong p_old ON p_old.maPhong = cthd.maPhong " +
                    "JOIN LoaiPhong lp_old ON lp_old.maLoaiPhong = p_old.maLoaiPhong " +
                    "JOIN Phong p_new ON p_new.maPhong = ? " +
                    "JOIN LoaiPhong lp_new ON lp_new.maLoaiPhong = p_new.maLoaiPhong " +
                    "WHERE hd.maDatPhong = ? AND cthd.maPhong = ? AND cthd.ngayTraThucTe IS NULL";

                try (PreparedStatement ps = conn.prepareStatement(infoSql)) {
                    ps.setString(1, newRoom);
                    ps.setString(2, maDatPhong);
                    ps.setString(3, oldRoom);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            maHD = rs.getString("maHD");
                            ngayNhanThucTe = rs.getTimestamp("ngayNhanPhong");
                            ngayTraDuKien = rs.getTimestamp("ngayTraPhong");
                            giaPhongCu = rs.getDouble("giaCu");
                            giaPhongMoi = rs.getDouble("giaMoi");
                            soLuongKhach = rs.getInt("soLuongNguoiO");
                        } else {
                            // If not checked in (only in booking detail), do a simple update
                            String updateDetailSimple = "UPDATE ChiTietDatPhong SET maPhong = ?, ghiChu = ISNULL(ghiChu, '') + ? WHERE maDatPhong = ? AND maPhong = ?";
                            try (PreparedStatement psSimple = conn.prepareStatement(updateDetailSimple)) {
                                psSimple.setString(1, newRoom);
                                psSimple.setString(2, "\n[Đổi phòng trước nhận: " + reason + "]");
                                psSimple.setString(3, maDatPhong);
                                psSimple.setString(4, oldRoom);
                                psSimple.executeUpdate();
                            }
                            
                            // Update room statuses
                            String upOld = "UPDATE Phong SET trangThaiPhong = 'Trong' WHERE maPhong = ?";
                            String upNew = "UPDATE Phong SET trangThaiPhong = 'DangSuDung' WHERE maPhong = ? AND (SELECT trangThaiPhong FROM Phong WHERE maPhong = ?) = 'DangSuDung'";
                            // (Actually if not checked in, old might be 'Trong' already, but better be safe)
                            
                            conn.commit();
                            return true;
                        }
                    }
                }

                // 2. Calculate split
                Timestamp now = new Timestamp(System.currentTimeMillis());
                long nightsOld = java.time.temporal.ChronoUnit.DAYS.between(ngayNhanThucTe.toLocalDateTime().toLocalDate(), now.toLocalDateTime().toLocalDate());
                if (nightsOld < 1) nightsOld = 1; // At least 1 night if they stayed

                long totalNightsOriginal = java.time.temporal.ChronoUnit.DAYS.between(ngayNhanThucTe.toLocalDateTime().toLocalDate(), ngayTraDuKien.toLocalDateTime().toLocalDate());
                long nightsNew = totalNightsOriginal - nightsOld;
                if (nightsNew < 1) nightsNew = 1; // If swapping on the last day, still 1 night for the new room?

                double costOld = nightsOld * giaPhongCu;
                double costNew = nightsNew * giaPhongMoi;

                // 3. Update old room stay: set end to now
                String updateOldStay = "UPDATE ChiTietHoaDon SET ngayTraPhong = ?, ngayTraThucTe = ?, soDem = ?, thanhTien = ? WHERE maHD = ? AND maPhong = ?";
                try (PreparedStatement psOld = conn.prepareStatement(updateOldStay)) {
                    psOld.setTimestamp(1, now);
                    psOld.setTimestamp(2, now);
                    psOld.setLong(3, nightsOld);
                    psOld.setDouble(4, costOld);
                    psOld.setString(5, maHD);
                    psOld.setString(6, oldRoom);
                    psOld.executeUpdate();
                }

                // 4. Insert new room stay
                String insertNewStay = "INSERT INTO ChiTietHoaDon (maHD, maPhong, ngayNhanPhong, ngayTraPhong, ngayTraThucTe, soDem, phuThu, phiPhat, thanhTien) VALUES (?, ?, ?, ?, NULL, ?, 0, 0, ?)";
                try (PreparedStatement psNew = conn.prepareStatement(insertNewStay)) {
                    psNew.setString(1, maHD);
                    psNew.setString(2, newRoom);
                    psNew.setTimestamp(3, now);
                    psNew.setTimestamp(4, ngayTraDuKien);
                    psNew.setLong(5, nightsNew);
                    psNew.setDouble(6, costNew);
                    psNew.executeUpdate();
                }

                // 5. Update ChiTietDatPhong (split or update?) - Update to current room for consistency
                String updateDetail = "UPDATE ChiTietDatPhong SET maPhong = ?, ghiChu = ISNULL(ghiChu, '') + ? WHERE maDatPhong = ? AND maPhong = ?";
                try (PreparedStatement psDetail = conn.prepareStatement(updateDetail)) {
                    psDetail.setString(1, newRoom);
                    psDetail.setString(2, "\n[Đổi từ " + oldRoom + " ngày " + now.toString() + ": " + reason + "]");
                    psDetail.setString(3, maDatPhong);
                    psDetail.setString(4, oldRoom);
                    psDetail.executeUpdate();
                }

                // 6. Update room statuses
                String updateOldRoom = "UPDATE Phong SET trangThaiPhong = 'BaoTri' WHERE maPhong = ?"; // Set to maintenance for cleaning
                String updateNewRoom = "UPDATE Phong SET trangThaiPhong = 'DangSuDung' WHERE maPhong = ?";
                try (PreparedStatement psStatusOld = conn.prepareStatement(updateOldRoom)) {
                    psStatusOld.setString(1, oldRoom);
                    psStatusOld.executeUpdate();
                }
                try (PreparedStatement psStatusNew = conn.prepareStatement(updateNewRoom)) {
                    psStatusNew.setString(1, newRoom);
                    psStatusNew.executeUpdate();
                }

                // 7. Update HoaDon total
                String updateHD = "UPDATE HoaDon SET tienPhong = (SELECT SUM(thanhTien) FROM ChiTietHoaDon WHERE maHD = ?) WHERE maHD = ?";
                try (PreparedStatement psHD = conn.prepareStatement(updateHD)) {
                    psHD.setString(1, maHD);
                    psHD.setString(2, maHD);
                    psHD.executeUpdate();
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
