package kqlhotel.entity;

public class ServiceDetail {
    private String maCTDV;
    private int soLuong;
    private double donGia;
    private double thanhTien;
    private String ghiChu;
    private String maDV;
    private String maHD;

    public ServiceDetail() {}

    public String getMaCTDV() { return maCTDV; }
    public void setMaCTDV(String maCTDV) { this.maCTDV = maCTDV; }
    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }
    public double getThanhTien() { return thanhTien; }
    public void setThanhTien(double thanhTien) { this.thanhTien = thanhTien; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public String getMaDV() { return maDV; }
    public void setMaDV(String maDV) { this.maDV = maDV; }
    public String getMaHD() { return maHD; }
    public void setMaHD(String maHD) { this.maHD = maHD; }
}
