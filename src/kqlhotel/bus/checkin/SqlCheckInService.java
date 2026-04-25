package kqlhotel.bus.checkin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import kqlhotel.bus.checkin.model.ArrivalDto;
import kqlhotel.bus.checkin.model.CheckInResult;
import kqlhotel.dao.ConnectDB;

public class SqlCheckInService implements CheckInService {

    @Override
    public List<ArrivalDto> findArrivals(LocalDate from, LocalDate to, String keyword) {
        if (from == null || to == null || to.isBefore(from)) {
            return Collections.emptyList();
        }

        Connection con = openConnection();
        if (con == null) return Collections.emptyList();

        // Aggregate booking + customer + room codes + checked-in flag in one round-trip.
        // checkedIn = there exists at least one ChiTietHoaDon row for the related HoaDon.
        String sql =
            "SELECT dp.maDatPhong, dp.ngayDat, dp.ngayNhanDuKien, dp.ngayTraDuKien, dp.tienCoc, " +
            "       kh.hoTenKH, kh.sdt, kh.CCCD, " +
            "       hd.maHD, " +
            "       (SELECT COUNT(*) FROM ChiTietHoaDon cthd WHERE cthd.maHD = hd.maHD) AS soCTHD, " +
            "       (SELECT COUNT(*) FROM ChiTietDatPhong ctdp WHERE ctdp.maDatPhong = dp.maDatPhong) AS soPhong " +
            "  FROM DatPhong dp " +
            "  JOIN KhachHang kh ON dp.maKH = kh.maKH " +
            "  LEFT JOIN HoaDon hd ON hd.maDatPhong = dp.maDatPhong " +
            " WHERE dp.ngayNhanDuKien >= ? AND dp.ngayNhanDuKien < ? " +
            (isBlank(keyword) ? "" :
                "   AND (dp.maDatPhong LIKE ? OR kh.hoTenKH LIKE ? OR kh.sdt LIKE ? OR kh.CCCD LIKE ?) ") +
            " ORDER BY dp.ngayNhanDuKien ASC, dp.maDatPhong ASC";

        List<ArrivalDto> arrivals = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(from.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
            if (!isBlank(keyword)) {
                String kw = "%" + keyword.trim() + "%";
                ps.setString(3, kw);
                ps.setString(4, kw);
                ps.setString(5, kw);
                ps.setString(6, kw);
            }

            // First pass: collect booking rows
            Map<String, Object[]> bookingRows = new LinkedHashMap<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maDP = rs.getString("maDatPhong");
                    bookingRows.put(maDP, new Object[]{
                        rs.getString("maHD"),
                        rs.getTimestamp("ngayDat"),
                        rs.getTimestamp("ngayNhanDuKien"),
                        rs.getTimestamp("ngayTraDuKien"),
                        rs.getLong("tienCoc"),
                        rs.getString("hoTenKH"),
                        rs.getString("sdt"),
                        rs.getString("CCCD"),
                        rs.getInt("soCTHD")
                    });
                }
            }

            if (bookingRows.isEmpty()) return Collections.emptyList();

            // Second pass: load room codes per booking
            Map<String, List<String>> roomMap = loadRoomCodes(con, bookingRows.keySet());

            for (Map.Entry<String, Object[]> e : bookingRows.entrySet()) {
                String maDP = e.getKey();
                Object[] r = e.getValue();
                LocalDateTime nNhan = ((Timestamp) r[2]).toLocalDateTime();
                LocalDateTime nTra = ((Timestamp) r[3]).toLocalDateTime();
                int nights = (int) Math.max(1,
                    ChronoUnit.DAYS.between(nNhan.toLocalDate(), nTra.toLocalDate()));
                List<String> rooms = roomMap.getOrDefault(maDP, Collections.emptyList());
                arrivals.add(new ArrivalDto(
                    maDP,
                    (String) r[0],
                    ((Timestamp) r[1]).toLocalDateTime(),
                    nNhan,
                    nTra,
                    (Long) r[4],
                    (String) r[5],
                    (String) r[6],
                    (String) r[7],
                    rooms,
                    nights,
                    ((Integer) r[8]) > 0
                ));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
        return arrivals;
    }

