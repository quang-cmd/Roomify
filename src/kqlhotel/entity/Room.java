package kqlhotel.entity;

public class Room {
    private String maPhong;
    private String loaiPhong;
    private int tang;
    private String trangThaiPhong;
    
    // Premium fields
    private RoomType roomType;
    private Double deposit;

    public Room() {}

    public Room(String maPhong) {
        this.maPhong = maPhong;
    }

    public Room(String roomId, Double deposit, RoomType roomType, Integer floor, String status) {
        this.maPhong = roomId;
        this.deposit = deposit;
        this.roomType = roomType;
        this.tang = (floor != null) ? floor : 0;
        this.trangThaiPhong = status;
        if (roomType != null) this.loaiPhong = roomType.getMaLoaiPhong();
    }

    public String getMaPhong() { return maPhong; }
    public void setMaPhong(String maPhong) { this.maPhong = maPhong; }
    public String getRoomId() { return maPhong; }
    public void setRoomId(String roomId) { this.maPhong = roomId; }

    public String getLoaiPhong() { return loaiPhong; }
    public void setLoaiPhong(String loaiPhong) { this.loaiPhong = loaiPhong; }
    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { 
        this.roomType = roomType; 
        if (roomType != null) this.loaiPhong = roomType.getMaLoaiPhong();
    }

    public int getTang() { return tang; }
    public void setTang(int tang) { this.tang = tang; }
    public Integer getFloor() { return tang; }
    public void setFloor(Integer floor) { this.tang = (floor != null) ? floor : 0; }

    public String getTrangThaiPhong() { return trangThaiPhong; }
    public void setTrangThaiPhong(String trangThaiPhong) { this.trangThaiPhong = trangThaiPhong; }
    public String getStatus() { return trangThaiPhong; }
    public void setStatus(String status) { this.trangThaiPhong = status; }

    public Double getDeposit() { return deposit; }
    public void setDeposit(Double deposit) { this.deposit = deposit; }
}
