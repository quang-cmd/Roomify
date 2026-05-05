package kqlhotel.entity;

import java.time.LocalDateTime;

public class Promotion {
    private String maKM;
    private String tenKM;
    private Double dieuKienApDung;
    private String loaiKM;
    private double giaTriToiDa;
    private double tienKhuyenMai;
    private LocalDateTime ngayBatDau;
    private LocalDateTime ngayKetThuc;
    private String trangThaiKM;

    public Promotion() {}

    // Getters and Setters
    public String getMaKM() { return maKM; }
    public void setMaKM(String maKM) { this.maKM = maKM; }
    public String getTenKM() { return tenKM; }
    public void setTenKM(String tenKM) { this.tenKM = tenKM; }
    public double getDieuKienApDung() { return dieuKienApDung; }
    public void setDieuKienApDung(double dieuKienApDung) { this.dieuKienApDung = dieuKienApDung; }
    public String getLoaiKM() { return loaiKM; }
    public void setLoaiKM(String loaiKM) { this.loaiKM = loaiKM; }
    public double getGiaTriToiDa() { return giaTriToiDa; }
    public void setGiaTriToiDa(double giaTriToiDa) { this.giaTriToiDa = giaTriToiDa; }
    public double getTienKhuyenMai() { return tienKhuyenMai; }
    public void setTienKhuyenMai(double tienKhuyenMai) { this.tienKhuyenMai = tienKhuyenMai; }
    public LocalDateTime getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(LocalDateTime ngayBatDau) { this.ngayBatDau = ngayBatDau; }
    public LocalDateTime getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(LocalDateTime ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }
    public String getTrangThaiKM() { return trangThaiKM; }
    public void setTrangThaiKM(String trangThaiKM) { this.trangThaiKM = trangThaiKM; }
}
