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
import kqlhotel.dao.payment.PaymentDAO;
import kqlhotel.entity.statistics.ExpenseRecord;
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
    private final PaymentDAO paymentDAO = new PaymentDAO();

    /** Tổng doanh thu trong khoảng [start, end]. */
    public double getRevenue(LocalDateTime start, LocalDateTime end) {
        return paymentDAO.getSuccessfulRevenue(start, end);
    }

    public int countTotalRooms() {
        return countQuery("SELECT COUNT(*) FROM Phong WHERE trangThaiPhong <> 'BaoTri'");
    }

    public int countOccupiedRooms() {
        return countQuery("SELECT COUNT(*) FROM Phong WHERE trangThaiPhong = 'DangSuDung'");
    }

    public int countBookings(LocalDateTime start, LocalDateTime end) {
        String sql =
            "SELECT COUNT(*) " +
            "FROM DatPhong " +
            "WHERE ngayDat >= ? AND ngayDat < ? " +
            "  AND trangThaiDatPhong <> 'DaHuy'";
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

    public int countUpcomingCheckInRooms(LocalDate date) {
        String sql =
            "SELECT COUNT(*) " +
            "FROM ChiTietDatPhong ctdp " +
            "JOIN DatPhong dp ON dp.maDatPhong = ctdp.maDatPhong " +
            "WHERE ctdp.ngayNhanDuKien >= ? AND ctdp.ngayNhanDuKien < ? " +
            "  AND dp.trangThaiDatPhong = 'DaDat'";
        return countDateRangeQuery(sql, date.atStartOfDay(), date.plusDays(1).atStartOfDay());
    }

    public int countUpcomingCheckInBookings(LocalDate date) {
        String sql =
            "SELECT COUNT(DISTINCT ctdp.maDatPhong) " +
            "FROM ChiTietDatPhong ctdp " +
            "JOIN DatPhong dp ON dp.maDatPhong = ctdp.maDatPhong " +
            "WHERE ctdp.ngayNhanDuKien >= ? AND ctdp.ngayNhanDuKien < ? " +
            "  AND dp.trangThaiDatPhong = 'DaDat'";
        return countDateRangeQuery(sql, date.atStartOfDay(), date.plusDays(1).atStartOfDay());
    }

    private int countDateRangeQuery(String sql, LocalDateTime start, LocalDateTime end) {
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(start));
            ps.setTimestamp(2, Timestamp.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("StatisticsDAO.countDateRangeQuery: " + e.getMessage());
        }
        return 0;
    }

    public double getManualExpenses(LocalDateTime start, LocalDateTime end) {
        if (!chiPhiTableExists()) {
            return 0;
        }
        String sql =
            "SELECT COALESCE(SUM(soTien), 0) AS total " +
            "FROM ChiPhi " +
            "WHERE ngayChi >= ? AND ngayChi < ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(start));
            ps.setTimestamp(2, Timestamp.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("StatisticsDAO.getManualExpenses: " + e.getMessage());
        }
        return 0;
    }

    public double getSalaryExpenses(LocalDate start, LocalDate end) {
        long days = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
        if (days <= 0) {
            return 0;
        }
        String sql =
            "SELECT COALESCE(SUM(luong), 0) AS monthlySalary " +
            "FROM NhanVien nv " +
            "JOIN TaiKhoan tk ON nv.tenDangNhap = tk.tenDangNhap " +
            "WHERE tk.trangThaiTK = 'DangHoatDong'";
        try (Connection con = ConnectDB.getInstance().getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return (rs.getDouble("monthlySalary") / 30.0) * days;
            }
        } catch (SQLException e) {
            System.err.println("StatisticsDAO.getSalaryExpenses: " + e.getMessage());
        }
        return 0;
    }

    public List<ExpenseRecord> getExpenses() {
        List<ExpenseRecord> list = new ArrayList<>();
        if (!chiPhiTableExists()) {
            return list;
        }
        String sql =
            "SELECT maChiPhi, loaiChiPhi, tenChiPhi, soTien, ngayChi, ghiChu " +
            "FROM ChiPhi ORDER BY maChiPhi ASC";
        try (Connection con = ConnectDB.getInstance().getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("ngayChi");
                list.add(new ExpenseRecord(
                    rs.getInt("maChiPhi"),
                    rs.getString("loaiChiPhi"),
                    rs.getString("tenChiPhi"),
                    rs.getDouble("soTien"),
                    ts == null ? null : ts.toLocalDateTime(),
                    rs.getString("ghiChu")
                ));
            }
        } catch (SQLException e) {
            System.err.println("StatisticsDAO.getExpenses: " + e.getMessage());
        }
        return list;
    }

    public boolean addExpense(String type, String name, double amount, LocalDateTime date, String note,
                              String maNV, String maPC) {
        if (!chiPhiTableExists()) {
            return false;
        }
        if (maNV == null || maNV.isBlank()) {
            return false;
        }
        String sql = "INSERT INTO ChiPhi (loaiChiPhi, tenChiPhi, soTien, ngayChi, ghiChu, maNV, maPC) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, type);
            ps.setString(2, name);
            ps.setBigDecimal(3, java.math.BigDecimal.valueOf(amount));
            ps.setTimestamp(4, Timestamp.valueOf(date));
            ps.setString(5, note);
            ps.setString(6, maNV);
            ps.setString(7, maPC == null || maPC.isBlank() ? null : maPC);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("StatisticsDAO.addExpense: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteExpense(int id) {
        if (!chiPhiTableExists()) {
            return false;
        }
        String sql = "DELETE FROM ChiPhi WHERE maChiPhi = ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("StatisticsDAO.deleteExpense: " + e.getMessage());
            return false;
        }
    }

    private boolean chiPhiTableExists() {
        return countQuery("SELECT CASE WHEN OBJECT_ID('dbo.ChiPhi', 'U') IS NULL THEN 0 ELSE 1 END") == 1;
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

        LocalDate startDate = now.minusMonths(monthsBack - 1L).withDayOfMonth(1);
        Map<String, Double> revenueByMonth = paymentDAO.getSuccessfulMonthlyRevenue(startDate, now);
        for (Map.Entry<String, Double> entry : revenueByMonth.entrySet()) {
            if (buckets.containsKey(entry.getKey())) {
                buckets.put(entry.getKey(), entry.getValue());
            }
        }

        List<RevenuePoint> result = new ArrayList<>(buckets.size());
        for (Map.Entry<String, Double> e : buckets.entrySet()) {
            result.add(new RevenuePoint(e.getKey(), e.getValue()));
        }
        return result;
    }

    /**
     * Doanh thu theo tháng trong khoảng [start, end].
     */
    public List<RevenuePoint> getMonthlyRevenue(LocalDate start, LocalDate end) {
        Map<String, Double> buckets = new LinkedHashMap<>();
        LocalDate cur = start.withDayOfMonth(1);
        LocalDate endCur = end.withDayOfMonth(1);
        while (!cur.isAfter(endCur)) {
            buckets.put(String.format("%02d/%02d", cur.getMonthValue(), cur.getYear() % 100), 0.0);
            cur = cur.plusMonths(1);
        }

        Map<String, Double> revenueByMonth = paymentDAO.getSuccessfulMonthlyRevenue(start, end);
        for (Map.Entry<String, Double> entry : revenueByMonth.entrySet()) {
            if (buckets.containsKey(entry.getKey())) {
                buckets.put(entry.getKey(), entry.getValue());
            }
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

        Map<LocalDate, Double> revenueByDate = paymentDAO.getSuccessfulDailyRevenue(start, end);
        for (Map.Entry<LocalDate, Double> entry : revenueByDate.entrySet()) {
            LocalDate dt = entry.getKey();
            String key = String.format("%02d/%02d", dt.getDayOfMonth(), dt.getMonthValue());
            if (buckets.containsKey(key)) {
                buckets.put(key, entry.getValue());
            }
        }

        List<RevenuePoint> result = new ArrayList<>(buckets.size());
        for (Map.Entry<String, Double> e : buckets.entrySet()) {
            result.add(new RevenuePoint(e.getKey(), e.getValue()));
        }
        return result;
    }

    public Map<String, Double> getDailyManualExpenses(LocalDate start, LocalDate end) {
        Map<String, Double> buckets = new LinkedHashMap<>();
        if (!chiPhiTableExists()) return buckets;

        String sql =
            "SELECT CAST(ngayChi AS DATE) AS dt, SUM(soTien) AS total " +
            "FROM ChiPhi " +
            "WHERE ngayChi >= ? AND ngayChi < ? " +
            "GROUP BY CAST(ngayChi AS DATE)";

        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(start.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(end.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate dt = rs.getDate("dt").toLocalDate();
                    String key = String.format("%02d/%02d", dt.getDayOfMonth(), dt.getMonthValue());
                    buckets.put(key, rs.getDouble("total"));
                }
            }
        } catch (SQLException e) {
            System.err.println("StatisticsDAO.getDailyManualExpenses: " + e.getMessage());
        }
        return buckets;
    }

    public Map<String, Double> getMonthlyManualExpenses(LocalDate start, LocalDate end) {
        Map<String, Double> buckets = new LinkedHashMap<>();
        if (!chiPhiTableExists()) return buckets;

        String sql =
            "SELECT YEAR(ngayChi) AS yr, MONTH(ngayChi) AS mo, SUM(soTien) AS total " +
            "FROM ChiPhi " +
            "WHERE ngayChi >= ? AND ngayChi < ? " +
            "GROUP BY YEAR(ngayChi), MONTH(ngayChi)";

        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(start.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(end.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String key = String.format("%02d/%02d", rs.getInt("mo"), rs.getInt("yr") % 100);
                    buckets.put(key, rs.getDouble("total"));
                }
            }
        } catch (SQLException e) {
            System.err.println("StatisticsDAO.getMonthlyManualExpenses: " + e.getMessage());
        }
        return buckets;
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
     * Mỗi DatPhong chỉ trả về 1 dòng, gộp danh sách phòng để booking nhiều phòng không bị hiểu nhầm.
     * Status lấy từ DatPhong.trangThaiDatPhong.
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
            "    SELECT dp.maDatPhong, dp.ngayDat, kh.hoTenKH, " +
            "           STRING_AGG(CAST(p.maPhong AS VARCHAR(10)), ', ') WITHIN GROUP (ORDER BY p.maPhong) AS maPhong, " +
            "           STRING_AGG(CAST(lp.tenLoaiPhong AS NVARCHAR(100)), N', ') WITHIN GROUP (ORDER BY p.maPhong) AS tenLoaiPhong, " +
            "           CASE dp.trangThaiDatPhong " +
            "                WHEN 'DaDat' THEN N'Sắp nhận' " +
            "                WHEN 'DangO' THEN N'Đang ở' " +
            "                WHEN 'DaTra' THEN N'Đã trả' " +
            "                WHEN 'DaHuy' THEN N'Đã hủy' " +
            "                ELSE dp.trangThaiDatPhong END AS trangThai " +
            "    FROM DatPhong dp " +
            "    JOIN KhachHang        kh   ON dp.maKH        = kh.maKH " +
            "    JOIN ChiTietDatPhong  ctdp ON dp.maDatPhong  = ctdp.maDatPhong " +
            "    JOIN Phong            p    ON ctdp.maPhong   = p.maPhong " +
            "    JOIN LoaiPhong        lp   ON p.maLoaiPhong  = lp.maLoaiPhong " +
            "    GROUP BY dp.maDatPhong, dp.ngayDat, kh.hoTenKH, dp.trangThaiDatPhong " +
            ") ranked " +
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
            "    JOIN DatPhong dp ON dp.maDatPhong = hd.maDatPhong " +
            "    WHERE cthd.ngayNhanPhong IS NOT NULL " +
            "      AND dp.trangThaiDatPhong <> 'DaHuy' " +
            "      AND hd.trangThai <> 'DaHuy' " +
            "    UNION " +
            "    SELECT CAST(ctdp.ngayNhanDuKien AS DATE) AS inDate, " +
            "           CAST(ctdp.ngayTraDuKien AS DATE) AS outDate, " +
            "           ctdp.maPhong " +
            "    FROM ChiTietDatPhong ctdp " +
            "    JOIN DatPhong dp ON dp.maDatPhong = ctdp.maDatPhong " +
            "    WHERE dp.trangThaiDatPhong = 'DaDat' " +
            "      AND NOT EXISTS (" +
            "          SELECT 1 FROM ChiTietHoaDon cthd2 " +
            "          JOIN HoaDon hd2 ON hd2.maHD = cthd2.maHD " +
            "          WHERE hd2.maDatPhong = dp.maDatPhong AND cthd2.maPhong = ctdp.maPhong" +
            "      )" +
            ")" +
            "SELECT ds.dt AS date, (SELECT cnt FROM TotalRooms) AS totalRooms, COUNT(DISTINCT o.maPhong) AS occupiedRooms " +
            "FROM DateSeries ds " +
            "LEFT JOIN Occupied o ON ds.dt >= o.inDate AND ds.dt < o.outDate " +
            "GROUP BY ds.dt " +
            "ORDER BY ds.dt " +
            "OPTION (MAXRECURSION 0)";

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
        String sql = """
            WITH DateSeries AS (
                SELECT CAST(? AS DATE) AS dt
                UNION ALL
                SELECT DATEADD(DAY, 1, dt) FROM DateSeries WHERE dt < ?
            ),
            TotalRooms AS (
                SELECT COUNT(*) AS cnt FROM Phong WHERE trangThaiPhong <> 'BaoTri'
            ),
            RoomStats AS (
                SELECT CAST(cthd.ngayNhanPhong AS DATE) AS dt,
                       SUM(cthd.thanhTien) AS roomRevenue,
                       SUM(cthd.soDem) AS totalRoomNights
                FROM ChiTietHoaDon cthd
                JOIN HoaDon hd ON cthd.maHD = hd.maHD
                JOIN DatPhong dp ON dp.maDatPhong = hd.maDatPhong
                WHERE cthd.ngayNhanPhong >= ? AND cthd.ngayNhanPhong < ?
                  AND dp.trangThaiDatPhong <> 'DaHuy'
                  AND hd.trangThai <> 'DaHuy'
                GROUP BY CAST(cthd.ngayNhanPhong AS DATE)
            ),
            PaymentStats AS (
                SELECT CAST(ngayTT AS DATE) AS dt,
                       SUM(CASE
                           WHEN loaiGD = 'HoanTien' THEN -soTienTT
                           ELSE soTienTT
                       END) AS totalRevenue
                FROM ThanhToan
                WHERE ngayTT >= ? AND ngayTT < ?
                  AND trangThaiTT = 'ThanhToanThanhCong'
                GROUP BY CAST(ngayTT AS DATE)
            )
            SELECT ds.dt AS date,
                   COALESCE(rs.roomRevenue / NULLIF(CAST(rs.totalRoomNights AS DECIMAL(18, 2)), 0), 0) AS adr,
                   COALESCE(rs.roomRevenue / NULLIF(CAST(tr.cnt AS DECIMAL(18, 2)), 0), 0) AS revpar,
                   COALESCE(ps.totalRevenue / NULLIF(CAST(tr.cnt AS DECIMAL(18, 2)), 0), 0) AS trevpar
            FROM DateSeries ds
            CROSS JOIN TotalRooms tr
            LEFT JOIN RoomStats rs ON ds.dt = rs.dt
            LEFT JOIN PaymentStats ps ON ds.dt = ps.dt
            ORDER BY ds.dt
            OPTION (MAXRECURSION 0)
        """;

        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(start));
            ps.setDate(2, java.sql.Date.valueOf(end));
            ps.setTimestamp(3, Timestamp.valueOf(start.atStartOfDay()));
            ps.setTimestamp(4, Timestamp.valueOf(end.plusDays(1).atStartOfDay()));
            ps.setTimestamp(5, Timestamp.valueOf(start.atStartOfDay()));
            ps.setTimestamp(6, Timestamp.valueOf(end.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate date = rs.getDate("date").toLocalDate();
                    double adr = rs.getDouble("adr");
                    double revpar = rs.getDouble("revpar");
                    double trevpar = rs.getDouble("trevpar");
                    list.add(new HotelKpiPoint(date, adr, revpar, trevpar));
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
            "JOIN DatPhong dp ON dp.maDatPhong = hd.maDatPhong " +
            "WHERE cthd.ngayNhanPhong >= ? AND cthd.ngayNhanPhong < ? " +
            "  AND dp.trangThaiDatPhong <> 'DaHuy' " +
            "  AND hd.trangThai <> 'DaHuy'";

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

        // TrevPAR uses the actual successful cash flow recorded in ThanhToan.
        double revenue = paymentDAO.getSuccessfulRevenue(start.atStartOfDay(), end.plusDays(1).atStartOfDay());
        return revenue / (totalRooms * days);
    }

    /**
     * ADR snapshot cho range.
     */
    public double getAdr(LocalDate start, LocalDate end) {
        String sql =
            "SELECT SUM(cthd.thanhTien) AS roomRevenue, SUM(cthd.soDem) AS totalNights " +
            "FROM ChiTietHoaDon cthd " +
            "JOIN HoaDon hd ON cthd.maHD = hd.maHD " +
            "JOIN DatPhong dp ON dp.maDatPhong = hd.maDatPhong " +
            "WHERE cthd.ngayNhanPhong >= ? AND cthd.ngayNhanPhong < ? " +
            "  AND dp.trangThaiDatPhong <> 'DaHuy' " +
            "  AND hd.trangThai <> 'DaHuy'";

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
