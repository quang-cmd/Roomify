package kqlhotel.entity;

import java.time.LocalDateTime;

public class Customer {
    private String maKH;
    private String hoTenKH;
    private boolean gioiTinh;
    private LocalDateTime ngaySinh;
    private String email;
    private String sdt;
    private String CCCD;
    private String quocTich;
    private String diaChi;
    private String hangKH;
    private int diemTichLuy;

    public Customer() {}

    // Getters and Setters
    public String getMaKH() { return maKH; }
    public void setMaKH(String maKH) { this.maKH = maKH; }
    public String getHoTenKH() { return hoTenKH; }
    public void setHoTenKH(String hoTenKH) { this.hoTenKH = hoTenKH; }
    public boolean isGioiTinh() { return gioiTinh; }
    public void setGioiTinh(boolean gioiTinh) { this.gioiTinh = gioiTinh; }
    public LocalDateTime getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDateTime ngaySinh) { this.ngaySinh = ngaySinh; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }
    public String getCCCD() { return CCCD; }
    public void setCCCD(String CCCD) { this.CCCD = CCCD; }
    public String getQuocTich() { return quocTich; }
    public void setQuocTich(String quocTich) { this.quocTich = quocTich; }
    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    public String getHangKH() { return hangKH; }
    public void setHangKH(String hangKH) { this.hangKH = hangKH; }
    public int getDiemTichLuy() { return diemTichLuy; }
    public void setDiemTichLuy(int diemTichLuy) { this.diemTichLuy = diemTichLuy; }
}
