package kqlhotel.dao.booking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import kqlhotel.dao.ConnectDB;
import kqlhotel.entity.BookingEntity;
import kqlhotel.entity.BookingRoomEntity;
import kqlhotel.entity.Customer;
import kqlhotel.entity.GuestStayDetail;
import kqlhotel.entity.Invoice;

public class BookingDaoSqlServer implements BookingDao {
    private static final int CHECK_IN_HOUR = 14;
    private static final int CHECK_OUT_HOUR = 12;

    @Override
    public String createBooking(BookingEntity booking) {
        try {
            return createBooking(ConnectDB.getInstance().getConnection(), booking);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void createBookingRooms(List<BookingRoomEntity> bookingRooms) {
        try {
            createBookingRooms(ConnectDB.getInstance().getConnection(), bookingRooms);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String createBooking(Connection con, BookingEntity booking) throws SQLException {
        if (con == null || booking == null) {
            return null;
        }

        String maDatPhong = booking.getMaDatPhong();
        if (maDatPhong == null || maDatPhong.isBlank()) {
            maDatPhong = nextId(con, "DatPhong", "maDatPhong", "DP", 5);
            booking.setMaDatPhong(maDatPhong);
        }

        String sql = """
            INSERT INTO DatPhong
                (maDatPhong, ngayDat, tienCoc, trangThaiDatPhong, ghiChu, maKH, maNV)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maDatPhong);
            ps.setTimestamp(2, Timestamp.valueOf(booking.getNgayDat()));
            ps.setBigDecimal(3, java.math.BigDecimal.valueOf(booking.getTienCoc()));
            ps.setString(4, booking.getTrangThaiDatPhong());
            ps.setString(5, booking.getGhiChu());
            ps.setString(6, booking.getMaKH());
            ps.setString(7, booking.getMaNV());
            ps.executeUpdate();
        }

        return maDatPhong;
    }

    @Override
    public void createBookingRooms(Connection con, List<BookingRoomEntity> bookingRooms) throws SQLException {
        if (con == null || bookingRooms == null || bookingRooms.isEmpty()) {
            return;
        }

        String sql = """
            INSERT INTO ChiTietDatPhong
                (maDatPhong, maPhong, ngayNhanDuKien, ngayTraDuKien, donGiaDat, soLuongNguoiO, ghiChu)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (BookingRoomEntity room : bookingRooms) {
                ps.setString(1, room.getMaDatPhong());
                ps.setString(2, room.getMaPhong());
                ps.setTimestamp(3, Timestamp.valueOf(room.getNgayNhanDuKien()));
                ps.setTimestamp(4, Timestamp.valueOf(room.getNgayTraDuKien()));
                ps.setBigDecimal(5, java.math.BigDecimal.valueOf(room.getDonGiaDat()));
                ps.setInt(6, room.getSoLuongNguoiO());
                ps.setString(7, room.getGhiChu());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    @Override
    public void createGuestStayDetails(Connection con, List<GuestStayDetail> guestStayDetails) throws SQLException {
        if (con == null || guestStayDetails == null || guestStayDetails.isEmpty()) {
            return;
        }

        String sql = """
            INSERT INTO ChiTietKhachO
                (maDatPhong, maPhong, hoTen, cccd, sdt, vaiTro)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (GuestStayDetail detail : guestStayDetails) {
                ps.setString(1, detail.getMaDatPhong());
                ps.setString(2, detail.getMaPhong());
                ps.setString(3, detail.getHoTen());
                ps.setString(4, detail.getCccd());
                ps.setString(5, detail.getSdt());
                ps.setString(6, detail.getVaiTro());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    @Override
    public String createInvoice(Connection con, Invoice invoice) throws SQLException {
        if (con == null || invoice == null) {
            return null;
        }

        String maHD = invoice.getMaHD();
        if (maHD == null || maHD.isBlank()) {
            maHD = nextId(con, "HoaDon", "maHD", "HD", 5);
            invoice.setMaHD(maHD);
        }

        String sql = """
            INSERT INTO HoaDon
                (maHD, ngayLapHD, ngayThanhToan, ghiChu, soLuongNguoiO, tienPhong,
                 tienDichVu, tienKhuyenMai, tienThue, tongTienThanhToan, phiDoiPhong,
                 maKM, maKH, maNV, phuongThucTT, trangThai, maDatPhong)
            VALUES (?, ?, ?, ?, ?, ?, 0, 0, ?, ?, 0, NULL, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHD);
            ps.setTimestamp(2, Timestamp.valueOf(invoice.getNgayLapHD()));
            if (invoice.getNgayThanhToan() == null) {
                ps.setNull(3, java.sql.Types.TIMESTAMP);
            } else {
                ps.setTimestamp(3, Timestamp.valueOf(invoice.getNgayThanhToan()));
            }
            ps.setString(4, invoice.getGhiChu());
            ps.setInt(5, invoice.getSoLuongNguoi());
            ps.setBigDecimal(6, java.math.BigDecimal.valueOf(invoice.getTienPhong()));
            ps.setBigDecimal(7, java.math.BigDecimal.valueOf(invoice.getTienThue()));
            ps.setBigDecimal(8, java.math.BigDecimal.valueOf(invoice.getTongTienThanhToan()));
            ps.setString(9, invoice.getMaKhachHang());
            ps.setString(10, invoice.getMaNhanVien());
            ps.setString(11, invoice.getPhuongThucTT());
            ps.setString(12, invoice.getTrangThai());
            ps.setString(13, invoice.getMaDatPhong());
            ps.executeUpdate();
        }

        return maHD;
    }

    @Override
    public String pickAvailableRoomId(Connection con, String roomTypeName, LocalDate checkIn,
                                      LocalDate checkOut, Set<String> excludeRoomIds) throws SQLException {
        String sql = """
            SELECT p.maPhong
            FROM Phong p
            JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong
            WHERE lp.tenLoaiPhong = ?
              AND p.trangThaiPhong <> 'BaoTri'
              AND NOT EXISTS (
                  SELECT 1
                  FROM ChiTietDatPhong ctdp
                  JOIN DatPhong dp ON dp.maDatPhong = ctdp.maDatPhong
                  WHERE ctdp.maPhong = p.maPhong
                    AND dp.trangThaiDatPhong IN ('DaDat', 'DangO')
                    AND ? < ctdp.ngayTraDuKien
                    AND ? > ctdp.ngayNhanDuKien
              )
            ORDER BY NEWID()
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, roomTypeName);
            ps.setTimestamp(2, Timestamp.valueOf(checkIn.atTime(CHECK_IN_HOUR, 0)));
            ps.setTimestamp(3, Timestamp.valueOf(checkOut.atTime(CHECK_OUT_HOUR, 0)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String roomId = rs.getString(1);
                    if (excludeRoomIds == null || !excludeRoomIds.contains(roomId)) {
                        return roomId;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String upsertCustomer(Connection con, Customer customer) throws SQLException {
        if (con == null || customer == null || normalizeNullable(customer.getCCCD()) == null) {
            return null;
        }

        String cccd = customer.getCCCD().trim();
        try (PreparedStatement ps = con.prepareStatement("SELECT maKH FROM KhachHang WHERE CCCD = ?")) {
            ps.setString(1, cccd);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String maKH = rs.getString(1);
                    updateCustomerContact(con, maKH, customer);
                    return maKH;
                }
            }
        }

        if (normalizeNullable(customer.getSdt()) != null) {
            try (PreparedStatement ps = con.prepareStatement("SELECT maKH FROM KhachHang WHERE sdt = ?")) {
                ps.setString(1, customer.getSdt().trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return null;
                    }
                }
            }
        }

        String maKH = nextId(con, "KhachHang", "maKH", "KH", 5);
        customer.setMaKH(maKH);
        String sql = """
            INSERT INTO KhachHang
                (maKH, hoTenKH, gioiTinh, ngaySinh, email, sdt, CCCD, quocTich, diaChi, hangKH, diemTichLuy)
            VALUES (?, ?, 1, ?, ?, ?, ?, N'Viet Nam', NULL, 'Dong', 0)
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKH);
            ps.setString(2, customer.getHoTenKH());
            ps.setTimestamp(3, Timestamp.valueOf(customer.getNgaySinh()));
            ps.setString(4, normalizeNullable(customer.getEmail()));
            ps.setString(5, customer.getSdt() == null ? "" : customer.getSdt().trim());
            ps.setString(6, cccd);
            ps.executeUpdate();
        }
        return maKH;
    }

    @Override
    public String findCustomerNameByPhoneWithDifferentId(Connection con, String phone, String idNo) throws SQLException {
        String normalizedPhone = normalizeNullable(phone);
        if (normalizedPhone == null) {
            return null;
        }

        String normalizedId = normalizeNullable(idNo);
        String sql = """
            SELECT TOP 1 hoTenKH
            FROM KhachHang
            WHERE sdt = ?
              AND (? IS NULL OR CCCD <> ?)
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, normalizedPhone);
            ps.setString(2, normalizedId);
            ps.setString(3, normalizedId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("hoTenKH");
                }
            }
        }
        return null;
    }

    @Override
    public String resolveStaffId(Connection con) throws SQLException {
        String sql = """
            SELECT TOP 1 nv.maNV
            FROM NhanVien nv
            JOIN TaiKhoan tk ON nv.tenDangNhap = tk.tenDangNhap
            WHERE tk.trangThaiTK = 'DangHoatDong'
            ORDER BY nv.maNV
        """;

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString(1);
            }
        }
        return null;
    }

    private void updateCustomerContact(Connection con, String maKH, Customer customer) throws SQLException {
        String email = normalizeNullable(customer.getEmail());
        String phone = normalizeNullable(customer.getSdt());
        if (email == null && phone == null) {
            return;
        }

        String sql = """
            UPDATE KhachHang
            SET email = CASE WHEN ? IS NULL OR ? = '' THEN email ELSE ? END,
                sdt = CASE WHEN ? IS NULL OR ? = '' THEN sdt ELSE ? END
            WHERE maKH = ?
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, email);
            ps.setString(3, email);
            ps.setString(4, phone);
            ps.setString(5, phone);
            ps.setString(6, phone);
            ps.setString(7, maKH);
            ps.executeUpdate();
        }
    }

    private String normalizeNullable(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private String nextId(Connection con, String table, String column, String prefix, int totalLength) throws SQLException {
        int nextSeq = nextSequence(con, table, column, prefix);
        int digits = totalLength - prefix.length();
        return prefix + String.format("%0" + digits + "d", nextSeq);
    }

    private int nextSequence(Connection con, String table, String column, String prefix) throws SQLException {
        String sql = "SELECT TOP 1 " + column + " FROM " + table + " WHERE " + column + " LIKE ? ORDER BY " + column + " DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String last = rs.getString(1);
                    String numPart = last.substring(prefix.length()).replaceAll("[^0-9]", "");
                    if (!numPart.isEmpty()) {
                        return Integer.parseInt(numPart) + 1;
                    }
                }
            }
        }
        return 1;
    }
}
