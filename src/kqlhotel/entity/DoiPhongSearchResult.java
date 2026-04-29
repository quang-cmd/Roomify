package kqlhotel.entity;

import java.time.LocalDateTime;

public class DoiPhongSearchResult {
    private String maChiTietDatPhong;
    private String maDatPhong;
    private String maKhachHang;
    private String tenKhachHang;
    private String soDienThoai;
    private String cccd;
    private String maPhongHienTai;
    private String maLoaiPhongHienTai;
    private String loaiPhongHienTai;
    private LocalDateTime ngayNhan;
    private LocalDateTime ngayTra;
    private int soLuongNguoiO;
    private int sucChuaToiDaPhongHienTai;

    public String getMaChiTietDatPhong() {
        return maChiTietDatPhong;
    }

    public void setMaChiTietDatPhong(String maChiTietDatPhong) {
        this.maChiTietDatPhong = maChiTietDatPhong;
    }

    public String getMaDatPhong() {
        return maDatPhong;
    }

    public void setMaDatPhong(String maDatPhong) {
        this.maDatPhong = maDatPhong;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getMaPhongHienTai() {
        return maPhongHienTai;
    }

    public void setMaPhongHienTai(String maPhongHienTai) {
        this.maPhongHienTai = maPhongHienTai;
    }

    public String getLoaiPhongHienTai() {
        return loaiPhongHienTai;
    }

    public String getMaLoaiPhongHienTai() {
        return maLoaiPhongHienTai;
    }

    public void setMaLoaiPhongHienTai(String maLoaiPhongHienTai) {
        this.maLoaiPhongHienTai = maLoaiPhongHienTai;
    }

    public void setLoaiPhongHienTai(String loaiPhongHienTai) {
        this.loaiPhongHienTai = loaiPhongHienTai;
    }

    public LocalDateTime getNgayNhan() {
        return ngayNhan;
    }

    public void setNgayNhan(LocalDateTime ngayNhan) {
        this.ngayNhan = ngayNhan;
    }

    public LocalDateTime getNgayTra() {
        return ngayTra;
    }

    public void setNgayTra(LocalDateTime ngayTra) {
        this.ngayTra = ngayTra;
    }

    public int getSoLuongNguoiO() {
        return soLuongNguoiO;
    }

    public void setSoLuongNguoiO(int soLuongNguoiO) {
        this.soLuongNguoiO = soLuongNguoiO;
    }

    public int getSucChuaToiDaPhongHienTai() {
        return sucChuaToiDaPhongHienTai;
    }

    public void setSucChuaToiDaPhongHienTai(int sucChuaToiDaPhongHienTai) {
        this.sucChuaToiDaPhongHienTai = sucChuaToiDaPhongHienTai;
    }
}
