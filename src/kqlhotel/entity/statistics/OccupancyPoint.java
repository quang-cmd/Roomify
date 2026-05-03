package kqlhotel.entity.statistics;

import java.time.LocalDate;

/**
 * 1 điểm dữ liệu cho biểu đồ tỷ lệ lấp đầy theo thời gian.
 * - date: Ngày
 * - occupiedRooms: Số phòng đang sử dụng
 * - totalRooms: Tổng số phòng vật lý
 * - rate: Tỷ lệ lấp đầy (0.0 - 1.0)
 */
public class OccupancyPoint {
    private final LocalDate date;
    private final int occupiedRooms;
    private final int totalRooms;

    public OccupancyPoint(LocalDate date, int occupiedRooms, int totalRooms) {
        this.date = date;
        this.occupiedRooms = occupiedRooms;
        this.totalRooms = totalRooms;
    }

    public LocalDate getDate() { return date; }
    public int getOccupiedRooms() { return occupiedRooms; }
    public int getTotalRooms() { return totalRooms; }

    public double getRate() {
        return totalRooms == 0 ? 0.0 : (double) occupiedRooms / totalRooms;
    }

    public String getLabel() {
        return String.format("%02d/%02d", date.getDayOfMonth(), date.getMonthValue());
    }
}
