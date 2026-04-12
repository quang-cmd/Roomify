package kqlhotel.entity;

public class LoaiPhong {
    private String maLoaiPhong;
    private String tenLoaiPhong;
    private int soLuongPhong;
    private double giaPhong;
    private int sucChuaToiDa;
    private Double dienTich;
    private String moTa;
    private String tienNghi;

    public LoaiPhong() {
    }

    public LoaiPhong(String maLoaiPhong, String tenLoaiPhong, int soLuongPhong, double giaPhong, int sucChuaToiDa, Double dienTich, String moTa, String tienNghi) {
        this.maLoaiPhong = maLoaiPhong;
        this.tenLoaiPhong = tenLoaiPhong;
        this.soLuongPhong = soLuongPhong;
        this.giaPhong = giaPhong;
        this.sucChuaToiDa = sucChuaToiDa;
        this.dienTich = dienTich;
        this.moTa = moTa;
        this.tienNghi = tienNghi;
    }

    public String getMaLoaiPhong() { return maLoaiPhong; }
    public void setMaLoaiPhong(String maLoaiPhong) { this.maLoaiPhong = maLoaiPhong; }

    public String getTenLoaiPhong() { return tenLoaiPhong; }
    public void setTenLoaiPhong(String tenLoaiPhong) { this.tenLoaiPhong = tenLoaiPhong; }

    public int getSoLuongPhong() { return soLuongPhong; }
    public void setSoLuongPhong(int soLuongPhong) { this.soLuongPhong = soLuongPhong; }

    public double getGiaPhong() { return giaPhong; }
    public void setGiaPhong(double giaPhong) { this.giaPhong = giaPhong; }

    public int getSucChuaToiDa() { return sucChuaToiDa; }
    public void setSucChuaToiDa(int sucChuaToiDa) { this.sucChuaToiDa = sucChuaToiDa; }

    public Double getDienTich() { return dienTich; }
    public void setDienTich(Double dienTich) { this.dienTich = dienTich; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public String getTienNghi() { return tienNghi; }
    public void setTienNghi(String tienNghi) { this.tienNghi = tienNghi; }
}
