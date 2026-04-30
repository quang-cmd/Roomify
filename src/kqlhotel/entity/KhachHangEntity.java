package kqlhotel.entity;

import java.util.Date;

public class KhachHangEntity {

    private String maKH;
    private String tenKH;
    private String gioiTinh;
    private Date ngaySinh;
    private String email;
    private String sdt;
    private String CCCD;
    private String quocTich;
    private String diaChi;
    private String hangKH;
    private int diemTichLuy;
    private int tongDatPhong;
    private double tongChiTieu;
    private Date ngayDatGanNhat;
    private boolean dangHoatDong;

    // ===== GETTER & SETTER =====

    public String getMaKH() { return maKH; }
    public void setMaKH(String maKH) { this.maKH = maKH; }

    public String getTenKH() { return tenKH; }
    public void setTenKH(String tenKH) { this.tenKH = tenKH; }

    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }

    public Date getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(Date ngaySinh) { this.ngaySinh = ngaySinh; }

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

    public Date getNgayDatGanNhat() { return ngayDatGanNhat; }
    public void setNgayDatGanNhat(Date ngayDatGanNhat) { this.ngayDatGanNhat = ngayDatGanNhat; }

    public boolean isDangHoatDong() { return dangHoatDong; }
    public void setDangHoatDong(boolean dangHoatDong) { this.dangHoatDong = dangHoatDong; }
}
