package kqlhotel.bus.booking;

import java.time.LocalDate;

public class BookingSearchRequest {
    private final String roomType;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private final int adults;
    private final int children;
    private final Long minPrice;
    private final Long maxPrice;

    public BookingSearchRequest(String roomType, LocalDate checkInDate, LocalDate checkOutDate, int adults, int children) {
        this(roomType, checkInDate, checkOutDate, adults, children, null, null);
    }

    public BookingSearchRequest(String roomType, LocalDate checkInDate, LocalDate checkOutDate,
                                int adults, int children, Long minPrice, Long maxPrice) {
        this.roomType = roomType;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.adults = adults;
        this.children = children;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    public String getRoomType() {
        return roomType;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public int getAdults() {
        return adults;
    }

    public int getChildren() {
        return children;
    }

    public Long getMinPrice() {
        return minPrice;
    }

    public Long getMaxPrice() {
        return maxPrice;
    }

    public boolean hasPriceFilter() {
        return minPrice != null || maxPrice != null;
    }
}
