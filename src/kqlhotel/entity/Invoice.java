package kqlhotel.entity;

import java.time.LocalDateTime;

public class Invoice {
    private String maHD;
    private LocalDateTime ngayLapHD;
    private LocalDateTime ngayThanhToan;
    private String ghiChu;
    private int soLuongNguoi;
    private double tienPhong;
    private double tienDichVu;
    private double tienKhuyenMai;
    private double tienThue;
    private double tongTienThanhToan;
    private double phiDoiPhong;
    private String maKhuyenMai;
    private String maKhachHang;
    private String maNhanVien;
    private String phuongThucTT;
    private String trangThai;
    private String maDatPhong;

    public Invoice() {}

    public String getMaHD() {
        return maHD;
    }

    public void setMaHD(String maHD) {
        this.maHD = maHD;
    }

    public LocalDateTime getNgayLapHD() {
        return ngayLapHD;
    }

    public void setNgayLapHD(LocalDateTime ngayLapHD) {
        this.ngayLapHD = ngayLapHD;
    }

    public LocalDateTime getNgayThanhToan() {
        return ngayThanhToan;
    }

    public void setNgayThanhToan(LocalDateTime ngayThanhToan) {
        this.ngayThanhToan = ngayThanhToan;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public int getSoLuongNguoi() {
        return soLuongNguoi;
    }

    public void setSoLuongNguoi(int soLuongNguoi) {
        this.soLuongNguoi = soLuongNguoi;
    }

    public double getTienPhong() {
        return tienPhong;
    }

    public void setTienPhong(double tienPhong) {
        this.tienPhong = tienPhong;
    }

    public double getTienDichVu() {
        return tienDichVu;
    }

    public void setTienDichVu(double tienDichVu) {
        this.tienDichVu = tienDichVu;
    }

    public double getTienKhuyenMai() {
        return tienKhuyenMai;
    }

    public void setTienKhuyenMai(double tienKhuyenMai) {
        this.tienKhuyenMai = tienKhuyenMai;
    }

    public double getTienThue() {
        return tienThue;
    }

    public void setTienThue(double tienThue) {
        this.tienThue = tienThue;
    }

    public double getTongTienThanhToan() {
        return tongTienThanhToan;
    }

    public void setTongTienThanhToan(double tongTienThanhToan) {
        this.tongTienThanhToan = tongTienThanhToan;
    }

    public double getPhiDoiPhong() {
        return phiDoiPhong;
    }

    public void setPhiDoiPhong(double phiDoiPhong) {
        this.phiDoiPhong = phiDoiPhong;
    }

    public String getMaKhuyenMai() {
        return maKhuyenMai;
    }

    public void setMaKhuyenMai(String maKhuyenMai) {
        this.maKhuyenMai = maKhuyenMai;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public String getPhuongThucTT() {
        return phuongThucTT;
    }

    public void setPhuongThucTT(String phuongThucTT) {
        this.phuongThucTT = phuongThucTT;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getMaDatPhong() {
        return maDatPhong;
    }

    public void setMaDatPhong(String maDatPhong) {
        this.maDatPhong = maDatPhong;
    }

    public String getTinhTrang() { return trangThai; }
    public void setTinhTrang(String tinhTrang) { this.trangThai = tinhTrang; }
}
