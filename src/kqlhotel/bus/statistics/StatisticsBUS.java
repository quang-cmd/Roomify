package kqlhotel.bus.statistics;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import kqlhotel.dao.statistics.StatisticsDAO;
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
}
