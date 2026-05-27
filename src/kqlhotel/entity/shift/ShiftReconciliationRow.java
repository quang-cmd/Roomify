package kqlhotel.entity.shift;

import java.time.LocalDateTime;

public class ShiftReconciliationRow {
    public final String maPC;
    public final String hoTenNV;
    public final String loaiCa;
    public final LocalDateTime thoiGianDuKienMoCa;
    public final LocalDateTime thoiGianMoCa;
    public final LocalDateTime thoiGianKetCa;
    public final double tienMoCa;
    public final double tienKetCa;
    public final double doanhThuHeThong;
    public final double doanhThuTienMat;
    public final String trangThai;

    public ShiftReconciliationRow(String maPC, String hoTenNV, String loaiCa,
                                  LocalDateTime thoiGianDuKienMoCa,
                                  LocalDateTime thoiGianMoCa, LocalDateTime thoiGianKetCa,
                                  double tienMoCa, double tienKetCa,
                                  double doanhThuHeThong, double doanhThuTienMat, String trangThai) {
        this.maPC = maPC;
        this.hoTenNV = hoTenNV;
        this.loaiCa = loaiCa;
        this.thoiGianDuKienMoCa = thoiGianDuKienMoCa;
        this.thoiGianMoCa = thoiGianMoCa;
        this.thoiGianKetCa = thoiGianKetCa;
        this.tienMoCa = tienMoCa;
        this.tienKetCa = tienKetCa;
        this.doanhThuHeThong = doanhThuHeThong;
        this.doanhThuTienMat = doanhThuTienMat;
        this.trangThai = trangThai;
    }
}
