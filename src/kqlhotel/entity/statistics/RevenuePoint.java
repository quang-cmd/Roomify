package kqlhotel.entity.statistics;

/** 1 điểm dữ liệu cho biểu đồ doanh thu theo tháng. Label dạng "MM/YY". */
public class RevenuePoint {
    private final String label;
    private final double revenue;

    public RevenuePoint(String label, double revenue) {
        this.label = label;
        this.revenue = revenue;
    }

    public String getLabel()   { return label; }
    public double getRevenue() { return revenue; }
}
