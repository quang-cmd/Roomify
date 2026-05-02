package kqlhotel.entity;

public class RoomType {
    private String roomTypeId;
    private String roomTypeName;
    private int roomCount;
    private double price;
    private int maxCapacity;
    private Double area;
    private String description;
    private String amenities;

    public RoomType() {
    }

    public RoomType(String roomTypeId, String roomTypeName, int roomCount, double price, int maxCapacity, Double area, String description, String amenities) {
        this.roomTypeId = roomTypeId;
        this.roomTypeName = roomTypeName;
        this.roomCount = roomCount;
        this.price = price;
        this.maxCapacity = maxCapacity;
        this.area = area;
        this.description = description;
        this.amenities = amenities;
    }

    public String getRoomTypeId() { return roomTypeId; }
    public void setRoomTypeId(String roomTypeId) { this.roomTypeId = roomTypeId; }

    public String getRoomTypeName() { return roomTypeName; }
    public void setRoomTypeName(String roomTypeName) { this.roomTypeName = roomTypeName; }

    public int getRoomCount() { return roomCount; }
    public void setRoomCount(int roomCount) { this.roomCount = roomCount; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }

    public Double getArea() { return area; }
    public void setArea(Double area) { this.area = area; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAmenities() { return amenities; }
    public void setAmenities(String amenities) { this.amenities = amenities; }
}
