package kqlhotel.entity.shift;

public class ShiftInfo {
    public final String maPC;
    public final String loaiCa;
    public final String gioBatDau;
    public final String gioKetThuc;
    public final String hoTenNV;
    public final double tienMoCa;
    public final double doanhThu;
    public final int soGiaoDich;

    public ShiftInfo(String maPC, String loaiCa, String gioBatDau, String gioKetThuc,
                     String hoTenNV, double tienMoCa, double doanhThu, int soGiaoDich) {
        this.maPC = maPC;
        this.loaiCa = loaiCa;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
        this.hoTenNV = hoTenNV;
        this.tienMoCa = tienMoCa;
        this.doanhThu = doanhThu;
        this.soGiaoDich = soGiaoDich;
    }
}
