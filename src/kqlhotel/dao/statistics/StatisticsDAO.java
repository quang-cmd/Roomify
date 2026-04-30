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
import kqlhotel.entity.statistics.RecentBooking;
import kqlhotel.entity.statistics.RevenuePoint;
import kqlhotel.entity.statistics.RoomTypeShare;

/**
 * Tổ hợp các query phục vụ dashboard thống kê.
 * Không thay thế các DAO khác — chỉ aggregate read-only.
 */
public class StatisticsDAO {

    /** Tổng doanh thu (DaThanhToan) trong khoảng [start, end]. */
    public double getRevenue(LocalDateTime start, LocalDateTime end) {
        String sql =
            "SELECT COALESCE(SUM(tongTienThanhToan), 0) AS total " +
            "FROM HoaDon " +
            "WHERE trangThai = 'DaThanhToan' " +
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
        return countQuery("SELECT COUNT(*) FROM Phong");
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
            "SELECT YEAR(ngayLapHD) AS yr, MONTH(ngayLapHD) AS mo, " +
            "       SUM(tongTienThanhToan) AS total " +
            "FROM HoaDon " +
            "WHERE trangThai = 'DaThanhToan' " +
            "  AND ngayLapHD >= ? AND ngayLapHD < ? " +
            "GROUP BY YEAR(ngayLapHD), MONTH(ngayLapHD)";
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
            "    SELECT dp.maDatPhong, dp.ngayDat, dp.ngayNhanDuKien, dp.ngayTraDuKien, " +
            "           kh.hoTenKH, p.maPhong, lp.tenLoaiPhong, " +
            "           CASE WHEN dp.ngayNhanDuKien > GETDATE() THEN N'Sắp đến' " +
            "                WHEN dp.ngayTraDuKien  < GETDATE() THEN N'Đã xong' " +
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
}
