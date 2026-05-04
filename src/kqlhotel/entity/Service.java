package kqlhotel.entity;

public class Service {
    private String maDV;
    private String tenDV;
    private double donGia;
    private String moTaDV;
    private String trangThaiDV;
    private String loaiDV;

    public Service() {}

    public String getMaDV() { return maDV; }
    public void setMaDV(String maDV) { this.maDV = maDV; }
    public String getTenDV() { return tenDV; }
    public void setTenDV(String tenDV) { this.tenDV = tenDV; }
    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }
    public double getGia() { return donGia; }
    public void setGia(double gia) { this.donGia = gia; }

    public String getMoTaDV() { return moTaDV; }
    public void setMoTaDV(String moTaDV) { this.moTaDV = moTaDV; }
    public String getMoTa() { return moTaDV; }
    public void setMoTa(String moTa) { this.moTaDV = moTa; }

    public String getTrangThaiDV() { return trangThaiDV; }
    public void setTrangThaiDV(String trangThaiDV) { this.trangThaiDV = trangThaiDV; }
    public String getTrangThai() { return trangThaiDV; }
    public void setTrangThai(String trangThai) { this.trangThaiDV = trangThai; }

    public String getLoaiDV() { return loaiDV; }
    public void setLoaiDV(String loaiDV) { this.loaiDV = loaiDV; }
}