    @Override
    public CheckInResult confirmCheckIn(String maDatPhong) {
        if (isBlank(maDatPhong)) return CheckInResult.fail("Thiếu mã đặt phòng.");

        Connection con = openConnection();
        if (con == null) return CheckInResult.fail("Không thể kết nối CSDL.");

        boolean originalAutoCommit = true;
        try {
            originalAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);

            // 1. Resolve HoaDon for this booking
            String maHD;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT maHD FROM HoaDon WHERE maDatPhong = ?")) {
                ps.setString(1, maDatPhong);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return CheckInResult.fail("Chưa có hóa đơn cho booking này.");
                    }
                    maHD = rs.getString(1);
                }
            }

            // 2. Guard: bail out if already checked-in
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) FROM ChiTietHoaDon WHERE maHD = ?")) {
                ps.setString(1, maHD);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        con.rollback();
                        return CheckInResult.fail("Booking này đã nhận phòng trước đó.");
                    }
                }
            }

            // 3. Pull all reserved rooms
            List<Object[]> ctdpRows = new ArrayList<>();
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT maPhong, ngayNhanDuKien, ngayTraDuKien, donGiaDat " +
                    "  FROM ChiTietDatPhong WHERE maDatPhong = ?")) {
                ps.setString(1, maDatPhong);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ctdpRows.add(new Object[]{
                            rs.getString(1),
                            rs.getTimestamp(2),
                            rs.getTimestamp(3),
                            rs.getBigDecimal(4)
                        });
                    }
                }
            }
            if (ctdpRows.isEmpty()) {
                con.rollback();
                return CheckInResult.fail("Booking không có phòng nào.");
            }

            // 4. INSERT ChiTietHoaDon (one row per reserved room).
            //    ngayNhanPhong = now (actual arrival)
            //    ngayTraPhong  = expected check-out
            //    soDem         = max(1, days from now to ngayTraPhong)
            //    thanhTien     = donGiaDat * soDem
            LocalDateTime now = LocalDateTime.now();
            String insertSql = "INSERT INTO ChiTietHoaDon " +
                "(maHD, maPhong, ngayNhanPhong, ngayTraPhong, soDem, phuThu, thanhTien) " +
                "VALUES (?, ?, ?, ?, ?, 0, ?)";
            try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                for (Object[] row : ctdpRows) {
                    String maPhong = (String) row[0];
                    Timestamp tsTra = (Timestamp) row[2];
                    LocalDateTime ngayTra = tsTra.toLocalDateTime();
                    long soDem = Math.max(1,
                        ChronoUnit.DAYS.between(now.toLocalDate(), ngayTra.toLocalDate()));
                    java.math.BigDecimal donGia = (java.math.BigDecimal) row[3];
                    java.math.BigDecimal thanhTien = donGia.multiply(java.math.BigDecimal.valueOf(soDem));

                    ps.setString(1, maHD);
                    ps.setString(2, maPhong);
                    ps.setTimestamp(3, Timestamp.valueOf(now));
                    ps.setTimestamp(4, tsTra);
                    ps.setInt(5, (int) soDem);
                    ps.setBigDecimal(6, thanhTien);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // 5. Mark rooms as in-use
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE Phong SET trangThaiPhong = 'DangSuDung' WHERE maPhong = ?")) {
                for (Object[] row : ctdpRows) {
                    ps.setString(1, (String) row[0]);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            con.commit();
            return CheckInResult.ok(maHD, ctdpRows.size(),
                "Nhận phòng thành công. " + ctdpRows.size() + " phòng đã được kích hoạt.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            try { con.rollback(); } catch (SQLException ignored) {}
            return CheckInResult.fail("Lỗi CSDL: " + ex.getMessage());
        } finally {
            try { con.setAutoCommit(originalAutoCommit); } catch (SQLException ignored) {}
        }
    }

    private Map<String, List<String>> loadRoomCodes(Connection con, java.util.Set<String> bookingIds) throws SQLException {
        if (bookingIds.isEmpty()) return Collections.emptyMap();

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < bookingIds.size(); i++) {
            placeholders.append(i == 0 ? "?" : ",?");
        }

        String sql = "SELECT maDatPhong, maPhong FROM ChiTietDatPhong " +
            "WHERE maDatPhong IN (" + placeholders + ") ORDER BY maDatPhong, maPhong";

        Map<String, List<String>> result = new LinkedHashMap<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int idx = 1;
            for (String id : bookingIds) ps.setString(idx++, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maDP = rs.getString(1);
                    String maP = rs.getString(2);
                    result.computeIfAbsent(maDP, k -> new ArrayList<>()).add(maP);
                }
            }
        }
        return result;
    }

    private Connection openConnection() {
        Connection con = ConnectDB.getInstance().getConnection();
        try {
            if (con == null || con.isClosed()) {
                ConnectDB.getInstance().connect();
                con = ConnectDB.getInstance().getConnection();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        return con;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
