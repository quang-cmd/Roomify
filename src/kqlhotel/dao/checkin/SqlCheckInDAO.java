package kqlhotel.dao.checkin;

import java.math.BigDecimal;
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
import kqlhotel.bus.checkin.model.RoomCheckInCommand;
import kqlhotel.dao.ConnectDB;

public class SqlCheckInDAO implements CheckInDAO {

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
                        "  AND dp.trangThaiDatPhong = 'DaDat' " +
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
                        false
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }

        return arrivals;
    }

    @Override
    public String getInvoiceIdByBooking(String maDatPhong) throws Exception {
        Connection con = openConnection();
        if (con == null) throw new SQLException("Không thể kết nối CSDL.");
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

    @Override
    public boolean hasInvoiceDetails(String maHD) throws Exception {
        Connection con = openConnection();
        if (con == null) throw new SQLException("Không thể kết nối CSDL.");
        String sql = "SELECT COUNT(*) FROM ChiTietHoaDon WHERE maHD = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    @Override
    public boolean syncExistingCheckIn(String maDatPhong, String maHD) throws Exception {
        Connection con = openConnection();
        if (con == null) throw new SQLException("Khong the ket noi CSDL.");

        boolean oldAutoCommit = con.getAutoCommit();
        try {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE Phong SET trangThaiPhong = 'DangSuDung' " +
                            "WHERE maPhong IN (" +
                            "    SELECT maPhong FROM ChiTietHoaDon " +
                            "    WHERE maHD = ? AND ngayNhanPhong IS NOT NULL AND ngayTraThucTe IS NULL" +
                            ")")) {
                ps.setString(1, maHD);
                ps.executeUpdate();
            }

            int updatedBooking;
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE DatPhong SET trangThaiDatPhong = 'DangO' " +
                            "WHERE maDatPhong = ? AND trangThaiDatPhong = 'DaDat'")) {
                ps.setString(1, maDatPhong);
                updatedBooking = ps.executeUpdate();
            }

            con.commit();
            return updatedBooking > 0;
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(oldAutoCommit);
        }
    }

    @Override
    public String getInvoiceStatus(String maHD) throws Exception {
        Connection con = openConnection();
        if (con == null) throw new SQLException("Không thể kết nối CSDL.");
        String sql = "SELECT trangThai FROM HoaDon WHERE maHD = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("trangThai");
            }
        }
        return null;
    }

    @Override
    public List<Object[]> getReservedRooms(String maDatPhong) throws Exception {
        Connection con = openConnection();
        if (con == null) throw new SQLException("Không thể kết nối CSDL.");
        List<Object[]> list = new ArrayList<>();
        String sql =
                "SELECT c.maPhong, c.ngayNhanDuKien, c.ngayTraDuKien, c.donGiaDat, p.trangThaiPhong " +
                        "FROM ChiTietDatPhong c JOIN Phong p ON c.maPhong = p.maPhong " +
                        "WHERE c.maDatPhong = ? " +
                        "ORDER BY c.maPhong";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maDatPhong);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                            rs.getString("maPhong"),
                            rs.getTimestamp("ngayNhanDuKien"),
                            rs.getTimestamp("ngayTraDuKien"),
                            rs.getBigDecimal("donGiaDat"),
                            rs.getString("trangThaiPhong")
                    });
                }
            }
        }
        return list;
    }

    @Override
    public void executeCheckInTransaction(String maDatPhong, String maHD, List<RoomCheckInCommand> rooms, BigDecimal totalRoom) throws Exception {
        Connection con = openConnection();
        if (con == null) throw new SQLException("Không thể kết nối CSDL.");
        
        boolean oldAutoCommit = con.getAutoCommit();
        try {
            con.setAutoCommit(false);
            
            String insertSql =
                    "INSERT INTO ChiTietHoaDon " +
                            "(maHD, maPhong, ngayNhanPhong, ngayTraPhong, ngayTraThucTe, soDem, phuThu, thanhTien) " +
                        "VALUES (?, ?, ?, ?, NULL, ?, ?, ?)";

            try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                for (RoomCheckInCommand room : rooms) {
                    ps.setString(1, maHD);
                    ps.setString(2, room.getMaPhong());
                    ps.setTimestamp(3, Timestamp.valueOf(room.getNgayNhanThucTe()));
                    ps.setTimestamp(4, Timestamp.valueOf(room.getNgayTraDuKien()));
                    ps.setInt(5, room.getSoDem());
                    ps.setBigDecimal(6, room.getPhuThu());
                    ps.setBigDecimal(7, room.getThanhTien());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE Phong SET trangThaiPhong = 'DangSuDung' WHERE maPhong = ?")) {
                for (RoomCheckInCommand room : rooms) {
                    ps.setString(1, room.getMaPhong());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE DatPhong SET trangThaiDatPhong = 'DangO' WHERE maDatPhong = ? AND trangThaiDatPhong = 'DaDat'")) {
                ps.setString(1, maDatPhong);
                int updated = ps.executeUpdate();
                if (updated == 0) {
                    throw new SQLException("Khong the cap nhat trang thai dat phong sang DangO.");
                }
            }

            updateInvoiceMoney(con, maHD, totalRoom);

            con.commit();
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(oldAutoCommit);
        }
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
