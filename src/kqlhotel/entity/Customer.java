package kqlhotel.entity;

import java.time.LocalDateTime;
import java.util.Date;
import java.time.ZoneId;

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
    
    // Premium Stats
    private int tongDatPhong;
    private double tongChiTieu;
    private LocalDateTime ngayDatGanNhat;
    private boolean dangHoatDong;

    public Customer() {}

    public String getMaKH() { return maKH; }
    public void setMaKH(String maKH) { this.maKH = maKH; }
    public String getHoTenKH() { return hoTenKH; }
    public void setHoTenKH(String hoTenKH) { this.hoTenKH = hoTenKH; }
    public boolean isGioiTinh() { return gioiTinh; }
    public void setGioiTinh(boolean gioiTinh) { this.gioiTinh = gioiTinh; }
    public String getGioiTinh() { return gioiTinh ? "Nam" : "Nữ"; }
    public void setGioiTinh(String gt) { this.gioiTinh = "Nam".equalsIgnoreCase(gt); }
    
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

    public int getTongDatPhong() { return tongDatPhong; }
    public void setTongDatPhong(int tongDatPhong) { this.tongDatPhong = tongDatPhong; }
    public double getTongChiTieu() { return tongChiTieu; }
    public void setTongChiTieu(double tongChiTieu) { this.tongChiTieu = tongChiTieu; }
    public LocalDateTime getNgayDatGanNhat() { return ngayDatGanNhat; }
    public void setNgayDatGanNhat(LocalDateTime ngayDatGanNhat) { this.ngayDatGanNhat = ngayDatGanNhat; }
    
    public Date getNgayDatGanNhatDate() {
        return ngayDatGanNhat == null ? null : Date.from(ngayDatGanNhat.atZone(ZoneId.systemDefault()).toInstant());
    }

    public void setNgayDatGanNhatDate(Date date) {
        this.ngayDatGanNhat = date == null ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public boolean isDangHoatDong() { return dangHoatDong; }
    public void setDangHoatDong(boolean dangHoatDong) { this.dangHoatDong = dangHoatDong; }
}
