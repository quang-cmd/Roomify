package kqlhotel.entity.statistics;

import java.time.LocalDate;

/**
 * Point dữ liệu cho KPI khách sạn chuẩn ngành: ADR, RevPAR, TrevPAR theo ngày.
 */
public class HotelKpiPoint {
    private final LocalDate date;
    private final double adr;      // Average Daily Rate
    private final double revpar;   // Revenue Per Available Room
    private final double trevpar;  // Total Revenue Per Available Room

    public HotelKpiPoint(LocalDate date, double adr, double revpar, double trevpar) {
        this.date = date;
        this.adr = adr;
        this.revpar = revpar;
        this.trevpar = trevpar;
    }

    public LocalDate getDate() { return date; }
    public double getAdr() { return adr; }
    public double getRevpar() { return revpar; }
    public double getTrevpar() { return trevpar; }

    public String getLabel() {
        return String.format("%02d/%02d", date.getDayOfMonth(), date.getMonthValue());
    }
}
