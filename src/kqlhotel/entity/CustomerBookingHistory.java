package kqlhotel.entity;

import java.time.LocalDateTime;
import java.util.Date;
import java.time.ZoneId;

public class CustomerBookingHistory {
    private String maHD;
    private LocalDateTime ngayLapHD;
    private double tongTien;
    private String tinhTrang;
    
    // For 20:10 UI compatibility
    private String maDatPhong;

    public CustomerBookingHistory() {}

    public String getMaHD() { return maHD; }
    public void setMaHD(String maHD) { this.maHD = maHD; }
    public String getMaHoaDon() { return maHD; }

    public String getMaDatPhong() { return maDatPhong; }
    public void setMaDatPhong(String maDatPhong) { this.maDatPhong = maDatPhong; }

    public LocalDateTime getNgayLapHD() { return ngayLapHD; }
    public void setNgayLapHD(LocalDateTime ngayLapHD) { this.ngayLapHD = ngayLapHD; }
    
    public Date getNgayLap() {
        return ngayLapHD == null ? null : Date.from(ngayLapHD.atZone(ZoneId.systemDefault()).toInstant());
    }

    public double getTongTien() { return tongTien; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }

    public String getTinhTrang() { return tinhTrang; }
    public void setTinhTrang(String tinhTrang) { this.tinhTrang = tinhTrang; }
    public String getTrangThai() { return tinhTrang; }
}
