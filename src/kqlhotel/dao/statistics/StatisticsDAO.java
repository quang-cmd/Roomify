package kqlhotel.dao.statistics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import kqlhotel.dao.ConnectDB;
import kqlhotel.entity.statistics.HotelKpiPoint;
import kqlhotel.entity.statistics.OccupancyPoint;
import kqlhotel.entity.statistics.RecentBooking;
import kqlhotel.entity.statistics.RevenuePoint;
import kqlhotel.entity.statistics.RoomTypeShare;

/**
 * Tổ hợp các query phục vụ dashboard thống kê.
 * Không thay thế các DAO khác — chỉ aggregate read-only.
 */
public class StatisticsDAO {

    /** Điều kiện doanh thu: DaThanhToan hoặc DaHuy nhưng đã thu tiền. */
    private static final String REVENUE_COND =
        "(trangThai = 'DaThanhToan' OR (trangThai = 'DaHuy' AND tongTienThanhToan > 0))";

    /** Tổng doanh thu trong khoảng [start, end]. */
    public double getRevenue(LocalDateTime start, LocalDateTime end) {
        String sql =
            "SELECT COALESCE(SUM(tongTienThanhToan), 0) AS total " +
            "FROM HoaDon " +
            "WHERE " + REVENUE_COND + " " +
            "  AND ngayThanhToan BETWEEN ? AND ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(start));
            ps.setTimestamp(2, Timestamp.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("StatisticsDAO.getRevenue: " + e.getMessage());
        }
        return 0;
    }

    public int countTotalRooms() {
        return countQuery("SELECT COUNT(*) FROM Phong WHERE trangThaiPhong <> 'BaoTri'");
    }

    public int countOccupiedRooms() {
        return countQuery("SELECT COUNT(*) FROM Phong WHERE trangThaiPhong = 'DangSuDung'");
    }

