package kqlhotel.entity.statistics;

/**
 * Tổng hợp KPI cho dashboard thống kê.
 * - revenue       : Tổng doanh thu (VND) các hoá đơn DaThanhToan trong range.
 * - totalRooms    : Tổng số phòng vật lý hiện có.
 * - occupiedRooms : Số phòng 'DangSuDung' tại snapshot hiện tại.
 * - totalBookings : Số lượt đặt phòng mới trong range.
 */
public class KpiSummary {
    private final double revenue;
    private final double expenses;
    private final double profit;
    private final int totalRooms;
    private final int occupiedRooms;
    private final int totalBookings;

    public KpiSummary(double revenue, int totalRooms, int occupiedRooms, int totalBookings) {
        this(revenue, 0, totalRooms, occupiedRooms, totalBookings);
    }

    public KpiSummary(double revenue, double expenses, int totalRooms, int occupiedRooms, int totalBookings) {
        this.revenue = revenue;
        this.expenses = expenses;
        this.profit = revenue - expenses;
        this.totalRooms = totalRooms;
        this.occupiedRooms = occupiedRooms;
        this.totalBookings = totalBookings;
    }

    public double getRevenue()       { return revenue; }
    public double getExpenses()      { return expenses; }
    public double getProfit()        { return profit; }
    public int    getTotalRooms()    { return totalRooms; }
    public int    getOccupiedRooms() { return occupiedRooms; }
    public int    getTotalBookings() { return totalBookings; }

    public double getOccupancyRate() {
        return totalRooms == 0 ? 0.0 : (double) occupiedRooms / totalRooms;
    }
}
