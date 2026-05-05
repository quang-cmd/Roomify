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
    public String getRoomTypeId() { return maLoaiPhong; }
    public void setMaLoaiPhong(String maLoaiPhong) { this.maLoaiPhong = maLoaiPhong; }
    public void setRoomTypeId(String id) { this.maLoaiPhong = id; }

    public String getTenLoaiPhong() { return tenLoaiPhong; }
    public String getRoomTypeName() { return tenLoaiPhong; }
    public void setTenLoaiPhong(String tenLoaiPhong) { this.tenLoaiPhong = tenLoaiPhong; }
    public void setRoomTypeName(String name) { this.tenLoaiPhong = name; }

    public int getSoLuongPhong() { return soLuongPhong; }
    public int getRoomCount() { return soLuongPhong; }
    public void setSoLuongPhong(int soLuongPhong) { this.soLuongPhong = soLuongPhong; }
    public void setRoomCount(int count) { this.soLuongPhong = count; }

    public double getGiaPhong() { return giaPhong; }
    public double getPrice() { return giaPhong; }
    public void setGiaPhong(double giaPhong) { this.giaPhong = giaPhong; }
    public void setPrice(double price) { this.giaPhong = price; }

    public int getSucChuaToiDa() { return sucChuaToiDa; }
    public int getMaxCapacity() { return sucChuaToiDa; }
    public void setSucChuaToiDa(int sucChuaToiDa) { this.sucChuaToiDa = sucChuaToiDa; }
    public void setMaxCapacity(int max) { this.sucChuaToiDa = max; }

    public Double getDienTich() { return dienTich; }
    public Double getArea() { return dienTich; }
    public void setDienTich(Double dienTich) { this.dienTich = dienTich; }
    public void setArea(Double area) { this.dienTich = area; }

    public String getMoTa() { return moTa; }
    public String getDescription() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public void setDescription(String desc) { this.moTa = desc; }

    public String getTienNghi() { return tienNghi; }
    public String getAmenities() { return tienNghi; }
    public void setTienNghi(String tienNghi) { this.tienNghi = tienNghi; }
    public void setAmenities(String amenities) { this.tienNghi = amenities; }
}
