package kqlhotel.gui.model;

import java.awt.Color;
import java.util.List;
import kqlhotel.bus.booking.RoomOptionDto;

public class RoomCardData {
    public final RoomOptionDto optionDto;
    public final String roomType;
    public final String price;
    public final String status;
    public final String occupancyRate;
    public final int capacity;
    public final int childCapacity;
    public final String capacityHint;
    public final boolean capacityEnough;
    public final List<String> amenities;
    public final Color bg;
    public final Color tone;

    public RoomCardData(RoomOptionDto optionDto, String roomType, String price, String status, String occupancyRate,
                        int capacity, int childCapacity, String capacityHint, boolean capacityEnough,
                        List<String> amenities, Color bg, Color tone) {
        this.optionDto = optionDto;
        this.roomType = roomType;
        this.price = price;
        this.status = status;
        this.occupancyRate = occupancyRate;
        this.capacity = capacity;
        this.childCapacity = childCapacity;
        this.capacityHint = capacityHint;
        this.capacityEnough = capacityEnough;
        this.amenities = amenities;
        this.bg = bg;
        this.tone = tone;
    }
}