    public int countBookings(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT COUNT(*) FROM DatPhong WHERE ngayDat BETWEEN ? AND ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(start));
            ps.setTimestamp(2, Timestamp.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("StatisticsDAO.countBookings: " + e.getMessage());
        }
        return 0;
    }

    private int countQuery(String sql) {
        try (Connection con = ConnectDB.getInstance().getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("StatisticsDAO.countQuery: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Doanh thu N tháng gần nhất (bao gồm tháng hiện tại), thứ tự thời gian tăng dần.
     * Tháng không có dữ liệu vẫn xuất hiện với revenue = 0 để chart hiển thị đầy đủ.
     */
    public List<RevenuePoint> getMonthlyRevenue(int monthsBack) {
        LocalDate now = LocalDate.now();
        Map<String, Double> buckets = new LinkedHashMap<>();
        for (int i = monthsBack - 1; i >= 0; i--) {
            LocalDate m = now.minusMonths(i);
            buckets.put(String.format("%02d/%02d", m.getMonthValue(), m.getYear() % 100), 0.0);
        }

        LocalDateTime startBound = now.minusMonths(monthsBack - 1L).withDayOfMonth(1).atStartOfDay();
        LocalDateTime endBound   = LocalDateTime.now().plusDays(1);

        String sql =
            "SELECT YEAR(ngayThanhToan) AS yr, MONTH(ngayThanhToan) AS mo, " +
            "       SUM(tongTienThanhToan) AS total " +
            "FROM HoaDon " +
            "WHERE " + REVENUE_COND + " " +
            "  AND ngayThanhToan >= ? AND ngayThanhToan < ? " +
            "GROUP BY YEAR(ngayThanhToan), MONTH(ngayThanhToan)";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(startBound));
            ps.setTimestamp(2, Timestamp.valueOf(endBound));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String key = String.format("%02d/%02d", rs.getInt("mo"), rs.getInt("yr") % 100);
                    if (buckets.containsKey(key)) {
                        buckets.put(key, rs.getDouble("total"));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("StatisticsDAO.getMonthlyRevenue: " + e.getMessage());
        }

        List<RevenuePoint> result = new ArrayList<>(buckets.size());
        for (Map.Entry<String, Double> e : buckets.entrySet()) {
            result.add(new RevenuePoint(e.getKey(), e.getValue()));
        }
        return result;
    }

    /**
     * Doanh thu theo ngày trong khoảng [start, end].
     * Ngày không có dữ liệu vẫn xuất hiện với revenue = 0.
     */
    public List<RevenuePoint> getDailyRevenue(LocalDate start, LocalDate end) {
        Map<String, Double> buckets = new LinkedHashMap<>();
        LocalDate cur = start;
        while (!cur.isAfter(end)) {
            buckets.put(String.format("%02d/%02d", cur.getDayOfMonth(), cur.getMonthValue()), 0.0);
            cur = cur.plusDays(1);
        }

        String sql =
            "SELECT CAST(ngayThanhToan AS DATE) AS dt, SUM(tongTienThanhToan) AS total " +
            "FROM HoaDon " +
            "WHERE " + REVENUE_COND + " " +
            "  AND ngayThanhToan >= ? AND ngayThanhToan < ? " +
            "GROUP BY CAST(ngayThanhToan AS DATE)";

        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(start.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(end.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate dt = rs.getDate("dt").toLocalDate();
                    String key = String.format("%02d/%02d", dt.getDayOfMonth(), dt.getMonthValue());
                    if (buckets.containsKey(key)) {
                        buckets.put(key, rs.getDouble("total"));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("StatisticsDAO.getDailyRevenue: " + e.getMessage());
        }

        List<RevenuePoint> result = new ArrayList<>(buckets.size());
        for (Map.Entry<String, Double> e : buckets.entrySet()) {
            result.add(new RevenuePoint(e.getKey(), e.getValue()));
        }
        return result;
    }

    public List<RoomTypeShare> getRoomTypeDistribution() {
        List<RoomTypeShare> list = new ArrayList<>();
        String sql =
            "SELECT lp.tenLoaiPhong AS name, COUNT(p.maPhong) AS cnt " +
            "FROM LoaiPhong lp " +
            "LEFT JOIN Phong p ON lp.maLoaiPhong = p.maLoaiPhong " +
            "GROUP BY lp.tenLoaiPhong " +
            "ORDER BY cnt DESC";
        try (Connection con = ConnectDB.getInstance().getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new RoomTypeShare(rs.getString("name"), rs.getInt("cnt")));
            }
        } catch (SQLException e) {
            System.err.println("StatisticsDAO.getRoomTypeDistribution: " + e.getMessage());
        }
        return list;
    }

    /**
     * Lấy {@code limit} đặt phòng gần nhất theo {@code ngayDat DESC}.
     * Mỗi DatPhong chỉ trả về 1 dòng (chọn phòng có maPhong đầu tiên qua ROW_NUMBER).
     * Status được suy luận theo ngày so với GETDATE().
     */
    public List<RecentBooking> getRecentBookings(int limit) {
        return queryRecentBookings(limit);
    }

    /**
     * Lấy toàn bộ booking gần đây để hiển thị trong dialog "Xem tất cả".
     * Đặt cap 1000 để tránh vô tình DoS UI nếu DB có quá nhiều record.
     */
    public List<RecentBooking> getAllRecentBookings() {
        return queryRecentBookings(1000);
    }

    private List<RecentBooking> queryRecentBookings(int limit) {
        List<RecentBooking> list = new ArrayList<>();
        String sql =
            "SELECT TOP (?) maPhong, hoTenKH, tenLoaiPhong, trangThai, ngayDat " +
            "FROM ( " +
            "    SELECT dp.maDatPhong, dp.ngayDat, ctdp.ngayNhanDuKien, ctdp.ngayTraDuKien, " +
            "           kh.hoTenKH, p.maPhong, lp.tenLoaiPhong, " +
            "           CASE WHEN ctdp.ngayNhanDuKien > GETDATE() THEN N'Sắp đến' " +
            "                WHEN ctdp.ngayTraDuKien  < GETDATE() THEN N'Đã xong' " +
            "                ELSE N'Đang ở' END AS trangThai, " +
            "           ROW_NUMBER() OVER (PARTITION BY dp.maDatPhong ORDER BY p.maPhong) AS rn " +
            "    FROM DatPhong dp " +
            "    JOIN KhachHang        kh   ON dp.maKH        = kh.maKH " +
            "    JOIN ChiTietDatPhong  ctdp ON dp.maDatPhong  = ctdp.maDatPhong " +
            "    JOIN Phong            p    ON ctdp.maPhong   = p.maPhong " +
            "    JOIN LoaiPhong        lp   ON p.maLoaiPhong  = lp.maLoaiPhong " +
            ") ranked " +
            "WHERE rn = 1 " +
            "ORDER BY ngayDat DESC";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("ngayDat");
                    list.add(new RecentBooking(
                        rs.getString("maPhong"),
                        rs.getString("hoTenKH"),
                        rs.getString("tenLoaiPhong"),
                        rs.getString("trangThai"),
                        ts == null ? null : ts.toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("StatisticsDAO.queryRecentBookings: " + e.getMessage());
        }
        return list;
    }

    /**
     * Tỷ lệ lấp đầy theo ngày trong khoảng [start, end].
     * Đếm số phòng đang sử dụng tại mỗi ngày dựa trên ChiTietHoaDon.
     * Phòng được coi là "đang dùng" nếu ngayNhanPhong <= date < COALESCE(ngayTraThucTe, ngayTraPhong).
     */
    public List<OccupancyPoint> getOccupancyTrend(LocalDate start, LocalDate end) {
        List<OccupancyPoint> list = new ArrayList<>();

        String sql =
            "WITH DateSeries AS (" +
            "    SELECT CAST(? AS DATE) AS dt " +
            "    UNION ALL" +
            "    SELECT DATEADD(DAY, 1, dt) FROM DateSeries WHERE dt < ?" +
            ")," +
            "TotalRooms AS (SELECT COUNT(*) AS cnt FROM Phong WHERE trangThaiPhong != 'BaoTri')," +
            "Occupied AS (" +
            "    SELECT CAST(cthd.ngayNhanPhong AS DATE) AS inDate, " +
            "           COALESCE(CAST(cthd.ngayTraThucTe AS DATE), CAST(cthd.ngayTraPhong AS DATE), CAST(GETDATE() AS DATE)) AS outDate, " +
            "           cthd.maPhong " +
            "    FROM ChiTietHoaDon cthd " +
            "    JOIN HoaDon hd ON cthd.maHD = hd.maHD " +
            "    WHERE cthd.ngayNhanPhong IS NOT NULL AND hd.trangThai != 'DaHuy' " +
            "    UNION " +
            "    SELECT CAST(ctdp.ngayNhanDuKien AS DATE) AS inDate, " +
            "           CAST(ctdp.ngayTraDuKien AS DATE) AS outDate, " +
            "           ctdp.maPhong " +
            "    FROM ChiTietDatPhong ctdp " +
            "    JOIN HoaDon hd ON hd.maDatPhong = ctdp.maDatPhong " +
            "    WHERE hd.trangThai = 'ChuaThanhToan' " +
            "      AND NOT EXISTS (SELECT 1 FROM ChiTietHoaDon cthd2 WHERE cthd2.maHD = hd.maHD AND cthd2.maPhong = ctdp.maPhong)" +
            ")" +
            "SELECT ds.dt AS date, (SELECT cnt FROM TotalRooms) AS totalRooms, COUNT(DISTINCT o.maPhong) AS occupiedRooms " +
            "FROM DateSeries ds " +
            "LEFT JOIN Occupied o ON ds.dt >= o.inDate AND ds.dt < o.outDate " +
            "GROUP BY ds.dt " +
            "ORDER BY ds.dt";

        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(start));
            ps.setDate(2, java.sql.Date.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate date = rs.getDate("date").toLocalDate();
                    int total = rs.getInt("totalRooms");
                    int occupied = rs.getInt("occupiedRooms");
                    list.add(new OccupancyPoint(date, occupied, total));
                }
            }
        } catch (SQLException e) {
            System.err.println("StatisticsDAO.getOccupancyTrend: " + e.getMessage());
        }
        return list;
    }

    /**
     * ADR (Average Daily Rate) theo ngày trong khoảng [start, end].
     * ADR = Doanh thu phòng / Số phòng đã bán
     */
    public List<HotelKpiPoint> getAdrTrend(LocalDate start, LocalDate end) {
        List<HotelKpiPoint> list = new ArrayList<>();
        String sql =
            "WITH DateSeries AS (" +
            "    SELECT CAST(? AS DATE) AS dt " +
            "    UNION ALL" +
            "    SELECT DATEADD(DAY, 1, dt) FROM DateSeries WHERE dt < ?" +
            ")," +
            "DailyStats AS (" +
            "    SELECT CAST(cthd.ngayNhanPhong AS DATE) AS dt, " +
            "           SUM(cthd.thanhTien) AS roomRevenue, " +
            "           SUM(cthd.soDem) AS totalNights, " +
            "           COUNT(DISTINCT cthd.maPhong) AS roomsSold " +
            "    FROM ChiTietHoaDon cthd " +
            "    JOIN HoaDon hd ON cthd.maHD = hd.maHD " +
            "    WHERE cthd.ngayNhanPhong >= ? AND cthd.ngayNhanPhong < ? " +
            "      AND hd.trangThai = 'DaThanhToan'" +
            "    GROUP BY CAST(cthd.ngayNhanPhong AS DATE)" +
            ")" +
            "SELECT ds.dt AS date, " +
            "       COALESCE(ds2.roomRevenue / NULLIF(ds2.totalNights, 0), 0) AS adr, " +
            "       0 AS revpar, 0 AS trevpar " +
            "FROM DateSeries ds " +
            "LEFT JOIN DailyStats ds2 ON ds.dt = ds2.dt " +
            "ORDER BY ds.dt";

        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(start));
            ps.setDate(2, java.sql.Date.valueOf(end));
            ps.setTimestamp(3, Timestamp.valueOf(start.atStartOfDay()));
            ps.setTimestamp(4, Timestamp.valueOf(end.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate date = rs.getDate("date").toLocalDate();
                    double adr = rs.getDouble("adr");
                    list.add(new HotelKpiPoint(date, adr, 0, 0));
                }
            }
        } catch (SQLException e) {
            System.err.println("StatisticsDAO.getAdrTrend: " + e.getMessage());
        }
        return list;
    }

    /**
     * RevPAR (Revenue Per Available Room) snapshot cho range.
     * RevPAR = Tổng doanh thu phòng / (Tổng số phòng * số ngày)
     */
    public double getRevpar(LocalDate start, LocalDate end) {
        int days = (int) java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
        int totalRooms = countTotalRooms();
        if (totalRooms == 0 || days == 0) return 0.0;

        String sql =
            "SELECT SUM(cthd.thanhTien) AS roomRevenue " +
            "FROM ChiTietHoaDon cthd " +
            "JOIN HoaDon hd ON cthd.maHD = hd.maHD " +
            "WHERE cthd.ngayNhanPhong >= ? AND cthd.ngayNhanPhong < ? " +
            "  AND hd.trangThai = 'DaThanhToan'";

        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(start.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(end.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double revenue = rs.getDouble("roomRevenue");
                    return revenue / (totalRooms * days);
                }
            }
        } catch (SQLException e) {
            System.err.println("StatisticsDAO.getRevpar: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * TrevPAR (Total Revenue Per Available Room) snapshot cho range.
     * TrevPAR = Tổng doanh thu (phòng + dịch vụ + phí phạt hủy) / (Tổng số phòng * số ngày)
     */
    public double getTrevpar(LocalDate start, LocalDate end) {
        int days = (int) java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
        int totalRooms = countTotalRooms();
        if (totalRooms == 0 || days == 0) return 0.0;

        // Tính tổng doanh thu: hóa đơn đã thanh toán + phí phạt từ hóa đơn đã hủy có tiền
        String sql =
            "SELECT SUM(hd.tongTienThanhToan) AS totalRevenue " +
            "FROM HoaDon hd " +
            "WHERE hd.ngayThanhToan >= ? AND hd.ngayThanhToan < ? " +
            "  AND (hd.trangThai = 'DaThanhToan' OR (hd.trangThai = 'DaHuy' AND hd.tongTienThanhToan > 0))";

        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(start.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(end.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double revenue = rs.getDouble("totalRevenue");
                    return revenue / (totalRooms * days);
                }
            }
        } catch (SQLException e) {
            System.err.println("StatisticsDAO.getTrevpar: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * ADR snapshot cho range.
     */
    public double getAdr(LocalDate start, LocalDate end) {
        String sql =
            "SELECT SUM(cthd.thanhTien) AS roomRevenue, SUM(cthd.soDem) AS totalNights " +
            "FROM ChiTietHoaDon cthd " +
            "JOIN HoaDon hd ON cthd.maHD = hd.maHD " +
            "WHERE cthd.ngayNhanPhong >= ? AND cthd.ngayNhanPhong < ? " +
            "  AND hd.trangThai = 'DaThanhToan'";

        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(start.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(end.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double revenue = rs.getDouble("roomRevenue");
                    int nights = rs.getInt("totalNights");
                    return nights > 0 ? revenue / nights : 0.0;
                }
            }
        } catch (SQLException e) {
            System.err.println("StatisticsDAO.getAdr: " + e.getMessage());
        }
        return 0.0;
    }
}
