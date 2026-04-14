package kqlhotel.entity;

public class KhachHangEntity {

    private String maKH;
    private String tenKH;
    private String sdt;
    private String cccd;
    private String diaChi;
    private String trangThai;

    public KhachHangEntity() {
    }

    public KhachHangEntity(String maKH, String tenKH, String sdt,
                           String cccd, String diaChi, String trangThai) {
        this.maKH = maKH;
        this.tenKH = tenKH;
        this.sdt = sdt;
        this.cccd = cccd;
        this.diaChi = diaChi;
        this.trangThai = trangThai;
    }

    public String getMaKH() {
        return maKH;
    }

    public void setMaKH(String maKH) {
        this.maKH = maKH;
    }

    public String getTenKH() {
        return tenKH;
    }

    public void setTenKH(String tenKH) {
        this.tenKH = tenKH;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}