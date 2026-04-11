package kqlhotel.entity;

import java.time.LocalDateTime;

public class InvoiceDetail {
    private String maCTHD;
    private LocalDateTime ngayNhanPhong;
    private LocalDateTime ngayTraPhong;
    private int soDem;
    private double phuThu;
    private double thanhTien;
    private String maPhong;
    private String maHD;

    public InvoiceDetail() {}

    // Getters and Setters
    public String getMaCTHD() { return maCTHD; }
    public void setMaCTHD(String maCTHD) { this.maCTHD = maCTHD; }
    public LocalDateTime getNgayNhanPhong() { return ngayNhanPhong; }
    public void setNgayNhanPhong(LocalDateTime ngayNhanPhong) { this.ngayNhanPhong = ngayNhanPhong; }
    public LocalDateTime getNgayTraPhong() { return ngayTraPhong; }
    public void setNgayTraPhong(LocalDateTime ngayTraPhong) { this.ngayTraPhong = ngayTraPhong; }
    public int getSoDem() { return soDem; }
    public void setSoDem(int soDem) { this.soDem = soDem; }
    public double getPhuThu() { return phuThu; }
    public void setPhuThu(double phuThu) { this.phuThu = phuThu; }
    public double getThanhTien() { return thanhTien; }
    public void setThanhTien(double thanhTien) { this.thanhTien = thanhTien; }
    public String getMaPhong() { return maPhong; }
    public void setMaPhong(String maPhong) { this.maPhong = maPhong; }
    public String getMaHD() { return maHD; }
    public void setMaHD(String maHD) { this.maHD = maHD; }
}
