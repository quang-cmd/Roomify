package kqlhotel.entity;

public class Room {
    private String maPhong;
    private String loaiPhong;
    private int tang;
    private String trangThaiPhong;
    private Double deposit;
    
    // Premium UI Object Support
    private RoomType roomType;

    public Room() {
    }

    public Room(String maPhong) {
        this.maPhong = maPhong;
    }

    public Room(String maPhong, Double deposit, RoomType roomType, int tang, String trangThaiPhong) {
        this.maPhong = maPhong;
        this.tang = tang;
        this.trangThaiPhong = trangThaiPhong;
        this.roomType = roomType;
        if (roomType != null) {
            this.loaiPhong = roomType.getMaLoaiPhong();
        }
    }

    // Group's original methods
    public String getMaPhong() { return maPhong; }
    public void setMaPhong(String maPhong) { this.maPhong = maPhong; }
    public String getLoaiPhong() { return loaiPhong; }
    public void setLoaiPhong(String loaiPhong) { this.loaiPhong = loaiPhong; }
    public int getTang() { return tang; }
    public void setTang(int tang) { this.tang = tang; }
    public String getTrangThaiPhong() { return trangThaiPhong; }
    public void setTrangThaiPhong(String trangThaiPhong) { this.trangThaiPhong = trangThaiPhong; }

    // Premium UI Aliases (English)
    public String getRoomId() { return maPhong; }
    public void setRoomId(String roomId) { this.maPhong = roomId; }
    public int getFloor() { return tang; }
    public void setFloor(int floor) { this.tang = floor; }
    public String getStatus() { return trangThaiPhong; }
    public void setStatus(String status) { this.trangThaiPhong = status; }
    
    public Double getDeposit() { return deposit; }
    public void setDeposit(Double deposit) { this.deposit = deposit; }
    
    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { 
        this.roomType = roomType; 
        if (roomType != null) {
            this.loaiPhong = roomType.getMaLoaiPhong();
        }
    }
}
