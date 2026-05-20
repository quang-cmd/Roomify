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

    public List<SwapRoomSearchResult> searchBookings(String bookingId, String guestName, String phoneNumber,
                                                     String roomId) {
        List<SwapRoomSearchResult> results = new ArrayList<>();
        String sql = "SELECT TRIM(dp.maDatPhong) as maDatPhong, TRIM(kh.maKH) as maKH, kh.hoTenKH, kh.sdt, kh.CCCD, " +
                "TRIM(ctdp.maPhong) as maPhong, TRIM(p.maLoaiPhong) as maLoaiPhong, lp.tenLoaiPhong, lp.sucChuaToiDa, "
                +
                "ctdp.ngayNhanDuKien, ctdp.ngayTraDuKien, ctdp.soLuongNguoiO " +
                "FROM ChiTietDatPhong ctdp " +
                "JOIN DatPhong dp ON dp.maDatPhong = ctdp.maDatPhong " +
                "JOIN KhachHang kh ON kh.maKH = dp.maKH " +
                "JOIN Phong p ON p.maPhong = ctdp.maPhong " +
                "JOIN LoaiPhong lp ON lp.maLoaiPhong = p.maLoaiPhong " +
                "WHERE dp.trangThaiDatPhong IN ('DaDat', 'DangO') " +
                "AND (? = '' OR dp.maDatPhong LIKE ?) " +
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
        String sql = "SELECT TRIM(p.maPhong) as maPhong, TRIM(p.maLoaiPhong) as maLoaiPhong, lp.tenLoaiPhong, lp.sucChuaToiDa, p.tang, p.trangThaiPhong "
                +
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
                Timestamp now = new Timestamp(System.currentTimeMillis());

                String maHD = null;
                Timestamp ngayNhanThucTe = null;
                Timestamp ngayTraDuKien = null;
                double giaPhongCu = 0;
                double giaPhongMoi = 0;
                double giaPhongMoiApDung = 0;
                int soLuongKhach = 1;

                /*
                 * Nguyên tắc giá khi đổi phòng:
                 * - Cùng loại / phòng mới rẻ hơn: giữ giá phòng cũ
                 * - Phòng mới đắt hơn: từ thời điểm đổi trở đi dùng giá phòng mới
                 *
                 * Vì không thêm cột mới vào DB, giá áp dụng của phòng mới được lưu vào
                 * ChiTietDatPhong.donGiaDat. Khi trả phòng, CheckoutBUS sẽ đọc donGiaDat
                 * này để tính tiền cho dòng phòng mới.
                 */
                String infoSql = """
                    SELECT TOP 1
                        cthd.maHD,
                        cthd.ngayNhanPhong,
                        cthd.ngayTraPhong,
                        COALESCE(NULLIF(ctdp.donGiaDat, 0), lp_old.giaPhong) AS giaCu,
                        lp_new.giaPhong AS giaMoi,
                        hd.soLuongNguoiO
                    FROM ChiTietHoaDon cthd
                    JOIN HoaDon hd ON hd.maHD = cthd.maHD
                    LEFT JOIN ChiTietDatPhong ctdp
                        ON ctdp.maDatPhong = hd.maDatPhong
                       AND ctdp.maPhong = cthd.maPhong
                    JOIN Phong p_old ON p_old.maPhong = cthd.maPhong
                    JOIN LoaiPhong lp_old ON lp_old.maLoaiPhong = p_old.maLoaiPhong
                    JOIN Phong p_new ON p_new.maPhong = ?
                    JOIN LoaiPhong lp_new ON lp_new.maLoaiPhong = p_new.maLoaiPhong
                    WHERE hd.maDatPhong = ?
                      AND cthd.maPhong = ?
                      AND cthd.ngayTraThucTe IS NULL
                    ORDER BY cthd.ngayNhanPhong DESC
                """;

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
                        }
                    }
                }

                /*
                 * Trường hợp chưa check-in/chưa có ChiTietHoaDon:
                 * chỉ đổi phòng trong ChiTietDatPhong, không tách lịch sử hóa đơn.
                 */
                if (maHD == null || maHD.isBlank()) {
                    String insertDetailSimple = """
                        INSERT INTO ChiTietDatPhong (
                            maDatPhong, maPhong, ngayNhanDuKien, ngayTraDuKien,
                            donGiaDat, soLuongNguoiO, ghiChu
                        )
                        SELECT
                            ctdp.maDatPhong,
                            ?,
                            ctdp.ngayNhanDuKien,
                            ctdp.ngayTraDuKien,
                            CASE
                                WHEN lp_new.giaPhong > COALESCE(NULLIF(ctdp.donGiaDat, 0), lp_old.giaPhong)
                                    THEN lp_new.giaPhong
                                ELSE COALESCE(NULLIF(ctdp.donGiaDat, 0), lp_old.giaPhong)
                            END,
                            ctdp.soLuongNguoiO,
                            ISNULL(ctdp.ghiChu, '') + ?
                        FROM ChiTietDatPhong ctdp
                        JOIN Phong p_old ON p_old.maPhong = ctdp.maPhong
                        JOIN LoaiPhong lp_old ON lp_old.maLoaiPhong = p_old.maLoaiPhong
                        JOIN Phong p_new ON p_new.maPhong = ?
                        JOIN LoaiPhong lp_new ON lp_new.maLoaiPhong = p_new.maLoaiPhong
                        WHERE ctdp.maDatPhong = ?
                          AND ctdp.maPhong = ?
                    """;

                    try (PreparedStatement psInsert = conn.prepareStatement(insertDetailSimple)) {
                        psInsert.setString(1, newRoom);
                        psInsert.setString(2, "\n[Đổi phòng trước nhận từ " + oldRoom + " sang " + newRoom + ": " + reason + "]");
                        psInsert.setString(3, newRoom);
                        psInsert.setString(4, maDatPhong);
                        psInsert.setString(5, oldRoom);

                        if (psInsert.executeUpdate() <= 0) {
                            conn.rollback();
                            conn.setAutoCommit(autoCommit);
                            return false;
                        }
                    }

                    String updateKhachO = "UPDATE ChiTietKhachO SET maPhong = ? WHERE maDatPhong = ? AND maPhong = ?";
                    try (PreparedStatement psKhach = conn.prepareStatement(updateKhachO)) {
                        psKhach.setString(1, newRoom);
                        psKhach.setString(2, maDatPhong);
                        psKhach.setString(3, oldRoom);
                        psKhach.executeUpdate();
                    }

                    String deleteDetail = "DELETE FROM ChiTietDatPhong WHERE maDatPhong = ? AND maPhong = ?";
                    try (PreparedStatement psDelete = conn.prepareStatement(deleteDetail)) {
                        psDelete.setString(1, maDatPhong);
                        psDelete.setString(2, oldRoom);
                        psDelete.executeUpdate();
                    }

                    conn.commit();
                    conn.setAutoCommit(autoCommit);
                    return true;
                }

                if (ngayNhanThucTe == null) {
                    ngayNhanThucTe = now;
                }
                if (ngayTraDuKien == null) {
                    ngayTraDuKien = now;
                }

                giaPhongMoiApDung = giaPhongMoi > giaPhongCu ? giaPhongMoi : giaPhongCu;

                long nightsOld = java.time.temporal.ChronoUnit.DAYS.between(
                        ngayNhanThucTe.toLocalDateTime().toLocalDate(),
                        now.toLocalDateTime().toLocalDate()
                );

                long totalNights = java.time.temporal.ChronoUnit.DAYS.between(
                        ngayNhanThucTe.toLocalDateTime().toLocalDate(),
                        ngayTraDuKien.toLocalDateTime().toLocalDate()
                );

                if (totalNights < 1) {
                    totalNights = 1;
                }

                if (nightsOld <= 0) {
                    /*
                     * Đổi trong cùng ngày nhận phòng:
                     * không cần tách 2 dòng, chỉ chuyển dòng hiện tại sang phòng mới.
                     */
                    double newTotal = totalNights * giaPhongMoiApDung;

                    String updateSameDay = """
                        UPDATE ChiTietHoaDon
                        SET maPhong = ?,
                            soDem = ?,
                            thanhTien = ?
                        WHERE maHD = ?
                          AND maPhong = ?
                          AND ngayTraThucTe IS NULL
                    """;

                    try (PreparedStatement psSameDay = conn.prepareStatement(updateSameDay)) {
                        psSameDay.setString(1, newRoom);
                        psSameDay.setLong(2, totalNights);
                        psSameDay.setDouble(3, newTotal);
                        psSameDay.setString(4, maHD);
                        psSameDay.setString(5, oldRoom);

                        if (psSameDay.executeUpdate() <= 0) {
                            conn.rollback();
                            conn.setAutoCommit(autoCommit);
                            return false;
                        }
                    }

                    insertNewBookingDetail(conn, maDatPhong, oldRoom, newRoom, giaPhongMoiApDung, reason, now);
                    moveGuestsToNewRoom(conn, maDatPhong, oldRoom, newRoom);
                    deleteOldBookingDetail(conn, maDatPhong, oldRoom);
                    updateRoomStatus(conn, oldRoom, "Trong");
                    updateRoomStatus(conn, newRoom, "DangSuDung");
                } else {
                    /*
                     * Đổi sau khi đã ở phòng cũ ít nhất 1 đêm:
                     * tách lịch sử thành 2 dòng ChiTietHoaDon.
                     */
                    long nightsNew = totalNights - nightsOld;
                    if (nightsNew < 1) {
                        nightsNew = 1;
                    }

                    double costOld = nightsOld * giaPhongCu;
                    double costNew = nightsNew * giaPhongMoiApDung;

                    String updateOldStay = """
                        UPDATE ChiTietHoaDon
                        SET ngayTraPhong = ?,
                            ngayTraThucTe = ?,
                            soDem = ?,
                            thanhTien = ?
                        WHERE maHD = ?
                          AND maPhong = ?
                          AND ngayTraThucTe IS NULL
                    """;

                    try (PreparedStatement psOld = conn.prepareStatement(updateOldStay)) {
                        psOld.setTimestamp(1, now);
                        psOld.setTimestamp(2, now);
                        psOld.setLong(3, nightsOld);
                        psOld.setDouble(4, costOld);
                        psOld.setString(5, maHD);
                        psOld.setString(6, oldRoom);

                        if (psOld.executeUpdate() <= 0) {
                            conn.rollback();
                            conn.setAutoCommit(autoCommit);
                            return false;
                        }
                    }

                    String insertNewStay = """
                        INSERT INTO ChiTietHoaDon (
                            maHD, maPhong, ngayNhanPhong, ngayTraPhong,
                            ngayTraThucTe, soDem, phuThu, phiPhat, thanhTien
                        )
                        VALUES (?, ?, ?, ?, NULL, ?, 0, 0, ?)
                    """;

                    try (PreparedStatement psNew = conn.prepareStatement(insertNewStay)) {
                        psNew.setString(1, maHD);
                        psNew.setString(2, newRoom);
                        psNew.setTimestamp(3, now);
                        psNew.setTimestamp(4, ngayTraDuKien);
                        psNew.setLong(5, nightsNew);
                        psNew.setDouble(6, costNew);
                        psNew.executeUpdate();
                    }

                    insertNewBookingDetail(conn, maDatPhong, oldRoom, newRoom, giaPhongMoiApDung, reason, now);
                    moveGuestsToNewRoom(conn, maDatPhong, oldRoom, newRoom);
                    deleteOldBookingDetail(conn, maDatPhong, oldRoom);

                    updateRoomStatus(conn, oldRoom, "BaoTri");
                    updateRoomStatus(conn, newRoom, "DangSuDung");
                }

                String updateRoomTotal = """
                    UPDATE HoaDon
                    SET tienPhong = (
                        SELECT ISNULL(SUM(thanhTien), 0)
                        FROM ChiTietHoaDon
                        WHERE maHD = HoaDon.maHD
                    )
                    WHERE maHD = ?
                """;

                try (PreparedStatement psHD = conn.prepareStatement(updateRoomTotal)) {
                    psHD.setString(1, maHD);
                    psHD.executeUpdate();
                }

                String updateFinalTotal = """
                    UPDATE HoaDon
                    SET tienThue = CASE
                            WHEN (tienPhong + tienDichVu - tienKhuyenMai) > 0
                                THEN (tienPhong + tienDichVu - tienKhuyenMai) * 0.1
                            ELSE 0
                        END,
                        tongTienThanhToan = CASE
                            WHEN (tienPhong + tienDichVu - tienKhuyenMai) > 0
                                THEN (tienPhong + tienDichVu - tienKhuyenMai) * 1.1
                            ELSE 0
                        END + phiDoiPhong
                    WHERE maHD = ?
                """;

                try (PreparedStatement psFinal = conn.prepareStatement(updateFinalTotal)) {
                    psFinal.setString(1, maHD);
                    psFinal.executeUpdate();
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

    private void insertNewBookingDetail(Connection conn, String maDatPhong, String oldRoom, String newRoom,
                                        double donGiaApDung, String reason, Timestamp now) throws SQLException {
        String insertDetail = """
            INSERT INTO ChiTietDatPhong (
                maDatPhong, maPhong, ngayNhanDuKien, ngayTraDuKien,
                donGiaDat, soLuongNguoiO, ghiChu
            )
            SELECT
                maDatPhong,
                ?,
                ngayNhanDuKien,
                ngayTraDuKien,
                ?,
                soLuongNguoiO,
                ISNULL(ghiChu, '') + ?
            FROM ChiTietDatPhong
            WHERE maDatPhong = ?
              AND maPhong = ?
        """;

        try (PreparedStatement psInsert = conn.prepareStatement(insertDetail)) {
            psInsert.setString(1, newRoom);
            psInsert.setDouble(2, donGiaApDung);
            psInsert.setString(3, "\n[Đổi từ " + oldRoom + " sang " + newRoom + " lúc " + now + ": " + reason + "]");
            psInsert.setString(4, maDatPhong);
            psInsert.setString(5, oldRoom);
            psInsert.executeUpdate();
        }
    }

    private void moveGuestsToNewRoom(Connection conn, String maDatPhong, String oldRoom, String newRoom) throws SQLException {
        String updateKhachO = "UPDATE ChiTietKhachO SET maPhong = ? WHERE maDatPhong = ? AND maPhong = ?";

        try (PreparedStatement psKhach = conn.prepareStatement(updateKhachO)) {
            psKhach.setString(1, newRoom);
            psKhach.setString(2, maDatPhong);
            psKhach.setString(3, oldRoom);
            psKhach.executeUpdate();
        }
    }

    private void deleteOldBookingDetail(Connection conn, String maDatPhong, String oldRoom) throws SQLException {
        String deleteDetail = "DELETE FROM ChiTietDatPhong WHERE maDatPhong = ? AND maPhong = ?";

        try (PreparedStatement psDelete = conn.prepareStatement(deleteDetail)) {
            psDelete.setString(1, maDatPhong);
            psDelete.setString(2, oldRoom);
            psDelete.executeUpdate();
        }
    }

    private void updateRoomStatus(Connection conn, String maPhong, String trangThai) throws SQLException {
        String sql = "UPDATE Phong SET trangThaiPhong = ? WHERE maPhong = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            ps.setString(2, maPhong);
            ps.executeUpdate();
        }
    }

}

//
//
//