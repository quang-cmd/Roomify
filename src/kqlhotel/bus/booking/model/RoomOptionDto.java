package kqlhotel.bus.booking.model;

import java.util.List;

public class RoomOptionDto {
    private final String roomType;
    private final long nightlyPrice;
    private final String status;
    private final int availableRooms;
    private final List<String> amenities;

    public RoomOptionDto(String roomType, long nightlyPrice, String status, int availableRooms, List<String> amenities) {
        this.roomType = roomType;
        this.nightlyPrice = nightlyPrice;
        this.status = status;
        this.availableRooms = availableRooms;
        this.amenities = amenities;
    }

    public String getRoomType() {
        return roomType;
    }

    public long getNightlyPrice() {
        return nightlyPrice;
    }

    public String getStatus() {
        return status;
    }

    public int getAvailableRooms() {
        return availableRooms;
    }

    public List<String> getAmenities() {
        return amenities;
    }
}
