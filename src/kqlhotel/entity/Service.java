package kqlhotel.entity;

public class Service {
    private String maDV;
    private String tenDV;
    private double gia;
    private String loaiDV;
    private String moTa;

    private String trangThai;


    public Service() {}

    public Service(String maDV, String tenDV, double gia,
                   String loaiDV, String moTa, String trangThai) {
        this.maDV = maDV;
        this.tenDV = tenDV;
        this.gia = gia;
        this.loaiDV = loaiDV;
        this.moTa = moTa;
        this.trangThai = trangThai;
    }

    // Getters and Setters
    public String getMaDV() { return maDV; }
    public void setMaDV(String maDV) { this.maDV = maDV; }

    public String getTenDV() { return tenDV; }
    public void setTenDV(String tenDV) { this.tenDV = tenDV; }

    public double getGia() { return gia; }
    public void setGia(double gia) { this.gia = gia; }
    public double getDonGia() { return gia; } // Alias for compatibility

    public String getLoaiDV() { return loaiDV; }
    public void setLoaiDV(String loaiDV) { this.loaiDV = loaiDV; }

    public String getMoTa() { return moTa; }



    public void setMoTa(String moTa) { this.moTa = moTa; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
