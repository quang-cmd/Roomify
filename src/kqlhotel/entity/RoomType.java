package kqlhotel.entity;

public class RoomType {
    private String maLoaiPhong;
    private String tenLoaiPhong;
    private int soLuongPhong;
    private double giaPhong;
    private int sucChuaToiDa;
    private double dienTich;
    private String moTa;
    private String tienNghi;

    public RoomType() {}

    // Getters and Setters
    public String getMaLoaiPhong() { return maLoaiPhong; }
    public void setMaLoaiPhong(String maLoaiPhong) { this.maLoaiPhong = maLoaiPhong; }
    public String getRoomTypeId() { return maLoaiPhong; }
    public void setRoomTypeId(String id) { this.maLoaiPhong = id; }

    public String getTenLoaiPhong() { return tenLoaiPhong; }
    public void setTenLoaiPhong(String tenLoaiPhong) { this.tenLoaiPhong = tenLoaiPhong; }
    public String getRoomTypeName() { return tenLoaiPhong; }
    public void setRoomTypeName(String name) { this.tenLoaiPhong = name; }

    public int getSoLuongPhong() { return soLuongPhong; }
    public void setSoLuongPhong(int soLuongPhong) { this.soLuongPhong = soLuongPhong; }
    public int getRoomCount() { return soLuongPhong; }
    public void setRoomCount(int count) { this.soLuongPhong = count; }

    public double getGiaPhong() { return giaPhong; }
    public void setGiaPhong(double giaPhong) { this.giaPhong = giaPhong; }
    public double getPrice() { return giaPhong; }
    public double getGia() { return giaPhong; }
    public void setPrice(double price) { this.giaPhong = price; }

    public int getSucChuaToiDa() { return sucChuaToiDa; }
    public void setSucChuaToiDa(int sucChuaToiDa) { this.sucChuaToiDa = sucChuaToiDa; }
    public int getMaxCapacity() { return sucChuaToiDa; }
    public void setMaxCapacity(int capacity) { this.sucChuaToiDa = capacity; }
    public int getSucChua() { return sucChuaToiDa; }
    public void setSucChua(int suc) { this.sucChuaToiDa = suc; }

    public double getDienTich() { return dienTich; }
    public void setDienTich(double dienTich) { this.dienTich = dienTich; }
    public double getArea() { return dienTich; }
    public void setArea(double area) { this.dienTich = area; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public String getDescription() { return moTa; }
    public void setDescription(String desc) { this.moTa = desc; }

    public String getTienNghi() { return tienNghi; }
    public void setTienNghi(String tienNghi) { this.tienNghi = tienNghi; }
    public String getAmenities() { return tienNghi; }
    public void setAmenities(String amen) { this.tienNghi = amen; }
}
