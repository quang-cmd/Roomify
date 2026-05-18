package kqlhotel.entity.statistics;

/** 1 điểm dữ liệu cho biểu đồ doanh thu theo tháng. Label dạng "MM/YY". */
public class RevenuePoint {
    private final String label;
    private final double revenue;
    private double profit;

    public RevenuePoint(String label, double revenue) {
        this.label = label;
        this.revenue = revenue;
        this.profit = revenue; // Default to revenue if not set
    }

    public String getLabel()   { return label; }
    public double getRevenue() { return revenue; }
    public double getProfit()  { return profit; }
    public void setProfit(double profit) { this.profit = profit; }
}
