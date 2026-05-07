package kqlhotel.bus.checkin;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private static final LocalTime EARLY_CHECKIN_START = LocalTime.of(5, 0);
    private static final LocalTime EARLY_CHECKIN_50 = LocalTime.of(9, 0);
    private static final LocalTime CHECKIN_STANDARD = LocalTime.of(14, 0);

    @Override
    public List<ArrivalDto> findArrivals(LocalDate from, LocalDate to, String keyword) {
        if (from == null || to == null || to.isBefore(from)) {
            return Collections.emptyList();
        }

        Connection con = openConnection();
        if (con == null) return Collections.emptyList();

        String sql =
                "SELECT dp.maDatPhong, dp.ngayDat, dp.tienCoc, " +
                        "       MIN(ctdp.ngayNhanDuKien) AS ngayNhanDuKien, MAX(ctdp.ngayTraDuKien) AS ngayTraDuKien, " +
                        "       kh.hoTenKH, kh.sdt, kh.CCCD, hd.maHD, " +
                        "       (SELECT COUNT(*) FROM ChiTietHoaDon cthd WHERE cthd.maHD = hd.maHD) AS soCTHD " +
                        "FROM DatPhong dp " +
                        "JOIN KhachHang kh ON dp.maKH = kh.maKH " +
                        "JOIN ChiTietDatPhong ctdp ON ctdp.maDatPhong = dp.maDatPhong " +
                        "LEFT JOIN HoaDon hd ON hd.maDatPhong = dp.maDatPhong " +
                        "WHERE ctdp.ngayNhanDuKien >= ? AND ctdp.ngayNhanDuKien < ? " +
                        "  AND (hd.trangThai IS NULL OR hd.trangThai <> 'DaHuy') " +
                        (isBlank(keyword) ? "" :
                                "AND (dp.maDatPhong LIKE ? OR hd.maHD LIKE ? OR kh.hoTenKH LIKE ? OR kh.sdt LIKE ? OR kh.CCCD LIKE ?) ") +
                        "GROUP BY dp.maDatPhong, dp.ngayDat, dp.tienCoc, kh.hoTenKH, kh.sdt, kh.CCCD, hd.maHD " +
                        "ORDER BY MIN(ctdp.ngayNhanDuKien) ASC, dp.maDatPhong ASC";

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
                ps.setString(7, kw);
            }

            Map<String, Object[]> rows = new LinkedHashMap<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maDP = rs.getString("maDatPhong");
                    rows.put(maDP, new Object[]{
                            rs.getString("maHD"),
                            rs.getTimestamp("ngayDat"),
                            rs.getTimestamp("ngayNhanDuKien"),
                            rs.getTimestamp("ngayTraDuKien"),
                            rs.getBigDecimal("tienCoc"),
                            rs.getString("hoTenKH"),
                            rs.getString("sdt"),
                            rs.getString("CCCD"),
                            rs.getInt("soCTHD")
                    });
                }
            }

            if (rows.isEmpty()) return Collections.emptyList();

            Map<String, List<String>> roomMap = loadRoomCodes(con, rows.keySet());

            for (Map.Entry<String, Object[]> e : rows.entrySet()) {
                String maDP = e.getKey();
                Object[] r = e.getValue();

                LocalDateTime ngayDat = ((Timestamp) r[1]).toLocalDateTime();
                LocalDateTime ngayNhan = ((Timestamp) r[2]).toLocalDateTime();
                LocalDateTime ngayTra = ((Timestamp) r[3]).toLocalDateTime();

                int nights = (int) Math.max(1,
                        ChronoUnit.DAYS.between(ngayNhan.toLocalDate(), ngayTra.toLocalDate()));

                BigDecimal tienCoc = (BigDecimal) r[4];

                arrivals.add(new ArrivalDto(
                        maDP,
                        (String) r[0],
                        ngayDat,
                        ngayNhan,
                        ngayTra,
                        tienCoc == null ? 0L : tienCoc.longValue(),
                        (String) r[5],
                        (String) r[6],
                        (String) r[7],
                        roomMap.getOrDefault(maDP, Collections.emptyList()),
                        nights,
                        ((Integer) r[8]) > 0
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }

        return arrivals;
    }

    @Override
    public CheckInResult confirmCheckIn(String maDatPhong) {
        if (isBlank(maDatPhong)) {
            return CheckInResult.fail("Thiếu mã đặt phòng.");
        }

        Connection con = openConnection();
        if (con == null) {
            return CheckInResult.fail("Không thể kết nối CSDL.");
        }

        boolean oldAutoCommit = true;

        try {
            oldAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);

            String maHD = getInvoiceIdByBooking(con, maDatPhong);
            if (maHD == null) {
                con.rollback();
                return CheckInResult.fail("Chưa có hóa đơn cho booking này.");
            }

            if (hasInvoiceDetails(con, maHD)) {
                con.rollback();
                return CheckInResult.fail("Booking này đã nhận phòng trước đó.");
            }

            List<Object[]> rooms = getReservedRooms(con, maDatPhong);
            if (rooms.isEmpty()) {
                con.rollback();
                return CheckInResult.fail("Booking không có phòng nào.");
            }

            LocalDateTime now = LocalDateTime.now();

            // Validate: không được nhận phòng trước ngày dự kiến
            LocalDate earliest = rooms.stream()
                    .map(r -> ((Timestamp) r[1]).toLocalDateTime().toLocalDate())
                    .min(LocalDate::compareTo)
                    .orElse(LocalDate.now());
            if (LocalDate.now().isBefore(earliest)) {
                con.rollback();
                return CheckInResult.fail(
                    "Chưa đến ngày nhận phòng. Ngày nhận phòng dự kiến: "
                    + earliest.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ".");
            }
            BigDecimal totalRoom = BigDecimal.ZERO;

                String insertSql =
                    "INSERT INTO ChiTietHoaDon " +
                            "(maHD, maPhong, ngayNhanPhong, ngayTraPhong, ngayTraThucTe, soDem, phuThu, thanhTien) " +
                        "VALUES (?, ?, ?, ?, NULL, ?, ?, ?)";

            try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                for (Object[] row : rooms) {
                    String maPhong = (String) row[0];
                    LocalDateTime ngayNhanDuKien = ((Timestamp) row[1]).toLocalDateTime();
                    LocalDateTime ngayTraDuKien = ((Timestamp) row[2]).toLocalDateTime();
                    BigDecimal donGia = (BigDecimal) row[3];

                    LocalDateTime ngayNhanThucTe = now;

                    if (!ngayTraDuKien.isAfter(ngayNhanThucTe)) {
                        ngayNhanThucTe = ngayNhanDuKien;
                    }

                    int soDem = (int) Math.max(1,
                            ChronoUnit.DAYS.between(
                                    ngayNhanDuKien.toLocalDate(),
                                    ngayTraDuKien.toLocalDate()
                            ));

                    BigDecimal phuThu = calculateEarlyCheckInFee(donGia, ngayNhanThucTe);
                    BigDecimal thanhTien = donGia.multiply(BigDecimal.valueOf(soDem)).add(phuThu);
                    totalRoom = totalRoom.add(thanhTien);

                    ps.setString(1, maHD);
                    ps.setString(2, maPhong);
                    ps.setTimestamp(3, Timestamp.valueOf(ngayNhanThucTe));
                    ps.setTimestamp(4, Timestamp.valueOf(ngayTraDuKien));
                    ps.setInt(5, soDem);
                    ps.setBigDecimal(6, phuThu);
                    ps.setBigDecimal(7, thanhTien);
                    ps.addBatch();
                }

                ps.executeBatch();
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE Phong SET trangThaiPhong = 'DangSuDung' WHERE maPhong = ?")) {
                for (Object[] row : rooms) {
                    ps.setString(1, (String) row[0]);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            updateInvoiceMoney(con, maHD, totalRoom);

            con.commit();

            return CheckInResult.ok(
                    maHD,
                    rooms.size(),
                    "Nhận phòng thành công. " + rooms.size() + " phòng đã được kích hoạt."
            );

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                con.rollback();
            } catch (SQLException ignored) {
            }
            return CheckInResult.fail("Lỗi CSDL: " + e.getMessage());
        } finally {
            try {
                con.setAutoCommit(oldAutoCommit);
            } catch (SQLException ignored) {
            }
        }
    }

    private String getInvoiceIdByBooking(Connection con, String maDatPhong) throws SQLException {
        String sql = "SELECT maHD FROM HoaDon WHERE maDatPhong = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maDatPhong);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("maHD");
                }
            }
        }
        return null;
    }

    private boolean hasInvoiceDetails(Connection con, String maHD) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ChiTietHoaDon WHERE maHD = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private List<Object[]> getReservedRooms(Connection con, String maDatPhong) throws SQLException {
        List<Object[]> list = new ArrayList<>();

        String sql =
                "SELECT maPhong, ngayNhanDuKien, ngayTraDuKien, donGiaDat " +
                        "FROM ChiTietDatPhong " +
                        "WHERE maDatPhong = ? " +
                        "ORDER BY maPhong";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maDatPhong);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                            rs.getString("maPhong"),
                            rs.getTimestamp("ngayNhanDuKien"),
                            rs.getTimestamp("ngayTraDuKien"),
                            rs.getBigDecimal("donGiaDat")
                    });
                }
            }
        }

        return list;
    }

    private void updateInvoiceMoney(Connection con, String maHD, BigDecimal totalRoom) throws SQLException {
        BigDecimal totalService = BigDecimal.ZERO;

        String serviceSql = "SELECT COALESCE(SUM(thanhTien), 0) FROM ChiTietDichVu WHERE maHD = ?";
        try (PreparedStatement ps = con.prepareStatement(serviceSql)) {
            ps.setString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalService = rs.getBigDecimal(1);
                    if (totalService == null) {
                        totalService = BigDecimal.ZERO;
                    }
                }
            }
        }

        BigDecimal discount = BigDecimal.ZERO;

        String discountSql = "SELECT COALESCE(tienKhuyenMai, 0) FROM HoaDon WHERE maHD = ?";
        try (PreparedStatement ps = con.prepareStatement(discountSql)) {
            ps.setString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    discount = rs.getBigDecimal(1);
                    if (discount == null) {
                        discount = BigDecimal.ZERO;
                    }
                }
            }
        }

        BigDecimal beforeTax = totalRoom.add(totalService).subtract(discount);
        if (beforeTax.compareTo(BigDecimal.ZERO) < 0) {
            beforeTax = BigDecimal.ZERO;
        }

        BigDecimal tax = beforeTax.multiply(BigDecimal.valueOf(0.1));
        BigDecimal finalTotal = beforeTax.add(tax);

        String updateSql =
                "UPDATE HoaDon SET " +
                        "tienPhong = ?, " +
                        "tienDichVu = ?, " +
                        "tienThue = ?, " +
                        "tongTienThanhToan = ? " +
                        "WHERE maHD = ?";

        try (PreparedStatement ps = con.prepareStatement(updateSql)) {
            ps.setBigDecimal(1, totalRoom);
            ps.setBigDecimal(2, totalService);
            ps.setBigDecimal(3, tax);
            ps.setBigDecimal(4, finalTotal);
            ps.setString(5, maHD);
            ps.executeUpdate();
        }
    }

    private BigDecimal calculateEarlyCheckInFee(BigDecimal nightlyRate, LocalDateTime checkInTime) {
        if (nightlyRate == null || checkInTime == null) {
            return BigDecimal.ZERO;
        }
        LocalTime time = checkInTime.toLocalTime();
        if (!time.isBefore(CHECKIN_STANDARD)) {
            return BigDecimal.ZERO;
        }
        if (time.isBefore(EARLY_CHECKIN_START)) {
            return BigDecimal.ZERO;
        }
        if (!time.isBefore(EARLY_CHECKIN_50)) {
            return nightlyRate.multiply(BigDecimal.valueOf(0.3));
        }
        return nightlyRate.multiply(BigDecimal.valueOf(0.5));
    }

    private Map<String, List<String>> loadRoomCodes(Connection con, java.util.Set<String> bookingIds) throws SQLException {
        if (bookingIds.isEmpty()) {
            return Collections.emptyMap();
        }

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < bookingIds.size(); i++) {
            placeholders.append(i == 0 ? "?" : ",?");
        }

        String sql =
                "SELECT maDatPhong, maPhong " +
                        "FROM ChiTietDatPhong " +
                        "WHERE maDatPhong IN (" + placeholders + ") " +
                        "ORDER BY maDatPhong, maPhong";

        Map<String, List<String>> result = new LinkedHashMap<>();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int idx = 1;
            for (String id : bookingIds) {
                ps.setString(idx++, id);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maDP = rs.getString("maDatPhong");
                    String maP = rs.getString("maPhong");
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
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return con;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
