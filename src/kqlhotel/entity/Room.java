package kqlhotel.entity;

public class Room {
    private String roomId;
    private Double deposit;
    private RoomType roomType;
    private Integer floor;
    private String status;

    public Room() {
    }

    public Room(String roomId, Double deposit, RoomType roomType, Integer floor, String status) {
        this.roomId = roomId;
        this.deposit = deposit;
        this.roomType = roomType;
        this.floor = floor;
        this.status = status;
    }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public Double getDeposit() { return deposit; }
    public void setDeposit(Double deposit) { this.deposit = deposit; }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

    public Integer getFloor() { return floor; }
    public void setFloor(Integer floor) { this.floor = floor; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Aliases for compatibility
    public String getMaPhong() { return roomId; }
    public String getLoaiPhong() { return roomType != null ? roomType.getRoomTypeId() : null; }
    public String getTrangThai() { return status; }
}
