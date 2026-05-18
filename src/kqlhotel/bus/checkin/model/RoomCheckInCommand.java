package kqlhotel.bus.checkin.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RoomCheckInCommand {
    private String maPhong;
    private LocalDateTime ngayNhanDuKien;
    private LocalDateTime ngayTraDuKien;
    private LocalDateTime ngayNhanThucTe;
    private BigDecimal donGia;
    private int soDem;
    private BigDecimal phuThu;
    private BigDecimal thanhTien;

    public RoomCheckInCommand(String maPhong, LocalDateTime ngayNhanDuKien, LocalDateTime ngayTraDuKien, LocalDateTime ngayNhanThucTe, BigDecimal donGia, int soDem, BigDecimal phuThu, BigDecimal thanhTien) {
        this.maPhong = maPhong;
        this.ngayNhanDuKien = ngayNhanDuKien;
        this.ngayTraDuKien = ngayTraDuKien;
        this.ngayNhanThucTe = ngayNhanThucTe;
        this.donGia = donGia;
        this.soDem = soDem;
        this.phuThu = phuThu;
        this.thanhTien = thanhTien;
    }

    public String getMaPhong() { return maPhong; }
    public LocalDateTime getNgayNhanDuKien() { return ngayNhanDuKien; }
    public LocalDateTime getNgayTraDuKien() { return ngayTraDuKien; }
    public LocalDateTime getNgayNhanThucTe() { return ngayNhanThucTe; }
    public BigDecimal getDonGia() { return donGia; }
    public int getSoDem() { return soDem; }
    public BigDecimal getPhuThu() { return phuThu; }
    public BigDecimal getThanhTien() { return thanhTien; }
}
