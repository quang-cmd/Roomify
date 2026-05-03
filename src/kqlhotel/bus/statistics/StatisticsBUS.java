package kqlhotel.bus.statistics;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import kqlhotel.dao.statistics.StatisticsDAO;
import kqlhotel.entity.statistics.HotelKpiPoint;
import kqlhotel.entity.statistics.KpiSummary;
import kqlhotel.entity.statistics.OccupancyPoint;
import kqlhotel.entity.statistics.RecentBooking;
import kqlhotel.entity.statistics.RevenuePoint;
import kqlhotel.entity.statistics.RoomTypeShare;

/**
 * Tầng nghiệp vụ cho dashboard thống kê.
 * Wrap {@link StatisticsDAO} và áp dụng range filter cho UI.
 */
public class StatisticsBUS {
    private final StatisticsDAO dao = new StatisticsDAO();

    /** Số tháng hiển thị mặc định trên biểu đồ doanh thu. */
    private static final int MONTHLY_CHART_MONTHS = 6;

    /** Số dòng đặt phòng gần đây hiển thị mặc định. */
    private static final int RECENT_BOOKINGS_LIMIT = 8;

    /**
     * Tải KPI cho dashboard theo khoảng thời gian (tính từ hiện tại lùi về {@code daysBack} ngày).
     * Tổng phòng / Phòng đang dùng là snapshot, không phụ thuộc range.
     */
    public KpiSummary loadKpis(int daysBack) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(daysBack);
        double revenue   = dao.getRevenue(start, end);
        int totalRooms   = dao.countTotalRooms();
        int occupied     = dao.countOccupiedRooms();
        int totalBookings = dao.countBookings(start, end);
        return new KpiSummary(revenue, totalRooms, occupied, totalBookings);
    }

    public List<RevenuePoint> loadMonthlyRevenue() {
        return dao.getMonthlyRevenue(MONTHLY_CHART_MONTHS);
    }

    /**
     * Tải doanh thu theo range: 7/30 ngày → daily, 6 tháng → monthly.
     */
    public List<RevenuePoint> loadRevenueByRange(int daysBack) {
        if (daysBack <= 30) {
            // 7 hoặc 30 ngày: doanh thu theo ngày
            LocalDate end = LocalDate.now();
            LocalDate start = end.minusDays(daysBack - 1);
            return dao.getDailyRevenue(start, end);
        } else {
            // 6 tháng: doanh thu theo tháng
            return dao.getMonthlyRevenue(6);
        }
    }

    public List<RoomTypeShare> loadRoomTypeDistribution() {
        return dao.getRoomTypeDistribution();
    }

    public List<RecentBooking> loadRecentBookings() {
        return dao.getRecentBookings(RECENT_BOOKINGS_LIMIT);
    }

    /** Toàn bộ booking gần đây cho dialog "Xem tất cả" (cap 1000 trong DAO). */
    public List<RecentBooking> loadAllRecentBookings() {
        return dao.getAllRecentBookings();
    }

    /**
     * Tải tỷ lệ lấp đầy theo ngày trong khoảng thời gian.
     * @param daysBack Số ngày lùi về từ hôm nay
     */
    public List<OccupancyPoint> loadOccupancyTrend(int daysBack) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(daysBack - 1);
        return dao.getOccupancyTrend(start, end);
    }

    /**
     * Tải ADR (Average Daily Rate) cho range.
     * ADR = Doanh thu phòng / Số phòng đã bán
     */
    public double loadAdr(int daysBack) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(daysBack - 1);
        return dao.getAdr(start, end);
    }

    /**
     * Tải RevPAR (Revenue Per Available Room) cho range.
     * RevPAR = Tổng doanh thu phòng / (Tổng số phòng * số ngày)
     */
    public double loadRevpar(int daysBack) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(daysBack - 1);
        return dao.getRevpar(start, end);
    }

    /**
     * Tải TrevPAR (Total Revenue Per Available Room) cho range.
     * TrevPAR = Tổng doanh thu (phòng + dịch vụ) / (Tổng số phòng * số ngày)
     */
    public double loadTrevpar(int daysBack) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(daysBack - 1);
        return dao.getTrevpar(start, end);
    }

    /**
     * Tải ADR trend theo ngày (cho biểu đồ).
     */
    public List<HotelKpiPoint> loadAdrTrend(int daysBack) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(daysBack - 1);
        return dao.getAdrTrend(start, end);
    }

    /** Map nhãn nút range trên UI sang số ngày. */
    public static int rangeToDays(String range) {
        if (range == null) return 30;
        switch (range) {
            case "7 ngày":  return 7;
            case "30 ngày": return 30;
            case "6 tháng": return 180;
            default:        return 30;
        }
    }

    // ========== Overloads nhận LocalDate trực tiếp ==========

    public KpiSummary loadKpis(LocalDate start, LocalDate end) {
        double revenue = dao.getRevenue(start.atStartOfDay(), end.plusDays(1).atStartOfDay());
        int totalRooms = dao.countTotalRooms();
        int occupied = dao.countOccupiedRooms();
        int totalBookings = dao.countBookings(start.atStartOfDay(), end.plusDays(1).atStartOfDay());
        return new KpiSummary(revenue, totalRooms, occupied, totalBookings);
    }

    public double loadAdr(LocalDate start, LocalDate end) {
        return dao.getAdr(start, end);
    }

    public double loadRevpar(LocalDate start, LocalDate end) {
        return dao.getRevpar(start, end);
    }

    public double loadTrevpar(LocalDate start, LocalDate end) {
        return dao.getTrevpar(start, end);
    }

    public List<RevenuePoint> loadRevenueByRange(LocalDate start, LocalDate end) {
        long days = java.time.temporal.ChronoUnit.DAYS.between(start, end);
        if (days <= 30) {
            return dao.getDailyRevenue(start, end);
        } else {
            // Nếu > 30 ngày, group theo tháng
            return dao.getMonthlyRevenue((int) (days / 30) + 1);
        }
    }

    public List<OccupancyPoint> loadOccupancyTrend(LocalDate start, LocalDate end) {
        return dao.getOccupancyTrend(start, end);
    }

    public List<HotelKpiPoint> loadAdrTrend(LocalDate start, LocalDate end) {
        return dao.getAdrTrend(start, end);
    }
}
