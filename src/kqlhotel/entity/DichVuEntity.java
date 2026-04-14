package kqlhotel.entity;

public class DichVuEntity {

    private String maDV;
    private String tenDV;
    private double gia;
    private String loaiDV;
    private String moTa;
    private String trangThai;

    public DichVuEntity() {}

    public DichVuEntity(String maDV, String tenDV, double gia,
                        String loaiDV, String moTa, String trangThai) {
        this.maDV = maDV;
        this.tenDV = tenDV;
        this.gia = gia;
        this.loaiDV = loaiDV;
        this.moTa = moTa;
        this.trangThai = trangThai;
    }

    // ===== GETTER =====
    public String getMaDV() {
        return maDV;
    }

    public String getTenDV() {
        return tenDV;
    }

    public double getGia() {
        return gia;
    }

    public String getLoaiDV() {
        return loaiDV;
    }

    public String getMoTa() {
        return moTa;
    }

    public String getTrangThai() {
        return trangThai;
    }

    // ===== SETTER =====
    public void setMaDV(String maDV) {
        this.maDV = maDV;
    }

    public void setTenDV(String tenDV) {
        this.tenDV = tenDV;
    }

    public void setGia(double gia) {
        this.gia = gia;
    }

    public void setLoaiDV(String loaiDV) {
        this.loaiDV = loaiDV;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}