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
    
    // Premium UI Support
    private int roomCount;
    private String description;
    private String amenities;

    public RoomType() {}

    // Group's original methods
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
    public double getDienTich() { return dienTich; }
    public void setDienTich(double dienTich) { this.dienTich = dienTich; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public String getTienNghi() { return tienNghi; }
    public void setTienNghi(String tienNghi) { this.tienNghi = tienNghi; }

    // Premium UI Aliases (English)
    public String getRoomTypeId() { return maLoaiPhong; }
    public void setRoomTypeId(String id) { this.maLoaiPhong = id; }
    public String getRoomTypeName() { return tenLoaiPhong; }
    public void setRoomTypeName(String name) { this.tenLoaiPhong = name; }
    public double getPrice() { return giaPhong; }
    public void setPrice(double price) { this.giaPhong = price; }
    public int getMaxCapacity() { return sucChuaToiDa; }
    public void setMaxCapacity(int cap) { this.sucChuaToiDa = cap; }
    public Double getArea() { return dienTich; }
    public void setArea(Double area) { this.dienTich = (area != null ? area : 0.0); }
    
    public int getRoomCount() { return roomCount; }
    public void setRoomCount(int count) { 
        this.roomCount = count;
        this.soLuongPhong = count;
    }
    
    public String getDescription() { return description; }
    public void setDescription(String desc) {
        this.description = desc;
        this.moTa = desc;
    }
    
    public String getAmenities() { return amenities; }
    public void setAmenities(String amen) {
        this.amenities = amen;
        this.tienNghi = amen;
    }
}
